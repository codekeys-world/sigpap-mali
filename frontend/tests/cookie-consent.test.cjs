const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');

function load(storage) {
  const context = {
    exports: {},
    localStorage: storage,
    setTimeout: () => {},
    require: () => ({ Component: () => (t) => t, ViewChild: () => () => {} }),
  };
  vm.runInNewContext(
    ts.transpileModule(
      fs.readFileSync(
        path.join(__dirname, '../src/app/shared/cookie-consent/cookie-consent.component.ts'),
        'utf8',
      ),
      { compilerOptions: { module: ts.ModuleKind.CommonJS, experimentalDecorators: true } },
    ).outputText,
    context,
  );
  return context.exports.CookieConsentComponent;
}

test('both choices hide the banner and remain hidden after a reload', () => {
  let value = null;
  const C = load({ getItem: () => value, setItem: (_, v) => (value = v) });
  const c = new C();
  assert.equal(c.visible, true);
  c.choose('necessary-only');
  assert.equal(c.visible, false);
  assert.equal(JSON.parse(value).choice, 'necessary-only');
  assert.equal(new C().visible, false);
  c.choose('accepted');
  assert.equal(JSON.parse(value).choice, 'accepted');
  assert.equal(c.visible, false);
  assert.equal(new C().visible, false);
});

test('expired or malformed preferences are shown again and unavailable storage does not block the app', () => {
  for (const value of [
    'broken-json',
    JSON.stringify({ choice: 'accepted', savedAt: 0 }),
    JSON.stringify({ choice: 'invalid', savedAt: Date.now() }),
  ]) {
    const C = load({ getItem: () => value });
    assert.equal(new C().visible, true);
  }
  const C = load({
    getItem() {
      throw Error('blocked');
    },
    setItem() {
      throw Error('blocked');
    },
  });
  const c = new C();
  assert.equal(c.visible, true);
  assert.doesNotThrow(() => c.choose('necessary-only'));
  assert.equal(c.visible, false);
});
