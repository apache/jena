/**
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
package org.apache.jena.query;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

public class TestQueryCloningCornerCases {

    /**
     * Tests for the {@link Query} clone method.
     * Data and path blocks are mutable elements and modifications after a clone
     * should be possible independently.
     *
     */
    @Test
    public void testCloneOfDataAndPathBlocks()
    {
        String str = "PREFIX eg: <http://www.example.org/> "
                + "SELECT * { ?s eg:foo/eg:bar ?o } VALUES (?s ?o) { (eg:baz 1) }";
        Query query = QueryFactory.create(str);

        Query clone = TestQueryCloningEssentials.checkedClone(query);

        ElementPathBlock eltClone = (ElementPathBlock)((ElementGroup)clone.getQueryPattern()).get(0);
        eltClone.addTriple(new Triple(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property));

        Assert.assertNotEquals(eltClone, query);
    }
}
