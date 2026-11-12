import test from 'node:test';
import assert from 'node:assert/strict';
import { validate, resolve } from '../src/guard.js';

const ctx = { evidenceIds: new Set(['E1', 'E2']), count: 1 };
const ok = () => ({ decision: 'slip', evidence: ['E1'], level: 1, card: { title: 'Missed.', fact: 'The banner for Meena was due 17:00 and nothing was sent.', expect: 'Fix it or release it.', next: 'Pick a time.' }, draft: '', confidence: 0.9 });
const codes = (o, c = ctx) => validate(o, c).reasons.map((r) => r.code);

test('a clean slip passes', () => assert.equal(validate(ok(), ctx).ok, true));
test('no evidence forces none', () => assert.deepEqual(codes({ ...ok(), evidence: [] }), ['no-evidence']));
test('evidence must exist in context', () => assert.ok(codes({ ...ok(), evidence: ['E9'] }).includes('invented-evidence')));
test('decision none with no evidence is fine', () => assert.equal(validate({ ...ok(), decision: 'none', evidence: [], level: 1 }, ctx).ok, true));
test('malformed output', () => { assert.deepEqual(codes(null), ['shape']); assert.deepEqual(codes({ ...ok(), card: { title: 1 } }), ['shape']); });
test('level cannot exceed escalation', () => assert.ok(codes({ ...ok(), level: 3 }).includes('level-too-high')));
test('over 40 words is soft', () => {
  const o = ok(); o.card.fact = Array(41).join('word ');
  const r = validate(o, ctx); assert.equal(r.reasons[0].code, 'too-long'); assert.equal(r.reasons[0].hard, false);
});
test('banned moves', () => {
  for (const [text, code] of [["Don't worry about it.", 'pity'], ['You are lazy.', 'shame'], ['You lied to Meena.', 'says-lied'], ['Or else I will stop.', 'threat'], ['Other people manage this.', 'comparison'], ['Beta, send it.', 'pet-name'], ['Well done, now fix it.', 'praise-with-slip'], ['Send it now!', 'exclamation-on-slip']]) {
    const o = ok(); o.card.next = text; assert.ok(codes(o).includes(code), `${text} -> ${code}`);
  }
});
test('lists are rejected', () => { const o = ok(); o.card.expect = '- one\n- two'; assert.ok(codes(o).includes('list-on-card')); });
test('a draft that says lied is also rejected', () => { const o = ok(); o.draft = 'Sorry I lied.'; assert.ok(codes(o).includes('says-lied')); });

test('resolve: model, retry once, template', () => {
  const long = ok(); long.card.fact = Array(45).join('word ');
  const tpl = { template: true };
  assert.equal(resolve(ok(), undefined, ctx, tpl).source, 'model');
  assert.equal(resolve(long, ok(), ctx, tpl).source, 'model-retry');
  assert.equal(resolve(long, long, ctx, tpl).source, 'template');
  assert.equal(resolve(long, undefined, ctx, tpl).source, 'template');
  const invented = { ...ok(), evidence: ['E9'] };
  assert.equal(resolve(invented, ok(), ctx, tpl).source, 'template'); // hard failure: never retried
});
