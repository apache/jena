#!/bin/sh

# Get all files, inc .md1, .sha1
# Run above the module.

# mkdir -p repository.apache.org/content/repositories/orgapachejena-NNN/org/apache/jena
# wget -e robots=off --wait 1 --mirror -np https://repository.apache.org/content/repositories/orgapachejena-NNN/org/apache/jena
# mv repository.apache.org/content/repositories/orgapachejena-NNN/ REPO

rm -rf dist-sdb
mkdir dist-sdb
mkdir dist-sdb/binaries
mkdir dist-sdb/sources

find REPO -name \*.asc.md5 | xargs rm
find REPO -name \*.asc.sha1 | xargs rm

cp REPO/org/apache/jena/jena-sdb/*/jena-sdb-*-distribution.* dist-sdb/binaries
cp REPO/org/apache/jena/jena-sdb/*/jena-sdb-*-source-release.zip*  dist-sdb/sources
