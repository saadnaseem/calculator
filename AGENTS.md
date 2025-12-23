# Scientific Calculator (Android) — Agent Plan (AGENTS.md)

## Project goal
Build a polished Android scientific calculator app:
- Algebraic evaluation with operator precedence (not immediate execution)
- Parentheses support
- Scientific functions: sin/cos/tan, asin/acos/atan, ln/log10, sqrt, abs, exp, power (^), factorial (!)
- Constants: π, e, ANS (last answer)
- DEG/RAD mode toggle
- Calculation history (view, tap to reuse, clear; persisted across app restarts)
- Material 3 UI, responsive portrait/landscape, accessibility-friendly

Primary stack:
- Kotlin + Jetpack Compose + Material 3
- MVVM-ish (single-screen ViewModel) + unidirectional data flow (state down, events up)
- Pure-Kotlin calculator engine (no Android deps) with unit tests

Target SDK assumptions (edit if product needs differ):
- minSdk 21 (Compose minimum)
- targetSdk/compileSdk 35 (Play requirement for new apps/updates as of Aug 31, 2025)
If user specifies a different minSdk, adjust accordingly (must remain >= 21).

---

## Non-negotiables (rules for Codex)
1) Work milestone-by-milestone. Do NOT start a later milestone until current one passes its acceptance criteria.
2) No "eval", no JavaScript engine, no reflection hacks for expression evaluation.
3) Do NOT add new external dependencies without explicitly proposing them first.
   - Jetpack libraries (DataStore, Lifecycle, Navigation) are allowed if justified.
   - Prefer no serialization library; use a small custom encoding for history persistence unless approved otherwise.
4) Keep calculator logic testable and deterministic:
   - Expression parsing + evaluation must live in a pure Kotlin package/module.
5) Every milestone ends with:
   - `./gradlew assembleDebug` succeeds
   - Unit tests relevant to changes exist and pass (`./gradlew test`)
   - Lint passes or warnings are documented (`./gradlew lint`)
6) When editing code:
   - Make minimal, targeted changes
   - Avoid large refactors unless required for milestone acceptance
   - Keep UI composables mostly stateless; hoist state to ViewModel

---

## Build / test commands (run before finishing any milestone)
- ./gradlew assembleDebug
- ./gradlew test
- ./gradlew lint

Optional (if instrumentation tests are added in later milestones):
- ./gradlew connectedAndroidTest

---

## Repo structure (target)
app/
  src/main/java/.../ui/
    CalculatorScreen.kt
    CalculatorKeypad.kt
    HistoryPanel.kt
    theme/
  src/main/java/.../vm/
    CalculatorViewModel.kt
    CalculatorUiState.kt
    CalculatorEvent.kt
  src/main/java/.../data/
    HistoryRepository.kt
    HistoryStore.kt (DataStore)
core/ (or :calculator-engine module if you prefer)
  src/main/java/.../engine/
    Token.kt
    Tokenizer.kt
    ParserShuntingYard.kt
    Evaluator.kt
    Functions.kt
    Format.kt
  src/test/java/.../engine/
    EngineTests.kt

If multi-module is too heavy initially, keep core engine as a pure Kotlin package under app/
and extract to module later only if needed.

---

## Product behavior spec (high-level)
### Expression rules
- Operators: +, -, ×, ÷, ^, !
- Precedence (highest → lowest):
  1) factorial (!)
  2) power (^), right-associative
  3) unary minus (negation)
  4) × ÷
  5) + -
- Parentheses: ( )
- Functions require parentheses: sin(…), log(…), sqrt(…), etc.
- DEG/RAD affects trig functions only.
- Domain errors produce user-friendly "Error" state (divide by zero, log(<=0), sqrt(<0), factorial invalid, etc.)

### History
- Each successful evaluation appends a record:
  - expression (string)
  - result (string)
  - timestamp (optional)
- UI shows newest-first.
- Tap a history item:
  - tap expression loads it into the current expression editor
  - tap result inserts the result as ANS or replaces current expression (define exact UX in M0)
- Clear history action.

### Formatting
- Avoid ugly floating artifacts:
  - round display to a sensible number of significant digits (define in M0)
  - strip trailing zeros in decimals
  - use scientific notation for very large/small magnitudes
- Avoid "-0" (display "0").

---

## Implementation approach (engine)
### Core algorithm
- Tokenize input into:
  - numbers, operators, parentheses, constants, functions, commas (if multi-arg functions added later)
- Parse infix → postfix (RPN) using shunting-yard extended for functions and parentheses.
- Evaluate postfix using a stack.

### Numeric type
- Use a single internal numeric representation for simplicity.
  Recommendation:
  - Represent values as Double for trig/log/exp/pow (built-in math functions).
  - Add robust formatting + rounding for display to avoid typical Double artifacts.
  (Alternative is BigDecimal + converting for transcendentals; only do this if explicitly requested.)

### Function set (MVP scientific)
- sin, cos, tan
- asin, acos, atan
- ln (natural log), log (base10)
- sqrt
- abs
- exp (e^x)
- pow via ^ operator (and optionally pow(x,y) later)
- factorial (!) for non-negative integers only
- constants: pi, e
- ANS = last successful result

### DEG/RAD
- If DEG: convert degrees→radians before calling sin/cos/tan; convert radians→degrees for inverse trig outputs.
- If RAD: use raw radians.

---

## Milestones (with deliverables and acceptance criteria)

