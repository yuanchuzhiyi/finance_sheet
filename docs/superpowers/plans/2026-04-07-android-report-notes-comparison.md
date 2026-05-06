# Android Report Notes And Comparison Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Android report notes period-scoped and add manual comparison period support with delta/percent displays.

**Architecture:** Update the report model to support period-scoped notes with compatibility helpers, compute comparison state in the ViewModel, and surface comparison selectors plus delta summaries in the Compose UI. Add JVM tests for model behavior before changing production code.

**Tech Stack:** Kotlin, Jetpack Compose, StateFlow, DataStore Preferences, JUnit4

---

### Task 1: Add model tests for period-scoped notes and comparison math
**Files:**
- Modify: `android/app/build.gradle.kts`
- Create: `android/app/src/test/java/com/familyfinance/sheet/data/model/ReportDataTest.kt`

- [ ] Step 1: Add test source set dependencies if needed and write failing tests for item notes, bottom notes, and comparison calculations.
- [ ] Step 2: Run the JVM test task and confirm the new tests fail for the expected missing behavior.
- [ ] Step 3: Implement the minimal model changes to satisfy the tests.
- [ ] Step 4: Re-run the JVM tests and confirm they pass.

### Task 2: Wire comparison state into the ViewModel
**Files:**
- Modify: `android/app/src/main/java/com/familyfinance/sheet/viewmodel/MainViewModel.kt`
- Modify: `android/app/src/main/java/com/familyfinance/sheet/data/model/ReportData.kt`

- [ ] Step 1: Add failing tests or model assertions for comparison helpers if Task 1 did not already cover them.
- [ ] Step 2: Add comparison-period state, derived comparison options, and summary/group comparison accessors.
- [ ] Step 3: Run JVM tests again to confirm compatibility and helper behavior.

### Task 3: Update Compose UI for period-scoped notes and manual comparison
**Files:**
- Modify: `android/app/src/main/java/com/familyfinance/sheet/ui/screens/MainScreen.kt`
- Modify: `android/app/src/main/java/com/familyfinance/sheet/ui/components/CategoryGroupSection.kt`
- Modify: `android/app/src/main/java/com/familyfinance/sheet/ui/components/ReportItemRow.kt`
- Modify: `android/app/src/main/java/com/familyfinance/sheet/ui/components/NotesSection.kt`

- [ ] Step 1: Update the screen to show a comparison selector for the active period dimension.
- [ ] Step 2: Bind item-note edits and bottom-note edits to the selected period instead of shared fields.
- [ ] Step 3: Show delta amount and delta percent in summary cards and group subtotals when a comparison period is selected.
- [ ] Step 4: Build the Android debug target or compile Kotlin sources to catch Compose/API errors.

### Task 4: Update PDF summary output for comparison context
**Files:**
- Modify: `android/app/src/main/java/com/familyfinance/sheet/util/PdfGenerator.kt`

- [ ] Step 1: Extend PDF generation inputs for optional comparison summary data.
- [ ] Step 2: Render comparison information in the summary section when a comparison period is selected.
- [ ] Step 3: Re-run the selected verification commands.
