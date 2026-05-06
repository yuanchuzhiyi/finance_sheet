# Android Report Notes And Comparison Design

> Goal: Make Android report notes period-scoped and add manual period comparison for year/month/day views.

## Scope
- Make item notes period-scoped instead of shared across all periods.
- Make bottom notes section period-scoped instead of shared across all periods.
- Add manual comparison period selection constrained to the current view mode.
- Show amount delta and percentage delta in summary cards and group subtotals.
- Keep existing data readable via migration-friendly model defaults.

## Data Model
- Extend report items from single `note` to period-scoped notes map keyed by period string.
- Extend report data from shared `notes` list to `notesByPeriod` keyed by period string.
- Keep legacy fields for decode compatibility and map them into the selected period when new structures are absent.

## UI
- Add an optional comparison dropdown near the current period selector; options are limited to the same granularity list (years/months/days).
- Add a "no comparison" option.
- Show delta amount and percent in summary cards when a comparison period is selected.
- Show delta amount and percent in each category group subtotal row when a comparison period is selected.
- Keep row layout single-period for now to avoid over-crowding on mobile.

## Behavior Rules
- Reading/writing item notes always uses the currently selected period.
- Reading/writing bottom notes list always uses the currently selected period.
- Delta = current - comparison.
- Percent = delta / comparison when comparison != 0; otherwise show 0% when both zero, else show --.

## Migration
- Existing saved item `note` remains readable.
- Existing saved shared `notes` remains readable.
- New edits write only to the period-scoped structures.
