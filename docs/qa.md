# Manual QA Checklist (M8)

Use a physical device or emulator. Unless noted, perform checks in both portrait and landscape.

## Core flows
- Launch app → title shows, display defaults to `0`, mode shows `DEG`.
- Basic math: enter `1+2=` → result `3`, expression replaced by `3`.
- Precedence: `1+2×3=` → result `7`.
- Parentheses: `(1+2)*3=` → result `9`.
- Power right-assoc: `2^3^2=` → result `512`.
- Factorial: `5!` → `120`; `3.2!` → `Error`.
- Division by zero: `2÷0=` → shows `Error`, next digit input clears error state.

## Scientific
- DEG/RAD toggle: tap toggle to RAD, compute `sin(π/2)` → ~`1`; switch back to DEG and `sin(30)` → `0.5`.
- Inverse trig via 2nd: enable 2nd, `asin(0.5)` → `30` (DEG), disable 2nd to return to primary trig.
- Logs: `ln(e)` → `1`; `log(8,2)` → `3`; invalid base `log(1,1)` → `Error`.
- Roots/abs/exp: `sqrt(9)` → `3`; `sqrt(-1)` → `Error`; `abs(-3.5)` → `3.5`; `exp(1)` → starts `2.718...`.
- ANS: after `2+2=`, entering `×3=` yields `12`; leading `+` on empty inserts `ANS+` and evaluates correctly.

## History
- After a successful calculation, entry appears with expression/result/timestamp, newest first.
- Tap expression reloads it into editor; tap result sets expression/result to that value.
- Clear history dialog: opens via Clear button, Cancel dismisses, Clear removes entries; persistence remains cleared after relaunch.

## Persistence
- Close and relaunch: history remains, ANGLE mode persists, ANS persists from last success.

## Input ergonomics
- Leading operators auto-seed ANS; leading function does not auto-insert ANS so you can type your own argument.
- Backspace removes whole tokens when possible (e.g., deletes `sin(` or `ANS` in one tap).
- Decimal typing allows only one `.` per number; invalid sequences produce `Error` on evaluate.

## Display/formatting
- Large numbers: `10000000000` shows `1e10`; very small `-0.0000004` shows `-4e-7`; no `-0`.
- Trailing zeros trimmed (e.g., `1/3` displays `0.333333333333` within 12 sig figs).

## Layout & accessibility
- Portrait/landscape: keypad and scientific row remain reachable; history sheet usable.
- Semantics: display expression/result have content descriptions; keys/testTags exist (`key_7`, `key_+`, `key_=` etc.), toggles (`toggle_angle_mode`, `toggle_second`), history button (`open_history`).
- TalkBack reads button labels appropriately; bottom sheet dismissible by swipe/tap outside.

## Performance
- Rapid keying of 20+ characters stays responsive; opening history with ~50 entries remains smooth.

## Release sanity
- Run `./gradlew assembleDebug`, `./gradlew test`, `./gradlew lint` → all pass.


