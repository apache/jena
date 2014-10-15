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
package org.apache.jena.security.model.impl;

import java.io.* ;
import java.net.URL ;
import java.util.* ;

import org.apache.jena.security.AccessDeniedException ;
import org.apache.jena.security.SecurityEvaluator ;
import org.apache.jena.security.SecurityEvaluator.SecTriple ;
import org.apache.jena.security.graph.SecuredGraph ;
import org.apache.jena.security.graph.SecuredPrefixMapping ;
import org.apache.jena.security.impl.ItemHolder ;
import org.apache.jena.security.impl.SecuredItem ;
import org.apache.jena.security.impl.SecuredItemImpl ;
import org.apache.jena.security.impl.SecuredItemInvoker ;
import org.apache.jena.security.model.* ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.impl.CollectionGraph ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl ;
import com.hp.hpl.jena.shared.* ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.Filter ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;
import com.hp.hpl.jena.vocabulary.RDF ;

/**
 * Implementation of SecuredModel to be used by a SecuredItemInvoker proxy.
 */
public class SecuredModelImpl extends SecuredItemImpl implements SecuredModel
{

	// a class that implements ModelChangedListener
	private class SecuredModelChangedListener implements ModelChangedListener
	{
		private final ModelChangedListener wrapped;

		private SecuredModelChangedListener( final ModelChangedListener wrapped )
		{
			this.wrapped = wrapped;
		}

		@Override
		public void addedStatement( final Statement s )
		{
			if (canRead(s.asTriple()))
			{
				wrapped.addedStatement(s);
			}
		}

		@Override
		public void addedStatements( final List<Statement> statements )
		{
			if (canRead(Triple.ANY))
			{
				wrapped.addedStatements(statements);
			}
			else
			{
				final SecuredStatementIterator iter = new SecuredStatementIterator(
						holder.getSecuredItem(),
						WrappedIterator.create(statements.iterator()));
				try
				{
					wrapped.addedStatements(iter.toList());
				}
				finally
				{
					iter.close();
				}
			}
		}

		@Override
		public void addedStatements( final Model m )
		{
			if (canRead(Triple.ANY))
			{
				wrapped.addedStatements(m);
			}
			else
			{
				wrapped.addedStatements(SecuredModelImpl.getInstance(
						holder.getSecuredItem(), m));
			}
		}

		@Override
		public void addedStatements( final Statement[] statements )
		{
			if (canRead(Triple.ANY))
			{
				wrapped.addedStatements(statements);
			}
			else
			{
				final SecuredStatementIterator iter = new SecuredStatementIterator(
						holder.getSecuredItem(), WrappedIterator.create(Arrays
								.asList(statements).iterator()));
				try
				{
					final List<Statement> stmts = iter.toList();
					wrapped.addedStatements(stmts.toArray(new Statement[stmts
							.size()]));
				}
				finally
				{
					iter.close();
				}
			}
		}

		@Override
		public void addedStatements( final StmtIterator statements )
		{
			if (canRead(Triple.ANY))
			{
				wrapped.addedStatements(statements);
			}
			else
			{
				final SecuredStatementIterator iter = new SecuredStatementIterator(
						holder.getSecuredItem(), statements);
				try
				{
					wrapped.addedStatements(iter);
				}
				finally
				{
					iter.close();
				}
			}
		}

		@Override
		public void notifyEvent( final Model m, final Object event )
		{
			wrapped.notifyEvent(m, event);
		}

		@Override
		public void removedStatement( final Statement s )
		{
			if (canRead(s.asTriple()))
			{
				wrapped.removedStatement(s);
			}
		}

		@Override
		public void removedStatements( final List<Statement> statements )
		{

			if (canRead(Triple.ANY))
			{
				wrapped.removedStatements(statements);
			}
			else
			{
				final SecuredStatementIterator iter = new SecuredStatementIterator(
						holder.getSecuredItem(),
						WrappedIterator.create(statements.iterator()));
				try
				{
					wrapped.removedStatements(iter.toList());
				}
				finally
				{
					iter.close();
				}
			}
		}

		@Override
		public void removedStatements( final Model m )
		{
			if (canRead(Triple.ANY))
			{
				wrapped.removedStatements(m);
			}
			else
			{
				wrapped.removedStatements(SecuredModelImpl.getInstance(
						holder.getSecuredItem(), m));
			}
		}

		@Override
		public void removedStatements( final Statement[] statements )
		{
			if (canRead(Triple.ANY))
			{
				wrapped.removedStatements(statements);
			}
			else
			{
				final SecuredStatementIterator iter = new SecuredStatementIterator(
						holder.getSecuredItem(), WrappedIterator.create(Arrays
								.asList(statements).iterator()));
				try
				{
					final List<Statement> stmts = iter.toList();
					wrapped.removedStatements(stmts.toArray(new Statement[stmts
							.size()]));
				}
				finally
				{
					iter.close();
				}
			}
		}

		@Override
		public void removedStatements( final StmtIterator statements )
		{
			if (canRead(Triple.ANY))
			{
				wrapped.removedStatements(statements);
			}
			else
			{
				final SecuredStatementIterator iter = new SecuredStatementIterator(
						holder.getSecuredItem(), statements);
				try
				{
					wrapped.removedStatements(iter);
				}
				finally
				{
					iter.close();
				}
			}
		}
	}

	/*private class  ReadFilter extends Filter<Resource> {
		private SecuredItem si;
		private SecuredResource r;
		private Property p;
		ReadFilter( SecuredItem si, SecuredResource r, Property p )
		{
			this.si = si;
			this.r = r;
			this.p = p;
		}
		@Override
		public boolean accept(Resource o) {
			Triple t = new Triple( r.asNode(), p.asNode(), o.asNode());
			return si.canRead( SecuredItemImpl.convert( t ) );
		}};
*/
	private static final RDFReaderF readerFactory = new RDFReaderFImpl();
	//private static final RDFWriterF writerFactory = new RDFWriterFImpl();

