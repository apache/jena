/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.rfc3986;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Collect validation issues
 */
class IssueCollector implements BiConsumer<Issue, String> {
    List<Issue> issues = new ArrayList<>();
    List<String> msgs = new ArrayList<>();
    @Override
    public void accept(Issue issue, String msg) {
        issues.add(issue);
        msgs.add(msg);
    }

    public boolean isEmpty() {
        return issues.isEmpty();
    }

    public void apply(BiConsumer<Issue, String> action) {
        for ( int i = 0 ; i < issues.size() ; i++ ) {
            action.accept(issues.get(i), msgs.get(i));
        }
    }
}