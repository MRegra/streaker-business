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

elif [[ "$COMPONENT" == "global" ]]; then
  echo "Bumping global version to $VERSION"
  echo "$VERSION" > VERSION

else
  echo "Unknown component: $COMPONENT"
  exit 1
fi
