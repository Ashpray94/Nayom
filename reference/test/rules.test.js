import test from 'node:test';
import assert from 'node:assert/strict';
import { ms, iso, HOUR, MIN } from '../src/time.js';
import { normalize } from '../src/text.js';
import * as S from '../src/signals.js';
import { parseLedger, reduceLedger } from '../src/ledger.js';

const NOW = ms('2026-11-12T10:00:00+05:30'); // a Thursday

test('normalize: squeeze, punctuation, Devanagari ignored', () => {
  assert.equal(normalize('Sureee!!'), 'sure');
  assert.equal(normalize('Tomorrow, hello.'), 'tomorrow hello');
  assert.equal(normalize('ठीक है'), null);
});

test('SR-06 support rule suppresses and writes nothing', () => {
  const r = S.supportRule('I just want to die');
  assert.deepEqual([r.suppress, r.helpline, r.writeToVault], [true, '14416', false]);
  assert.equal(S.supportRule('I will send it tomorrow').suppress, false);
});

test('SIG-01 default due: before 15:00 today 18:00, after 15:00 tomorrow 11:00', () => {
  assert.equal(iso(S.defaultDue(ms('2026-11-12T14:59:00+05:30'))), '2026-11-12T18:00:00+05:30');
  assert.equal(iso(S.defaultDue(ms('2026-11-12T15:00:00+05:30'))), '2026-11-13T11:00:00+05:30');
});

test('SIG-01 capture: stated time, default time, weekday, hinglish, none', () => {
  const a = S.capturePromise({ text: "Sure, I'll send the file tomorrow", person: 'meena', nowMs: NOW });
  assert.equal(a.due_source, 'stated'); assert.equal(iso(a.due), '2026-11-13T11:00:00+05:30'); assert.equal(a.draft, false);
  const b = S.capturePromise({ text: 'will do, send it', person: 'meena', nowMs: NOW });
  assert.equal(b.due_source, 'default'); assert.equal(iso(b.due), '2026-11-12T18:00:00+05:30');
  const c = S.capturePromise({ text: 'update bhej dunga by monday', person: 'raj', nowMs: NOW });
  assert.equal(iso(c.due), '2026-11-16T18:00:00+05:30');
  assert.equal(S.capturePromise({ text: 'kal tak bhej dunga', person: 'raj', nowMs: NOW }).due_source, 'stated');
  assert.equal(S.capturePromise({ text: 'good morning', person: 'raj', nowMs: NOW }), null);
});

test('SIG-01 low confidence becomes a draft (To approve)', () => {
  const noPerson = S.capturePromise({ text: 'will do, send it', person: null, nowMs: NOW });
  assert.deepEqual([noPerson.confidence, noPerson.draft], [0.55, true]);
  const withPerson = S.capturePromise({ text: 'will do, send it', person: 'meena', nowMs: NOW });
  assert.deepEqual([withPerson.confidence, withPerson.draft], [0.6, false]);
  assert.equal(S.capturePromise({ text: 'send tomorrow', person: null, nowMs: NOW }).confidence, 0.7);
});

test('SIG-03 gate: light, full by load, full by reflex, out of window, not acceptance', () => {
  const base = { text: 'sure', lastAskAgeMin: 3, openDue48h: 0, houseRuleHit: false, secondsSinceAsk: 200 };
  assert.deepEqual(S.gate(base), { fire: true, type: 'light', pause_ms: 800 });
  assert.equal(S.gate({ ...base, openDue48h: 3 }).type, 'full');
  assert.equal(S.gate({ ...base, secondsSinceAsk: 20 }).type, 'full');
  assert.equal(S.gate({ ...base, houseRuleHit: true }).type, 'full');
  assert.equal(S.gate({ ...base, lastAskAgeMin: 11 }).fire, false);
  assert.equal(S.gate({ ...base, text: 'let me check' }).fire, false);
  assert.equal(S.gate({ ...base, text: 'SUREEE' }).fire, true);
});

