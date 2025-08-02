#!/bin/bash
# Usage: ./bump-version.sh 1.2.3

VERSION=$1

if [[ -z "$VERSION" ]]; then
  echo "No version passed to bump-version.sh"
  exit 1
fi

# Update the pom.xml version tag
echo "Bumping pom.xml to version $VERSION"
sed -i "s|<version>.*</version>|<version>$VERSION</version>|" backend/pom.xml

# Save version to VERSION file too
echo "$VERSION" > VERSION
