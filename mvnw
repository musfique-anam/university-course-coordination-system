#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ] ; then
  if [ -f /usr/local/etc/mavenrc ] ; then . /usr/local/etc/mavenrc ; fi
  if [ -f /etc/mavenrc ] ; then . /etc/mavenrc ; fi
  if [ -f "$HOME/.mavenrc" ] ; then . "$HOME/.mavenrc" ; fi
fi

cygwin=false
darwin=false
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true ;;
  Darwin*) darwin=true
    if [ -z "$JAVA_HOME" ]; then
      if [ -x "/usr/libexec/java_home" ]; then
        JAVA_HOME="$(/usr/libexec/java_home)"; export JAVA_HOME
      else
        JAVA_HOME="/Library/Java/Home"; export JAVA_HOME
      fi
    fi
    ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=$(java-config --jre-home)
  fi
fi

if $cygwin ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
  [ -n "$CLASSPATH" ] && CLASSPATH=$(cygpath --path --unix "$CLASSPATH")
fi

if $mingw ; then
  [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ] && JAVA_HOME="$(cd "$JAVA_HOME" || exit 1; pwd)"
fi

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="$(which javac 2>/dev/null)"
  if [ -n "$javaExecutable" ] && [ -x "$javaExecutable" ]; then
    readLink=$(which readlink 2>/dev/null)
    if [ -n "$readLink" ] && [ -x "$readLink" ]; then
      if $darwin ; then
        javaHome="$(dirname "$javaExecutable")"
        javaExecutable="$(cd "$javaHome" && pwd -P)/javac"
      else
        javaExecutable="$(readlink -f "$javaExecutable")"
      fi
      javaHome="$(dirname "$javaExecutable")"
      javaHome=$(echo "$javaHome" | sed 's|/bin$||')
      JAVA_HOME="$javaHome"
      export JAVA_HOME
    fi
  fi
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="$(command -v java)"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not set correctly or java not found." >&2
  exit 1
fi

find_maven_basedir() {
  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ] ; do
    if [ -d "$wdir/.mvn" ] ; then
      basedir=$wdir
      break
    fi
    wdir="$(cd "$wdir/.." 2>/dev/null || exit 1; pwd)"
  done
  printf '%s' "$(cd "$basedir" || exit 1; pwd)"
}

BASE_DIR=$(find_maven_basedir "$(dirname "$0")")
[ -z "$BASE_DIR" ] && exit 1
MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$BASE_DIR"}
export MAVEN_PROJECTBASEDIR

wrapperJarPath="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -r "$wrapperJarPath" ]; then
  wrapperUrl=""
  while IFS="=" read -r key value; do
    [ "$key" = "wrapperUrl" ] && wrapperUrl=$(echo "$value" | tr -d '\r')
  done < "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
  [ -z "$wrapperUrl" ] && wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  if command -v wget >/dev/null; then
    wget -q -O "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
  elif command -v curl >/dev/null; then
    curl -sL -o "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
  fi
fi

$cygwin && wrapperJarPath=$(cygpath --path --windows "$wrapperJarPath")

exec "$JAVACMD" \
  $MAVEN_OPTS $MAVEN_DEBUG_OPTS \
  -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
