// Defaults from spec 10 "settings.md defaults". Offsets: IST (+05:30).
export const TZ_MIN = 330;
export const DEFAULTS = {
  gate: { pause_ms: 800, ask_window_min: 10, reflex_s: 60 },
  load: { open_due_48h: 3 },
  promise: { grace_min: 30, draft_below: 0.6 },
  claim: { window_h: 4, min_minutes: 20 },
  clash: { min_gap_h: 4, min_shared_keywords: 2 },
  avoid: { read_h: { 1: 2, 2: 4, 3: 24 }, deleted_count: 2, deleted_window_min: 30, call_h: 24 },
  drift: { feed_min: 10, switches_h: 40, max_per_day: 3 },
  care: { stand_min: 120 },
  feedApps: ['instagram', 'youtube', 'x', 'facebook', 'reddit', 'snapchat'],
};
