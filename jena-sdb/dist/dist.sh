#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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
