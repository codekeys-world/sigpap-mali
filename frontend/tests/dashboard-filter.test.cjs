const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');
const { of, Subject } = require('rxjs');

class HttpParams {
  constructor(values = {}) {
    this.values = values;
  }
  set(key, value) {
    return new HttpParams({ ...this.values, [key]: String(value) });
  }
}

function loadComponent(relativePath) {
  const compiled = ts.transpileModule(fs.readFileSync(path.join(__dirname, relativePath), 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, experimentalDecorators: true },
  }).outputText;
  const context = {
    exports: {},
    localStorage: { getItem: () => null },
    require(name) {
      if (name === 'rxjs') return require('rxjs');
      if (name === '@angular/core')
        return {
          Component: () => (target) => target,
          ViewChild: () => () => {},
        };
      if (name === '@angular/common/http') return { HttpParams };
      if (name === 'chart.js')
        return {
          Chart: class {
            static register() {}
          },
          registerables: [],
        };
      return {};
    },
  };
  vm.runInNewContext(compiled, context);
  return context.exports;
}

test('a corps link filters the initial request, subsequent navigation clears obsolete filters, exports keep the corps', () => {
  const { PersonnelListComponent } = loadComponent(
    '../src/app/pages/personnel-list/personnel-list.component.ts',
  );
  const route = { queryParamMap: new Subject() };
  const requests = [];
  const http = {
    get(url, options) {
      requests.push({ url, params: options?.params?.values });
      return of(url === '/api/personnels' ? { content: [] } : []);
    },
  };
  const component = new PersonnelListComponent(http, {}, route, {}, { telecharger() {} }, {}, {});
  component.ngOnInit();
  const navigate = (values) => route.queryParamMap.next({ get: (key) => values[key] ?? null });
  navigate({ corpsSlug: 'corps-a', serviceSlug: 'service-a' });
  assert.equal(requests.at(-1).params.corpsSlug, 'corps-a');
  assert.equal(requests.at(-1).params.serviceSlug, 'service-a');
  assert.equal(component.f.corpsSlug, 'corps-a');
  component.exporter('xlsx');
  assert.equal(requests.at(-1).params.corpsSlug, 'corps-a');
  navigate({ corpsSlug: 'corps-b' });
  assert.equal(requests.at(-1).params.corpsSlug, 'corps-b');
  assert.equal(requests.at(-1).params.serviceSlug, undefined);
  navigate({});
  assert.equal(requests.at(-1).params.corpsSlug, undefined);
});

test('the dashboard navigates with the corps slug and ignores unassigned corps', () => {
  const { DashboardComponent } = loadComponent('../src/app/pages/dashboard/dashboard.component.ts');
  const navigations = [];
  const component = new DashboardComponent(
    {},
    {},
    { navigate: (...args) => navigations.push(args) },
  );
  component.consulterCorps({ slug: 'corps-a' });
  assert.equal(navigations.length, 1);
  assert.equal(navigations[0][0][0], '/app/personnel');
  assert.equal(navigations[0][1].queryParams.corpsSlug, 'corps-a');
  component.consulterCorps({ slug: null });
  assert.equal(navigations.length, 1);
});
