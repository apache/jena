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
package org.apache.jena.permissions.model.impl;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.graph.SecuredGraph;
import org.apache.jena.permissions.graph.SecuredPrefixMapping;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemImpl;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.permissions.model.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.shared.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Implementation of SecuredModel to be used by a SecuredItemInvoker proxy.
 */
public class SecuredModelImpl extends SecuredItemImpl implements SecuredModel {

	// a class that implements ModelChangedListener
	private class SecuredModelChangedListener implements ModelChangedListener {
		private final ModelChangedListener wrapped;

		private SecuredModelChangedListener(final ModelChangedListener wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public void addedStatement(final Statement s) throws AuthenticationRequiredException {
			if (canRead(s)) {
				wrapped.addedStatement(s);
			}
		}

		@Override
		public void addedStatements(final List<Statement> statements) throws AuthenticationRequiredException {
			if (canRead(Triple.ANY)) {
				wrapped.addedStatements(statements);
			} else {
				final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
						WrappedIterator.create(statements.iterator()));
				try {
					wrapped.addedStatements(iter.toList());
				} finally {
					iter.close();
				}
			}
		}

		@Override
		public void addedStatements(final Model m) throws AuthenticationRequiredException {
			if (canRead(Triple.ANY)) {
				wrapped.addedStatements(m);
			} else {
				wrapped.addedStatements(SecuredModelImpl.getInstance(holder.getSecuredItem(), m));
			}
		}

		@Override
		public void addedStatements(final Statement[] statements) throws AuthenticationRequiredException {
			if (canRead(Triple.ANY)) {
				wrapped.addedStatements(statements);
			} else {
				final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
						WrappedIterator.create(Arrays.asList(statements).iterator()));
				try {
					final List<Statement> stmts = iter.toList();
					wrapped.addedStatements(stmts.toArray(new Statement[stmts.size()]));
				} finally {
					iter.close();
				}
			}
		}

