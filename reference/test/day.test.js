import test from 'node:test';
import assert from 'node:assert/strict';
import { ms, iso, HOUR, MIN } from '../src/time.js';
import * as S from '../src/signals.js';
import { validate } from '../src/guard.js';

test('SIG-02 ask capture', () => {
  const now = ms('2026-11-12T09:15:00+05:30');
  const a = S.captureAsk({ text: 'Can you send the banner by friday?', from: 'meena', nowMs: now });
  assert.deepEqual([a.due_source, iso(a.due), a.confidence], ['stated', '2026-11-13T18:00:00+05:30', 0.9]);
  const bare = S.captureAsk({ text: 'please look at this', from: null, nowMs: now });
  assert.deepEqual([bare.due_source, bare.confidence], ['default', 0.6]); // 0.6 is not below the threshold
  assert.equal(S.captureAsk({ text: 'lunch was good', from: 'meena', nowMs: now }), null);
});

test('tier follows stakes, not reply speed', () => {
  const t1 = S.tierScore({ messages: ['Dear Sir, the contract and invoice, payment by tomorrow', 'client deliverable today'], keptCount: 2, missedCount: 1, perWeek: 12 });
  assert.equal(t1.tier, 1);
  assert.equal(S.tierScore({ messages: ['lol see you', 'what is for dinner'] }).tier, 3);
  assert.equal(S.tierScore({ messages: ['can you share the budget'] }).tier, 2);
});

// One day end to end: ask -> reflex yes -> promise -> clash -> missed -> escalation -> guarded card.
test('Thursday with Meena and Raj', () => {
  const ask = S.captureAsk({ text: 'Could you share the banner design file today please', from: 'meena', nowMs: ms('2026-11-12T09:15:00+05:30') });
  assert.ok(ask); // I-01 ask recorded
  const g = S.gate({ text: 'sureee', lastAskAgeMin: 0.3, openDue48h: 2, houseRuleHit: false, secondsSinceAsk: 18 });
  assert.deepEqual([g.fire, g.type], [true, 'full']); // reflex: yes within 60 s -> full gate

  const meena = { id: 'P1', person: 'meena', what: 'share the banner design file', due: ms('2026-11-12T12:00:00+05:30'), created: ms('2026-11-12T09:16:00+05:30') };
  const raj = S.capturePromise({ text: 'will send the banner design review tonight', person: 'raj', nowMs: ms('2026-11-12T11:00:00+05:30') });
  assert.equal(iso(raj.due), '2026-11-12T21:00:00+05:30');
  const clash = S.statementClash(meena, { id: 'P2', person: 'raj', what: 'send the banner design review', due: raj.due });
  assert.equal(clash.clash, true); // 12:00 vs 21:00, same task, two people

  const m = S.missedPromise({ promise: meena, events: [], nowMs: ms('2026-11-12T12:31:00+05:30') });
  assert.equal(m.status, 'missed');
  let count = 0;
  for (const e of ['slip', 'slip']) count = S.nextCount(count, e); // the clash and the miss
  assert.equal(S.escalationLevel(count), 'L2');

  const events = new Set(['E-pc-1', 'E-ph-2']);
  const card = { decision: 'slip', evidence: ['E-pc-1'], level: 2, card: { title: 'Missed.', fact: 'The banner file for Meena was due 12:00. Nothing was sent. This is the second slip this week.', expect: 'You fix it or you release it.', next: 'Pick a time to send it.' }, draft: '', confidence: 0.9 };
  assert.equal(validate(card, { evidenceIds: events, count }).ok, true);
});
