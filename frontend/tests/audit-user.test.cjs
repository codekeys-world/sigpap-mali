const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');
const { of, firstValueFrom } = require('rxjs');

const compiled = ts.transpileModule(
  fs.readFileSync(path.join(__dirname, '../src/app/core/audit-user.service.ts'), 'utf8'),
  {
    compilerOptions: { module: ts.ModuleKind.CommonJS, experimentalDecorators: true },
  },
).outputText;
const context = {
  exports: {},
  require(name) {
    if (name === 'rxjs') return require('rxjs');
    if (name === '@angular/core') return { Injectable: () => (target) => target };
    return {};
  },
};
vm.runInNewContext(compiled, context);

test('audit labels show the actual first name, last name and matricule and cache repeated authors', async () => {
  let requests = 0;
  const service = new context.exports.AuditUserService({
    get(url) {
      requests++;
      assert.equal(url, '/api/auth/auteurs/0159.203-M');
      return of({ matricule: '0159.203-M', prenom: 'Aminata', nom: 'Traore' });
    },
  });
  assert.equal(await firstValueFrom(service.identity('0159.203-M')), 'Aminata Traore (0159.203-M)');
  assert.equal(await firstValueFrom(service.identity('0159.203-M')), 'Aminata Traore (0159.203-M)');
  assert.equal(requests, 1);
});

test('missing or system authors are reported honestly without inventing an identity', async () => {
  let requests = 0;
  const service = new context.exports.AuditUserService({
    get() {
      requests++;
      return of({ matricule: 'DELETED', prenom: '', nom: '' });
    },
  });
  assert.equal(await firstValueFrom(service.identity()), 'Non renseigné');
  assert.equal(await firstValueFrom(service.identity('SYSTEME')), 'Système');
  assert.equal(requests, 0);
  assert.equal(await firstValueFrom(service.identity('DELETED')), 'DELETED');
});
