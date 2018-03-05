#!/bin/bash
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

# Simple backup script for Fuseki - adapt as needed.
# You may need to set the URL for example.
# This script trigger the backup. It does not wait for it to complete.
# Backups are placed in the /backups/ directory.

if [[ $# != 1 ]]
then
    echo "Usage : $(basename $0) NAME" 1>&2
    exit 1
fi

NAME="$1"

curl -XPOST "http://localhost:3030/\$/backup/${NAME}"

# You can track the background task with:
#curl 'http://localhost:3030/$/tasks'
