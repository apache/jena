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

package org.apache.jena.sparql.util.iso;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.EqualityTest;
import org.apache.jena.sparql.util.Iso;
import org.apache.jena.sparql.util.NodeIsomorphismMap;
import org.apache.jena.sparql.util.NodeUtils;

public class BNodeIso implements EqualityTest
{
    /** Kind of matching to perform. */
    // BNODES => blank node isomorphism.
    public enum Match { BNODES_TERM, BNODES_VALUE, EXACT_TERM, EXACT_VALUE }

    /**
     * Create a fresh {@link EqualityTest} that provides blank node isomorphism and
     * comparision by term.
     */
    public static EqualityTest bnodeIsoByTerm() {
        return new BNodeIso(NodeUtils.sameRdfTerm);
    }

    /**
     * Create a fresh {@link EqualityTest} that provides blank node isomorphism and
     * comparision by term.
     */
    public static EqualityTest bnodeIsoByValue() {
        return new BNodeIso(NodeUtils.sameValue);
    }

    private NodeIsomorphismMap mapping;
    private EqualityTest literalTest;

    private BNodeIso(EqualityTest literalTest) {
        this.mapping = new NodeIsomorphismMap();
        this.literalTest = literalTest;
    }

    @Override
    public boolean equal(Node n1, Node n2) {
        if ( n1 == null && n2 == null )
            return true;
        if ( n1 == null )
            return false;
        if ( n2 == null )
            return false;

        if ( n1.isURI() && n2.isURI() )
            return n1.equals(n2);

        if ( n1.isLiteral() && n2.isLiteral() )
            return literalTest.equal(n1, n2);

        if ( n1.isBlank() && n2.isBlank() )
            return Iso.nodeIso(n1, n2, mapping);

        if ( n1.isVariable() && n2.isVariable() )
            return mapping.makeIsomorphic(n1, n2);

        if ( n1.isTripleTerm() && n2.isTripleTerm() ) {
            Triple t1 = n1.getTriple();
            Triple t2 = n2.getTriple();
            return  Iso.tripleIso(t1, t2, mapping);
        }

        return false;
    }
}