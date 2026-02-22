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

package org.apache.jena.sparql.adapter;

import java.util.Optional;

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;

public class ParseCheckUtils
{
    // ----- Parse Check -----

    public static void setParseCheck(Context cxt, Boolean value) {
        cxt.set(ARQConstants.parseCheck, value);
    }

    public static Optional<Boolean> getParseCheck(DatasetGraph dsg) {
        return Optional.ofNullable(dsg).map(DatasetGraph::getContext).flatMap(ParseCheckUtils::getParseCheck);
    }

    public static Optional<Boolean> getParseCheck(Context cxt) {
        return Optional.ofNullable(cxt).map(c -> c.get(ARQConstants.parseCheck));
    }

    public static Optional<Boolean> getParseCheck(ContextAccumulator cxtAcc) {
        return Optional.ofNullable(cxtAcc).map(ca -> ca.get(ARQConstants.parseCheck));
    }

    public static boolean effectiveParseCheck(Boolean parseCheck, Context cxt) {
        return Optional.ofNullable(parseCheck).orElseGet(() -> getParseCheck(cxt).orElse(true));
    }

    public static boolean effectiveParseCheck(Boolean parseCheck, ContextAccumulator cxtAcc) {
        return Optional.ofNullable(parseCheck).orElseGet(() -> getParseCheck(cxtAcc).orElse(true));
    }
}
