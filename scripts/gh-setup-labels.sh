#!/usr/bin/env bash
# Creates the type / priority / area / size labels described in docs/project-management.md.
# NOT run automatically — review, then run yourself:
#   ./scripts/gh-setup-labels.sh
#
# Requires: gh CLI, authenticated, run from inside a checkout of this repo (or pass
# --repo owner/name to every `gh label create` call if run elsewhere).
#
# Same taxonomy as course_deals_ComposeMultiplatform's scripts/gh-setup-labels.sh —
# keeping both repos' labels identical is what makes the shared board's area filter
# meaningful across repos.
set -euo pipefail

create() {
  local name="$1" color="$2" description="$3"
  if gh label list --limit 200 --json name -q '.[].name' | grep -qxF "$name"; then
    echo "skip (exists): $name"
  else
    gh label create "$name" --color "$color" --description "$description"
    echo "created: $name"
  fi
}

delete() {
  local name="$1"
  if gh label list --limit 200 --json name -q '.[].name' | grep -qxF "$name"; then
    gh label delete "$name" --yes
    echo "deleted: $name"
  else
    echo "skip (already gone): $name"
  fi
}

# GitHub's default labels — not part of this repo's type/priority/area/size taxonomy.
# `bug`/`documentation`/`enhancement` overlap with `type: bug`/`type: docs`/`type: feature`;
# the rest aren't part of the documented scheme at all. Safe to remove as long as no open
# issue still relies on them — double-check before running if that's changed since this
# was written.
delete "bug"
delete "documentation"
delete "enhancement"
delete "duplicate"
delete "invalid"
delete "question"
delete "wontfix"
delete "good first issue"
delete "help wanted"

# type (exactly one per issue)
create "type: feature"  "1D76DB" "New functionality"
create "type: bug"      "D73A4A" "Something isn't working"
create "type: chore"    "C5DEF5" "Maintenance, no user-facing change"
create "type: docs"     "0075CA" "Documentation only"
create "type: refactor" "5319E7" "Restructuring, no behavior change"
create "type: test"     "BFD4F2" "Adding or correcting tests"
create "type: design"   "E99695" "UX/UI or design-system work"

# priority (exactly one per issue)
create "priority: P0" "B60205" "Blocking"
create "priority: P1" "D93F0B" "This session"
create "priority: P2" "FBCA04" "Backlog"
create "priority: P3" "0E8A16" "Someday"

# area (as many as apply)
create "area: android" "006B75" "Android app"
create "area: backend"  "5B0E86" "Spring Boot API/crawler"
create "area: web"      "1B6E67" "Web/KMP frontend"
create "area: design"   "F9D0C4" "Design system, UX"
create "area: testing"  "BFDADC" "Test infra/coverage"
create "area: ci"       "444444" "CI/CD pipelines"
create "area: infra"    "0052CC" "Infra, deployment, hosting"

# size (Fibonacci story points)
create "size: 1"  "C2E0C6" "~1 hr"
create "size: 2"  "C2E0C6" "~2 hrs"
create "size: 3"  "C2E0C6" "~half day"
create "size: 5"  "C2E0C6" "~1 day"
create "size: 8"  "FEF2C0" "~2 days"
create "size: 13" "F9C513" "Split before starting"
