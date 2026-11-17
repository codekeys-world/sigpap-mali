const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const ts = require('typescript');
const { of, throwError, firstValueFrom } = require('rxjs');

class Request {
  constructor(method, url, options = {}) {
    Object.assign(this, { method, url, withCredentials: false, ...options });
    this.headerValues = options.headerValues ?? {};
    this.headers = { has: (name) => name in this.headerValues };
  }
  clone(options) {
    return new Request(this.method, this.url, {
      ...this,
      ...options,
      headerValues: { ...this.headerValues, ...options.setHeaders },
    });
  }
}
class Response {
  constructor(body) {
    this.body = body;
  }
}
const loading = { start() {}, stop() {} };
const notification = { warning() {}, error() {} };
const compiled = ts.transpileModule(
  fs.readFileSync(
    require('node:path').join(__dirname, '../src/app/core/auth.interceptor.ts'),
    'utf8',
  ),
  {
    compilerOptions: { module: ts.ModuleKind.CommonJS },
  },
).outputText;
const context = {
  exports: {},
  require(name) {
    if (name === '@angular/common/http') return { HttpRequest: Request, HttpResponse: Response };
    if (name === '@angular/core')
      return {
        inject: (token) =>
          token === 'LoadingService'
            ? loading
            : token === 'NotificationService'
              ? notification
              : {},
      };
    if (name === 'rxjs') return require('rxjs');
    const token = name.includes('loading')
      ? 'LoadingService'
      : name.includes('notification')
        ? 'NotificationService'
        : 'AuthService';
    return { [token]: token };
  },
};
vm.runInNewContext(compiled, context);
const intercept = context.exports.authInterceptor;

test('profile PUT obtains a fresh token before sending the modification without browser cookies', async () => {
  const calls = [];
  await firstValueFrom(
    intercept(new Request('PUT', '/api/auth/moi'), (request) => {
      calls.push(request);
      return of(new Response(calls.length === 1 ? 'fresh-token' : {}));
    }),
  );
  assert.equal(calls.length, 2);
  assert.equal(calls[0].url, '/api/auth/csrf');
  assert.equal(calls[0].responseType, 'text');
  assert.equal(calls[1].headerValues['X-XSRF-TOKEN'], 'fresh-token');
  assert.ok(calls.every((request) => request.withCredentials));
});

test('explicit tokens, reads and external requests do not trigger preparation', async () => {
  for (const request of [
    new Request('POST', '/api/auth/login', { headerValues: { 'X-XSRF-TOKEN': 'explicit' } }),
    new Request('GET', '/api/auth/moi'),
    new Request('POST', 'https://example.com/api'),
  ]) {
    const calls = [];
    await firstValueFrom(
      intercept(request, (sent) => {
        calls.push(sent);
        return of(new Response({}));
      }),
    );
    assert.equal(calls.length, 1);
    assert.deepEqual(calls[0].headerValues, request.headerValues);
    assert.equal(calls[0].withCredentials, request.url.startsWith('/api/'));
  }
});

test('failed token preparation prevents the modification from being sent', async () => {
  let calls = 0;
  await assert.rejects(
    firstValueFrom(
      intercept(new Request('PUT', '/api/auth/moi'), () => {
        calls++;
        return throwError(() => ({ status: 403 }));
      }),
    ),
  );
  assert.equal(calls, 1);
});