### M0 — Requirements & UX spec (docs + examples)
Deliverables:
- docs/spec.md with:
  - Supported keys + exact behavior per key (including parentheses, DEG/RAD, history interactions)
  - Formatting rules (precision, sci notation thresholds)
  - Error handling behavior
  - At least 30 example expressions with expected outputs (include trig in DEG mode, precedence, factorial, logs, parentheses)
Acceptance:
- Spec is detailed enough to become unit tests without interpretation.

### M1 — Project scaffold (Compose + Material 3)
Deliverables:
- Android project created: Kotlin + Compose + Material 3 theme
- Set minSdk/targetSdk/compileSdk per assumptions
- App launches to CalculatorScreen placeholder
Acceptance:
- assembleDebug passes; app launches on emulator/device

### M2 — UI layout (calculator + scientific keys + history panel shell)
Deliverables:
- Calculator UI:
  - Display area (expression + result preview line)
  - Keypad supports digits/operators/parentheses
  - Scientific keypad area (portrait: collapsible or secondary row; landscape: always visible)
  - History panel UI scaffold (bottom sheet or side panel; empty state)
- All keys emit events to ViewModel; no full math yet required
Acceptance:
- Tapping keys updates expression string on screen
- History panel opens/closes and shows placeholder list

### M3 — Engine v1 (tokenizer + shunting-yard + evaluator) + unit tests
Deliverables:
- Pure Kotlin engine:
  - Tokenizer: numbers, operators, functions, parentheses, constants
  - Parser: infix→postfix (shunting-yard with function support)
  - Evaluator: stack-based RPN evaluation
  - Function library + DEG/RAD config
  - Error model (sealed class) for domain/syntax errors
- Unit tests generated from docs/spec.md examples
Acceptance:
- All spec examples covered by tests (>= 30)
- Tests include:
  - precedence: 1+2*3 = 7
  - right-assoc power: 2^3^2 = 512
  - parentheses: (1+2)*3 = 9
  - trig DEG: sin(30)=0.5, cos(0)=1
  - ln(e)=1, log(100)=2
  - factorial: 5! = 120; 3.2! => Error
  - divide by zero => Error

### M4 — ViewModel integration (real calculations + preview)
Deliverables:
- CalculatorViewModel:
  - UiState: expression, previewResult (nullable), error, degRadMode, historyPreviewCount, etc.
  - UiEvents: KeyPressed(Key), ToggleDegRad, Equals, Clear, Backspace, HistoryOpen/Close, HistoryTap
  - Reducer-style logic
- Equals performs evaluation and commits result to history
- Optional: live preview on each input change (if stable/performance OK)
Acceptance:
- End-to-end: user can input expression with functions + parentheses and get correct result
- Errors show "Error" and recover via Clear or next valid input (per spec)

### M5 — History feature (real list + persistence)
Deliverables:
- HistoryRepository and persistence:
  - In-memory list in ViewModel (source of truth)
  - Persist history and DEG/RAD setting via DataStore (Preferences DataStore is OK)
  - Define max history length (e.g., 50); drop oldest
- History UI:
  - Shows list of expression/result
  - Tap behaviors implemented (load expression / insert result)
  - Clear history action with confirmation (dialog)
Acceptance:
- History survives app restart
- UI updates instantly
- Clear works and persistence reflects cleared state

### M6 — Scientific polish (2nd functions, ANS, input ergonomics)
Deliverables:
- Add:
  - "2nd" toggle to show inverse trig (asin/acos/atan) (if not already directly available)
  - ANS button inserts last result token
  - Better input rules: implicit multiplication (optional stretch) like 2(3+4) or π(2) if enabled by spec
  - Better backspace behavior (delete token-aware, not just character, if feasible)
Acceptance:
- Usability improvements validated by manual QA checklist
- No regressions in unit tests

### M7 — UI tests + accessibility baseline
Deliverables:
- Compose UI tests:
  - basic flow: 1 + 2 = shows 3
  - precedence flow: 1 + 2 × 3 = shows 7
  - trig flow: DEG + sin(30) = shows 0.5
  - history flow: compute, verify history entry appears, tap to reload
- Semantics:
  - All keys have content descriptions and/or stable semantics tags for tests
  - Display is accessible for TalkBack
Acceptance:
- connectedAndroidTest passes (if available), otherwise local androidTest passes in CI emulator config

### M8 — Release readiness
Deliverables:
- docs/qa.md manual QA checklist (edge cases, rotation, large numbers, errors)
- App icon + proper app name
- Performance sanity:
  - evaluation is fast for typical input
  - no UI jank when history grows (cap list)
- Final lint cleanup
Acceptance:
- All gradle checks pass
- App feels “store-ready” for a small utility app

---

## Definition of Done (for any PR)
- Code compiles
- Tests added/updated
- No TODOs left in production code (unless explicitly tracked in docs/todo.md)
- UI changes include screenshots or a short screen recording note (if your workflow supports it)
- Update docs/spec.md if behavior changed

---

## Open questions (ask user if not already decided)
1) What exact min Android version do you want to support?
   - targetSdk 35 is required for new apps/updates
2) Default trig mode: DEG or RAD? (Plan assumes DEG)
3) Do you want multi-argument functions in MVP (e.g., log(x, base), pow(x,y))? yes 
4) Should history store timestamps and show them, or just expression/result? yes 
5) Should "Ans" be a button only, or also auto-insert after operators (like many calculators)? yes 