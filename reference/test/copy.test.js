import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

// Lints spec 13 against the banned list and the 40-word card limit.
const text = readFileSync(new URL('../../spec/13-copy-library.md', import.meta.url), 'utf8').split('\n');
const BANNED = ['poor you', "it's okay", "don't worry", 'no big deal', 'you got this', 'amazing', 'awesome', 'proud of you', 'lazy', 'useless', 'hopeless', 'failure', 'liar', 'stupid', 'again?!', "why can't you", 'beta', 'baby', 'sweetie', 'sweetheart'];
const SLIP_SECTIONS = ['Slip card', 'Mismatch card', 'Avoidance', 'Stretch'];

const cells = []; // { section, line, text }
let section = '';
text.forEach((l, i) => {
  if (l.startsWith('## ')) section = l.slice(3);
  if (!l.startsWith('|') || /^\|[\s:|-]+\|$/.test(l)) return;
  const row = l.split('|').slice(1, -1).map((c) => c.trim());
  if (section === 'Banned words and moves' || row[0] === 'Part' || row[0] === 'Choice') return;
  row.slice(1).forEach((c) => c && cells.push({ section, line: i + 1, text: c }));
});

test('copy lint: found cells', () => assert.ok(cells.length > 40, `only ${cells.length} cells`));

test('copy lint: no banned words or moves', () => {
  const hits = cells.flatMap((c) => BANNED.filter((b) => new RegExp(`(^|\\W)${b.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}(\\W|$)`, 'i').test(c.text)).map((b) => `L${c.line} "${b}": ${c.text}`));
  assert.deepEqual(hits, []);
});

test('copy lint: no exclamation marks on slips', () => {
  const hits = cells.filter((c) => SLIP_SECTIONS.some((s) => c.section.startsWith(s)) && c.text.includes('!')).map((c) => `L${c.line}: ${c.text}`);
  assert.deepEqual(hits, []);
});

test('copy lint: every cell is 40 words or fewer', () => {
  const hits = cells.filter((c) => c.text.split(/\s+/).length > 40).map((c) => `L${c.line} (${c.text.split(/\s+/).length}w)`);
  assert.deepEqual(hits, []);
});
