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

package org.apache.jena.riot.process.normalize;

import java.util.function.Function;

import org.apache.jena.graph.Node ;

/**
 * Convert literals to canonical form.
 * <p>
 * Strictly, this is "normalization" - XSD Schema 1.1 does not define a canonical form for all cases.
 * <p>
 * N.B. The normalization does produce forms for decimals and doubles that are correct as Turtle syntactic forms.
 * For doubles, but not floats, zero is "0.0e0", whereas Java produces "0.0".
 * For floats, the Java is returned for values with low precision.
 *
 * @deprecated Use {@link NormalizeRDFTerms}.
 */
@Deprecated
public class CanonicalizeLiteral implements Function<Node, Node>
{
    private static CanonicalizeLiteral singleton = new CanonicalizeLiteral();

    /**
     * Suitable for use in Turtle output syntax.
     * @deprecated Use {@code NormalizeRDFTerms.get().normalize}.
     */
    @Deprecated
    public static CanonicalizeLiteral get() { return singleton ; }

    @Override
    public Node apply(Node node) {
        return NormalizeRDFTerms.getTTL().normalize(node);
    }

    /** Convert the lexical form to a canonical form if one of the known datatypes,
     * otherwise return the node argument. (same object :: {@code ==})
     * @deprecated Use {@link NormalizeRDFTerms#normalizeValue}.
     */
    @Deprecated
    public static Node canonicalValue(Node node) {
        return NormalizeRDFTerms.getTTL().normalize(node);
    }
}
