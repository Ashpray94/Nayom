import test from 'node:test';
import assert from 'node:assert/strict';
import { ms } from '../src/time.js';
import * as V from '../src/vault.js';

test('IDs', () => {
  assert.equal(V.makeId('P', ms('2026-11-12T09:15:30+05:30'), 'ph', 1), 'P-20261112-091530-ph-01');
});

test('one writer per file', () => {
  assert.equal(V.canWrite('Nayom/events/ph/2026-11-12.md', 'ph'), true);
  assert.equal(V.canWrite('Nayom/events/pc/2026-11-12.md', 'ph'), false);
  assert.equal(V.canWrite('Nayom/ledger/lt/2026-11-12.md', 'lt'), true);
  assert.equal(V.canWrite('Nayom/verdicts/2026-11-12.md', 'ph'), false);
  assert.equal(V.canWrite('Nayom/verdicts/2026-11-12.md', 'pc'), true);
  assert.equal(V.canWrite('Nayom/people/meena.md', 'ph'), false);
  assert.equal(V.canWrite('Nayom/proposals/pending/R-1.md', 'ph'), false);
  assert.equal(V.canWrite('Nayom/proposals/approved/R-1.md', 'ph'), true);
  assert.equal(V.canWrite('Daily notes/today.md', 'ph'), false); // outside Nayom/
});

test('promise note round-trips through frontmatter', () => {
  const t = V.promiseNote({ id: 'P-1', person: 'meena', what: 'Send the final file', due: ms('2026-11-12T17:00:00+05:30'), due_source: 'stated', created: ms('2026-11-10T16:12:05+05:30'), source_app: 'com.whatsapp', quote_enc: 'v1:abc', confidence: 0.92 });
  const { props } = V.parseFrontmatter(t);
  assert.deepEqual([props.nayom, props.person, props.what, props.confidence, props.private], ['promise', '[[meena]]', 'Send the final file', 0.92, false]);
  assert.match(t, /^due: 2026-11-12T17:00:00\+05:30$/m);
});

test('private events carry only ts, kind, app', () => {
  assert.equal(V.eventLine({ private: true, ts: ms('2026-11-12T16:49:44+05:30'), kind: 'typed', app: 'com.x', person: 'a', quote_enc: 'v1:zz' }), '2026-11-12T16:49:44+05:30 | typed | com.x');
});

test('quote encryption: round-trip, tamper, wrong key, limits', () => {
  const k = V.newKey(), q = 'I will send it by evening';
  const e = V.encryptQuote(k, q);
  assert.match(e, /^v1:/); assert.equal(V.decryptQuote(k, e), q);
  assert.notEqual(V.encryptQuote(k, q), e); // fresh nonce
  const bad = 'v1:' + Buffer.from(Buffer.from(e.slice(3), 'base64').map((b, i, a) => (i === a.length - 1 ? b ^ 1 : b))).toString('base64');
  assert.throws(() => V.decryptQuote(k, bad));
  assert.throws(() => V.decryptQuote(V.newKey(), e));
  assert.throws(() => V.encryptQuote(k, 'x'.repeat(141)), RangeError);
  assert.throws(() => V.encryptQuote(k, 'Okay. I will do it.'), RangeError);
});

const memfs = (init) => { const m = new Map(Object.entries(init)); return { m, read: (p) => m.get(p), write: (p, t) => m.set(p, t), exists: (p) => m.has(p), move: (a, b) => { m.set(b, m.get(a)); m.delete(a); } }; };

test('adoption: plain note needs a proposal; adopted note is snapshotted before edit', () => {
  const note = '# Plan\nbody';
  const fs = memfs({ 'Notes/plan.md': note });
  const now = ms('2026-11-12T20:00:00+05:30');
  assert.throws(() => V.editExisting(fs, 'Notes/plan.md', 'x', now), V.NeedsProposal);
  fs.write('Notes/plan.md', V.adopt(note));
  assert.equal(V.isAdopted(fs.read('Notes/plan.md')), true);
  const before = fs.read('Notes/plan.md');
  V.editExisting(fs, 'Notes/plan.md', before + '\nmore', now);
  assert.equal([...fs.m.keys()].filter((k) => k.startsWith('Nayom/trash/')).length, 1);
  assert.equal(fs.read([...fs.m.keys()].find((k) => k.startsWith('Nayom/trash/'))), before);
});

test('adoption keeps existing properties', () => {
  const t = V.adopt('---\ntags: ["a"]\n---\nhello');
  assert.deepEqual(V.parseFrontmatter(t).props, { nayom: 'adopted', tags: ['a'] });
});

test('delete: never outright; adopted needs your tap', () => {
  const now = ms('2026-11-12T20:00:00+05:30');
  const fs = memfs({ 'Nayom/days/a.md': '---\nnayom: day\n---\n', 'Notes/b.md': V.adopt('b') });
  V.deleteNote(fs, 'Nayom/days/a.md', now, { createdByNayom: true });
  assert.equal(fs.exists('Nayom/days/a.md'), false);
  assert.equal([...fs.m.keys()].some((k) => k.startsWith('Nayom/trash/')), true);
  assert.throws(() => V.deleteNote(fs, 'Notes/b.md', now, { userTapped: false }), V.NeedsProposal);
  V.deleteNote(fs, 'Notes/b.md', now, { userTapped: true });
  assert.equal(fs.exists('Notes/b.md'), false);
  assert.throws(() => V.deleteNote(memfs({ 'N/c.md': 'plain' }), 'N/c.md', now, { userTapped: true }), V.NeedsProposal);
});

test('retention', () => {
  const now = ms('2026-11-12T00:00:00+05:30'), d = (n) => now - n * 86400000;
  const a = V.retention([
    { path: 'Nayom/events/ph/x.md', ts: d(31) }, { path: 'Nayom/events/ph/y.md', ts: d(29) },
    { path: 'Nayom/trash/z', ts: d(31) }, { path: 'Nayom/asks/A1.md', ts: d(91) },
    { path: 'Nayom/promises/P1.md', ts: d(900), hasQuote: true, id: 'P1' },
    { path: 'Nayom/promises/P2.md', ts: d(900), hasQuote: true, id: 'P2' },
    { path: 'Nayom/promises/P3.md', ts: d(900), hasQuote: true, id: 'P3' },
  ], now, { openSlipIds: new Set(['P2']), proofIds: new Set(['P3']) });
  assert.deepEqual(a.map((x) => `${x.path}:${x.action}`), [
    'Nayom/events/ph/x.md:reduce-to-daily-counts', 'Nayom/trash/z:purge', 'Nayom/asks/A1.md:move-to-trash', 'Nayom/promises/P1.md:blank-quote']);
});
