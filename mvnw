#!/bin/sh
MAVEN_VERSION=3.8.8
MAVEN_ZIP="apache-maven-$MAVEN_VERSION-bin.zip"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/$MAVEN_ZIP"
MAVEN_DIR="$HOME/.m2/wrapper/dists/apache-maven-$MAVEN_VERSION"

if [ ! -f "$MAVEN_DIR/apache-maven-$MAVEN_VERSION/bin/mvn" ]; then
    echo "Downloading Maven $MAVEN_VERSION..."
    mkdir -p "$MAVEN_DIR"
    curl -sLo "$MAVEN_DIR/$MAVEN_ZIP" "$MAVEN_URL"
    unzip -q "$MAVEN_DIR/$MAVEN_ZIP" -d "$MAVEN_DIR"
    rm "$MAVEN_DIR/$MAVEN_ZIP"
fi

exec "$MAVEN_DIR/apache-maven-$MAVEN_VERSION/bin/mvn" "$@"
