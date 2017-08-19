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

package org.apache.jena.sparql.function.library;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Locale;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.expr.nodevalue.NodeValueLang;
import org.apache.jena.sparql.expr.nodevalue.NodeValueSortKey;
import org.apache.jena.sparql.function.FunctionBase2;

/**
 * Access to a {@link NodeValue} which is a collection ({@link NodeValueSortKey}).
 * 
 * See {@link FN_CollationKey} for the Functions and Operators function.
 * 
 * This function takes two parameters. First is the collation (a string), second the
 * {@link NodeValue} result of an {@link Expr} (ExprVar, ExprFunctionN, NodeValue, etc).
 *
 * <p>If called with a prefix {@code afn}, e.g. {@code ORDER BY afn:collation("fi", ?label);}.
 * The first argument (in this case, "fi") is then resolved to a {@link Locale}, that is
 * used to build a {@link Collator}. If a locale does not match any known collator, then
 * a rule based collator ({@link RuleBasedCollator}) is returned, but with no rules,
 * returning values in natural order, not applying any specific collation order.</p>
 *
 * <p>The second argument, which is an {@link Expr}, will have its literal string value
 * extracted (or will raise an error if it is not possible). This means that if the
 * expr is a {@link NodeValueLang} (e.g. rendered from "Casa"@pt), the language tag will
 * be discarded, and only the literal string value (i.e. Casa) will be taken into account
 * for this function.</p>
 *
 * @see NodeValueSortKey
 * @see FN_CollationKey
 */
public class collation extends FunctionBase2 {

    public collation() {
        super();
    }

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {
        // retrieve collation value
        String collation = NodeFunctions.str(v1.asNode());
        // return a NodeValue that contains the v2 literal string, plus the given collation
        return NodeFunctions.sortKey(v2, collation);
    }

}
