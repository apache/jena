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

package org.apache.jena.testing_framework;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.CollectionFactory;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.junit.Assert;
import org.xenei.junit.contract.IProducer;

/**
 * provides useful functionality for testing models, eg building small models
 * from strings, testing equality, etc.
 * 
 * Currently this class extends GraphHelper and thus TestCase.
 */
public class ModelHelper extends GraphHelper {

	private static Model builderModel;

	static {
		builderModel = ModelFactory.createDefaultModel();
		builderModel.setNsPrefixes(PrefixMapping.Extended);
	}

	protected static final Model empty = ModelFactory.createDefaultModel();

	protected static Model extendedModel(IProducer<Model> producer) {
		Model result = producer.newInstance();
		result.setNsPrefixes(PrefixMapping.Extended);
		return result;
	}

	protected static String nice(RDFNode n) {
		return nice(n.asNode());
	}

	public static Statement statement(String fact) {
		StringTokenizer st = new StringTokenizer(fact);
		Resource sub = resource(st.nextToken());
		Property pred = property(st.nextToken());
		RDFNode obj = rdfNode(st.nextToken());
		return builderModel.createStatement(sub, pred, obj);
	}

	public static Statement statement(Resource s, Property p, RDFNode o) {
		return builderModel.createStatement(s, p, o);
	}

	public static RDFNode rdfNode(Model m, String s) {
		return m.asRDFNode(NodeCreateUtils.create(s));
	}

	public static RDFNode rdfNode(String s) {
		return rdfNode(builderModel, s);
	}

	public static <T extends RDFNode> T rdfNode(String s, Class<T> c) {
		return rdfNode(s).as(c);
	}

	public static Resource resource() {
		return ResourceFactory.createResource();
	}

	public static Resource resource(String s) {
		return (Resource) rdfNode(s);
	}

	// public static Resource resource(Model m, String s) {
	// return (Resource) rdfNode(m, s);
	// }

	public static Property property(String s) {
		return rdfNode(s).as(Property.class);
	}

	public static Property property(Model m, String s) {
		return rdfNode(m, s).as(Property.class);
	}

	public static Literal literal(String s, String lang) {
		return builderModel.createLiteral(s, lang);
	}

	public static Literal literal(String s) {
		return rdfNode(s).as(Literal.class);
	}

	// /**
	// * Create an array of Statements parsed from a semi-separated string.
	// *
	// * @param lockModel
	// * a model to serve as a statement factory
	// * @param facts
	// * a sequence of semicolon-separated "S P O" facts
	// * @return a Statement[] of the (S P O) statements from the string
	// */
	// public static Statement[] statements(Model m, String facts) {
	// ArrayList<Statement> sl = new ArrayList<Statement>();
	// StringTokenizer st = new StringTokenizer(facts, ";");
	// while (st.hasMoreTokens())
	// sl.add(statement(m, st.nextToken()));
	// return sl.toArray(new Statement[sl.size()]);
	// }

	/**
	 * Create an array of Statements parsed from a semi-separated string.
	 * 
	 * @param facts
	 *            a sequence of semicolon-separated "S P O" facts
	 * @return a Statement[] of the (S P O) statements from the string
	 */
	public static Statement[] statements(String facts) {
		ArrayList<Statement> sl = new ArrayList<Statement>();
		StringTokenizer st = new StringTokenizer(facts, ";");
		while (st.hasMoreTokens())
			sl.add(statement(st.nextToken()));
		return sl.toArray(new Statement[sl.size()]);
	}

	/**
	 * Create an array of Resources from a whitespace-separated string
	 * 
	 * @param items
	 *            a whitespace-separated sequence to feed to resource
	 * @return a Resource[] of the parsed resources
	 */
	public static Resource[] resources(String items) {
		ArrayList<Resource> rl = new ArrayList<Resource>();
		StringTokenizer st = new StringTokenizer(items);
		while (st.hasMoreTokens())
			rl.add(resource(st.nextToken()));
		return rl.toArray(new Resource[rl.size()]);
	}

	/**
	 * Answer the set of resources given by the space-separated
	 * <code>items</code> string. Each resource specification is interpreted as
	 * per <code>resource</code>.
	 */
	public static Set<Resource> resourceSet(String items) {
		Set<Resource> result = new HashSet<Resource>();
		StringTokenizer st = new StringTokenizer(items);
		while (st.hasMoreTokens())
			result.add(resource(st.nextToken()));
		return result;
	}

	/**
	 * add to a model all the statements expressed by a string.
	 * 
	 * Does not do any transaction manipulation.
	 * 
	 * @param m
	 *            the model to be updated
	 * @param facts
	 *            a sequence of semicolon-separated "S P O" facts
	 * @return the updated model
	 */
	public static Model modelAdd(Model m, String facts) {
		StringTokenizer semis = new StringTokenizer(facts, ";");

		while (semis.hasMoreTokens()) {
			StringTokenizer st = new StringTokenizer(semis.nextToken());
			Resource sub = resource(st.nextToken());
			Property pred = property(st.nextToken());
			RDFNode obj = rdfNode(st.nextToken());
			m.add(sub, pred, obj);
		}

		return m;
	}