	/**
	 * Get an instance of SecuredModel
	 * 
	 * @param securedItem
	 *            the item providing the security context.
	 * @param model
	 *            the Model to secure.
	 * @return The SecuredModel
	 */
	public static SecuredModel getInstance( final SecuredItem securedItem,
			final Model model )
	{
		return org.apache.jena.security.Factory.getInstance(
				securedItem.getSecurityEvaluator(), securedItem.getModelIRI(),
				model);
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
	public static SecuredModel getInstance(
			final SecurityEvaluator securityEvaluator, final String modelIRI,
			final Model model )
	{
		final ItemHolder<Model, SecuredModel> holder = new ItemHolder<Model, SecuredModel>(
				model);

		final SecuredModelImpl checker = new SecuredModelImpl(
				securityEvaluator, modelIRI, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (model instanceof SecuredModel)
		{
			if (checker.isEquivalent((SecuredModel) model))
			{
				return (SecuredModel) model;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(model.getClass(),
				checker));
	}

	// the item holder that contains this SecuredModel.
	private final ItemHolder<Model, SecuredModel> holder;

	// The secured graph that this securedModel contains.
	private final SecuredGraph graph;

	//
	Map<ModelChangedListener, SecuredModelChangedListener> listeners = new HashMap<ModelChangedListener, SecuredModelChangedListener>();

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
	private SecuredModelImpl( final SecurityEvaluator securityEvaluator,
			final String modelURI, final ItemHolder<Model, SecuredModel> holder )
	{
		super(securityEvaluator, modelURI, holder);
		this.graph = org.apache.jena.security.Factory.getInstance(this
				.getSecurityEvaluator(), this.getModelIRI(), holder
				.getBaseItem().getGraph());
		this.holder = holder;
	}
	
	 private RDFNode asObject( Object o )
     { 
		 return o instanceof RDFNode ? (RDFNode) o : ResourceFactory.createTypedLiteral( o ); 
     }

	@Override
	public SecuredModel abort()
	{
		holder.getBaseItem().abort();
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add( final List<Statement> statements )
	{
		checkUpdate();
		checkCreateStatement(WrappedIterator.create(statements.iterator()));
		holder.getBaseItem().add(statements);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add( final Model m )
	{
		checkUpdate();
		if (!canCreate(Triple.ANY))
		{
			checkCreateStatement(m.listStatements());
		}
		holder.getBaseItem().add(m);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add( final Resource s, final Property p, final RDFNode o )
	{
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), o.asNode()));
		holder.getBaseItem().add(s, p, o);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add( final Resource s, final Property p, final String o )
	{
		return add( s, p, o, false );
	}

	@Override
	public SecuredModel add( final Resource s, final Property p,
			final String o, final boolean wellFormed )
	{
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createLiteral(o,
				"", wellFormed)));
		holder.getBaseItem().add(s, p, o, wellFormed);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add( final Resource s, final Property p,
			final String lex, final RDFDatatype datatype )
	{
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createLiteral(lex,
				datatype)));
		holder.getBaseItem().add(s, p, lex, datatype);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add( final Resource s, final Property p,
			final String o, final String l )
	{
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createLiteral(o, l,
				false)));
		holder.getBaseItem().add(s, p, o, l);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add( final Statement s )
	{
		checkUpdate();
		checkCreate(s.asTriple());
		holder.getBaseItem().add(s);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel add( final Statement[] statements )
	{
		checkUpdate();
		if (!canCreate(Triple.ANY))
		{
			for (final Statement s : statements)
			{
				checkCreate(s.asTriple());
			}
		}
		holder.getBaseItem().add(statements);
		return holder.getSecuredItem();

	}

	@Override
	public SecuredModel add( final StmtIterator iter )
	{
		checkUpdate();
		if (!canCreate(Triple.ANY))
		{
			final List<Triple> lst = new ArrayList<Triple>();
			try
			{
				while (iter.hasNext())
				{
					final Statement s = iter.next();
					checkCreate(s.asTriple());
					lst.add(s.asTriple());
				}
				final Model m = ModelFactory
						.createModelForGraph(new CollectionGraph(lst));
				holder.getBaseItem().add(m.listStatements());
			}
			finally
			{
				iter.close();
			}
		}
		else
		{
			holder.getBaseItem().add(iter);
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final boolean o )
	{
		final Literal l = ResourceFactory.createTypedLiteral(o);
		if (l == null)
		{
			throw new IllegalArgumentException( "HOw did we get a null");
		}
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final char o )
	{
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final double o )
	{
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final float o )
	{
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final int o )
	{
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final Literal o )
	{
		return add(s, p, o);
	}

	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final long o )
	{
		final Literal l = ResourceFactory.createTypedLiteral(o);
		return add(s, p, l);
	}

	@Override
	@Deprecated
	public SecuredModel addLiteral( final Resource s, final Property p,
			final Object o )
	{
		return add(s, p, asObject(o));
	}

	@Override
	public SecuredRDFNode asRDFNode( final Node n )
	{
		return SecuredRDFNodeImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.asRDFNode(n));
	}

	@Override
	public SecuredStatement asStatement( final Triple t )
	{
		final ExtendedIterator<Triple> iter = holder.getBaseItem().getGraph()
				.find(t);
		final boolean exists = iter.hasNext();
		iter.close();
		if (exists)
		{
			checkRead();
			checkRead(t);
		}
		else
		{
			checkUpdate();
			checkCreate(t);
		}
		return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.asStatement(t));
	}

	@Override
	public SecuredModel begin()
	{
		holder.getBaseItem().begin();
		return holder.getSecuredItem();
	}

	private void checkCreate( final SecurityEvaluator.SecNode n, final Triple t )
	{
		checkRead(t);
		checkCreate(new SecurityEvaluator.SecTriple(n,
				SecuredItemImpl.convert(RDF.subject.asNode()),
				SecuredItemImpl.convert(t.getSubject())));
		checkCreate(new SecurityEvaluator.SecTriple(n,
				SecuredItemImpl.convert(RDF.predicate.asNode()),
				SecuredItemImpl.convert(t.getPredicate())));
		checkCreate(new SecurityEvaluator.SecTriple(n,
				SecuredItemImpl.convert(RDF.object.asNode()),
				SecuredItemImpl.convert(t.getObject())));
	}

	/*
	 * private void checkCreateAnonymousResource( final
	 * SecurityEvaluator.SecNode n )
	 * {
	 * checkUpdate();
	 * final SecurityEvaluator.SecTriple t = new SecurityEvaluator.SecTriple(n,
	 * SecurityEvaluator.SecNode.IGNORE, SecurityEvaluator.SecNode.IGNORE);
	 * checkCreate(t);
	 * }
	 */
	@Override
	public void close()
	{
		holder.getBaseItem().close();
	}

	@Override
	public SecuredModel commit()
	{
		holder.getBaseItem().commit();
		return holder.getSecuredItem();
	}

