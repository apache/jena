/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.test.jena4;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.test.X_RDFReaderF;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class TestRemoveBug
{
    // Make test suite runnable on it's own.
    static { RDFReaderFImpl.alternative(new X_RDFReaderF()); }

    public static TestSuite suite() {
        TestSuite ts = new TestSuite();
        ts.setName("TestRemoveBug");
        ts.addTest(new JUnit4TestAdapter(TestRemoveBug.class));
        return ts;
    }

	public TestRemoveBug(){ }

    @Test
    public void testRemoveBug_GH2076() {
        Model model = createModel();
        model.read("file:testing/reports/graph-mem-broken-iterator-delete-01.ttl", "TURTLE");
        Graph graph = model.getGraph();

        long x1 = graph.size();
        ExtendedIterator<Triple> it = graph.find();
        try {
            while (it.hasNext()) {
                Triple t = it.next();
                it.remove();
            }
        } finally {
            it.close();
        }
        long x2 = graph.size();
        assertEquals(0L, x2);
    }

	/*
	 * This seems to be the same bug as testBug2.
	 * It is intermittent - it fails much less than 1 in 50.
	 * The test above is a deterministic failure.
	 */
	//@Test
	public void testBug1()
	{
		final String src = "@prefix foaf:    <http://xmlns.com/foaf/0.1/> .\n"
				+ "<http://www.hp.com/people/Ian_Dickinson> foaf:mbox_sha1sum '896dfb5980f37c47ada8c2a2538888d0c39e582d' .\n"
				+
				// "[a foaf:Person ; foaf:name 'Ian Dickinson'  ; foaf:title 'Mr'  ;"
				// +
				// " foaf:givenname 'Ian'  ; foaf:family_name 'Dickinson' ;\n" +
				// " foaf:mbox_sha1sum '896dfb5980f37c47ada8c2a2538888d0c39e582d'  ;"
				// +
				// " foaf:homepage <http://www.iandickinson.me.uk>;\n" +
				// " foaf:phone <tel:+44-(117)-312-8796> ; " +
				// " foaf:depiction <http://www.iandickinson.me.uk/images/me2005.jpg>;"
				// +
				// " foaf:workInfoHomepage <http://www.hpl.hp.com/semweb>" +
				"[] foaf:name 'Ian Dickinson'  ;\n" + " foaf:p1 'v1'; \n"
				+ " foaf:p1 'v2'; \n" + " foaf:p1 'v3'; \n"
				+ " foaf:p1 'v4'; \n" + " foaf:p1 'v5'; \n"
				+ " foaf:p1 'v6'; \n" + " foaf:p1 'v7'; \n"
				+ " foaf:p1 'v8'; \n" + " foaf:p1 'v9'; \n" + ".";

		for (int count = 0; count < 1000; count++)
		{
			// System.out.println("Test " + count);
			final Model incoming = createModel();
			incoming.read(new StringReader(src), null, "N3");

			// Find the bNode that will be rewritten
			final Property name = incoming.createProperty(
					"http://xmlns.com/foaf/0.1/", "name");
			final ResIterator ri = incoming.listSubjectsWithProperty(name,
					"Ian Dickinson");
			final Resource bNode = ri.nextResource();
			ri.close();

			// Rewrite it to ground form
			final int originalCount = bNode.listProperties().toList().size();
			final Resource newR = incoming
					.createResource("http://www.hp.com/people/Ian_Dickinson");
			int runningCount = 0;
			final StmtIterator si = incoming.listStatements(bNode, null,
					(RDFNode) null);
			final Model additions = createModel();
			while (si.hasNext())
			{
				final Statement s = si.nextStatement();
				runningCount += 1;
				si.remove();
				// System.out.println("Rewrite " + s + " base on " + newR);
				additions.add(additions.createStatement(newR, s.getPredicate(),
						s.getObject()));
			}
			Assert.assertEquals("on iteration " + count + " with "
					+ bNode.asNode().getBlankNodeLabel(), originalCount,
					runningCount);
			incoming.add(additions);
			final Resource ian = incoming
					.getResource("http://www.hp.com/people/Ian_Dickinson");
			Assert.assertTrue("Smush failed on iteration " + count,
					ian.hasProperty(name));
		}
	}

	private Model createModel() {
	    // Must be GraphMem
        Graph graph = new GraphMem();
        Model model = ModelFactory.createModelForGraph(graph);
        return model;
    }
}
