import { TZ_MIN } from './settings.js';
export const MIN = 60000, HOUR = 3600000, DAY = 86400000;
export const ms = (iso) => Date.parse(iso);
export const iso = (t) => {
  const d = new Date(t + TZ_MIN * MIN);
  const p = (n, w = 2) => String(n).padStart(w, '0');
  return `${d.getUTCFullYear()}-${p(d.getUTCMonth() + 1)}-${p(d.getUTCDate())}T${p(d.getUTCHours())}:${p(d.getUTCMinutes())}:${p(d.getUTCSeconds())}+05:30`;
};
export const parts = (t) => {
  const d = new Date(t + TZ_MIN * MIN);
  return { y: d.getUTCFullYear(), mo: d.getUTCMonth(), d: d.getUTCDate(), h: d.getUTCHours(), mi: d.getUTCMinutes(), dow: d.getUTCDay() };
};
export const at = (t, dayOffset, h, mi = 0) => {
  const p = parts(t);
  return Date.UTC(p.y, p.mo, p.d + dayOffset, h, mi) - TZ_MIN * MIN;
};
