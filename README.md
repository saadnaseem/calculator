# Scientific Calculator (Android)

A polished Android scientific calculator app built with Kotlin and Jetpack Compose, featuring algebraic evaluation with operator precedence, scientific functions, calculation history, and a modern Material 3 UI.

## Features

### Core Functionality
- **Algebraic Evaluation**: Full operator precedence support (not immediate execution)
- **Parentheses Support**: Complete parentheses handling for complex expressions
- **Scientific Functions**:
  - Trigonometric: `sin`, `cos`, `tan`, `asin`, `acos`, `atan`
  - Logarithms: `ln` (natural log), `log` (base 10), `log(x, base)` (custom base)
  - Other: `sqrt`, `abs`, `exp`, power (`^`), factorial (`!`)
- **Constants**: π (pi), e, ANS (last answer)
- **DEG/RAD Mode**: Toggle between degrees and radians for trigonometric functions
- **Calculation History**: View, tap to reuse, and clear history (persisted across app restarts)
- **Modern UI**: Material 3 design, responsive portrait/landscape layouts, accessibility-friendly

### Expression Rules
- **Operators**: `+`, `-`, `×`, `÷`, `^` (right-associative), `!` (postfix factorial)
- **Precedence** (highest → lowest):
  1. Factorial (`!`)
  2. Power (`^`), right-associative
  3. Unary minus (negation)
  4. Multiplication and Division (`×`, `÷`)
  5. Addition and Subtraction (`+`, `-`)
- **Functions**: Require parentheses (e.g., `sin(30)`, `log(100)`)
- **Error Handling**: User-friendly error messages for domain errors (divide by zero, invalid log arguments, etc.)

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM pattern with unidirectional data flow (state down, events up)
- **State Management**: ViewModel with StateFlow
- **Persistence**: DataStore (Preferences) for history and settings
- **Testing**: JUnit for unit tests, Compose UI tests for integration tests

### Project Structure
```
app/
  src/main/java/com/example/calculator/
    ├── MainActivity.kt              # App entry point
    ├── engine/                      # Pure Kotlin calculator engine
    │   ├── CalculatorEngine.kt      # Main engine interface
    │   ├── Tokenizer.kt             # Expression tokenization
    │   ├── ParserShuntingYard.kt    # Infix to postfix conversion
    │   ├── Evaluator.kt             # RPN evaluation
    │   ├── Formatter.kt             # Number formatting
    │   ├── EngineTypes.kt           # Type definitions
    │   └── Token.kt                 # Token definitions
    ├── ui/                          # Compose UI components
    │   ├── CalculatorScreen.kt      # Main screen
    │   ├── CalculatorKeypad.kt      # Keypad component
    │   ├── HistoryPanel.kt          # History bottom sheet
    │   └── theme/                   # Material 3 theme
    ├── vm/                          # ViewModel layer
    │   ├── CalculatorViewModel.kt   # Main ViewModel
    │   ├── CalculatorUiState.kt     # UI state definitions
    │   ├── CalculatorEvent.kt       # UI events
    │   └── InputRules.kt            # Input validation rules
    └── data/                        # Data layer
        ├── HistoryRepository.kt     # History business logic
        └── HistoryStore.kt          # DataStore persistence

  src/test/java/com/example/calculator/
    ├── engine/EngineTests.kt        # Engine unit tests
    └── vm/InputRulesTest.kt         # Input rules tests

  src/androidTest/java/com/example/calculator/
    └── CalculatorUiTests.kt        # UI integration tests
```

### Design Principles
- **Pure Kotlin Engine**: Calculator logic is completely independent of Android, making it easily testable
- **No Eval**: Expression evaluation uses proper parsing (shunting-yard algorithm) and RPN evaluation
- **Deterministic**: All calculations are deterministic and testable
- **Stateless UI**: Composable functions are mostly stateless; state is hoisted to ViewModel

## Requirements

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 or later
- **Android SDK**:
  - minSdk: 21 (Android 5.0 Lollipop)
  - targetSdk: 35
  - compileSdk: 35
- **Gradle**: 8.6.1 or later (included via wrapper)

## Quick start

### Run from Android Studio
1. Open the repo in Android Studio.
2. Let Gradle sync finish.
3. Select an emulator/device (API 21+).
4. Click **Run** ▶︎ (launches `MainActivity`).

### Run from the command line
From the repo root:

```bash
./gradlew :app:installDebug
```

## Building the App

### Prerequisites
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files (Android Studio will do this automatically)

### Build Commands

#### Debug Build
```bash
./gradlew assembleDebug
```
This creates an APK at `app/build/outputs/apk/debug/app-debug.apk`

#### Release Build
```bash
./gradlew assembleRelease
```
Note: Release builds require signing configuration. For testing, use debug builds.

#### Install on Connected Device/Emulator
```bash
./gradlew installDebug
```

### Running the App
1. Connect an Android device or start an emulator (API 21+)
2. In Android Studio, click the "Run" button, or
3. Use command line: `./gradlew installDebug` then launch the app manually

## Testing

This repo has three layers of tests:
- **Unit tests** (fast, local JVM): engine + input rules
- **Instrumentation/UI tests** (slower, requires device/emulator): Compose UI flows
- **Manual QA checklist**: broader UX validation in `docs/qa.md`

