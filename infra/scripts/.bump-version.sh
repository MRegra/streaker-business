#!/bin/bash
# Usage: ./bump-version.sh <version> <frontend|backend|global>

VERSION=$1
COMPONENT=$2

if [[ -z "$VERSION" || -z "$COMPONENT" ]]; then
  echo "Usage: ./bump-version.sh <version> <frontend|backend|global>"
  exit 1
fi

if [[ "$COMPONENT" == "backend" ]]; then
  echo "Bumping backend version to $VERSION"
  POM_FILE="backend/pom.xml"

  if [[ -f $POM_FILE ]]; then
    awk -v new_version="$VERSION" '
    BEGIN { found_group = 0; found_artifact = 0; replaced = 0 }
    /<groupId>com<\/groupId>/ { found_group = 1 }
    /<artifactId>streaker<\/artifactId>/ { if (found_group) found_artifact = 1 }
    found_group && found_artifact && /<version>.*<\/version>/ && !replaced {
      sub(/<version>.*<\/version>/, "<version>" new_version "</version>")
      replaced = 1
    }
    { print }
    ' "$POM_FILE" > "$POM_FILE.tmp" && mv "$POM_FILE.tmp" "$POM_FILE"
  else
    echo "ERROR: $POM_FILE not found!"
    exit 1
  fi

  echo "$VERSION" > VERSION_BACKEND

elif [[ "$COMPONENT" == "frontend" ]]; then
  echo "Bumping frontend version to $VERSION"
  echo "$VERSION" > VERSION_FRONTEND

elif [[ "$COMPONENT" == "global" ]]; then
  echo "Bumping global version to $VERSION"
  echo "$VERSION" > VERSION

else
  echo "Unknown component: $COMPONENT"
  exit 1
fi
