const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');
const { of } = require('rxjs');

const compiled = ts.transpileModule(
  fs.readFileSync(path.join(__dirname, '../src/app/shared/crud/crud.component.ts'), 'utf8'),
  {
    compilerOptions: { module: ts.ModuleKind.CommonJS, experimentalDecorators: true },
  },
).outputText;
const context = {
  exports: {},
  require(name) {
    if (name === 'rxjs') return require('rxjs');
    if (name === '@angular/core')
      return {
        Component: () => (target) => target,
        Input: () => () => {},
        Output: () => () => {},
        EventEmitter: class {},
      };
    return {};
  },
};
vm.runInNewContext(compiled, context);
const { CrudComponent } = context.exports;

function loadConfig(filename) {
  const source = ts.transpileModule(fs.readFileSync(filename, 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS },
  }).outputText;
  const moduleContext = {
    exports: {},
    require(name) {
      return loadConfig(path.resolve(path.dirname(filename), name + '.ts'));
    },
  };
  vm.runInNewContext(source, moduleContext);
  return moduleContext.exports;
}
const { PERSONNEL_CFG } = loadConfig(path.join(__dirname, '../src/app/shared/crud.config.ts'));

for (const section of [
  'grades',
  'formations',
  'etudes',
  'affectations',
  'postes',
  'conges',
  'distinctions',
]) {
  test(`UTILISATEUR can read ${section} using the real configuration without requesting editing catalogs`, () => {
    const calls = [];
    const rows = [{ slug: 'record-1' }];
    const component = new CrudComponent(
      {
        get(url) {
          calls.push(url);
          return of(rows);
        },
      },
      { hasRole: (...roles) => roles.includes('UTILISATEUR') },
    );
    component.baseUrl = `/api/personnels/my-slug/${section}`;
    component.config = PERSONNEL_CFG[section];
    component.ngOnInit();
    assert.equal(component.canWrite, false);
    assert.deepEqual(calls, [component.baseUrl]);
    assert.equal(component.rows, rows);
    assert.equal(component.loaded, true);
    component.ngOnDestroy();
  });
}

for (const canWrite of [false, true]) {
  test(
    canWrite
      ? 'managers load editing choices'
      : 'read-only users load their history without restricted catalogs',
    () => {
      const calls = [];
      const component = new CrudComponent(
        {
          get(url) {
            calls.push(url);
            return of([]);
          },
        },
        { hasRole: () => canWrite },
      );
      component.baseUrl = '/api/personnels/my-slug/historiques-grades';
      component.config = {
        writeRoles: ['GESTIONNAIRE_RH'],
        fields: [
          { key: 'grade', type: 'ref', optionsUrl: '/api/grades/all' },
          { key: 'poste', suggest: { url: '/api/postes/all' } },
        ],
      };
      component.ngOnInit();
      assert.deepEqual(
        calls,
        canWrite ? [component.baseUrl, '/api/grades/all', '/api/postes/all'] : [component.baseUrl],
      );
      component.ngOnDestroy();
    },
  );
}
