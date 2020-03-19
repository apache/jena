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

import java.util.concurrent.TimeUnit;

import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

public class TestQueryCloningCornerCases {

	// @Test
	public void benchmarkQueryClone() {
		String str = "SELECT * { ?s ?p ?o }";
		int n = 1000000;

		// Warmup runs
		Query q = QueryFactory.create(str);
		for(int i = 0; i < n; ++i) {
			TestQueryCloningEssentials.slowClone(q);
			q.cloneQuery();
		}

		Stopwatch printParseSw = Stopwatch.createStarted();
		for(int i = 0; i < n; ++i) {
			TestQueryCloningEssentials.slowClone(q);
		}
		printParseSw.stop();

		Stopwatch transformSw = Stopwatch.createStarted();
		for(int i = 0; i < n; ++i) {
			q.cloneQuery();
		}
		transformSw.stop();

		double qpsPrintParse = n / (double)(printParseSw.elapsed(TimeUnit.MILLISECONDS) * 0.001);
		double qpsTransform = n / (double)(transformSw.elapsed(TimeUnit.MILLISECONDS) * 0.001);
		System.out.println("Queries/Second [Print-Parse: " + qpsPrintParse + "], [Transform: " + qpsTransform + "]");
	}

    /**
     * Tests for the {@link Query} clone method.
     * Data and path blocks are mutable elements and modifications after a clone
     * should be possible independently.
     *
     */
    @Test
    public void testCloneOfDataAndPathBlocks()
    {


        // FIXME Query.valuesDataBlock is not passed through the copy machinery and is thus not copied
        // Who is responsible for the copy? Wrapping as an ElementData and passing it through the ElementTransform
        // is not ideal, as the transform is free to yield an element of another - incompatible - type
        // String str = "PREFIX eg: <http://www.example.org/> "
        //        + "SELECT * { ?s eg:foo/eg:bar ?o } VALUES (?s ?o) { (eg:baz 1) }";

        String str = "PREFIX eg: <http://www.example.org/> "
          + "SELECT * { ?s eg:foo/eg:bar ?o VALUES (?s ?o) { (eg:baz 1) } }";

        Query query = QueryFactory.create(str);

        Query clone = TestQueryCloningEssentials.checkedClone(query);

        // Modification of the query pattern must not change the original query
        {
            Query cloneOfClone = clone.cloneQuery();
	        ElementPathBlock elt = (ElementPathBlock)((ElementGroup)cloneOfClone.getQueryPattern()).get(0);
	        elt.addTriple(new Triple(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property));

	        Assert.assertNotEquals(elt, query);
        }

        // After modifying the clone of a clone the initial clone must match the original query
        Assert.assertEquals(query, clone);

        // Modification of the value block must not change the original query
        {
            Query cloneOfClone = clone.cloneQuery();
            ElementData elt = (ElementData)((ElementGroup)cloneOfClone.getQueryPattern()).get(1);
            elt.getRows().add(BindingFactory.create());
            Assert.assertNotEquals(query, cloneOfClone);
        }

        Assert.assertEquals(query, clone);
    }
}