	/**
	 * create a memory based model with extended prefixes and initialises it
	 * with statements parsed from a string.
	 * 
	 * does all insertions in a transaction.
	 * 
	 * @param facts
	 * @return Model
	 */
	public static Model memModel(String facts) {
		Model model = ModelFactory.createMemModelMaker().createFreshModel();
		model.setNsPrefixes(PrefixMapping.Extended);
		txnBegin(model);
		modelAdd(model, facts);
		txnCommit(model);
		return model;
	}

	/**
	 * Creates a model with extended prefixes and initialises it with statements
	 * parsed from a string.
	 * 
	 * does all insertions in a transaction.
	 * 
	 * @param facts
	 *            a string in semicolon-separated "S P O" format
	 * @return a model containing those facts
	 */
	public static Model modelWithStatements(
			IProducer<? extends Model> producer, String facts) {
		Model m = createModel(producer);
		txnBegin(m);
		modelAdd(m, facts);
		txnCommit(m);
		return m;
	}

	/**
	 * Creates a model with extended prefixes and initialises it with statements
	 * parsed from the statement iterator.
	 * 
	 * Does all insertions in a transaction.
	 */
	public static Model modelWithStatements(
			IProducer<? extends Model> producer, final StmtIterator it) {
		Model m = createModel(producer);
		txnBegin(m);
		while (it.hasNext()) {
			m.add(it.nextStatement());
		}
		txnCommit(m);
		return m;
	}

	/**
	 * make a model and give it Extended prefixes
	 */
	public static Model createModel(IProducer<? extends Model> producer) {
		Model result = producer.newInstance();
		result.setNsPrefixes(PrefixMapping.Extended);
		return result;
	}

	/**
	 * test that two models are isomorphic and fail if they are not.
	 * 
	 * @param title
	 *            a String appearing at the beginning of the failure message
	 * @param wanted
	 *            the model value that is expected
	 * @param got
	 *            the model value to check
	 */
	public static void assertIsoModels(String title, Model wanted, Model got) {
		if (wanted.isIsomorphicWith(got) == false) {
			Map<Node, Object> map = CollectionFactory.createHashedMap();
			fail(title + ": expected " + nice(wanted.getGraph(), map)
					+ "\n but had " + nice(got.getGraph(), map));
		}
	}

	public static void assertContainsAll(final Model model, final Model model2) {
		for (final StmtIterator s = model2.listStatements(); s.hasNext();) {
			Assert.assertTrue(model.contains(s.nextStatement()));
		}
	}

	public static void assertSameStatements(final Model model,
			final Model model2) {
		assertContainsAll(model, model2);
		assertContainsAll(model2, model);
	}

	public static Property prop(final String uri) {
		return ResourceFactory.createProperty("eh:/" + uri);
	}

	public static Resource res(final String uri) {
		return ResourceFactory.createResource("eh:/" + uri);
	}

	/**
	 * Fail if the two models are not isomorphic. See
	 * assertIsoModels(String,Model,Model).
	 */
	public static void assertIsoModels(Model wanted, Model got) {
		assertIsoModels("models must be isomorphic", wanted, got);
	}

	public static final boolean tvBoolean = true;
	public static final byte tvByte = 1;
	public static final short tvShort = 2;
	public static final int tvInt = -1;
	public static final long tvLong = -2;
	public static final char tvChar = '!';
	public static final float tvFloat = (float) 123.456;
	public static final double tvDouble = -123.456;
	public static final String tvString = "test 12 string";
	public static final double dDelta = 0.000000005;

	public static final float fDelta = 0.000005f;

	public static final Object tvLitObj = new LitTestObj(1234);
	public static final LitTestObj tvObject = new LitTestObj(12345);

	public static class LitTestObj {
		protected long content;

		public LitTestObj(final long l) {
			content = l;
		}

		public LitTestObj(final String s) {
			content = Long.parseLong(s.substring(1, s.length() - 1));
		}

		@Override
		public boolean equals(final Object o) {
			return (o instanceof LitTestObj)
					&& (content == ((LitTestObj) o).content);
		}

		@Override
		public int hashCode() {
			return (int) (content ^ (content >> 32));
		}

		@Override
		public String toString() {
			return "[" + Long.toString(content) + "]";
		}

		public long getContent() {
			return content;
		}
	}

	/**
	 * Begin a transaction on the model if transactions are supported.
	 * 
	 * @param m
	 */
	public static Model txnBegin(Model m) {
		if (m.supportsTransactions()) {
			return m.begin();
		}
		return m;
	}

	/**
	 * Commit the transaction on the model if transactions are supported.
	 * 
	 * @param m
	 */
	public static Model txnCommit(Model m) {
		if (m.supportsTransactions()) {
			return m.commit();
		}
		return m;
	}

	/**
	 * Rollback (abort) the transaction on the model if transactions are
	 * supported.
	 * 
	 * @param m
	 */
	public static Model txnRollback(Model m) {
		if (m.supportsTransactions()) {
			return m.abort();
		}
		return m;
	}
}
