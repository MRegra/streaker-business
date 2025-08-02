#!/bin/bash
# Usage: ./bump-version.sh <version> <component>

VERSION=$1
COMPONENT=$2

if [[ -z "$VERSION" || -z "$COMPONENT" ]]; then
  echo "Usage: ./bump-version.sh <version> <frontend|backend>"
  exit 1
fi

if [[ "$COMPONENT" == "backend" ]]; then
  echo "Bumping backend version to $VERSION"
  if [[ -f backend/pom.xml ]]; then
    sed -i "s|<version>.*</version>|<version>$VERSION</version>|" backend/pom.xml
  else
    echo "ERROR: backend/pom.xml not found!"
    exit 1
  fi
  echo "$VERSION" > VERSION_BACKEND
elif [[ "$COMPONENT" == "frontend" ]]; then
  echo "Bumping frontend version to $VERSION"
  echo "$VERSION" > VERSION_FRONTEND
else
  echo "Unknown component: $COMPONENT"
  exit 1
fi