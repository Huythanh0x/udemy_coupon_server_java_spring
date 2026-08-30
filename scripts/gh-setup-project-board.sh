#!/usr/bin/env bash
# Creates the shared GitHub Projects (v2) board and wires up this repo as a source.
# NOT run automatically — review, then run yourself:
#   ./scripts/gh-setup-project-board.sh
#
# Requires: gh CLI >= 2.x with the `project` extension surface (built in on recent
# versions), authenticated as the account that should own the board (a user-owned
# project, since this is a personal-project board, not an org).
#
# Idempotent: safe to re-run — reuses an existing "Course Deals" project instead of
# creating a duplicate, and re-linking an already-linked repo is a no-op.
#
# This only adds THIS repo as a source. Run the matching "gh project link" step again
# from a checkout of course_deals_ComposeMultiplatform (or just re-run this script with
# that repo's owner/name) to add the second source, per docs/project-board.md.
set -euo pipefail

OWNER_ARG="${1:-@me}"
TITLE="Course Deals"

# `gh project link --owner` compares literally against the repo's owner login, so "@me"
# has to be resolved to a real login before use (passing "@me" through as-is is what
# causes "has different owner from '@me'").
if [ "$OWNER_ARG" = "@me" ]; then
  OWNER="$(gh api user --jq .login)"
else
  OWNER="$OWNER_ARG"
fi

EXISTING_NUMBER="$(gh project list --owner "$OWNER" --format json -q ".projects[] | select(.title==\"$TITLE\") | .number" | head -n1)"

if [ -n "${EXISTING_NUMBER:-}" ]; then
  echo "Reusing existing project '${TITLE}' (#${EXISTING_NUMBER}) for ${OWNER}"
  PROJECT_NUMBER="$EXISTING_NUMBER"
else
  echo "Creating project '${TITLE}' for ${OWNER}..."
  PROJECT_NUMBER="$(gh project create --owner "$OWNER" --title "$TITLE" --format json -q .number)"
  echo "Created: https://github.com/users/${OWNER}/projects/${PROJECT_NUMBER}"
fi

# `--repo` must be just the repo name here, not "owner/name" — the owner already comes
# from --owner, and gh rejects a --repo value whose embedded owner doesn't match it.
REPO_NAME="$(gh repo view --json name -q .name)"
echo "Linking ${OWNER}/${REPO_NAME} as a source..."
gh project link "$PROJECT_NUMBER" --owner "$OWNER" --repo "$REPO_NAME"

cat <<EOF

Next (manual, one-time, in the Projects UI — not scriptable via gh today):
  1. Add the "Status" single-select field with options: Backlog, Ready, In Progress, In Review, Done
     (see docs/project-board.md for column meanings).
  2. Link course_deals_ComposeMultiplatform as a second source (run from that repo's checkout,
     or from anywhere with):
       gh project link ${PROJECT_NUMBER} --owner ${OWNER} --repo course_deals_ComposeMultiplatform
  3. Turn on workflow automations (Settings > Workflows on the project):
       - "Item added to project" -> Status: Backlog
       - "Pull request opened" -> Status: In Review
       - "Item closed" -> Status: Done
EOF
