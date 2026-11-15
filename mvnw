#!/usr/bin/env sh
set -eu

VERSION='3.10.0'
DIST="apache-maven-$VERSION"
INSTALL_ROOT="${HOME}/.m2/wrapper/dists/meeplehub-$DIST"
MAVEN_HOME="$INSTALL_ROOT/$DIST"
MAVEN_BIN="$MAVEN_HOME/bin/mvn"
ARCHIVE="${TMPDIR:-/tmp}/$DIST-bin.zip"
URL="https://dlcdn.apache.org/maven/maven-3/$VERSION/binaries/$DIST-bin.zip"
EXPECTED_SHA256='1f6d9909266510f039f59aa0e13dcd2c66da85f043e56276e41f2918f8bddaff'

if [ ! -x "$MAVEN_BIN" ]; then
    mkdir -p "$INSTALL_ROOT"
    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "$URL" -o "$ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$URL" -O "$ARCHIVE"
    else
        echo 'Either curl or wget is required to download Maven.' >&2
        exit 1
    fi

    if command -v sha256sum >/dev/null 2>&1; then
        ACTUAL_SHA256=$(sha256sum "$ARCHIVE" | cut -d ' ' -f 1)
    elif command -v shasum >/dev/null 2>&1; then
        ACTUAL_SHA256=$(shasum -a 256 "$ARCHIVE" | cut -d ' ' -f 1)
    else
        echo 'sha256sum or shasum is required to verify Maven.' >&2
        rm -f "$ARCHIVE"
        exit 1
    fi

    if [ "$ACTUAL_SHA256" != "$EXPECTED_SHA256" ]; then
        echo 'Maven distribution checksum mismatch.' >&2
        rm -f "$ARCHIVE"
        exit 1
    fi

    command -v unzip >/dev/null 2>&1 || { echo 'unzip is required.' >&2; exit 1; }
    unzip -q "$ARCHIVE" -d "$INSTALL_ROOT"
    rm -f "$ARCHIVE"
fi

exec "$MAVEN_BIN" "$@"
