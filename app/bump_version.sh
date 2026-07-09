#!/usr/bin/env bash
# Bumps the app version, writes a Play changelog and (unless DRY_RUN=1) commits, tags and pushes.
# Usage: app/bump_version.sh <major|minor|patch> "<changelog>"
set -euo pipefail

BUMP_TYPE="${1:?bump type (major|minor|patch) required}"
CHANGELOG="${2:?changelog content required}"

PROPS="app/version.properties"
[[ -f "$PROPS" ]] || { echo "Missing $PROPS" >&2; exit 1; }

read_prop() { grep -E "^$1=" "$PROPS" | head -1 | cut -d= -f2- | tr -d '\r'; }

major="$(read_prop version.major)"
minor="$(read_prop version.minor)"
patch="$(read_prop version.patch)"

case "$BUMP_TYPE" in
  major) major=$((major + 1)); minor=0; patch=0 ;;
  minor) minor=$((minor + 1)); patch=0 ;;
  patch) patch=$((patch + 1)) ;;
  *) echo "BUMP_TYPE must be one of: major, minor, patch" >&2; exit 1 ;;
esac

semver="${major}.${minor}.${patch}"
# Mirror app/build.gradle.kts: versionCode = major*10000 + minor*100 + patch
version_code=$((major * 10000 + minor * 100 + patch))
echo "New version: ${semver} (${version_code})"

sed -i \
  -e "s/^version.major=.*/version.major=${major}/" \
  -e "s/^version.minor=.*/version.minor=${minor}/" \
  -e "s/^version.patch=.*/version.patch=${patch}/" \
  -e "s/^version.semver=.*/version.semver=${semver}/" \
  "$PROPS"

changelog_file="fastlane/metadata/android/en-US/changelogs/${version_code}.txt"
mkdir -p "$(dirname "$changelog_file")"
printf '%b\n' "$CHANGELOG" > "$changelog_file"
echo "Wrote changelog: ${changelog_file}"

if [[ "${DRY_RUN:-}" == "1" ]]; then
  echo "DRY_RUN=1: skipping git commit/tag/push"
else
  git add "$PROPS" "$changelog_file"
  git commit -m "🔖 Prepare release ${semver} (${version_code})" -m "[skip ci]"
  git tag -a "$semver" -m "Release ${semver}"
  git push origin HEAD --tags
fi

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  {
    echo "version=${semver}"
    echo "versioncode=${version_code}"
  } >> "$GITHUB_OUTPUT"
fi
