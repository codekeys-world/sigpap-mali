const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');

const source = ts.transpileModule(
  fs.readFileSync(path.join(__dirname, '../src/app/core/auth.service.ts'), 'utf8'),
  {
    compilerOptions: { module: ts.ModuleKind.CommonJS, experimentalDecorators: true },
  },
).outputText;
const storage = { getItem: () => null, removeItem() {} };
const context = {
  exports: {},
  sessionStorage: storage,
  localStorage: storage,
  require(name) {
    if (name === 'rxjs') return require('rxjs');
    if (name === '@angular/core')
      return {
        Injectable: () => (target) => target,
        signal(initial) {
          let value = initial;
          const signal = () => value;
          signal.set = (next) => {
            value = next;
          };
          signal.asReadonly = () => signal;
          return signal;
        },
      };
    return {};
  },
};
vm.runInNewContext(source, context);

test('login and profile responses produce the same session identity and permissions', () => {
  const service = new context.exports.AuthService({}, {});
  const identity = { slug: 'agent-slug', matricule: 'AGENT', nom: 'Nom', prenom: 'Prénom' };
  const login = service.profilDepuisReponse({
    ...identity,
    role: 'UTILISATEUR',
    autorisations: ['LIRE_PERSONNEL'],
  });
  const profile = service.profilDepuisReponse({
    ...identity,
    role: { nom: 'UTILISATEUR', autorisations: [{ nom: 'LIRE_PERSONNEL' }] },
  });
  assert.deepEqual(login, profile);
  assert.equal(profile.slug, identity.slug);
  assert.equal(profile.autorisations[0], 'LIRE_PERSONNEL');
});

test('service card printing is unavailable to simple users and unauthenticated sessions', () => {
  const service = new context.exports.AuthService({}, {});
  assert.equal(service.canPrintServiceCard(), false);
  assert.equal(service.canEditProfile(), false);
  assert.equal(service.canViewAudit(), false);
  for (const role of [
    'UTILISATEUR',
    'ADMINISTRATEUR',
    'GESTIONNAIRE_RH',
    'DIRECTEUR_REGIONAL',
    'CHEF_ETABLISSEMENT',
    'DIRECTEUR_GENERAL',
    'DIRECTEUR_GENERAL_ADJOINT',
    'CONSULTATION',
  ]) {
    service._session.set({ role });
    assert.equal(service.canPrintServiceCard(), role !== 'UTILISATEUR', role);
    assert.equal(service.canEditProfile(), role !== 'UTILISATEUR', role);
    assert.equal(service.canViewAudit(), role !== 'UTILISATEUR', role);
  }
});

test('a simple user cannot submit identity changes but can submit a password change', () => {
  const profileSource = ts.transpileModule(
    fs.readFileSync(path.join(__dirname, '../src/app/pages/profile/profile.component.ts'), 'utf8'),
    {
      compilerOptions: { module: ts.ModuleKind.CommonJS, experimentalDecorators: true },
    },
  ).outputText;
  const profileContext = {
    exports: {},
    require(name) {
      if (name === '@angular/core') return { Component: () => (target) => target };
      return {};
    },
  };
  vm.runInNewContext(profileSource, profileContext);
  let identityWrites = 0;
  let passwordWrites = 0;
  const profile = new profileContext.exports.ProfileComponent(
    {
      put() {
        identityWrites++;
        return { subscribe() {} };
      },
    },
    {},
    {
      canEditProfile: () => false,
      changerMotDePasse() {
        passwordWrites++;
        return { subscribe() {} };
      },
    },
    {},
  );
  profile.identite = { nom: 'Nom', prenom: 'Prenom', sexe: 'M' };
  profile.enregistrerIdentite();
  assert.equal(identityWrites, 0);
  profile.f = {
    ancienMotDePasse: 'old-password',
    nouveauMotDePasse: 'new-password',
    confirmation: 'new-password',
  };
  profile.valider();
  assert.equal(passwordWrites, 1);
});