### Run everything (recommended before opening a PR)

```bash
./gradlew assembleDebug test lint
```

If you also want to run instrumentation tests:

```bash
./gradlew connectedAndroidTest
```

### Unit Tests

Unit tests verify the core calculator engine logic independently of Android components.

#### Run All Unit Tests
```bash
./gradlew test
```

#### Run Specific Test Class
```bash
./gradlew :app:testDebugUnitTest --tests "com.example.calculator.engine.EngineTests"
./gradlew :app:testDebugUnitTest --tests "com.example.calculator.vm.InputRulesTest"
```

#### View Test Results
After running tests, view the HTML report:
- **Location**: `app/build/reports/tests/testDebugUnitTest/index.html`
- Open in a browser to see detailed results

#### Test Coverage

**Engine Tests** (`EngineTests.kt`):
- Tests all 35+ example expressions from the spec
- Covers operator precedence, parentheses, scientific functions
- Validates error handling (divide by zero, domain errors, etc.)
- Tests DEG/RAD mode behavior
- Validates ANS constant functionality

**Input Rules Tests** (`InputRulesTest.kt`):
- Tests input validation logic
- Validates expression formatting rules

### UI Tests (Instrumentation Tests)

UI tests verify end-to-end user interactions using Compose testing framework.

#### Prerequisites
- An emulator or physical device must be connected (API 21+)
- Device/emulator should be unlocked
- For stable test runs, disable system animations on the device/emulator:
  - Developer Options → **Window animation scale** = Off
  - Developer Options → **Transition animation scale** = Off
  - Developer Options → **Animator duration scale** = Off

#### Run All UI Tests
```bash
./gradlew connectedAndroidTest
```

#### Run Specific UI Test Class
```bash
./gradlew :app:connectedDebugAndroidTest --tests "com.example.calculator.CalculatorUiTests"
```

#### View Test Results
- **Location**: `app/build/reports/androidTests/connected/index.html`
- Open in a browser to see detailed results

#### UI Test Coverage

**Calculator UI Tests** (`CalculatorUiTests.kt`):
- Basic arithmetic flows (e.g., `1 + 2 = 3`)
- Operator precedence (e.g., `1 + 2 × 3 = 7`)
- Trigonometric functions in DEG mode (e.g., `sin(30) = 0.5`)
- History functionality (compute, verify entry appears, tap to reload)
- Error handling flows

### Manual Testing

For comprehensive manual testing, refer to the [QA Checklist](docs/qa.md) which covers:
- Core calculation flows
- Scientific function testing
- History persistence
- Input ergonomics
- Display formatting
- Layout and accessibility
- Performance validation

#### Quick Manual Test Scenarios

1. **Basic Math**: `1 + 2 =` → Should show `3`
2. **Precedence**: `1 + 2 × 3 =` → Should show `7` (not `9`)
3. **Parentheses**: `(1 + 2) × 3 =` → Should show `9`
4. **Trigonometry**: `sin(30)` in DEG mode → Should show `0.5`
5. **History**: After calculation, open history panel and verify entry appears
6. **Error Handling**: `2 ÷ 0 =` → Should show `Error`

### Linting

Run Android Lint to check for code quality issues:

```bash
./gradlew lint
```

View lint results:
- **HTML Report**: `app/build/reports/lint-results-debug.html`
- **Text Report**: `app/build/reports/lint-results-debug.txt`

### Continuous Integration

For CI/CD pipelines, run all checks:

```bash
./gradlew assembleDebug test lint
```

### Running tests from Android Studio

#### Unit tests
1. Open `app/src/test/java/.../EngineTests.kt`
2. Click the green gutter icon next to the class or a test method
3. Or use **Run** → **Run 'All Tests'** (if configured)

#### Instrumentation/Compose UI tests
1. Start an emulator or connect a device
2. Open `app/src/androidTest/java/.../CalculatorUiTests.kt`
3. Click the green gutter icon to run

### Troubleshooting

- **No connected devices / “No devices found”**
  - Start an emulator in Android Studio (Device Manager), or connect a device with USB debugging enabled.
  - Confirm the device is visible: `adb devices`

- **UI tests are flaky**
  - Disable animations (see prerequisites above).
  - Ensure the emulator is not under heavy load; close other emulators and background apps.

- **Gradle/JDK issues**
  - Ensure Android Studio is using **JDK 17** (Preferences/Settings → Build Tools → Gradle).

## Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Keep functions small and focused
- Document complex logic with comments

### Testing Requirements
- All new features must include unit tests
- UI changes should include UI tests where applicable
- Tests must pass before merging changes

### Architecture Rules
- Keep calculator engine pure Kotlin (no Android dependencies)
- UI composables should be stateless; hoist state to ViewModel
- Use unidirectional data flow (state down, events up)
- Avoid adding external dependencies without justification

## Project Status

This project follows a milestone-based development approach. See [AGENTS.md](AGENTS.md) for the milestone breakdown and acceptance criteria, and `docs/spec.md` for the behavior spec that drives unit tests.

## Documentation

- **[AGENTS.md](AGENTS.md)**: Detailed project plan, milestones, and development guidelines
- **[docs/spec.md](docs/spec.md)**: Complete requirements and UX specification with example expressions
- **[docs/qa.md](docs/qa.md)**: Manual QA checklist for testing

