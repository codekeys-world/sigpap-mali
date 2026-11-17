const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');
const { of } = require('rxjs');

function load(relative) {
  const filename = path.join(__dirname, '../src/app', relative);
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
  vm.runInNewContext(
    ts.transpileModule(fs.readFileSync(filename, 'utf8'), {
      compilerOptions: { module: ts.ModuleKind.CommonJS, experimentalDecorators: true },
    }).outputText,
    context,
  );
  return context.exports;
}

test('forgot-password and reset reject invalid input without sending requests', () => {
  const { LoginComponent } = load('pages/login/login.component.ts');
  const requests = [];
  const c = new LoginComponent(
    { initialize: () => of(null) },
    {},
    { warning() {}, error() {} },
    {
      post: (...args) => {
        requests.push(args);
        return of({});
      },
    },
    { snapshot: { queryParamMap: { get: () => null } } },
  );
  c.email = 'bad-address';
  c.demanderLien();
  c.jeton = 'token';
  c.nouveauMotDePasse = 'New-password-123';
  c.confirmation = 'different';
  c.reinitialiser();
  assert.equal(requests.length, 0);
  c.email = 'agent@example.ml';
  assert.equal(c.emailValide, true);
  c.confirmation = c.nouveauMotDePasse;
  assert.equal(c.resetValide, true);
  c.nouveauMotDePasse = '        ';
  c.confirmation = c.nouveauMotDePasse;
  assert.equal(c.resetValide, false);
});

test('profile validates identity and prevents submitting an unchanged password', () => {
  const { ProfileComponent } = load('pages/profile/profile.component.ts');
  let sent = false;
  const c = new ProfileComponent(
    {},
    { warning() {} },
    {
      canEditProfile: () => true,
      changerMotDePasse: () => {
        sent = true;
        return of({});
      },
    },
    {},
    {},
  );
  c.identite = {
    nom: 'Diallo',
    prenom: 'Aminata',
    sexe: 'F',
    email: 'bad-address',
    telephone: 'abc',
  };
  assert.equal(c.identiteValide, false);
  c.identite.email = 'agent@example.ml';
  c.identite.telephone = '+223 76 12 34 56';
  assert.equal(c.identiteValide, true);
  c.identite.dateNaissance = '2999-01-01';
  assert.equal(c.identiteValide, false);
  c.f = {
    ancienMotDePasse: 'Password123',
    nouveauMotDePasse: 'Password123',
    confirmation: 'Password123',
  };
  c.valider();
  assert.equal(sent, false);
});

test('generic forms reject whitespace, invalid references and fractional integers', () => {
  const { CrudComponent } = load('shared/crud/crud.component.ts');
  const c = new CrudComponent({}, { hasRole: () => true }, {}, {}, {});
  c.model = { nom: '   ', grade: {}, ageRetraite: 60.5 };
  assert.ok(c.erreur({ key: 'nom', required: true }));
  assert.ok(c.erreur({ key: 'grade', type: 'ref', required: true }));
  assert.ok(c.erreur({ key: 'ageRetraite', type: 'number', min: 40, max: 75 }));
  c.model = { nom: 'Corps administratif', grade: { slug: 'grade-1' }, ageRetraite: 60 };
  assert.equal(c.erreur({ key: 'nom', required: true }), null);
  assert.equal(c.erreur({ key: 'grade', type: 'ref', required: true }), null);
  assert.equal(c.erreur({ key: 'ageRetraite', type: 'number', min: 40, max: 75 }), null);
});
