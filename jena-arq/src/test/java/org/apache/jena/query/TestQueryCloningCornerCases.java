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
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.syntax.ElementData;
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
          + "SELECT * { ?s eg:foo/eg:bar ?o VALUES (?s ?o) { (eg:baz 1) } }";

        Query query = QueryFactory.create(str);
        Query clone = TestQueryCloningEssentials.checkedClone(query);

        // Modification of the query pattern must not change the original query
        {
            Query cloneOfClone = clone.cloneQuery();
            ElementPathBlock elt = (ElementPathBlock)((ElementGroup)cloneOfClone.getQueryPattern()).get(0);
            elt.addTriple(Triple.create(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property));

            Assert.assertNotEquals(elt, query);
        }

        // After modifying the clone of a clone the initial clone must match the original query
        Assert.assertEquals(query, clone);

        // Modification of the value block must not change the original query
        {
            Query cloneOfClone = clone.cloneQuery();
            ElementData elt = (ElementData)((ElementGroup)cloneOfClone.getQueryPattern()).get(1);
            elt.getRows().add(BindingFactory.empty());
            Assert.assertNotEquals(query, cloneOfClone);
        }

        Assert.assertEquals(query, clone);
    }

    @Test
    public void testCloneOfValuesDataBlock() {
        String str = "PREFIX eg: <http://www.example.org/> "
                + "SELECT * { ?s eg:foo/eg:bar ?o } VALUES (?s ?o) { (eg:baz 1) }";

        Query query = QueryFactory.create(str);

        // Modifications of a query's values data block
        // The cloned query's lists of variables and bindings are independent
        // from those from the original query
        {
            Query clone = TestQueryCloningEssentials.checkedClone(query);

            clone.getValuesData().clear();
            Assert.assertEquals(0, clone.getValuesData().size());
            Assert.assertNotEquals(0, query.getValuesData().size());

            clone.getValuesVariables().clear();
            Assert.assertEquals(0, clone.getValuesVariables().size());
            Assert.assertNotEquals(0, query.getValuesVariables().size());
        }
    }
}
