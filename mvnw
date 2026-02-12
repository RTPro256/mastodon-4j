#!/bin/sh
# Maven Wrapper script - https://maven.apache.org/wrapper/

MAVEN_PROJECTBASEDIR="${MAVEN_PROJECTBASEDIR:-$(pwd)}"

if [ -z "$MAVEN_WRAPPER_DISTRIBUTION" ]; then
  MAVEN_WRAPPER_DISTRIBUTION="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
fi

if [ -f "$MAVEN_WRAPPER_DISTRIBUTION" ]; then
  MAVEN_WRAPPER_DISTRIBUTION_URL=$(grep -E '^distributionUrl=' "$MAVEN_WRAPPER_DISTRIBUTION" | sed 's/distributionUrl=//')
  MAVEN_WRAPPER_JAR_URL=$(grep -E '^wrapperUrl=' "$MAVEN_WRAPPER_DISTRIBUTION" | sed 's/wrapperUrl=//')
fi

MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_LAUNCHER="org.apache.maven.wrapper.MavenWrapperMain"

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  if [ -z "$MAVEN_WRAPPER_JAR_URL" ]; then
    MAVEN_WRAPPER_JAR_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  fi
  mkdir -p "$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$MAVEN_WRAPPER_JAR_URL" -o "$MAVEN_WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$MAVEN_WRAPPER_JAR_URL" -O "$MAVEN_WRAPPER_JAR"
  else
    echo "Error: curl or wget is required to download the Maven wrapper jar." >&2
    exit 1
  fi
fi

JAVA_CMD=${JAVA_HOME:+"$JAVA_HOME/bin/java"}
JAVA_CMD=${JAVA_CMD:-java}

exec "$JAVA_CMD" -classpath "$MAVEN_WRAPPER_JAR" \
  -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" \
  $MAVEN_WRAPPER_LAUNCHER "$@"