		@Override
		public void addedStatements(final StmtIterator statements) throws AuthenticationRequiredException {
			if (canRead(Triple.ANY)) {
				wrapped.addedStatements(statements);
			} else {
				final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(), statements);
				try {
					wrapped.addedStatements(iter);
				} finally {
					iter.close();
				}
			}
		}

		@Override
		public void notifyEvent(final Model m, final Object event) {
			wrapped.notifyEvent(m, event);
		}

		@Override
		public void removedStatement(final Statement s) throws AuthenticationRequiredException {
			if (canRead(s)) {
				wrapped.removedStatement(s);
			}
		}

		@Override
		public void removedStatements(final List<Statement> statements) throws AuthenticationRequiredException {

			if (canRead(Triple.ANY)) {
				wrapped.removedStatements(statements);
			} else {
				final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
						WrappedIterator.create(statements.iterator()));
				try {
					wrapped.removedStatements(iter.toList());
				} finally {
					iter.close();
				}
			}
		}

		@Override
		public void removedStatements(final Model m) throws AuthenticationRequiredException {
			if (canRead(Triple.ANY)) {
				wrapped.removedStatements(m);
			} else {
				wrapped.removedStatements(SecuredModelImpl.getInstance(holder.getSecuredItem(), m));
			}
		}

		@Override
		public void removedStatements(final Statement[] statements) throws AuthenticationRequiredException {
			if (canRead(Triple.ANY)) {
				wrapped.removedStatements(statements);
			} else {
				final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
						WrappedIterator.create(Arrays.asList(statements).iterator()));
				try {
					final List<Statement> stmts = iter.toList();
					wrapped.removedStatements(stmts.toArray(new Statement[stmts.size()]));
				} finally {
					iter.close();
				}
			}
		}

		@Override
		public void removedStatements(final StmtIterator statements) throws AuthenticationRequiredException {
			if (canRead(Triple.ANY)) {
				wrapped.removedStatements(statements);
			} else {
				final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(), statements);
				try {
					wrapped.removedStatements(iter);
				} finally {
					iter.close();
				}
			}
		}
	}

	private static final RDFReaderF readerFactory = new RDFReaderFImpl();

	/**
	 * Get an instance of SecuredModel
	 * 
	 * @param securedItem
	 *            the item providing the security context.
	 * @param model
	 *            the Model to secure.
	 * @return The SecuredModel
	 */
	public static SecuredModel getInstance(final SecuredItem securedItem, final Model model) {
		return org.apache.jena.permissions.Factory.getInstance(securedItem.getSecurityEvaluator(),
				securedItem.getModelIRI(), model);
	}

	/**
	 * Get an instance of SecuredModel
	 * 
	 * @param securityEvaluator
	 *            The security evaluator to use
	 * @param modelIRI
	 *            The IRI (graph IRI) to name this model.
	 * @param model
	 *            The Model to secure.
	 * @return the SecuredModel
	 */
	public static SecuredModel getInstance(final SecurityEvaluator securityEvaluator, final String modelIRI,
			final Model model) {
		final ItemHolder<Model, SecuredModel> holder = new ItemHolder<>(model);

		final SecuredModelImpl checker = new SecuredModelImpl(securityEvaluator, modelIRI, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (model instanceof SecuredModel) {
			if (checker.isEquivalent((SecuredModel) model)) {
				return (SecuredModel) model;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(model.getClass(), checker));
	}

	// the item holder that contains this SecuredModel.
	private final ItemHolder<Model, SecuredModel> holder;

	// The secured graph that this securedModel contains.
	private final SecuredGraph graph;

	Map<ModelChangedListener, SecuredModelChangedListener> listeners = new HashMap<>();

	/**
	 * Constructor.
	 * 
	 * @param securityEvaluator
	 *            The security evaluator to use
	 * @param modelURI
	 *            The securedModel IRI to verify against.
	 * @param holder
	 *            The item holder that will contain this SecuredModel.
	 */
	private SecuredModelImpl(final SecurityEvaluator securityEvaluator, final String modelURI,
			final ItemHolder<Model, SecuredModel> holder) {
		super(securityEvaluator, modelURI, holder);
		this.graph = org.apache.jena.permissions.Factory.getInstance(securityEvaluator, modelURI,
				holder.getBaseItem().getGraph());
		this.holder = holder;
	}

	private RDFNode asObject(Object o) {
		return o instanceof RDFNode ? (RDFNode) o : ResourceFactory.createTypedLiteral(o);
	}

	@Override
	public SecuredModel abort() {
		holder.getBaseItem().abort();
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add(final List<Statement> statements)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreateFrontsTriples(WrappedIterator.create(statements.iterator()));
		holder.getBaseItem().add(statements);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add(final Model m)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canCreate(Triple.ANY)) {
			checkCreateFrontsTriples(m.listStatements());
		}
		holder.getBaseItem().add(m);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add(final Resource s, final Property p, final RDFNode o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), o.asNode()));
		holder.getBaseItem().add(s, p, o);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add(final Resource s, final Property p, final String o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		return add(s, p, o, false);
	}

	@Override
	public SecuredModel add(final Resource s, final Property p, final String o, final boolean wellFormed)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createLiteral(o, "", wellFormed)));
		holder.getBaseItem().add(s, p, o, wellFormed);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add(final Resource s, final Property p, final String lex, final RDFDatatype datatype)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createLiteral(lex, datatype)));
		holder.getBaseItem().add(s, p, lex, datatype);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add(final Resource s, final Property p, final String o, final String l)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createLiteral(o, l, false)));
		holder.getBaseItem().add(s, p, o, l);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add(final Statement s)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(s);
		holder.getBaseItem().add(s);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add(final Statement[] statements)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canCreate(Triple.ANY)) {
			for (final Statement s : statements) {
				checkCreate(s);
			}
		}
		holder.getBaseItem().add(statements);
		return holder.getSecuredItem();

	}

	@Override
	public SecuredModel add(final StmtIterator iter)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canCreate(Triple.ANY)) {
			final List<Triple> lst = new ArrayList<>();
			try {
				while (iter.hasNext()) {
					final Statement s = iter.next();
					checkCreate(s);
					lst.add(s.asTriple());
				}
				final Model m = ModelFactory.createModelForGraph(new CollectionGraph(lst));
				holder.getBaseItem().add(m.listStatements());
			} finally {
				iter.close();
			}
		} else {
			holder.getBaseItem().add(iter);
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel addLiteral(final Resource s, final Property p, final boolean o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		final Literal l = ResourceFactory.createTypedLiteral(o);
		if (l == null) {
			throw new IllegalArgumentException("How did we get a null");
		}
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral(final Resource s, final Property p, final char o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral(final Resource s, final Property p, final double o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral(final Resource s, final Property p, final float o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral(final Resource s, final Property p, final int o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral(final Resource s, final Property p, final Literal o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		return add(s, p, o);
	}

	@Override
	public SecuredModel addLiteral(final Resource s, final Property p, final long o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	@Deprecated
	public SecuredModel addLiteral(final Resource s, final Property p, final Object o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		return add(s, p, asObject(o));
	}

	@Override
	public SecuredRDFNode asRDFNode(final Node n) {
		return SecuredRDFNodeImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().asRDFNode(n));
	}

	@Override
	public SecuredStatement asStatement(final Triple t)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		final ExtendedIterator<Triple> iter = holder.getBaseItem().getGraph().find(t);
		final boolean exists = iter.hasNext();
		iter.close();
		if (exists) {
			checkRead();
			checkRead(t);
		} else {
			checkUpdate();
			checkCreate(t);
		}
		return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().asStatement(t));
	}

	@Override
	public SecuredModel begin() {
		holder.getBaseItem().begin();
		return holder.getSecuredItem();
	}

	@Override
	public void close() {
		holder.getBaseItem().close();
	}

	@Override
	public SecuredModel commit() {
		holder.getBaseItem().commit();
		return holder.getSecuredItem();
	}

	@Override
	public boolean contains(final Resource s, final Property p)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listStatements(s, p, (RDFNode) null));
		try {
			return iter.hasNext();
		} finally {
			iter.close();
		}
	}

	@Override
	public boolean contains(final Resource s, final Property p, final RDFNode o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listStatements(s, p, o));
		try {
			return iter.hasNext();
		} finally {
			iter.close();
		}
	}

	@Override
	public boolean contains(final Resource s, final Property p, final String o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listStatements(s, p, o));
		try {
			return iter.hasNext();
		} finally {
			iter.close();
		}
	}

	@Override
	public boolean contains(final Resource s, final Property p, final String o, final String l)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listStatements(s, p, o, l));
		try {
			return iter.hasNext();
		} finally {
			iter.close();
		}
	}

	@Override
	public boolean contains(final Statement s) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		checkRead(s);
		return holder.getBaseItem().contains(s);
	}

	@Override
	public boolean containsAll(final Model model) throws ReadDeniedException, AuthenticationRequiredException {
		return containsAll(model.listStatements());
	}

	@Override
	public boolean containsAll(final StmtIterator iter) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		final boolean doCheck = canRead(Triple.ANY);
		try {
			while (iter.hasNext()) {
				final Statement stmt = iter.next();
				if (doCheck) {
					checkRead(stmt);
				}
				if (!holder.getBaseItem().contains(stmt)) {
					return false;
				}
			}
			return true;
		} finally {
			iter.close();
		}
	}

	@Override
	public boolean containsAny(final Model model) throws ReadDeniedException, AuthenticationRequiredException {
		return containsAny(model.listStatements());

	}

	@Override
	public boolean containsAny(final StmtIterator iter) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		final boolean skipCheck = canRead(Triple.ANY);
		try {
			while (iter.hasNext()) {
				final Statement stmt = iter.next();
				if (skipCheck || canRead(stmt)) {
					if (holder.getBaseItem().contains(stmt)) {
						return true;
					}
				}
			}
			return false;
		} finally {
			iter.close();
		}
	}

	@Override
	public boolean containsLiteral(final Resource s, final Property p, final boolean o)
			throws ReadDeniedException, AuthenticationRequiredException {
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral(final Resource s, final Property p, final char o)
			throws ReadDeniedException, AuthenticationRequiredException {
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral(final Resource s, final Property p, final double o)
			throws ReadDeniedException, AuthenticationRequiredException {
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral(final Resource s, final Property p, final float o)
			throws ReadDeniedException, AuthenticationRequiredException {
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral(final Resource s, final Property p, final int o)
			throws ReadDeniedException, AuthenticationRequiredException {
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral(final Resource s, final Property p, final long o)
			throws ReadDeniedException, AuthenticationRequiredException {
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral(final Resource s, final Property p, final Object o)
			throws ReadDeniedException, AuthenticationRequiredException {
		return contains(s, p, asObject(o));
	}

	@Override
	public boolean containsResource(final RDFNode r) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(new Triple(Node.ANY, Node.ANY, Node.ANY))) {
			return holder.getBaseItem().containsResource(r);
		} else {
			ExtendedIterator<Statement> iter = listStatements(null, null, r);
			if (r.isResource()) {

				if (r.isURIResource()) {
					iter = iter.andThen(
							listStatements(null, ResourceFactory.createProperty(r.asNode().getURI()), (RDFNode) null));
				} else {
					iter = iter.andThen(listStatements(null,
							ResourceFactory.createProperty(r.asNode().getBlankNodeLabel()), (RDFNode) null));
				}
				iter = iter.andThen(listStatements(r.asResource(), null, (RDFNode) null));
			}
			try {
				return iter.hasNext();
			} finally {
				iter.close();
			}
		}
	}

	@Override
	public SecuredAlt createAlt() throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(SecurityEvaluator.FUTURE, RDF.type.asNode(), RDF.Alt.asNode()));
		return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createAlt());
	}

	@Override
	public SecuredAlt createAlt(final String uri)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(), RDF.Alt.asNode()));
		return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createAlt(uri));
	}

	@Override
	public SecuredBag createBag() throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(SecurityEvaluator.FUTURE, RDF.type.asNode(), RDF.Bag.asNode()));
		return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createBag());
	}

	@Override
	public SecuredBag createBag(final String uri)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(), RDF.Bag.asNode()));
		return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createBag(uri));
	}

	private Model createCopy() throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		return ModelFactory.createDefaultModel().add(holder.getSecuredItem());
	}

	@Override
	public SecuredRDFList createList()
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(SecurityEvaluator.FUTURE, RDF.first.asNode(), RDF.nil.asNode()));
		return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createList());
	}

	@Override
	public SecuredRDFList createList(final Iterator<? extends RDFNode> members)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(SecurityEvaluator.FUTURE, RDF.rest.asNode(), SecurityEvaluator.FUTURE));

		boolean canCreateAny = canCreate(new Triple(SecurityEvaluator.FUTURE, RDF.first.asNode(), Node.ANY));
		if (!canCreateAny) {
			// have to check each of the possible entries in the list for
			// creation.
			final List<RDFNode> nodes = new ArrayList<>();
			while (members.hasNext()) {

				final RDFNode n = members.next();
				checkCreate(new Triple(SecurityEvaluator.FUTURE, RDF.first.asNode(), n.asNode()));
				nodes.add(n);
			}
			return SecuredRDFListImpl.getInstance(holder.getSecuredItem(),
					holder.getBaseItem().createList(nodes.iterator()));

		} else {
			return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createList(members));
		}
	}

	@Override
	public SecuredRDFList createList(RDFNode... members)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		return createList(Arrays.asList(members).iterator());
	}

	@Override
	public SecuredLiteral createLiteral(final String v) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createLiteral(v));
	}

	@Override
	public SecuredLiteral createLiteral(final String v, final boolean wellFormed) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createLiteral(v, wellFormed));

	}

	@Override
	public SecuredLiteral createLiteral(final String v, final String language) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createLiteral(v, language));
	}

	@Override
	public SecuredStatement createLiteralStatement(final Resource s, final Property p, final boolean o) {
		return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement(final Resource s, final Property p, final char o) {
		return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement(final Resource s, final Property p, final double o) {
		return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement(final Resource s, final Property p, final float o) {
		return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement(final Resource s, final Property p, final int o) {
		return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement(final Resource s, final Property p, final long o) {
		return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement(final Resource s, final Property p, final Object o) {
		return createStatement(s, p, asObject(o));
	}

	@Override
	public SecuredProperty createProperty(final String uri) {
		return SecuredPropertyImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createProperty(uri));
	}

	@Override
	public SecuredProperty createProperty(final String nameSpace, final String localName) {
		return SecuredPropertyImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createProperty(nameSpace, localName));
	}

	@Override
	public SecuredReifiedStatement createReifiedStatement(final Statement s)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreateReified(null, s);
		return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createReifiedStatement(s));
	}

	@Override
	public SecuredReifiedStatement createReifiedStatement(final String uri, final Statement s)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreateReified(uri, s);
		return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createReifiedStatement(uri, s));
	}

	@Override
	public SecuredResource createResource() {
		// checkCreateAnonymousResource(SecurityEvaluator.FUTURE);
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource());
	}

	@Override
	public SecuredResource createResource(final AnonId id) {
		// checkCreateAnonymousResource(new SecurityEvaluator.SecNode(
		// SecurityEvaluator.SecNode.Type.Anonymous, id.getLabelString()));
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(id));
	}

	@Override
	public SecuredResource createResource(final Resource type)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		final Triple t = new Triple(SecurityEvaluator.FUTURE, RDF.type.asNode(), type.asNode());
		checkCreate(t);

		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(type));
	}

	@Override
	@Deprecated
	public SecuredResource createResource(final ResourceF f) throws AuthenticationRequiredException {
		return createResource(null, f);
	}

	@Override
	public SecuredResource createResource(final String uri) {
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(uri));

	}

	@Override
    public Resource createResource(Statement statement) {
        throw new UnsupportedOperationException("SecuredModel.createResource(Statement)");
    }

    @Override
	public SecuredResource createResource(final String uri, final Resource type)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		final Resource r = ResourceFactory.createResource(uri);
		final Triple t = new Triple(r.asNode(), RDF.type.asNode(), type.asNode());
		if (holder.getBaseItem().contains(r, RDF.type, type)) {
			checkRead();
			checkRead(t);
		} else {
			checkUpdate();
			checkCreate(t);
		}
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(uri, type));

	}

	@Override
	@Deprecated
	public SecuredResource createResource(final String uri, final ResourceF f) {
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(uri, f));
	}

	@Override
	public SecuredSeq createSeq() throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(SecurityEvaluator.FUTURE, RDF.type.asNode(), RDF.Alt.asNode()));
		return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createSeq());
	}

	@Override
	public SecuredSeq createSeq(final String uri)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(), RDF.Alt.asNode()));
		return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createSeq(uri));
	}

	@Override
	public SecuredStatement createStatement(final Resource s, final Property p, final RDFNode o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), o.asNode()));
		return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createStatement(s, p, o));
	}

	@Override
	public SecuredStatement createStatement(final Resource s, final Property p, final String o)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createURI(o)));
		return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createStatement(s, p, o));
	}

	@Override
	public SecuredStatement createStatement(final Resource s, final Property p, final String o,
			final boolean wellFormed)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		return createStatement(s, p, o, "", wellFormed);
	}

	@Override
	public SecuredStatement createStatement(final Resource s, final Property p, final String o, final String l)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		return createStatement(s, p, o, l, false);
	}

	@Override
	public SecuredStatement createStatement(final Resource s, final Property p, final String o, final String l,
			final boolean wellFormed)
			throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createLiteral(o, l, wellFormed)));
		return SecuredStatementImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createStatement(s, p, o, l, wellFormed));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final boolean v) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final Calendar d) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(d));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final char v) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final double v) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final float v) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final int v) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final long v) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final Object value) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(value));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final Object value, final RDFDatatype dtype) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createTypedLiteral(value, dtype));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final Object value, final String typeURI) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createTypedLiteral(value, typeURI));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final String v) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final String lex, final RDFDatatype dtype) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createTypedLiteral(lex, dtype));
	}

	@Override
	public SecuredLiteral createTypedLiteral(final String lex, final String typeURI) {
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createTypedLiteral(lex, typeURI));
	}

	@Override
	public Model difference(final Model model) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			return holder.getBaseItem().difference(model);
		} else {
			return createCopy().difference(model);
		}
	}

	@Override
	public void enterCriticalSection(final boolean readLockRequested)
			throws UpdateDeniedException, ReadDeniedException, AuthenticationRequiredException {
		if (readLockRequested) {
			checkRead();
		} else {
			checkUpdate();
		}
		holder.getBaseItem().enterCriticalSection(readLockRequested);
	}

	@Override
	public void executeInTxn(Runnable action) {
		holder.getBaseItem().executeInTxn(action);
	}

	@Override
	public <T> T calculateInTxn(Supplier<T> action) {
		return holder.getBaseItem().calculateInTxn(action);
	}

	@Override
	public String expandPrefix(final String prefixed) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().expandPrefix(prefixed);
	}

	@Override
	public SecuredAlt getAlt(final Resource r) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		checkRead(new Triple(r.asNode(), RDF.type.asNode(), RDF.Alt.asNode()));
		return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getAlt(r));
	}

	@Override
	public SecuredAlt getAlt(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		checkRead(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(), RDF.Alt.asNode()));
		return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getAlt(uri));
	}

	@Override
	public SecuredResource getAnyReifiedStatement(final Statement s)
			throws ReadDeniedException, UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
		final RSIterator it = listReifiedStatements(s);
		if (it.hasNext()) {
			try {
				return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(), it.nextRS());
			} finally {
				it.close();
			}
		} else {
			return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(), createReifiedStatement(s));
		}
	}

	@Override
	public SecuredBag getBag(final Resource r) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		checkRead(new Triple(r.asNode(), RDF.type.asNode(), RDF.Bag.asNode()));
		return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getBag(r));
	}

	@Override
	public SecuredBag getBag(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		checkRead(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(), RDF.Bag.asNode()));
		return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getBag(uri));
	}
	
	@Override
	public SecuredSeq getSeq(final Resource r) throws ReadDeniedException, AuthenticationRequiredException {
	    checkRead();
	    checkRead(new Triple(r.asNode(), RDF.type.asNode(), RDF.Seq.asNode()));
	    return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getSeq(r));
	}

	@Override
	public SecuredSeq getSeq(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
	    checkRead();
	    checkRead(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(), RDF.Seq.asNode()));
	    return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getSeq(uri));
	}



    @Override
    public SecuredRDFList getList( String uri ) throws ReadDeniedException, AuthenticationRequiredException { 
        checkRead();
        return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getList(uri));
    }
    
    @Override
    public SecuredRDFList getList( Resource r ) throws ReadDeniedException, AuthenticationRequiredException { 
        checkRead();
        return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getList(r));
    } 
	
	@Override
	public SecuredGraph getGraph() {
		return graph;
	}

	@Override
	public Lock getLock() {
		return holder.getBaseItem().getLock();
	}

	@Override
	public Map<String, String> getNsPrefixMap() throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().getNsPrefixMap();
	}

	@Override
	public String getNsPrefixURI(final String prefix) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().getNsPrefixURI(prefix);
	}

	@Override
	public String getNsURIPrefix(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().getNsURIPrefix(uri);
	}

	@Override
	public SecuredStatement getProperty(final Resource s, final Property p)
			throws ReadDeniedException, AuthenticationRequiredException {
		final StmtIterator stmt = listStatements(s, p, (RDFNode) null);
		try {
			if (stmt.hasNext()) {
				return SecuredStatementImpl.getInstance(holder.getSecuredItem(), stmt.next());
			}
			return null;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@Override
	public SecuredStatement getProperty(Resource s, Property p, String lang)
			throws ReadDeniedException, AuthenticationRequiredException {
		final StmtIterator stmt = listStatements(s, p, null, lang);
		try {
			if (stmt.hasNext()) {
				return SecuredStatementImpl.getInstance(holder.getSecuredItem(), stmt.next());
			}
			return null;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@Override
	public SecuredProperty getProperty(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return SecuredPropertyImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getProperty(uri));
	}

	@Override
	public SecuredProperty getProperty(final String nameSpace, final String localName)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return SecuredPropertyImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().getProperty(nameSpace, localName));
	}

	@Override
	public SecuredRDFNode getRDFNode(final Node n)
			throws ReadDeniedException, UpdateDeniedException, AuthenticationRequiredException {
		RDFNode rdfNode = null;
		if (n.isLiteral()) {
			rdfNode = ResourceFactory.createTypedLiteral(n.getLiteralLexicalForm(), n.getLiteralDatatype());
		} else if (n.isURI()) {
			rdfNode = ResourceFactory.createProperty(n.getURI());
		} else if (n.isBlank()) {
			rdfNode = ResourceFactory.createResource(n.getBlankNodeId().toString());
		} else {
			throw new IllegalArgumentException("Illegal Node type: " + n.getClass());
		}

		if (holder.getBaseItem().containsResource(rdfNode)) {
			checkRead();
		} else {
			checkUpdate();
		}
		if (n.isLiteral()) {
			return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
					holder.getBaseItem().getRDFNode(n).asLiteral());
		} else {
			return SecuredResourceImpl.getInstance(holder.getSecuredItem(),
					holder.getBaseItem().getRDFNode(n).asResource());
		}
	}

	@Override
	public RDFReaderI getReader() {
		return holder.getBaseItem().getReader();
	}

	@Override
	public RDFReaderI getReader(final String lang) {
		return holder.getBaseItem().getReader(lang);
	}

	@Override
	public SecuredStatement getRequiredProperty(final Resource s, final Property p)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			return SecuredStatementImpl.getInstance(holder.getSecuredItem(),
					holder.getBaseItem().getRequiredProperty(s, p));
		} else {
			final SecuredStatementIterator si = listStatements(s, p, (RDFNode) null);
			try {
				if (si.hasNext()) {
					return (SecuredStatement) si.next();
				} else {
					throw new PropertyNotFoundException(p);
				}
			} finally {
				si.close();
			}
		}
	}

	@Override
	public SecuredStatement getRequiredProperty(final Resource s, final Property p, String lang)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			return SecuredStatementImpl.getInstance(holder.getSecuredItem(),
					holder.getBaseItem().getRequiredProperty(s, p, lang));
		} else {
			final SecuredStatementIterator si = listStatements(s, p, null, lang);
			try {
				if (si.hasNext()) {
					return (SecuredStatement) si.next();
				} else {
					throw new PropertyNotFoundException(p);
				}
			} finally {
				si.close();
			}
		}
	}

	@Override
	public SecuredResource getResource(final String uri) {
		return createResource(uri);
	}

	@Override
	@Deprecated
	public SecuredResource getResource(final String uri, final ResourceF f) {
		return createResource(uri, f);
	}

	@Override
	public RDFWriterI getWriter() {
		return holder.getBaseItem().getWriter();
	}

	@Override
	public RDFWriterI getWriter(final String lang) {
		return holder.getBaseItem().getWriter(lang);
	}

	@Override
	public boolean independent() {
		return false;
	}

	@Override
	public Model intersection(final Model model) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (!canRead(Triple.ANY)) {
			return holder.getBaseItem().intersection(model);
		} else {
			return createCopy().intersection(model);
		}
	}

	@Override
	public boolean isClosed() {
		return holder.getBaseItem().isClosed();
	}

	@Override
	public boolean isEmpty() throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().isEmpty();
	}

	@Override
	public boolean isIsomorphicWith(final Model g) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		final boolean retval = holder.getBaseItem().isIsomorphicWith(g);
		if (retval && !canRead(Triple.ANY)) {
			// in this case we have to check all the items in the graph to see
			// if the user can read
			// them all.
			final ExtendedIterator<Statement> stmtIter = holder.getBaseItem().listStatements();
			try {
				while (stmtIter.hasNext()) {
					if (!canRead(stmtIter.next())) {
						return false;
					}
				}
			} finally {
				if (stmtIter != null) {
					stmtIter.close();
				}
			}
		}
		return retval;
	}

	@Override
	public boolean isReified(final Statement s) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		checkRead(s);

		final RSIterator it = listReifiedStatements(s);
		try {
			return it.hasNext();
		} finally {
			it.close();
		}
	}

	@Override
	public void leaveCriticalSection() {
		holder.getBaseItem().leaveCriticalSection();
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
			final boolean object) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listLiteralStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
			final char object) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listLiteralStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
			final double object) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listLiteralStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
			final float object) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listLiteralStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
			final long object) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listLiteralStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
			final int object) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listLiteralStatements(subject, predicate, object));
	}

	@Override
	public NsIterator listNameSpaces() throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().listNameSpaces();
	}

	@Override
	public SecuredNodeIterator<RDFNode> listObjects() throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<RDFNode> nIter = holder.getBaseItem().listObjects();
		if (!canRead(Triple.ANY)) {
			nIter = nIter.filterKeep(new ObjectFilter());
		}
		return new SecuredNodeIterator<>(holder.getSecuredItem(), nIter);
	}

	@Override
	public SecuredNodeIterator<RDFNode> listObjectsOfProperty(final Property p)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<RDFNode> nIter = holder.getBaseItem().listObjectsOfProperty(p);
		if (!canRead(Triple.ANY)) {
			nIter = nIter.filterKeep(new ObjectFilter(p));
		}
		return new SecuredNodeIterator<>(holder.getSecuredItem(), nIter);
	}

	@Override
	public SecuredNodeIterator<RDFNode> listObjectsOfProperty(final Resource s, final Property p)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<RDFNode> nIter = holder.getBaseItem().listObjectsOfProperty(s, p);
		if (!canRead(Triple.ANY)) {
			nIter = nIter.filterKeep(new ObjectFilter(p));
		}
		return new SecuredNodeIterator<>(holder.getSecuredItem(), nIter);
	}

	@Override
	public SecuredRSIterator listReifiedStatements() throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredRSIterator(holder.getSecuredItem(), holder.getBaseItem().listReifiedStatements());
	}

	@Override
	public SecuredRSIterator listReifiedStatements(final Statement st)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		checkRead(st);
		return new SecuredRSIterator(holder.getSecuredItem(), holder.getBaseItem().listReifiedStatements(st));
	}

	@Override
	public SecuredResIterator listResourcesWithProperty(final Property p)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);

	}

	@Override
	public SecuredResIterator listResourcesWithProperty(final Property p, final boolean o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listResourcesWithProperty(final Property p, final char o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listResourcesWithProperty(final Property p, final double o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listResourcesWithProperty(final Property p, final float o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listResourcesWithProperty(final Property p, final long o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listResourcesWithProperty(final Property p, final Object o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listResourcesWithProperty(final Property p, final RDFNode o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, o));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredStatementIterator listStatements() throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem().listStatements());
	}

	@Override
	public SecuredStatementIterator listStatements(final Resource s, final Property p, final RDFNode o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem().listStatements(s, p, o));
	}

	@Override
	public SecuredStatementIterator listStatements(final Resource subject, final Property predicate,
			final String object) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listStatements(final Resource subject, final Property predicate,
			final String object, final String lang) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(),
				holder.getBaseItem().listStatements(subject, predicate, object, lang));
	}

	@Override
	public SecuredStatementIterator listStatements(final Selector s)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem().listStatements(s));
	}

	@Override
	public SecuredResIterator listSubjects() throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjects();
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter());
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listSubjectsWithProperty(final Property p)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjectsWithProperty(p);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listSubjectsWithProperty(final Property p, final RDFNode o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjectsWithProperty(p, o);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, o));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listSubjectsWithProperty(final Property p, final String o)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjectsWithProperty(p, o);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, ResourceFactory.createPlainLiteral(o)));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredResIterator listSubjectsWithProperty(final Property p, final String o, final String l)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjectsWithProperty(p, o, l);
		if (!canRead(Triple.ANY)) {
			rIter = rIter.filterKeep(new ResourceFilter(p, ResourceFactory.createLangLiteral(o, l)));
		}
		return new SecuredResIterator(holder.getSecuredItem(), rIter);
	}

	@Override
	public SecuredPrefixMapping lock() throws UpdateDeniedException, AuthenticationRequiredException {
		checkUpdate();
		holder.getBaseItem().lock();
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel notifyEvent(final Object e) {
		holder.getBaseItem().notifyEvent(e);
		return holder.getSecuredItem();
	}

	@Override
	public String qnameFor(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().qnameFor(uri);
	}

	@Override
	public SecuredModel query(final Selector s) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return SecuredModelImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().query(new SecuredSelector(holder.getSecuredItem(), s)));
	}

	@Override
	public SecuredModel read(final InputStream in, final String base)
			throws UpdateDeniedException, AuthenticationRequiredException {
		checkUpdate();
		SecuredModelImpl.readerFactory.getReader().read(holder.getSecuredItem(), in, base);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel read(final InputStream in, final String base, final String lang)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkUpdate();
		SecuredModelImpl.readerFactory.getReader(lang).read(holder.getSecuredItem(), in, base);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel read(final Reader reader, final String base)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkUpdate();
		SecuredModelImpl.readerFactory.getReader().read(holder.getSecuredItem(), reader, base);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel read(final Reader reader, final String base, final String lang)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkUpdate();
		SecuredModelImpl.readerFactory.getReader(lang).read(holder.getSecuredItem(), reader, base);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel read(final String url) throws ReadDeniedException, AuthenticationRequiredException {
		checkUpdate();
		SecuredModelImpl.readerFactory.getReader().read(holder.getSecuredItem(), url);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel read(final String url, final String lang)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkUpdate();
		SecuredModelImpl.readerFactory.getReader(lang).read(holder.getSecuredItem(), url);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel read(final String url, final String base, final String lang)
			throws ReadDeniedException, AuthenticationRequiredException {
		try (InputStream is = new URL(url).openStream()) {
			read(is, base, lang);
		} catch (final IOException e) {
			throw new WrappedIOException(e);
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel register(final ModelChangedListener listener)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (!listeners.containsKey(listener)) {
			final SecuredModelChangedListener secL = new SecuredModelChangedListener(listener);
			listeners.put(listener, secL);
			holder.getBaseItem().register(secL);
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove(final List<Statement> statements)
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canDelete(Triple.ANY)) {
			for (final Statement s : statements) {
				checkDelete(s);
			}
		}
		holder.getBaseItem().remove(statements);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove(final Model m)
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canDelete(Triple.ANY)) {
			final StmtIterator iter = m.listStatements();
			try {
				while (iter.hasNext()) {
					final Statement stmt = iter.next();
					checkDelete(stmt);
				}
			} finally {
				iter.close();
			}
		}
		holder.getBaseItem().remove(m);

		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove(final Resource s, final Property p, final RDFNode o)
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkDelete(new Triple(s.asNode(), p.asNode(), o.asNode()));
		holder.getBaseItem().remove(s, p, o);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove(final Statement s)
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkDelete(wildCardTriple(s));
		holder.getBaseItem().remove(s);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove(final Statement[] statements)
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canDelete(Triple.ANY)) {
			for (final Statement s : statements) {
				checkDelete(s);
			}
		}
		holder.getBaseItem().remove(statements);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove(final StmtIterator iter)
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canDelete(Triple.ANY)) {
			final List<Triple> lst = new ArrayList<>();
			try {
				while (iter.hasNext()) {
					final Statement s = iter.next();
					checkDelete(s);
					lst.add(s.asTriple());
				}
				final Model m = ModelFactory.createModelForGraph(new CollectionGraph(lst));
				holder.getBaseItem().remove(m.listStatements());
			} finally {
				iter.close();
			}
		} else {
			holder.getBaseItem().remove(iter);
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel removeAll()
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canDelete(Triple.ANY)) {
			final StmtIterator iter = holder.getBaseItem().listStatements();
			try {
				while (iter.hasNext()) {
					checkDelete(iter.next());
				}
			} finally {
				iter.close();
			}
		}
		holder.getBaseItem().removeAll();
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel removeAll(final Resource s, final Property p, final RDFNode r)
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canDelete(new Triple(wildCardNode(s), wildCardNode(p), wildCardNode(r)))) {
			final StmtIterator iter = holder.getBaseItem().listStatements(s, p, r);
			try {
				while (iter.hasNext()) {
					checkDelete(iter.next());
				}
			} finally {
				iter.close();
			}
		}
		holder.getBaseItem().removeAll(s, p, r);
		return holder.getSecuredItem();
	}

	@Override
	public void removeAllReifications(final Statement s)
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (canDelete(new Triple(Node.ANY, RDF.subject.asNode(), wildCardNode(s.getSubject())))
				&& canDelete(new Triple(Node.ANY, RDF.predicate.asNode(), wildCardNode(s.getPredicate())))
				&& canDelete(new Triple(Node.ANY, RDF.object.asNode(), wildCardNode(s.getObject())))) {
			holder.getBaseItem().removeAllReifications(s);
		} else {
			final RSIterator iter = holder.getBaseItem().listReifiedStatements(s);
			try {
				while (iter.hasNext()) {
					final ReifiedStatement rs = iter.next();
					checkDelete(new Triple(rs.asNode(), RDF.subject.asNode(), wildCardNode(s.getSubject())));
					checkDelete(new Triple(rs.asNode(), RDF.predicate.asNode(), wildCardNode(s.getPredicate())));
					checkDelete(new Triple(rs.asNode(), RDF.object.asNode(), wildCardNode(s.getObject())));
				}
				holder.getBaseItem().removeAllReifications(s);
			} finally {
				iter.close();
			}

		}
	}

	@Override
	public SecuredModel removeNsPrefix(final String prefix)
			throws UpdateDeniedException, AuthenticationRequiredException {
		checkUpdate();
		holder.getBaseItem().removeNsPrefix(prefix);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel clearNsPrefixMap() {
		checkUpdate();
		holder.getBaseItem().clearNsPrefixMap();
		return holder.getSecuredItem();
	}

	@Override
	public void removeReification(final ReifiedStatement rs)
			throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		if (!canDelete(Triple.ANY)) {
			final StmtIterator stmtIter = rs.listProperties();
			try {
				while (stmtIter.hasNext()) {
					checkDelete(stmtIter.next());
				}
			} finally {
				stmtIter.close();
			}
		}
		holder.getBaseItem().removeReification(rs);
	}

	@Override
	public boolean samePrefixMappingAs(final PrefixMapping other)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().samePrefixMappingAs(other);
	}

	@Override
	public SecuredModel setNsPrefix(final String prefix, final String uri)
			throws UpdateDeniedException, AuthenticationRequiredException {
		checkUpdate();
		holder.getBaseItem().setNsPrefix(prefix, uri);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel setNsPrefixes(final Map<String, String> map)
			throws UpdateDeniedException, AuthenticationRequiredException {
		checkUpdate();
		holder.getBaseItem().setNsPrefixes(map);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel setNsPrefixes(final PrefixMapping other)
			throws UpdateDeniedException, AuthenticationRequiredException {
		checkUpdate();
		holder.getBaseItem().setNsPrefixes(other);
		return holder.getSecuredItem();
	}

	@Override
	public String shortForm(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().shortForm(uri);
	}
	
    @Override
    public boolean hasNoMappings() {
        checkRead();
        return holder.getBaseItem().hasNoMappings();
    }

    @Override
    public int numPrefixes() {
        checkRead();
        return holder.getBaseItem().numPrefixes();
    }

	@Override
	public long size() throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().size();
	}

	@Override
	public boolean supportsSetOperations() {
		return holder.getBaseItem().supportsSetOperations();
	}

	@Override
	public boolean supportsTransactions() {
		return holder.getBaseItem().supportsTransactions();
	}

	@Override
	public Model union(final Model model) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			return holder.getBaseItem().union(model);
		} else {
			return createCopy().union(model);
		}
	}

	@Override
	public SecuredModel unregister(final ModelChangedListener listener) {
		if (listeners.containsKey(listener)) {
			final SecuredModelChangedListener secL = listeners.get(listener);
			holder.getBaseItem().unregister(secL);
			listeners.remove(listener);
		}
		return holder.getSecuredItem();
	}

	private Node wildCardNode(final RDFNode node) {
		return node == null ? Node.ANY : node.asNode();
	}

	private Triple wildCardTriple(final Statement s) {
		return new Triple(wildCardNode(s.getSubject()), wildCardNode(s.getPredicate()), wildCardNode(s.getObject()));
	}

	@Override
	public SecuredModel withDefaultMappings(final PrefixMapping map)
			throws UpdateDeniedException, AuthenticationRequiredException {
		checkUpdate();
		holder.getBaseItem().withDefaultMappings(map);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredResource wrapAsResource(final Node n) {
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().wrapAsResource(n));
	}

	@Override
	public SecuredModel write(final OutputStream out) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			holder.getBaseItem().write(out);
		} else {
			getWriter().write(holder.getSecuredItem(), out, "");
		}
		return holder.getSecuredItem();

	}

	@Override
	public SecuredModel write(final OutputStream out, final String lang)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			holder.getBaseItem().write(out, lang);
		} else {
			getWriter(lang).write(holder.getSecuredItem(), out, "");
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel write(final OutputStream out, final String lang, final String base)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			holder.getBaseItem().write(out, lang, base);
		} else {
			getWriter(lang).write(holder.getSecuredItem(), out, base);
		}
		return holder.getSecuredItem();

	}

	@Override
	public SecuredModel write(final Writer writer) throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			holder.getBaseItem().write(writer);
		} else {
			getWriter().write(holder.getSecuredItem(), writer, "");
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel write(final Writer writer, final String lang)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			holder.getBaseItem().write(writer, lang);
		} else {
			getWriter(lang).write(holder.getSecuredItem(), writer, "");
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel write(final Writer writer, final String lang, final String base)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		if (canRead(Triple.ANY)) {
			holder.getBaseItem().write(writer, lang, base);
		} else {
			getWriter(lang).write(holder.getSecuredItem(), writer, base);
		}
		return holder.getSecuredItem();

	}

	private class ResourceFilter implements Predicate<Resource> {
		Property p;
		RDFNode o;

		ResourceFilter() {
			this(null, null);
		}

		ResourceFilter(Property p) {
			this(p, null);
		}

		ResourceFilter(Property p, RDFNode o) {
			this.p = p;
			this.o = o;
		}

		@Override
		public boolean test(Resource s) {
			StmtIterator iter = listStatements(s, p, o);
			try {
				return iter.hasNext();
			} finally {
				iter.close();
			}
		}

	}

	private class ObjectFilter implements Predicate<RDFNode> {
		Resource s;
		Property p;

		ObjectFilter() {
			this(null, null);
		}

		ObjectFilter(Property p) {
			this(null, p);
		}

		ObjectFilter(Resource s, Property p) {
			this.s = s;
			this.p = p;
		}

		@Override
		public boolean test(RDFNode o) {
			StmtIterator iter = listStatements(s, p, o);
			try {
				return iter.hasNext();
			} finally {
				iter.close();
			}
		}
	}
}
