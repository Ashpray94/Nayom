# 06. Design language

Owner role: visual / UI designer. Inputs: 04, 05, decision D-15 (warm, colourful, cheerful).

## Principles

1. **Warm, not soft.** Sunny colours, firm content. The look is cheerful; the words are plain and direct.
2. **A slip must never hide.** It uses colour, a shape and a word, never colour alone.
3. **Three things, not thirty.** Today shows three tasks. Cards show one decision.
4. **One face only: the icon.** She has a robot face on the app icon and nowhere else. Inside the app she is a voice and a colour, not a drawing. No mascot in cards, empty states or notifications.
5. **Reuse first.** Cards, list rows, dialogs, toasts and icons from Private Folder are reused and restyled with these tokens. New components only where listed in the last section.

## Colour tokens: light (default)

Teal family (D-22). Light is the default (D-23). Contrast figures are measured values, WCAG ratio.

| Token | Hex | Use | Check |
| --- | --- | --- | --- |
| `bg` | `#F2F8F7` | App background | – |
| `surface` | `#FFFFFF` | Cards | – |
| `ink` | `#12302F` | Main text | 13.12 on bg, 14.10 on surface |
| `muted` | `#4B6663` | Secondary text | 5.78 on bg |
| `teal` | `#23706B` | Primary fill, buttons, links, today highlight | white on teal 5.83; teal on bg 5.43 |
| `teal-tint` | `#D6ECE8` | Selected, info background | ink 11.43 |
| `mint` | `#5FDDB0` | Glow and accents only (icon, eyes), never text | – |
| `amber` | `#F5A524` | Due soon. Fill only, with `ink` text | ink on amber 6.91 |
| `amber-tint` | `#FFE9B8` | Due-soon card background | ink 11.82 |
| `berry` | `#B3265B` | Missed, doesn't match | white on berry 6.28; berry on bg 5.84 |
| `berry-tint` | `#FBE1EA` | Missed card background | ink 11.45 |
| `leaf` | `#3F7A1F` | Kept, done | white on leaf 5.23; leaf on bg 4.87 |
| `leaf-tint` | `#E3F1D8` | Kept card background | ink 11.98 |
| `sky` | `#2563B8` | Care items | white on sky 5.91; sky on bg 5.50 |
| `sky-tint` | `#DCE8FA` | Care card background | ink 11.39 |

Rules:
- Amber is never used for text on light backgrounds, only as a fill with `ink` text.
- Teal, leaf and sky are close in brightness (contrast 1.01 to 1.11 between them). A state is never shown by colour alone: Kept carries a check icon and the word "Kept"; Missed carries the "!" shape, a thick left bar and the word "Missed"; Care carries the heart icon.
- Amber and berry are also close (3.08), so Due soon carries a clock icon and its time.

## Colour tokens: dark

| Token | Hex | Check |
| --- | --- | --- |
| `bg` | `#0B1B1D` | – |
| `surface` | `#122A2C` | – |
| `ink` | `#E8F5F2` | 15.79 on bg, 13.47 on surface |
| `muted` | `#A9C4C0` | 9.54 on bg |
| `teal` | `#5FD0C0` | on bg 9.48; ink-dark on teal 9.48 |
| `amber` | `#FFB938` | on bg 10.30 |
| `berry` (text) | `#FF7AA8` | 7.24 on bg |
| `leaf` (text) | `#9FD66B` | 10.35 on bg |
| `sky` (text) | `#7FB0FF` | 8.04 on bg |

Appearance setting: Light (default), Dark, Follow phone. Nayom starts in Light and never switches on its own (D-23). It does not ask for location, so it cannot time dark by sunset.

## Type

Family: Nunito (open licence, rounded, friendly), bundled in the app. Numbers use tabular figures so due times line up.

| Style | Size / line | Weight | Use |
| --- | --- | --- | --- |
| Display | 32 / 40 | ExtraBold | Date on Today |
| Title | 22 / 28 | Bold | Card headings |
| Body | 16 / 24 | Regular | Everything else |
| Label | 14 / 20 | SemiBold | Buttons, chips |
| Caption | 12 / 16 | Regular | Evidence line, timestamps |

Layouts must hold at 200% font size and at 30% longer text, because Hinglish is often longer than English.

## Space, shape, depth

- 4-point grid. Common steps: 4, 8, 12, 16, 24, 32.
- Screen edge padding 16. Card padding 16. Gap between cards 12.
- Radius: cards 16, sheets 24, buttons 999 (pill), chips 999.
- Depth: flat. A 1-point warm outline `#E8D9C3` in light mode, `#3A2E22` in dark.
- Minimum touch target 48 × 48.
- Primary buttons sit in the bottom third of the screen for one-hand use.

## Icons

Rounded outline set, 24 size, 2 stroke. The Private Folder service already ships Material icons, so the same family is used here.

| Meaning | Icon |
| --- | --- |
| Missed | Triangle with "!" |
| Kept | Check in circle |
| Due soon | Clock |
| Care | Heart |
| Private | Eye with a line |
| Pause | Two bars |
| Support | Phone |

## Motion

- Standard duration 200 ms, ease-out. Cards slide up from the bottom.
- The gate pause is a ring that fills over 5 seconds. With reduced motion on, the ring becomes a number counting 5 to 1.
- One firm haptic tick when a full-screen card appears. No sound.
- Nothing bounces or wiggles.

## Component states (each component has all of these)

default, pressed, disabled, focused (2-point sky ring), loading (loader icon swapped in), error (toast).

## Key screens in detail

### Gate card (I-01), full screen
1. Top: Title "Hold on." (see 13), then the ask in one line: "Ravi · Banner tweak · Today".
2. Middle: a leaf or berry strip. "Open and due in 48 h: 3". "Pushes out: Meena's file, 17:00" in a berry-tint box with the "!" icon.
3. Bottom: three buttons, full width, stacked: Yes, Later, No. All disabled for 5 seconds with the ring above them. A fourth text link: "Why am I seeing this".
4. Back gesture is ignored for 5 seconds.

### Slip card (I-02)
Same shell. Berry bar on the left. Title "Missed". One line of fact. One line of evidence in Caption. Two buttons: "Fix it by…" and "Release". A text link "Wrong" opens S-20.

### Today (S-01)
Display date and day at top. Three task cards under it, each with a person chip, a due time and a state colour. One care card in sky-tint at the bottom. A round Debrief button, teal, bottom right.

### Lock-screen widget (I-05)
Date, day, the next due item and time. No private text, because a lock screen is public.

## Accessibility

- Every colour pair above passes 4.5:1 for text; large text passes 3:1.
- Every state has a text label and a shape, not only a colour.
- Screen reader names for every control; the gate announces "Pause, 5 seconds" then "Buttons ready".
- Works with one hand, with large fonts, and without sound.

## New components (everything else is reused)

| Component | Why it is new |
| --- | --- |
| Full-screen interrupt card with pause ring | No existing equivalent |
| Today three-task stack | Specific to Nayom |
| Proof wall entry | Specific to Nayom |
| Heartbeat chip ("Reading: on") | Specific to the readers |

Changes: Removed sunset timing for dark mode, because Nayom has no location permission.
