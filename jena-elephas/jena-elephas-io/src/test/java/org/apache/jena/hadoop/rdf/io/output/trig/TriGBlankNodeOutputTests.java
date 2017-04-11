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

package org.apache.jena.hadoop.rdf.io.output.trig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.hadoop.rdf.io.RdfIOConstants;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ResIterator ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad ;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for TriG output with blank nodes
 * 
 * 
 * 
 */
@RunWith(Parameterized.class)
public class TriGBlankNodeOutputTests extends StreamedTriGOutputTest {

	@SuppressWarnings("hiding")
    static long $bs1 = RdfIOConstants.DEFAULT_OUTPUT_BATCH_SIZE;
	@SuppressWarnings("hiding")
	static long $bs2 = 1000;
	@SuppressWarnings("hiding")
	static long $bs3 = 100;
	@SuppressWarnings("hiding")
	static long $bs4 = 1;

	/**
	 * @return Test parameters
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { $bs1 }, { $bs2 }, { $bs3 },
				{ $bs4 } });
	}

	/**
	 * Creates new tests
	 * 
	 * @param batchSize
	 *            Batch size
	 */
	public TriGBlankNodeOutputTests(long batchSize) {
		super(batchSize);
	}

	@Override
	protected Iterator<QuadWritable> generateTuples(int num) {
		List<QuadWritable> qs = new ArrayList<QuadWritable>();
		Node subject = NodeFactory.createBlankNode();
		for (int i = 0; i < num; i++) {
			Quad t = new Quad(
					NodeFactory.createURI("http://example.org/graphs/" + i),
					subject,
					NodeFactory.createURI("http://example.org/predicate"),
					NodeFactory.createLiteral(Integer.toString(i),
							XSDDatatype.XSDinteger));
			qs.add(new QuadWritable(t));
		}
		return qs.iterator();
	}

	@Override
	protected void checkTuples(File f, long expected) {
		super.checkTuples(f, expected);

		Model m = RDFDataMgr.loadModel("file://" + f.getAbsolutePath(),
				this.getRdfLanguage());
		ResIterator iter = m.listSubjects();
		Set<Node> subjects = new HashSet<Node>();
		while (iter.hasNext()) {
			Resource res = iter.next();
			Assert.assertTrue(res.isAnon());
			subjects.add(res.asNode());
		}
		// Should only be one subject unless the data was empty in which case
		// there will be zero subjects
		Assert.assertEquals(expected == 0 ? 0 : 1, subjects.size());
	}

	@Override
	protected OutputFormat<NullWritable, QuadWritable> getOutputFormat() {
		return new TriGOutputFormat<NullWritable>();
	}

}
