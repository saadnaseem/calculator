# Scientific Calculator – Requirements & UX Spec (M0)

## Scope & defaults
- Stack: Kotlin, Jetpack Compose, Material 3; pure Kotlin engine (no Android deps).
- Modes: algebraic (operator precedence), not immediate-execution.
- Angle mode default: DEG (user can toggle DEG/RAD).
- Numeric type: Double internally; deterministic, pure functions.
- No implicit multiplication (must enter explicit ×); may revisit later milestones.

## Supported keys & behaviors
- Digits/decimal: `0-9`, `.` (one decimal point per number).
- Operators: `+`, `-`, `×`, `÷`, `^` (right-associative), `!` (postfix factorial, highest precedence).
- Parentheses: `(`, `)`; must balance before evaluate.
- Functions (require parentheses):
  - Trig: `sin`, `cos`, `tan`; inverse: `asin`, `acos`, `atan`.
  - Logs/exp: `ln`, `log` (base 10), `log(x, base)` (two-arg), `exp`.
  - Roots/abs: `sqrt`, `abs`.
- Constants: `π`, `e`, `ANS` (last successful result).
- Mode toggle: `DEG/RAD` switches angle interpretation (trig only). In DEG, inverse trig outputs degrees; in RAD, outputs radians.
- ANS behavior:
  - Initialized to 0 on first launch/app start.
  - Updated only after successful `=` evaluation.
  - Pressing operator or function first may auto-insert `ANS` when needed (e.g., starting with `+` yields `ANS+`; `sin` inserts `sin(ANS)` only if no number/function in progress).
  - Dedicated `ANS` key inserts the literal token.
- Backspace: deletes last character; does not auto-fix tokens (future token-aware backspace possible in later milestone).
- Clear: clears current expression and transient error; leaves history and ANS unchanged.
- Equals: validates syntax (balanced parentheses, valid tokens), evaluates current expression with mode; on success, replaces expression with result string, updates ANS, appends history entry; on error, show `Error`, keep last stable expression and ANS.
- History panel:
  - Shows newest-first list of entries: expression, result, timestamp.
  - Tap expression → loads expression into editor (replaces current).
  - Tap result → inserts that value as current expression (equivalent to ANS = result and expression = result string).
  - Clear history action removes all entries (with confirmation).

## Parsing & evaluation rules
- Precedence (high → low): `!` → `^` (right-assoc) → unary minus → `×` `÷` → `+` `-`.
- Unary minus applies to the immediate following term/expression.
- Factorial valid only for integers >= 0; non-integer or negative → error.
- Trig:
  - DEG: inputs converted degrees→radians; inverse trig outputs in degrees.
  - RAD: inputs/outputs in radians.
- Logs:
  - `ln(x)`: domain x > 0.
  - `log(x)`: base 10, domain x > 0.
  - `log(x, b)`: domain x > 0, b > 0, b ≠ 1; result = log_b(x) via change of base.
- Division: divide by zero → error.
- Power: `a^b` uses Double `pow`; respects right-associativity.

## Formatting rules (display)
- Significant digits: 12 significant digits for final display (after evaluation).
- Trailing zeros trimmed in fractional part; remove trailing decimal point if empty.
- Scientific notation when `|value| >= 1e9` or `0 < |value| < 1e-6`; format like `1.23456789e+09` with up to 12 significant digits.
- Negative zero normalized to `0`.
- Rounding: standard half-away-from-zero on the last kept digit after computing in Double.

## Error handling (display `Error`)
- Syntax: invalid token, unexpected operator, mismatched parentheses, empty function args, missing operand.
- Math domain: log/ln of non-positive, sqrt of negative, factorial of non-integer or negative, divide by zero, invalid log base (<=0 or =1), overflow/NaN from Double operations.
- When `Error` shown: ANS remains last successful result; user can continue typing to clear error or press Clear.

## History
- Each successful evaluation appends {expression string as entered, formatted result, timestamp}.
- Stored newest-first; cap 50 entries (drop oldest when exceeding).
- Persists across restarts (via DataStore in later milestone; spec here defines behavior).

## Example expressions (expected output)
1. `1+2*3` → `7`
2. `(1+2)*3` → `9`
3. `2^3^2` → `512` (right-assoc)
4. `-3^2` → `-9`
5. `(-3)^2` → `9`
6. `5!` → `120`
7. `0!` → `1`
8. `3.2!` → `Error`
9. `sin(30)` (DEG) → `0.5`
10. `cos(0)` (DEG) → `1`
11. `tan(45)` (DEG) → `1`
12. `asin(0.5)` (DEG) → `30`
13. `acos(1)` (DEG) → `0`
14. `atan(1)` (DEG) → `45`
15. `ln(e)` → `1`
16. `log(1000)` → `3`
17. `log(8,2)` → `3`
18. `sqrt(9)` → `3`
19. `sqrt(-1)` → `Error`
20. `abs(-3.5)` → `3.5`
21. `exp(1)` → `2.71828182846`
22. `1/3` → `0.333333333333`
23. `2/0` → `Error`
24. `(2+3)*(4-1)` → `15`
25. `π+e` → `5.85987448205`
26. `-0.0000004` → `-4e-7`
27. `10000000000` → `1e10`
28. `ANS+5` (ANS initial 0) → `5`
29. After `2+2 = 4`, `ANS*3` → `12`
30. `sin(30)!` → `Error` (factorial non-integer)
31. `sqrt(abs(-16))` → `4`
32. `log(1,10)` → `0`
33. `log(1,1)` → `Error` (invalid base)
34. `tan(90)` (DEG) → `Error` (undefined)
35. `asin(2)` → `Error` (domain)

## Notes for tests (M3+)
- Tokenization must treat commas inside functions as separators (only for `log(x, b)` for now).
- No implicit multiplication; `2(3+4)` should be rejected as syntax error.
- Unary minus is distinct from binary minus; parser must disambiguate.
- ANS token should be recognized in tokenizer and substituted with stored Double before evaluation.

## Status notes
- M3 (engine) implemented and covered by unit tests in `app/src/test/java/com/example/calculator/engine/EngineTests.kt` using the 35 example expressions listed above (specExamples).

