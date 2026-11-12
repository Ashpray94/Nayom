import test from 'node:test';
import assert from 'node:assert/strict';
import { ms } from '../src/time.js';
import * as R from '../src/routine.js';
import { reduceLedger, parseLedger } from '../src/ledger.js';

test('wake: initial until 5 nights, then median, corrections weigh 3', () => {
  assert.equal(R.learnWake(['07:00', '07:10']), '07:30');
  assert.equal(R.learnWake(['07:00', '07:10', '07:20', '07:15', '07:05']), '07:10');
  assert.equal(R.learnWake(['07:00', '07:10', '07:20', '07:15', '07:05'], ['06:30']), '07:05'); // 6 votes of 8 weight pull it earlier
});

test('bedtime: after-midnight starts count as late evening', () => {
  assert.equal(R.learnBedtime(['23:40', '00:20', '00:10', '23:50', '00:30']), '00:10');
  assert.equal(R.learnBedtime(['23:00']), '23:30');
});

test('work blocks: 20-minute minimum, near gaps merge', () => {
  const t = (a, b) => ({ start: ms(`2026-11-12T${a}:00+05:30`), end: ms(`2026-11-12T${b}:00+05:30`) });
  const blocks = R.workBlocks([t('09:30', '09:50'), t('09:55', '10:30'), t('11:00', '11:10'), t('14:00', '14:19')]);
  assert.equal(blocks.length, 1);
  assert.equal(blocks[0].end - blocks[0].start, 60 * 60000);
});

test('proof line and praise rules', () => {
  const m = reduceLedger(parseLedger(['2026-11-10T10:00:00+05:30 | P1 | kept | E1 | ph |', '2026-11-10T11:00:00+05:30 | P2 | missed | | ph |', '2026-11-11T11:00:00+05:30 | P3 | open | | ph |'].join('\n')));
  assert.equal(R.weekCount(m, ['P1', 'P2', 'P3']).line, 'Kept 1 of 3');
  assert.equal(R.praiseAllowed({ keptInARow: 1 }), false);
  assert.equal(R.praiseAllowed({ keptInARow: 3 }), true);
  assert.equal(R.praiseAllowed({ keptInARow: 3, slipInSameCard: true }), false);
  assert.equal(R.praiseAllowed({ keptInARow: 4, lastPraisedAtStreak: 4 }), false);
});