const promise = { id: 'P1', person: 'meena', what: 'Send the final banner file', due: ms('2026-11-12T17:00:00+05:30'), created: ms('2026-11-10T16:00:00+05:30') };
test('SIG-04 missed: grace, evidence, tapped done', () => {
  const late = ms('2026-11-12T17:31:00+05:30');
  assert.equal(S.missedPromise({ promise, events: [], nowMs: ms('2026-11-12T17:29:00+05:30') }).status, 'open');
  const miss = S.missedPromise({ promise, events: [], nowMs: late });
  assert.deepEqual([miss.status, miss.countsTowardEscalation], ['missed', true]);
  const sent = { id: 'E1', ts: ms('2026-11-12T16:49:00+05:30'), kind: 'sent', person: 'meena', tags: ['attachment'] };
  assert.deepEqual(S.missedPromise({ promise, events: [sent], nowMs: late }), { status: 'kept', evidence: ['E1'] });
  const file = { id: 'E2', ts: ms('2026-11-12T16:00:00+05:30'), kind: 'file-exported', file: 'Banner_final.png' };
  assert.equal(S.missedPromise({ promise, events: [file], nowMs: late }).status, 'kept');
  const done = S.missedPromise({ promise, events: [], nowMs: late, tappedDone: true });
  assert.deepEqual([done.unverified, done.countsTowardEscalation], [true, false]);
});

test('SIG-05 claim vs activity', () => {
  const claim = { ts: ms('2026-11-12T15:00:00+05:30'), text: 'almost done' };
  const span = (a, b) => ({ app: 'figma', title: 'Banner v2', start: ms(`2026-11-12T${a}:00+05:30`), end: ms(`2026-11-12T${b}:00+05:30`) });
  const args = { claim, taskText: 'banner design', events: [], askedAt: ms('2026-11-12T09:00:00+05:30'), pcOn: true };
  assert.equal(S.claimMismatch({ ...args, spans: [span('12:00', '12:10')] }).mismatch, true); // 10 min
  assert.equal(S.claimMismatch({ ...args, spans: [span('12:00', '12:25')] }).mismatch, false); // 25 min
  assert.equal(S.claimMismatch({ ...args, spans: [span('09:00', '10:30')] }).mismatch, true); // outside 4h window
  assert.equal(S.claimMismatch({ ...args, spans: [], pcOn: false }).provisional, true);
  const sent = { ...claim, text: 'sent' };
  assert.equal(S.claimMismatch({ ...args, claim: sent, spans: [] }).mismatch, true);
  const exp = { id: 'E9', ts: ms('2026-11-12T14:00:00+05:30'), kind: 'file-exported' };
  assert.equal(S.claimMismatch({ ...args, claim: sent, spans: [], events: [exp] }).mismatch, false);
});

test('SIG-06 clash: same task, two people, gap over 4 h', () => {
  const a = { id: 'A', person: 'meena', what: 'banner design file', due: ms('2026-11-12T12:00:00+05:30') };
  const b = { id: 'B', person: 'raj', what: 'banner design review', due: ms('2026-11-12T18:00:00+05:30') };
  assert.equal(S.statementClash(a, b).clash, true);
  assert.equal(S.statementClash(a, { ...b, due: ms('2026-11-12T15:00:00+05:30') }).clash, false);
  assert.equal(S.statementClash(a, { ...b, what: 'invoice' }).clash, false);
  assert.equal(S.statementClash(a, { ...b, person: 'meena' }).clash, false);
});

