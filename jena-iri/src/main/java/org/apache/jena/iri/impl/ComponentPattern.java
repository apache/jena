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

package org.apache.jena.iri.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.iri.ViolationCodes ;


public class ComponentPattern implements ViolationCodes {
    final Pattern pattern;

    final GroupAction actions[];

    static final List<Pattern> allPatterns = new ArrayList<>();

    ComponentPattern(String p) {
        ComponentPatternParser parser = new ComponentPatternParser(p);
        pattern = parser.get();
        actions = parser.actions();
//        System.err.println(allPatterns.size() + ": " + p + " ==> "
//                + pattern.toString());
        allPatterns.add(pattern);
    }

    

    public void analyse(Parser parser, int range) {
        Matcher m = pattern.matcher(parser.get(range));
        if (!m.matches()) {
            parser.recordError(range, SCHEME_PATTERN_MATCH_FAILED);
            return;
        }
        for (int g = 1; g <= m.groupCount(); g++)
            if (m.start(g) != -1)
                actions[g].check(m, parser, range);

    }
}
