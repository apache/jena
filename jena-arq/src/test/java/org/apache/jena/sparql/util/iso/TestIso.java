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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Iso;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/** {@link Iso} contains atomic and order list matching. */
public class TestIso {

    @Test public void iso_terms_iri_01() { testTerms(":s", ":s", true); }
    @Test public void iso_terms_iri_02() { testTerms(":s", ":x", false); }

    @Test public void iso_terms_bnode_01() { testTerms("_:s", "_:x", true); }
    @Test public void iso_terms_bnode_02() { testTerms("_:s", "_:s", true); }

    @Test public void iso_terms_literal_01() { testTerms("'abc'", "'abc'", true); }
    @Test public void iso_terms_literal_02() { testTerms("'abc'", "'xyz'", false); }

    @Test public void iso_terms_tripleTerms_01() { testTerms("<<( :s :p :o )>>", "<<( :s :p :o )>>", true); }
    @Test public void iso_terms_tripleTerms_02() { testTerms("<<(:s :p :z )>>", "<<( :s :p :o )>>", false); }

    @Test public void iso_terms_tripleTerms_03() { testTerms("<<( _:b :p :o )>>", "<<( _:x :p :o )>>", true); }
    @Test public void iso_terms_tripleTerms_04() { testTerms("<<( _:b :p _:b )>>", "<<( _:x :p _:z )>>", false); }

    @Test public void iso_terms_50() { testTerms(":s", "_:b", false); }

    @Test public void iso_triples_01() { testTriples("(:s :p :o)", "(:s :p :o)", true); }
    @Test public void iso_triples_02() { testTriples("(_:b :p :o)", "(_:x :p :o)", true); }
    @Test public void iso_triples_03() { testTriples("(_:b :p :o)", "(:s :p _:b)", false); }
    @Test public void iso_triples_04() { testTriples("(_:b :p _:b)", "(_:x1 :p _:x2)", false); }

    private void testTerms(String str1, String str2, boolean expected) {
        Node n1 = SSE.parseNode(str1);
        Node n2 = SSE.parseNode(str2);
        NodeIsomorphismMap isoMap = new NodeIsomorphismMap();
        boolean result = Iso.nodeIso(n1, n2, isoMap);
        assertEquals(expected, result);
    }

    private void testTriples(String str1, String str2, boolean expected) {
        Triple t1 = SSE.parseTriple(str1);
        Triple t2 = SSE.parseTriple(str2);
        NodeIsomorphismMap isoMap = new NodeIsomorphismMap();
        boolean result = Iso.tripleIso(t1, t2, isoMap);
        assertEquals(expected, result);
    }
}
