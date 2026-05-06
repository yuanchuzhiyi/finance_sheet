#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

PROJECT="FamilyFinance.xcodeproj"
SCHEME="FamilyFinance"

if [[ ! -d "$PROJECT" ]]; then
  echo "Missing $ROOT_DIR/$PROJECT. Run: $ROOT_DIR/scripts/generate_xcodeproj.sh" >&2
  exit 1
fi

METHOD="${1:-development}" # development | ad-hoc | app-store | enterprise
OUT_DIR="${2:-build/ipa}"
TEAM_ID="${TEAM_ID:-}"
BUNDLE_ID="${BUNDLE_ID:-}"

ARCHIVE_PATH="build/${SCHEME}.xcarchive"
EXPORT_OPTIONS="build/ExportOptions.plist"

mkdir -p build

if [[ -n "$TEAM_ID" ]]; then
  TEAM_BLOCK="<key>teamID</key><string>${TEAM_ID}</string>"
else
  TEAM_BLOCK=""
fi

cat > "$EXPORT_OPTIONS" <<PLIST
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>method</key><string>${METHOD}</string>
  <key>signingStyle</key><string>automatic</string>
  ${TEAM_BLOCK}
</dict>
</plist>
PLIST

XCODEBUILD_SIGN_ARGS=()
if [[ -n "$TEAM_ID" ]]; then
  XCODEBUILD_SIGN_ARGS+=("DEVELOPMENT_TEAM=${TEAM_ID}")
fi
if [[ -n "$BUNDLE_ID" ]]; then
  XCODEBUILD_SIGN_ARGS+=("PRODUCT_BUNDLE_IDENTIFIER=${BUNDLE_ID}")
fi

echo "Archiving…"
xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration Release \
  -destination "generic/platform=iOS" \
  -allowProvisioningUpdates \
  "${XCODEBUILD_SIGN_ARGS[@]}" \
  archive \
  -archivePath "$ARCHIVE_PATH"

echo "Exporting IPA…"
xcodebuild \
  -exportArchive \
  -archivePath "$ARCHIVE_PATH" \
  -exportPath "$OUT_DIR" \
  -exportOptionsPlist "$EXPORT_OPTIONS" \
  -allowProvisioningUpdates \
  "${XCODEBUILD_SIGN_ARGS[@]}"

echo "Done. Output: $ROOT_DIR/$OUT_DIR"