test('SIG-07 avoidance', () => {
  const read = ms('2026-11-12T10:00:00+05:30');
  assert.equal(S.readUnanswered({ tier: 1, readAt: read, nowMs: read + 2 * HOUR, inWorkHours: true }), true);
  assert.equal(S.readUnanswered({ tier: 1, readAt: read, nowMs: read + 2 * HOUR, inWorkHours: false }), false);
  assert.equal(S.readUnanswered({ tier: 3, readAt: read, nowMs: read + 23 * HOUR, inWorkHours: true }), false);
  assert.equal(S.typedAndDeleted([NOW - 20 * MIN, NOW - 5 * MIN], NOW), true);
  assert.equal(S.typedAndDeleted([NOW - 40 * MIN, NOW - 5 * MIN], NOW), false);
  assert.equal(S.missedCallUnreturned(NOW - 25 * HOUR, null, NOW), true);
});

test('SIG-08 distraction needs a due promise, work block and cap', () => {
  const b = { app: 'YouTube', continuousMin: 10, switchesLastHour: 0, dueWithin24h: true, workBlockActive: true, cardsToday: 0 };
  assert.equal(S.distraction(b), true);
  assert.equal(S.distraction({ ...b, continuousMin: 9 }), false);
  assert.equal(S.distraction({ ...b, app: 'figma', continuousMin: 99, switchesLastHour: 40 }), true);
  assert.equal(S.distraction({ ...b, dueWithin24h: false }), false);
  assert.equal(S.distraction({ ...b, cardsToday: 3 }), false);
});

test('SIG-12 escalation and Wrong never raises', () => {
  assert.deepEqual([0, 1, 2, 3, 4, 9].map(S.escalationLevel), ['none', 'L1', 'L2', 'L2', 'L3', 'L3']);
  let c = 0; for (const e of ['slip', 'slip', 'wrong-tap']) c = S.nextCount(c, e);
  assert.equal(c, 2);
  assert.equal(S.nextCount(c, 'kept-on-time'), 1);
  assert.equal(S.nextCount(0, 'kept-on-time'), 0);
  assert.equal(S.patternQuiet(2), false); assert.equal(S.patternQuiet(3), true);
});

test('scaffold fade', () => {
  assert.deepEqual(S.reminders({ weeklyKeptShare: [0.7, 0.85] }), { twoHour: true, dueTime: true, tier23: true });
  assert.deepEqual(S.reminders({ weeklyKeptShare: [0.82, 0.85] }), { twoHour: false, dueTime: true, tier23: true });
  assert.deepEqual(S.reminders({ weeklyKeptShare: [0.91, 0.92, 0.95, 0.93] }), { twoHour: false, dueTime: true, tier23: false });
  assert.deepEqual(S.reminders({ weeklyKeptShare: [0.95, 0.95, 0.95, 0.55] }), { twoHour: true, dueTime: true, tier23: true });
});

test('ledger: parse, validate, reduce across devices, dedupe', () => {
  const ph = `\`\`\`
ts | id | status | evidence | by | new_due
2026-11-10T16:12:05+05:30 | P1 | open | | ph |
2026-11-12T12:00:00+05:30 | P1 | moved | | ph | 2026-11-12T19:00:00+05:30
\`\`\``;
  const pc = `2026-11-12T16:50:11+05:30 | P1 | kept | E1,E2 | pc |
2026-11-12T16:50:11+05:30 | P1 | kept | E1,E2 | pc |`;
  const m = reduceLedger([...parseLedger(ph), ...parseLedger(pc)]);
  const p1 = m.get('P1');
  assert.deepEqual([p1.status, p1.due, p1.evidence], ['kept', '2026-11-12T19:00:00+05:30', ['E1', 'E2']]);
  assert.deepEqual(p1.history, ['open', 'moved', 'kept']);
  assert.throws(() => parseLedger('2026-11-12T10:00:00+05:30 | P1 | moved | | ph |'));
  assert.throws(() => parseLedger('2026-11-12T10:00:00+05:30 | P1 | kept | | ph | 2026-11-13T10:00:00+05:30'));
  assert.throws(() => parseLedger('2026-11-12T10:00:00+05:30 | P1 | bogus | | ph |'));
});
