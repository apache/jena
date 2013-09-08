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

package com.hp.hpl.jena.graph.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.Parameterized.Parameters;

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.impl.FileGraph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.testing_framework.AbstractGraphProducer;
import com.hp.hpl.jena.testing_framework.AbstractGraphProducerUser;
import com.hp.hpl.jena.testing_framework.GraphProducerInterface;
import com.hp.hpl.jena.util.FileUtils;

/**
 * Test FileGraph by seeing if we can make some file graphs and then read them
 * back.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ FileGraphSuite.TestNames.class,
		FileGraphSuite.FileNameTests.class,
		FileGraphSuite.FileNameTransactionTests.class })
public class FileGraphSuite {

	public static class GraphProducer extends AbstractGraphProducer {
		private String suffix = ".nt";
		private String prefix = "tfg";
		private boolean strict = true;

		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public void setStrict(boolean strict) {
			this.strict = strict;
		}

		@Override
		protected void afterClose(Graph g) {
			((FileGraph) g).name.delete();
		}

		@Override
		protected Graph createNewGraph() {
			File foo = FileUtils.tempFileName(prefix, suffix);
			foo.deleteOnExit();
			return new FileGraph(foo, true, strict);
		}

		final public FileGraph newGraph(FileGraph.NotifyOnClose notifyOnClose) {
			File foo = FileUtils.tempFileName(prefix, suffix);
			foo.deleteOnExit();
			FileGraph retval = new FileGraph(notifyOnClose, foo, true, strict);
			graphList.add(retval);
			return retval;
		}

	}

	/**
	 * Test that the graph encoded as the test-string content can be written out
	 * to a temporary file generated from the prefix and suffix, and then read
	 * back correctly. The temporary files are marked as delete-on-exit to try
	 * and avoid cluttering the user's filespace ...
	 */
	@RunWith(Parameterized.class)
	public static class TestNames extends AbstractGraphProducerUser {
		private GraphProducer graphProducer = new GraphProducer();

		@Override
		public GraphProducerInterface getGraphProducer() {
			return graphProducer;
		}

		// TODO want a wider variety of cases, now we've discovered how to
		// abstract.
		@Parameters(name = "TestNames: content '{'{0}'}' prefix {1} suffix {2}")
		public static Iterable<Object[]> data() {
			Object[][] result = { { "x /R y", "xxxA", ".rdf" },
					{ "x /R y", "xxxB", ".n3" }, { "x /R y", "xxxC", ".nt" },
					{ "x /R y; p /R q", "xxxD", ".rdf" },
					{ "x /R y; p /R q", "xxxE", ".n3" },
					{ "x /R y; p /R q", "xxxF", ".nt" },
					{ "http://domain/S ftp:ftp/P O", "xxxG", ".rdf" },
					{ "http://domain/S ftp:ftp/P O", "xxxH", ".nt" },
					{ "http://domain/S ftp:ftp/P O", "xxxI", ".n3" }, };
			return Arrays.asList(result);
		}

		private final String content;

		public TestNames(String content, String prefix, String suffix) {
			this.content = content;
			graphProducer.setPrefix(prefix);
			graphProducer.setSuffix(suffix);
		}

		@Test
		public void test() {
			Graph original = graphWith(graphProducer.newGraph(), content);
			FileGraph g = (FileGraph) graphProducer.newGraph();
			GraphUtil.addInto(g, original);
			g.close();
			Graph g2 = new FileGraph(g.name, false, true);
			assertIsomorphic(original, g2);
			g2.close();
		}

	}

	public static class FileNameTests extends AbstractGraphProducerUser {
		private GraphProducer graphProducer = new GraphProducer();

		@Override
		public GraphProducerInterface getGraphProducer() {
			return graphProducer;
		}

		@Test
		public void testPlausibleGraphname() {
			assertTrue(FileGraph.isPlausibleGraphName("agnessi.rdf"));
			assertTrue(FileGraph.isPlausibleGraphName("parabola.nt"));
			assertTrue(FileGraph.isPlausibleGraphName("hyperbola.n3"));
			assertTrue(FileGraph.isPlausibleGraphName("chris.dollin.n3"));
			assertTrue(FileGraph.isPlausibleGraphName("hedgehog.spine.end.rdf"));
		}

		@Test
		public void testisPlausibleUppercaseGraphname() {
			assertTrue(FileGraph.isPlausibleGraphName("LOUDER.RDF"));
			assertTrue(FileGraph.isPlausibleGraphName("BRIDGE.NT"));
			assertTrue(FileGraph.isPlausibleGraphName("NOTN2.N3"));
			assertTrue(FileGraph.isPlausibleGraphName("chris.dollin.N3"));
			assertTrue(FileGraph.isPlausibleGraphName("hedgehog.spine.end.RDF"));
		}

		@Test
		public void testImPlausibleGraphName() {
			assertFalse(FileGraph.isPlausibleGraphName("undecorated"));
			assertFalse(FileGraph.isPlausibleGraphName("danger.exe"));
			assertFalse(FileGraph.isPlausibleGraphName("pretty.jpg"));
			assertFalse(FileGraph.isPlausibleGraphName("FileGraph.java"));
			assertFalse(FileGraph.isPlausibleGraphName("infix.rdf.c"));
		}
	}

	public static class FileNameTransactionTests extends
			AbstractGraphProducerUser {

		private GraphProducer graphProducer = new GraphProducer();

		@Override
		public GraphProducerInterface getGraphProducer() {
			return graphProducer;
		}

		@Test
		public void testTransactionCommit() {
			Graph initial = graphWith(graphProducer.newGraph(),
					"initial hasValue 42; also hasURI hello");
			Graph extra = graphWith(graphProducer.newGraph(),
					"extra hasValue 17; also hasURI world");
			FileGraph g = (FileGraph) graphProducer.newGraph();

			GraphUtil.addInto(g, initial);
			g.getTransactionHandler().begin();
			GraphUtil.addInto(g, extra);
			g.getTransactionHandler().commit();
			Graph union = graphWith(graphProducer.newGraph(), "");
			GraphUtil.addInto(union, initial);
			GraphUtil.addInto(union, extra);
			assertIsomorphic(union, g);
			Model inFile = ModelFactory.createDefaultModel();
			inFile.read("file:///" + g.name, "N-TRIPLES");
			assertIsomorphic(union, inFile.getGraph());
		}

		@Test
		public void testTransactionCommitThenAbort() {
			Graph initial = graphWith(graphProducer.newGraph(),
					"A pings B; B pings C");
			Graph extra = graphWith(graphProducer.newGraph(),
					"C pingedBy B; fileGraph rdf:type Graph");
			File foo = FileUtils.tempFileName("fileGraph", ".nt");
			FileGraph g = (FileGraph) graphProducer.newGraph();

			g.getTransactionHandler().begin();
			GraphUtil.addInto(g, initial);
			g.getTransactionHandler().commit();
			g.getTransactionHandler().begin();
			GraphUtil.addInto(g, extra);
			g.getTransactionHandler().abort();
			assertIsomorphic(initial, g);
			Model inFile = ModelFactory.createDefaultModel();
			inFile.read("file:///" + g.name, "N-TRIPLES");
			assertIsomorphic(initial, inFile.getGraph());
		}

		@Test
		public void testClosingNotifys() {
			final List<File> history = new ArrayList<File>();
			FileGraph.NotifyOnClose n = new FileGraph.NotifyOnClose() {
				@Override
				public void notifyClosed(File f) {
					history.add(f);
				}
			};
			graphProducer.setSuffix(".nt");
			FileGraph g = graphProducer.newGraph(n);
			assertEquals(new ArrayList<File>(), history);
			g.close();
			assertEquals(oneElementList(g.name), history);
		}

		protected List<Object> oneElementList(Object x) {
			List<Object> result = new ArrayList<Object>();
			result.add(x);
			return result;
		}
	}

}
