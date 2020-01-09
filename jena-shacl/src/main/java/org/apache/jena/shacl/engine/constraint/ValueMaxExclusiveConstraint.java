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

package org.apache.jena.shacl.engine.constraint;

import org.apache.jena.graph.Node;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.expr.Expr;

/** sh:maxExclusive */
public class ValueMaxExclusiveConstraint extends ValueRangeConstraint {

    public ValueMaxExclusiveConstraint(Node value) {
        super(value, SHACL.MaxExclusiveConstraintComponent);
    }

    @Override
    protected String getErrorMessage(Node n) {
        return String.format("Data value %s is not less than %s", ShLib.displayStr(n), nodeValue);
    }

    @Override
    protected boolean test(int r) {
        return Expr.CMP_GREATER == r ;
    }

    @Override
    protected String getName() {
        return "maxExclusive";
    }
}