	@Override
	public boolean contains( final Resource s, final Property p )
	{
		checkRead();
		final SecuredStatementIterator iter = new SecuredStatementIterator(
				holder.getSecuredItem(), holder.getBaseItem().listStatements(s, p, (RDFNode) null));
		try
		{
			return iter.hasNext();
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public boolean contains( final Resource s, final Property p, final RDFNode o )
	{
		checkRead();
		final SecuredStatementIterator iter = new SecuredStatementIterator(
				holder.getSecuredItem(), holder.getBaseItem().listStatements(s, p, o));
		try
		{
			return iter.hasNext();
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public boolean contains( final Resource s, final Property p, final String o )
	{
		checkRead();
		final SecuredStatementIterator iter = new SecuredStatementIterator(
				holder.getSecuredItem(), holder.getBaseItem().listStatements(s, p, o));
		try
		{
			return iter.hasNext();
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public boolean contains( final Resource s, final Property p,
			final String o, final String l )
	{
		checkRead();
		final SecuredStatementIterator iter = new SecuredStatementIterator(
				holder.getSecuredItem(), holder.getBaseItem().listStatements(s, p, o, l));
		try
		{
			return iter.hasNext();
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public boolean contains( final Statement s )
	{
		checkRead();
		checkRead(s);
		return holder.getBaseItem().contains(s);
	}

	@Override
	public boolean containsAll( final Model model )
	{
		return containsAll(model.listStatements());
	}

	@Override
	public boolean containsAll( final StmtIterator iter )
	{
		checkRead();
		final boolean doCheck = canRead(Triple.ANY);
		try
		{
			while (iter.hasNext())
			{
				final Statement stmt = iter.next();
				if (doCheck)
				{
					checkRead(stmt);
				}
				if (!holder.getBaseItem().contains(stmt))
				{
					return false;
				}
			}
			return true;
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public boolean containsAny( final Model model )
	{
		return containsAny(model.listStatements());

	}

	@Override
	public boolean containsAny( final StmtIterator iter )
	{
		checkRead();
		final boolean skipCheck = canRead(Triple.ANY);
		try
		{
			while (iter.hasNext())
			{
				final Statement stmt = iter.next();
				if (skipCheck || canRead(stmt))
				{
					if (holder.getBaseItem().contains(stmt))
					{
						return true;
					}
				}
			}
			return false;
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final boolean o )
	{
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final char o )
	{
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final double o )
	{
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final float o )
	{
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final int o )
	{
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final long o )
	{
		return contains(s, p, ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final Object o )
	{
		return contains(s, p, asObject(o));
	}

	@Override
	public boolean containsResource( final RDFNode r )
	{
		checkRead();
		if (canRead(new Triple(Node.ANY, Node.ANY, Node.ANY)))
		{
			return holder.getBaseItem().containsResource(r);
		}
		else
		{
			ExtendedIterator<Statement> iter = listStatements(null, null, r);
			if (r.isResource())
			{

				if (r.isURIResource())
				{
					iter = iter
							.andThen(listStatements(null, ResourceFactory
									.createProperty(r.asNode().getURI()),
									(RDFNode) null));
				}
				else
				{
					iter = iter.andThen(listStatements(null, ResourceFactory
							.createProperty(r.asNode().getBlankNodeLabel()),
							(RDFNode) null));
				}
				iter = iter.andThen(listStatements(r.asResource(), null,
						(RDFNode) null));
			}
			try
			{
				return iter.hasNext();
			}
			finally
			{
				iter.close();
			}
		}
	}

	@Override
	public SecuredAlt createAlt()
	{
		checkUpdate();
		checkCreate(new SecurityEvaluator.SecTriple(
				SecurityEvaluator.SecNode.FUTURE,
				SecuredItemImpl.convert(RDF.type.asNode()),
				SecuredItemImpl.convert(RDF.Alt.asNode())));
		return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createAlt());
	}

	@Override
	public SecuredAlt createAlt( final String uri )
	{
		checkUpdate();
		checkCreate(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(),
				RDF.Alt.asNode()));
		return SecuredAltImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createAlt(uri));
	}

	@Override
	public SecuredBag createBag()
	{
		checkUpdate();
		checkCreate(new SecurityEvaluator.SecTriple(
				SecurityEvaluator.SecNode.FUTURE,
				SecuredItemImpl.convert(RDF.type.asNode()),
				SecuredItemImpl.convert(RDF.Bag.asNode())));
		return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createBag());
	}

	@Override
	public SecuredBag createBag( final String uri )
	{
		checkUpdate();
		checkCreate(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(),
				RDF.Bag.asNode()));
		return SecuredBagImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createBag(uri));
	}

	private Model createCopy()
	{
		return ModelFactory.createDefaultModel().add(holder.getSecuredItem());
	}

	@Override
	public SecuredRDFList createList()
	{
		checkUpdate();
		return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createList());
	}

	@Override
	public SecuredRDFList createList( final Iterator<? extends RDFNode> members )
	{
		checkUpdate();
		checkCreate(new SecurityEvaluator.SecTriple(
				SecurityEvaluator.SecNode.FUTURE,
				SecuredItemImpl.convert(RDF.rest.asNode()),
				SecurityEvaluator.SecNode.FUTURE));
		if (!(canCreate(new SecurityEvaluator.SecTriple(
				SecurityEvaluator.SecNode.FUTURE,
				SecuredItemImpl.convert(RDF.first.asNode()),
				SecuredItemImpl.convert(Node.ANY)))))
		{
			final List<RDFNode> nodes = new ArrayList<RDFNode>();
			while (members.hasNext())
			{

				final RDFNode n = members.next();
				checkCreate(new SecurityEvaluator.SecTriple(
						SecurityEvaluator.SecNode.FUTURE,
						SecuredItemImpl.convert(RDF.first.asNode()),
						SecuredItemImpl.convert(n.asNode())));
				nodes.add(n);
			}
			return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
					.createList(nodes.iterator()));

		}
		else
		{
			return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
					.createList(members));
		}
	}

	@Override
	public SecuredRDFList createList( final RDFNode[] members )
	{
		return createList(Arrays.asList(members).iterator());
	}

	@Override
	public SecuredLiteral createLiteral( final String v )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createLiteral(v));
	}

	@Override
	public SecuredLiteral createLiteral( final String v,
			final boolean wellFormed )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createLiteral(v, wellFormed));

	}

	@Override
	public SecuredLiteral createLiteral( final String v, final String language )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createLiteral(v, language));
	}

	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final boolean o )
	{
		return createStatement(s, p,
				ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final char o )
	{
		return createStatement(s, p,
				ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final double o )
	{
		return createStatement(s, p,
				ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final float o )
	{
		return createStatement(s, p,
				ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final int o )
	{
		return createStatement(s, p,
				ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final long o )
	{
		return createStatement(s, p,
				ResourceFactory.createTypedLiteral(o));
	}

	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final Object o )
	{
		return createStatement(s, p, asObject(o));
	}

	@Override
	public SecuredProperty createProperty( final String uri )
	{
		return SecuredPropertyImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createProperty(uri));
	}

	@Override
	public SecuredProperty createProperty( final String nameSpace,
			final String localName )
	{
		checkUpdate();
		return SecuredPropertyImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createProperty(nameSpace, localName));
	}

	@Override
	public ReifiedStatement createReifiedStatement( final Statement s )
	{
		checkUpdate();
		checkCreate(SecurityEvaluator.SecNode.FUTURE, s.asTriple());
		return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(), holder
				.getBaseItem().createReifiedStatement(s));
	}

	@Override
	public ReifiedStatement createReifiedStatement( final String uri,
			final Statement s )
	{
		checkUpdate();
		checkCreate(new SecurityEvaluator.SecNode(
				SecurityEvaluator.SecNode.Type.URI, uri), s.asTriple());
		return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(), holder
				.getBaseItem().createReifiedStatement(uri, s));
	}

	@Override
	public SecuredResource createResource()
	{
		// checkCreateAnonymousResource(SecurityEvaluator.SecNode.FUTURE);
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createResource());
	}

	@Override
	public SecuredResource createResource( final AnonId id )
	{
		// checkCreateAnonymousResource(new SecurityEvaluator.SecNode(
		// SecurityEvaluator.SecNode.Type.Anonymous, id.getLabelString()));
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createResource(id));
	}

	@Override
	public SecuredResource createResource( final Resource type )
	{
		checkUpdate();
		final SecurityEvaluator.SecTriple t = new SecurityEvaluator.SecTriple(
				SecurityEvaluator.SecNode.FUTURE,
				SecuredItemImpl.convert(RDF.type.asNode()),
				SecuredItemImpl.convert(type.asNode()));
		checkCreate(t);

		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createResource(type));
	}

	@Override
	@Deprecated
	public SecuredResource createResource( final ResourceF f )
	{
		return createResource(null, f);
	}

	@Override
	public SecuredResource createResource( final String uri )
	{
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createResource(uri));

	}

	@Override
	public SecuredResource createResource( final String uri, final Resource type )
	{
		final Resource r = ResourceFactory.createResource(uri);
		final SecurityEvaluator.SecTriple t = new SecurityEvaluator.SecTriple(
				SecuredItemImpl.convert(r.asNode()),
				SecuredItemImpl.convert(RDF.type.asNode()),
				SecuredItemImpl.convert(type.asNode()));
		if (holder.getBaseItem().contains(r, RDF.type, type))
		{
			checkRead();
			checkRead(t);
		}
		else
		{
			checkUpdate();
			checkCreate(t);
		}
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createResource(uri, type));

	}

	@Override
	@Deprecated
	public SecuredResource createResource( final String uri, final ResourceF f )
	{
		// Resource resource = f.createResource( ResourceFactory.createResource( uri )
		// );
		// checkCreateResource( resource );
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createResource(uri, f));
	}

	@Override
	public SecuredSeq createSeq()
	{
		checkUpdate();
		checkCreate(new SecurityEvaluator.SecTriple(
				SecurityEvaluator.SecNode.FUTURE,
				SecuredItemImpl.convert(RDF.type.asNode()),
				SecuredItemImpl.convert(RDF.Alt.asNode())));
		return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createSeq());
	}

	@Override
	public SecuredSeq createSeq( final String uri )
	{
		checkUpdate();
		checkCreate(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(),
				RDF.Alt.asNode()));
		return SecuredSeqImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().createSeq(uri));
	}

	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final RDFNode o )
	{
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), o.asNode()));
		return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createStatement(s, p, o));
	}

	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o )
	{
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createURI(o)));
		return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createStatement(s, p, o));
	}

	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o, final boolean wellFormed )
	{
		return createStatement(s, p, o, "", wellFormed);
	}

	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o, final String l )
	{
		return createStatement(s, p, o, l, false);
	}

	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o, final String l,
			final boolean wellFormed )
	{
		checkUpdate();
		checkCreate(new Triple(s.asNode(), p.asNode(), NodeFactory.createLiteral(o, l,
				wellFormed)));
		return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createStatement(s, p, o, l, wellFormed));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final boolean v )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final Calendar d )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(d));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final char v )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final double v )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final float v )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final int v )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final long v )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final Object value )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(value));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final Object value,
			final RDFDatatype dtype )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(value, dtype));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final Object value,
			final String typeURI )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(value, typeURI));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final String v )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(v));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final String lex,
			final RDFDatatype dtype )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(lex, dtype));
	}

	@Override
	public SecuredLiteral createTypedLiteral( final String lex,
			final String typeURI )
	{
		return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.createTypedLiteral(lex, typeURI));
	}

	@Override
	public Model difference( final Model model )
	{
		checkRead();
		if (canRead(Triple.ANY))
		{
			return holder.getBaseItem().difference(model);
		}
		else
		{
			return createCopy().difference(model);
		}
	}

	@Override
	public void enterCriticalSection( final boolean readLockRequested )
	{
		if (readLockRequested)
		{
			checkRead();
		}
		else
		{
			checkUpdate();
		}
		holder.getBaseItem().enterCriticalSection(readLockRequested);
	}

	@Override
	public Object executeInTransaction( final Command cmd )
	{
		return holder.getBaseItem().executeInTransaction(cmd);
	}

	@Override
	public String expandPrefix( final String prefixed )
	{
		checkRead();
		return holder.getBaseItem().expandPrefix(prefixed);
	}

	@Override
	public SecuredAlt getAlt( final Resource r )
	{
		checkRead();
		checkRead(new Triple(r.asNode(), RDF.type.asNode(), RDF.Alt.asNode()));
		return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getAlt(r));
	}

	@Override
	public SecuredAlt getAlt( final String uri )
	{
		checkRead();
		checkRead(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(),
				RDF.Alt.asNode()));
		return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.getAlt(uri));
	}

	@Override
	public SecuredResource getAnyReifiedStatement( final Statement s )
	{
		final RSIterator it = listReifiedStatements(s);
		if (it.hasNext())
		{
			try
			{
				return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(),
						it.nextRS());
			}
			finally
			{
				it.close();
			}
		}
		else
		{
			return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(),
					createReifiedStatement(s));
		}
	}

	@Override
	public SecuredBag getBag( final Resource r )
	{
		checkRead();
		checkRead(new Triple(r.asNode(), RDF.type.asNode(), RDF.Bag.asNode()));
		return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getBag(r));
	}

	@Override
	public SecuredBag getBag( final String uri )
	{
		checkRead();
		checkRead(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(),
				RDF.Bag.asNode()));
		return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.getBag(uri));
	}

	@Override
	public SecuredGraph getGraph()
	{
		return graph;
	}

	@Override
	public Lock getLock()
	{
		return holder.getBaseItem().getLock();
	}

	@Override
	public Map<String, String> getNsPrefixMap()
	{
		checkRead();
		return holder.getBaseItem().getNsPrefixMap();
	}

	@Override
	public String getNsPrefixURI( final String prefix )
	{
		checkRead();
		return holder.getBaseItem().getNsPrefixURI(prefix);
	}

	@Override
	public String getNsURIPrefix( final String uri )
	{
		checkRead();
		return holder.getBaseItem().getNsURIPrefix(uri);
	}

	@Override
	public SecuredStatement getProperty( final Resource s, final Property p )
	{
		final StmtIterator stmt = listStatements(s, p, (RDFNode) null);
		try
		{
			if (stmt.hasNext())
			{
				return SecuredStatementImpl.getInstance(holder.getSecuredItem(), stmt.next());
			}
			return null;
		}
		finally
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

	@Override
	public SecuredProperty getProperty( final String uri )
	{
		checkRead();
		return SecuredPropertyImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.getProperty(uri));
	}

	@Override
	public SecuredProperty getProperty( final String nameSpace,
			final String localName )
	{
		checkRead();
		return SecuredPropertyImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.getProperty(nameSpace, localName));
	}

	@Override
	public SecuredRDFNode getRDFNode( final Node n )
	{
		RDFNode rdfNode = null;
		if (n.isLiteral())
		{
			rdfNode = ResourceFactory.createTypedLiteral(
					n.getLiteralLexicalForm(), n.getLiteralDatatype());
		}
		else if (n.isURI())
		{
			rdfNode = ResourceFactory.createProperty(n.getURI());
		}
		else if (n.isBlank())
		{
			rdfNode = ResourceFactory.createResource(n.getBlankNodeId()
					.toString());
		}
		else
		{
			throw new IllegalArgumentException("Illegal SecNode type: " + n);
		}

		if (holder.getBaseItem().containsResource(rdfNode))
		{
			checkRead();
		}
		else
		{
			checkUpdate();
		}
		if (n.isLiteral())
		{
			return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
					.getRDFNode(n).asLiteral());
		}
		else
		{
			return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
					.getRDFNode(n).asResource());
		}
	}

	@Override
	public RDFReader getReader()
	{
		return holder.getBaseItem().getReader();
	}

	@Override
	public RDFReader getReader( final String lang )
	{
		return holder.getBaseItem().getReader(lang);
	}

	@Override
	public void resetRDFReaderF() {
		holder.getBaseItem().resetRDFReaderF();
	}

	@Override
	public String removeReader(String lang) throws IllegalArgumentException {
		return holder.getBaseItem().removeReader(lang);
	}
	
	@Override
	public SecuredStatement getRequiredProperty( final Resource s,
			final Property p )
	{
		checkRead();
		if (canRead(Triple.ANY))
		{
			return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
					.getRequiredProperty(s, p));
		}
		else
		{
			final SecuredStatementIterator si = listStatements(s, p,
					(RDFNode) null);
			try
			{
				if (si.hasNext())
				{
					return (SecuredStatement) si.next();
				}
				else
				{
					throw new PropertyNotFoundException(p);
				}
			}
			finally
			{
				si.close();
			}
		}
	}

	@Override
	public SecuredResource getResource( final String uri )
	{
		return createResource(uri);
	}

	@Override
	@Deprecated
	public SecuredResource getResource( final String uri, final ResourceF f )
	{
		return createResource(uri, f);
	}

	@Override
	public SecuredSeq getSeq( final Resource r )
	{
		checkRead();
		checkRead(new Triple(r.asNode(), RDF.type.asNode(), RDF.Seq.asNode()));
		return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getSeq(r));
	}

	@Override
	public SecuredSeq getSeq( final String uri )
	{
		checkRead();
		checkRead(new Triple(NodeFactory.createURI(uri), RDF.type.asNode(),
				RDF.Seq.asNode()));
		return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.getSeq(uri));
	}

	@Override
	public RDFWriter getWriter()
	{
		return holder.getBaseItem().getWriter();
	}

	@Override
	public RDFWriter getWriter( final String lang )
	{
		return holder.getBaseItem().getWriter(lang);
	}
	
	@Override
	public void resetRDFWriterF() {
		holder.getBaseItem().resetRDFWriterF();
	}

	@Override
	public String removeWriter(String lang) throws IllegalArgumentException {
		return holder.getBaseItem().removeWriter(lang);
	}

	@Override
	public boolean independent()
	{
		return false;
	}

	@Override
	public Model intersection( final Model model )
	{
		checkRead();
		if (!canRead(Triple.ANY))
		{
			return holder.getBaseItem().intersection(model);
		}
		else
		{
			return createCopy().intersection(model);
		}
	}

	@Override
	public boolean isClosed()
	{
		return holder.getBaseItem().isClosed();
	}

	@Override
	public boolean isEmpty()
	{
		checkRead();
		return holder.getBaseItem().isEmpty();
	}

	@Override
	public boolean isIsomorphicWith( final Model g )
	{
		checkRead();
		final boolean retval = holder.getBaseItem().isIsomorphicWith(g);
		if (retval && !canRead(Triple.ANY))
		{
			// in this case we have to check all the items in the graph to see
			// if the user can read
			// them all.
			final ExtendedIterator<Statement> stmtIter = holder.getBaseItem()
					.listStatements();
			try
			{
				while (stmtIter.hasNext())
				{
					if (!canRead(stmtIter.next().asTriple()))
					{
						return false;
					}
				}
			}
			finally
			{
				if (stmtIter != null)
				{
					stmtIter.close();
				}
			}
		}
		return retval;
	}

	@Override
	public boolean isReified( final Statement s )
	{
		checkRead();
		checkRead(s.asTriple());

		final RSIterator it = listReifiedStatements(s);
		try
		{
			return it.hasNext();
		}
		finally
		{
			it.close();
		}
	}

	@Override
	public void leaveCriticalSection()
	{
		holder.getBaseItem().leaveCriticalSection();
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate,
			final boolean object )
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listLiteralStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate, final char object )
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listLiteralStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate,
			final double object )
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listLiteralStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate, final float object )
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listLiteralStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate, final long object )
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listLiteralStatements(subject, predicate, object));
	}

	@Override
	public NsIterator listNameSpaces()
	{
		checkRead();
		return holder.getBaseItem().listNameSpaces();
	}

	@Override
	public SecuredNodeIterator<RDFNode> listObjects()
	{
		checkRead();
		ExtendedIterator<RDFNode> nIter = holder.getBaseItem().listObjects();
		if (!canRead(SecTriple.ANY))
		{
			nIter = nIter.filterKeep( new ObjectFilter());
		}
		return new SecuredNodeIterator<RDFNode>(holder.getSecuredItem(), nIter);
	}

	@Override
	public SecuredNodeIterator<RDFNode> listObjectsOfProperty( final Property p )
	{
		checkRead();
		ExtendedIterator<RDFNode> nIter = holder.getBaseItem().listObjectsOfProperty(p);
		if (!canRead(SecTriple.ANY))
		{
			nIter = nIter.filterKeep( new ObjectFilter(p));
		}
		return new SecuredNodeIterator<RDFNode>(holder.getSecuredItem(), nIter);
	}

	@Override
	public SecuredNodeIterator<RDFNode> listObjectsOfProperty( final Resource s,
			final Property p )
	{
		checkRead();
		ExtendedIterator<RDFNode> nIter = holder.getBaseItem().listObjectsOfProperty(s, p);
		if (!canRead(SecTriple.ANY))
		{
			nIter = nIter.filterKeep( new ObjectFilter(p));
		}
		return new SecuredNodeIterator<RDFNode>(holder.getSecuredItem(), nIter);
	}

	@Override
	public SecuredRSIterator listReifiedStatements()
	{
		checkRead();
		return new SecuredRSIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listReifiedStatements());
	}

	@Override
	public SecuredRSIterator listReifiedStatements( final Statement st )
	{
		checkRead();
		checkRead(st);
		return new SecuredRSIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listReifiedStatements(st));
	}

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p));
		}
		return new SecuredResIterator( holder.getSecuredItem(), rIter );
		
	}

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final boolean o )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator( holder.getSecuredItem(), rIter );
	}

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final char o )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator( holder.getSecuredItem(), rIter );
	}

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final double o )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator( holder.getSecuredItem(), rIter );
	}

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final float o )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator( holder.getSecuredItem(), rIter );
	}

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final long o )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator( holder.getSecuredItem(), rIter );
	}

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final Object o )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
		}
		return new SecuredResIterator( holder.getSecuredItem(), rIter );
	}

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final RDFNode o )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listResourcesWithProperty(p, o);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, o));
		}
		return new SecuredResIterator( holder.getSecuredItem(), rIter );
	}

	@Override
	public SecuredStatementIterator listStatements()
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listStatements());
	}

	@Override
	public SecuredStatementIterator listStatements( final Resource s,
			final Property p, final RDFNode o )
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listStatements(s, p, o));
	}

	@Override
	public SecuredStatementIterator listStatements( final Resource subject,
			final Property predicate, final String object )
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listStatements(subject, predicate, object));
	}

	@Override
	public SecuredStatementIterator listStatements( final Resource subject,
			final Property predicate, final String object, final String lang )
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listStatements(subject, predicate, object, lang));
	}

	@Override
	public SecuredStatementIterator listStatements( final Selector s )
	{
		checkRead();
		return new SecuredStatementIterator(holder.getSecuredItem(), holder.getBaseItem()
				.listStatements(s));
	}

	@Override
	public SecuredResIterator listSubjects()
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjects();
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter());
		}
		return new SecuredResIterator(holder.getSecuredItem(),rIter);
	}

	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjectsWithProperty(p);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p));
		}
		return new SecuredResIterator(holder.getSecuredItem(),rIter);
	}

	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p,
			final RDFNode o )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjectsWithProperty(p, o);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, o));
		}
		return new SecuredResIterator(holder.getSecuredItem(),rIter);
	}

	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p,
			final String o )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjectsWithProperty(p, o);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, ResourceFactory.createPlainLiteral(o)));
		}
		return new SecuredResIterator(holder.getSecuredItem(),rIter);
	}

	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p,
			final String o, final String l )
	{
		checkRead();
		ExtendedIterator<Resource> rIter = holder.getBaseItem().listSubjectsWithProperty(p, o, l);
		if (!canRead( SecTriple.ANY))
		{
			rIter=rIter.filterKeep( new ResourceFilter(p, ResourceFactory.createLangLiteral(o, l)));
		}
		return new SecuredResIterator(holder.getSecuredItem(),rIter);	
	}

	@Override
	public SecuredPrefixMapping lock()
	{
		checkUpdate();
		holder.getBaseItem().lock();
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel notifyEvent( final Object e )
	{
		holder.getBaseItem().notifyEvent(e);
		return holder.getSecuredItem();
	}

	@Override
	public String qnameFor( final String uri )
	{
		checkRead();
		return holder.getBaseItem().qnameFor(uri);
	}

	@Override
	public SecuredModel query( final Selector s )
	{
		checkRead();
		return SecuredModelImpl.getInstance(holder.getSecuredItem(),
				holder.getBaseItem().query(new SecuredSelector(holder.getSecuredItem(), s)));
	}

	@Override
	public SecuredModel read( final InputStream in, final String base )
	{
		checkUpdate();
		try
		{
			SecuredModelImpl.readerFactory.getReader().read(holder.getSecuredItem(), in, base);
			return holder.getSecuredItem();
		}
		catch (final JenaException e)
		{
			if ((e.getCause() != null)
					&& (e.getCause() instanceof AccessDeniedException))
			{
				throw (AccessDeniedException) e.getCause();
			}
			throw e;
		}
	}

	@Override
	public SecuredModel read( final InputStream in, final String base,
			final String lang )
	{
		checkUpdate();
		try
		{
			SecuredModelImpl.readerFactory.getReader(lang).read(holder.getSecuredItem(), in, base);
			return holder.getSecuredItem();
		}
		catch (final JenaException e)
		{
			if ((e.getCause() != null)
					&& (e.getCause() instanceof AccessDeniedException))
			{
				throw (AccessDeniedException) e.getCause();
			}
			throw e;
		}
	}

	@Override
	public SecuredModel read( final Reader reader, final String base )
	{
		checkUpdate();
		try
		{
			SecuredModelImpl.readerFactory.getReader().read(holder.getSecuredItem(), reader, base);
			return holder.getSecuredItem();
		}
		catch (final JenaException e)
		{
			if ((e.getCause() != null)
					&& (e.getCause() instanceof AccessDeniedException))
			{
				throw (AccessDeniedException) e.getCause();
			}
			throw e;
		}
	}

	@Override
	public SecuredModel read( final Reader reader, final String base,
			final String lang )
	{
		checkUpdate();
		try
		{
			SecuredModelImpl.readerFactory.getReader(lang).read(holder.getSecuredItem(), reader,
					base);
			return holder.getSecuredItem();
		}
		catch (final JenaException e)
		{
			if ((e.getCause() != null)
					&& (e.getCause() instanceof AccessDeniedException))
			{
				throw (AccessDeniedException) e.getCause();
			}
			throw e;
		}
	}

	@Override
	public SecuredModel read( final String url )
	{
		checkUpdate();
		try
		{
			SecuredModelImpl.readerFactory.getReader().read(holder.getSecuredItem(), url);
			return holder.getSecuredItem();
		}
		catch (final JenaException e)
		{
			if ((e.getCause() != null)
					&& (e.getCause() instanceof AccessDeniedException))
			{
				throw (AccessDeniedException) e.getCause();
			}
			throw e;
		}
	}

	@Override
	public SecuredModel read( final String url, final String lang )
	{
		checkUpdate();
		try
		{
			SecuredModelImpl.readerFactory.getReader(lang).read(holder.getSecuredItem(), url);
			return holder.getSecuredItem();
		}
		catch (final JenaException e)
		{
			if ((e.getCause() != null)
					&& (e.getCause() instanceof AccessDeniedException))
			{
				throw (AccessDeniedException) e.getCause();
			}
			throw e;
		}
	}

	@Override
	public SecuredModel read( final String url, final String base,
			final String lang )
	{
		try
		{
			final InputStream is = new URL(url).openStream();
			try
			{
				read(is, base, lang);
			}
			finally
			{
				if (null != is)
				{
					is.close();
				}
			}
		}
		catch (final IOException e)
		{
			throw new WrappedIOException(e);
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel register( final ModelChangedListener listener )
	{
		checkRead();
		if (!listeners.containsKey(listener))
		{
			final SecuredModelChangedListener secL = new SecuredModelChangedListener(
					listener);
			listeners.put(listener, secL);
			holder.getBaseItem().register(secL);
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove( final List<Statement> statements )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			for (final Statement s : statements)
			{
				checkDelete(s.asTriple());
			}
		}
		holder.getBaseItem().remove(statements);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove( final Model m )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			final StmtIterator iter = m.listStatements();
			try
			{
				while (iter.hasNext())
				{
					final Statement stmt = iter.next();
					checkDelete(stmt);
				}
			}
			finally
			{
				iter.close();
			}
		}
		holder.getBaseItem().remove(m);

		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove( final Resource s, final Property p, final RDFNode o )
	{
		checkUpdate();
		checkDelete(new Triple(s.asNode(), p.asNode(), o.asNode()));
		holder.getBaseItem().remove(s, p, o);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove( final Statement s )
	{
		checkUpdate();
		checkDelete(wildCardTriple(s));
		holder.getBaseItem().remove(s);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove( final Statement[] statements )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			for (final Statement s : statements)
			{
				checkDelete(s.asTriple());
			}
		}
		holder.getBaseItem().remove(statements);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel remove( final StmtIterator iter )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			final List<Triple> lst = new ArrayList<Triple>();
			try
			{
				while (iter.hasNext())
				{
					final Statement s = iter.next();
					checkDelete(s.asTriple());
					lst.add(s.asTriple());
				}
				final Model m = ModelFactory
						.createModelForGraph(new CollectionGraph(lst));
				holder.getBaseItem().remove(m.listStatements());
			}
			finally
			{
				iter.close();
			}
		}
		else
		{
			holder.getBaseItem().remove(iter);
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel removeAll()
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			final StmtIterator iter = holder.getBaseItem().listStatements();
			try
			{
				while (iter.hasNext())
				{
					checkDelete(iter.next());
				}
			}
			finally
			{
				iter.close();
			}
		}
		holder.getBaseItem().removeAll();
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel removeAll( final Resource s, final Property p,
			final RDFNode r )
	{
		checkUpdate();
		if (!canDelete(new Triple(wildCardNode(s), wildCardNode(p),
				wildCardNode(r))))
		{
			final StmtIterator iter = holder.getBaseItem().listStatements(s, p,
					r);
			try
			{
				while (iter.hasNext())
				{
					checkDelete(iter.next());
				}
			}
			finally
			{
				iter.close();
			}
		}
		holder.getBaseItem().removeAll(s, p, r);
		return holder.getSecuredItem();
	}

	@Override
	public void removeAllReifications( final Statement s )
	{
		checkUpdate();
		if (canDelete(new Triple(Node.ANY, RDF.subject.asNode(),
				wildCardNode(s.getSubject())))
				&& canDelete(new Triple(Node.ANY, RDF.predicate.asNode(),
						wildCardNode(s.getPredicate())))
				&& canDelete(new Triple(Node.ANY, RDF.object.asNode(),
						wildCardNode(s.getObject()))))
		{
			holder.getBaseItem().removeAllReifications(s);
		}
		else
		{
			final RSIterator iter = holder.getBaseItem().listReifiedStatements(
					s);
			try
			{
				while (iter.hasNext())
				{
					final ReifiedStatement rs = iter.next();
					checkDelete(new Triple(rs.asNode(), RDF.subject.asNode(),
							wildCardNode(s.getSubject())));
					checkDelete(new Triple(rs.asNode(), RDF.predicate.asNode(),
							wildCardNode(s.getPredicate())));
					checkDelete(new Triple(rs.asNode(), RDF.object.asNode(),
							wildCardNode(s.getObject())));
				}
				holder.getBaseItem().removeAllReifications(s);
			}
			finally
			{
				iter.close();
			}

		}
	}

	@Override
	public SecuredPrefixMapping removeNsPrefix( final String prefix )
	{
		checkUpdate();
		holder.getBaseItem().removeNsPrefix(prefix);
		return holder.getSecuredItem();
	}

	@Override
	public void removeReification( final ReifiedStatement rs )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			final StmtIterator stmtIter = rs.listProperties();
			try
			{
				while (stmtIter.hasNext())
				{
					checkDelete(stmtIter.next().asTriple());
				}
			}
			finally
			{
				stmtIter.close();
			}
		}
		holder.getBaseItem().removeReification(rs);
	}

	@Override
	public boolean samePrefixMappingAs( final PrefixMapping other )
	{
		checkRead();
		return holder.getBaseItem().samePrefixMappingAs(other);
	}

	@Override
	public SecuredPrefixMapping setNsPrefix( final String prefix,
			final String uri )
	{
		checkUpdate();
		holder.getBaseItem().setNsPrefix(prefix, uri);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredPrefixMapping setNsPrefixes( final Map<String, String> map )
	{
		checkUpdate();
		holder.getBaseItem().setNsPrefixes(map);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredPrefixMapping setNsPrefixes( final PrefixMapping other )
	{
		checkUpdate();
		holder.getBaseItem().setNsPrefixes(other);
		return holder.getSecuredItem();
	}

	@Override
	public String setReaderClassName( final String lang, final String className )
	{
		checkUpdate();
		return holder.getBaseItem().setReaderClassName(lang, className);
	}

	@Override
	public String setWriterClassName( final String lang, final String className )
	{
		checkUpdate();
		return holder.getBaseItem().setWriterClassName(lang, className);
	}

	@Override
	public String shortForm( final String uri )
	{
		checkRead();
		return holder.getBaseItem().shortForm(uri);
	}

	@Override
	public long size()
	{
		checkRead();
		return holder.getBaseItem().size();
	}

	@Override
	public boolean supportsSetOperations()
	{
		return holder.getBaseItem().supportsTransactions();
	}

	@Override
	public boolean supportsTransactions()
	{
		return holder.getBaseItem().supportsTransactions();
	}

	@Override
	public Model union( final Model model )
	{
		checkRead();
		if (canRead(Triple.ANY))
		{
			return holder.getBaseItem().union(model);
		}
		else
		{
			return createCopy().union(model);
		}
	}

	@Override
	public SecuredModel unregister( final ModelChangedListener listener )
	{
		if (listeners.containsKey(listener))
		{
			final SecuredModelChangedListener secL = listeners.get(listener);
			holder.getBaseItem().unregister(secL);
			listeners.remove(listener);
		}
		return holder.getSecuredItem();
	}

	private Node wildCardNode( final RDFNode node )
	{
		return node == null ? Node.ANY : node.asNode();
	}

	private Triple wildCardTriple( final Statement s )
	{
		return new Triple(wildCardNode(s.getSubject()),
				wildCardNode(s.getPredicate()), wildCardNode(s.getObject()));
	}

	@Override
	public SecuredPrefixMapping withDefaultMappings( final PrefixMapping map )
	{
		checkUpdate();
		holder.getBaseItem().withDefaultMappings(map);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredResource wrapAsResource( final Node n )
	{
		return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem()
				.wrapAsResource(n));
	}

	@Override
	public SecuredModel write( final OutputStream out )
	{
		checkRead();
		if (canRead(Triple.ANY))
		{
			holder.getBaseItem().write(out);
		}
		else
		{
			getWriter().write(holder.getSecuredItem(), out, "");
		}
		return holder.getSecuredItem();

	}

	@Override
	public SecuredModel write( final OutputStream out, final String lang )
	{
		checkRead();
		if (canRead(Triple.ANY))
		{
			holder.getBaseItem().write(out, lang);
		}
		else
		{
			getWriter(lang).write(holder.getSecuredItem(), out, "");
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel write( final OutputStream out, final String lang,
			final String base )
	{
		checkRead();
		if (canRead(Triple.ANY))
		{
			holder.getBaseItem().write(out, lang, base);
		}
		else
		{
			getWriter(lang).write(holder.getSecuredItem(), out, base);
		}
		return holder.getSecuredItem();

	}

	@Override
	public SecuredModel write( final Writer writer )
	{
		checkRead();
		if (canRead(Triple.ANY))
		{
			holder.getBaseItem().write(writer);
		}
		else
		{
			getWriter().write(holder.getSecuredItem(), writer, "");
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel write( final Writer writer, final String lang )
	{
		checkRead();
		if (canRead(Triple.ANY))
		{
			holder.getBaseItem().write(writer, lang);
		}
		else
		{
			getWriter(lang).write(holder.getSecuredItem(), writer, "");
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredModel write( final Writer writer, final String lang,
			final String base )
	{
		checkRead();
		if (canRead(Triple.ANY))
		{
			holder.getBaseItem().write(writer, lang, base);
		}
		else
		{
			getWriter(lang).write(holder.getSecuredItem(), writer, base);
		}
		return holder.getSecuredItem();

	}
	
	private class ResourceFilter extends Filter<Resource> {
		Property p;
		RDFNode o;
		
		ResourceFilter() {
			this(null, null);
		}

		ResourceFilter( Property p)
		{
			this(p,null);
		}
		
		ResourceFilter( Property p, RDFNode o)
		{
			this.p = p;
			this.o = o;
		}
		
		@Override
		public boolean accept(Resource s) {
			StmtIterator iter = listStatements(s, p, o);
			try {
				return iter.hasNext();
			}
			finally {
				iter.close();
			}
		}
		
	}
	
	private class ObjectFilter extends Filter<RDFNode> {
		Resource s;
		Property p;

		ObjectFilter(  ) {
			this(null,null);
		}
		
		ObjectFilter( Property p )
		{
			this(null, p );
		}
		
		ObjectFilter( Resource s, Property p)
		{
			this.s = s;
			this.p = p;
		}
		
		@Override
		public boolean accept(RDFNode o) {
			StmtIterator iter = listStatements(s, p, o);
			try {
				return iter.hasNext();
			}
			finally {
				iter.close();
			}
		}
	}

}
