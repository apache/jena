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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Container;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.WrappedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemInvoker;
import org.apache.jena.security.model.SecuredContainer;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.utils.ContainerFilter;
import org.apache.jena.security.utils.PermStatementFilter;

/**
 * Implementation of SecuredContainer to be used by a SecuredItemInvoker proxy.
 */
public class SecuredContainerImpl extends SecuredResourceImpl implements
		SecuredContainer
{
	/**
	 * Constructor
	 * 
	 * @param securedModel
	 *            the Secured Model to use.
	 * @param container
	 *            The container to secure.
	 * @return The SecuredResource
	 */
	public static SecuredContainer getInstance(
			final SecuredModel securedModel, final Container container )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (container == null)
		{
			throw new IllegalArgumentException("Container may not be null");
		}

		// check that resource has a securedModel.
		Container goodContainer = container;
		if (goodContainer.getModel() == null)
		{
			container.asNode();
			goodContainer = securedModel.createBag();
		}

		final ItemHolder<Container, SecuredContainer> holder = new ItemHolder<Container, SecuredContainer>(
				goodContainer);

		final SecuredContainerImpl checker = new SecuredContainerImpl(
				securedModel, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (goodContainer instanceof SecuredContainer)
		{
			if (checker.isEquivalent((SecuredContainer) goodContainer))
			{
				return (SecuredContainer) goodContainer;
			}
		}

		return holder.setSecuredItem(new SecuredItemInvoker(container
				.getClass(), checker));

	}

	// the item holder that contains this SecuredContainer.
	private final ItemHolder<? extends Container, ? extends SecuredContainer> holder;

	/**
	 * Constructor
	 * 
	 * @param securedModel
	 *            the Secured Model to use.
	 * @param holder
	 *            The item holder that will contain this SecuredContainer
	 */
	protected SecuredContainerImpl(
			final SecuredModel securedModel,
			final ItemHolder<? extends Container, ? extends SecuredContainer> holder )
	{
		super(securedModel, holder);
		this.holder = holder;
		// listener=new ChangeListener();
		// holder.getBaseItem().getModel().register(listener);
	}

	protected RDFNode asObject( Object o )
    { 
		 return o instanceof RDFNode ? (RDFNode) o : ResourceFactory.createTypedLiteral( o ); 
    }
	
	protected RDFNode asLiteral( String o, String l )
	{
		return holder.getBaseItem().getModel().createLiteral(o, l);
	}
	
	@Override
	public SecuredContainer add( final boolean o )
	{
		return add( asObject( o ));
	}

	@Override
	public SecuredContainer add( final char o )
	{
		return add( asObject( o ));
	}

	@Override
	public SecuredContainer add( final double o )
	{
		return add( asObject( o ));
	}

	@Override
	public SecuredContainer add( final float o )
	{
		return add( asObject( o ));
	}

	@Override
	public SecuredContainer add( final long o )
	{
		return add( asObject( o ));
	}

	@Override
	public SecuredContainer add( final Object o )
	{
		return add( asObject( o ));
	}

	@Override
	public SecuredContainer add( final RDFNode o )
	{
		checkUpdate();
		final int pos = holder.getBaseItem().size();
		checkAdd(pos, o.asNode());
		holder.getBaseItem().add(o);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredContainer add( final String o )
	{
		return add( asLiteral( o, "" ));
	}

	@Override
	public SecuredContainer add( final String o, final String l )
	{
		return add( asLiteral( o, l));
	}

	protected void checkAdd( final int pos, final Literal literal )
	{
		checkAdd(pos, literal.asNode());
	}

	protected void checkAdd( final int pos, final Node node )
	{
		checkCreate(new Triple(holder.getBaseItem().asNode(), RDF.li(pos)
				.asNode(), node));
	}

	@Override
	public boolean contains( final boolean o )
	{
		return contains( asObject( o ) );
	}

	@Override
	public boolean contains( final char o )
	{
		return contains( asObject( o ) );
	}

	@Override
	public boolean contains( final double o )
	{
		return contains( asObject( o ) );
	}

	@Override
	public boolean contains( final float o )
	{
		return contains( asObject( o ) );
	}

	@Override
	public boolean contains( final long o )
	{
		return contains( asObject( o ) );
	}

	@Override
	public boolean contains( final Object o )
	{
		return contains( asObject( o ) );
	}

	@Override
	public boolean contains( final RDFNode o )
	{
		// iterator check reads
		final SecuredNodeIterator<RDFNode> iter = iterator();
		while (iter.hasNext())
		{
			if (iter.next().asNode().equals(o.asNode()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains( final String o )
	{
		return contains( asLiteral( o, "" ));
	}

	@Override
	public boolean contains( final String o, final String l )
	{
		return contains( asLiteral( o, l ));
	}

	protected int getAddIndex()
	{
		int pos = -1;
		final ExtendedIterator<Statement> iter = holder.getBaseItem()
				.listProperties();
		try
		{
			while (iter.hasNext())
			{
				pos = Math.max(pos, getIndex(iter.next().getPredicate()));
			}
		}
		finally
		{
			iter.close();
		}
		return pos + 1;
	}

	protected static int getIndex( final Property p )
	{
		if (p.getNameSpace().equals(RDF.getURI())
				&& p.getLocalName().startsWith("_"))
		{
			try
			{
				return Integer.parseInt(p.getLocalName().substring(1));
			}
			catch (final NumberFormatException e)
			{
				// acceptable;
			}
		}
		return -1;
	}

	protected ExtendedIterator<Statement> getStatementIterator(
			final Action perm )
	{
		return holder.getBaseItem().listProperties()
				.filterKeep(new ContainerFilter())
				.filterKeep(new PermStatementFilter(perm, this));
	}

	protected ExtendedIterator<Statement> getStatementIterator(
			final Set<Action> perm )
	{
		return holder.getBaseItem().listProperties()
				.filterKeep(new ContainerFilter())
				.filterKeep(new PermStatementFilter(perm, this));
	}

	@Override
	public boolean isAlt()
	{
		return holder.getBaseItem().isAlt();
	}

	@Override
	public boolean isBag()
	{
		return holder.getBaseItem().isBag();
	}

	@Override
	public boolean isSeq()
	{
		return holder.getBaseItem().isSeq();
	}

	@Override
	public SecuredNodeIterator<RDFNode> iterator()
	{
		// listProperties calls checkRead();
        SecuredStatementIterator iter = listProperties(); 
        try {
	        SortedSet<Statement> result = new TreeSet<Statement>( new ContainerComparator() );
	        while (iter.hasNext()) {
	        	Statement stmt = iter.next();
	        	if (stmt.getPredicate().getOrdinal() > 0)
	        	{
	        		result.add( stmt );
	        	}
	        }
	        return new SecuredNodeIterator<RDFNode>(getModel(), new StatementRemovingIterator(result.iterator()).mapWith( new NodeMap() ) );
        }
        finally {
        	iter.close();
        }
	}

	@Override
	public SecuredNodeIterator<RDFNode> iterator( final Set<Action> perms )
	{
		checkRead();
		final Set<Action> permsCopy = new HashSet<Action>(perms);
		permsCopy.add(Action.Read);
		final ExtendedIterator<RDFNode> ni = getStatementIterator(perms)
				.mapWith(new Map1<Statement, RDFNode>() {

					@Override
					public RDFNode map1( final Statement o )
					{
						return o.getObject();
					}
				});
		return new SecuredNodeIterator<RDFNode>(getModel(), ni);

	}

	@Override
	public SecuredContainer remove( final Statement s )
	{
		checkUpdate();
		checkDelete(s.asTriple());
		holder.getBaseItem().remove(s);
		return holder.getSecuredItem();
	}

	@Override
	public int size()
	{
		checkRead();
		return holder.getBaseItem().size();
	}
	/*
	 * private synchronized void resetIndexes()
	 * {
	 * indexes.clear();
	 * }
	 */
	/*
	/**
	 * find the position of i in the array
	 * 
	 * @param i
	 * @return the position or x<0 if not found.
	 */
	/*
	 * protected int mapValue( int i )
	 * {
	 * rebuildIndex();
	 * return Collections.binarySearch( indexes, i );
	 * }
	 * 
	 * // return the value at position i
	 * protected int unmapValue( int i )
	 * {
	 * return indexes.get(i);
	 * }
	 * 
	 * 
	 * private synchronized void rebuildIndex()
	 * {
	 * if (indexes.isEmpty())
	 * {
	 * ExtendedIterator<Statement> iter = getStatementIterator( Action.Read );
	 * try {
	 * while (iter.hasNext())
	 * {
	 * indexes.add( getIndex( iter.next().getPredicate() ) );
	 * }
	 * }
	 * finally {
	 * iter.close();
	 * }
	 * Collections.sort(indexes);
	 * }
	 * }
	 * 
	 * private class ChangeListener implements ModelChangedListener
	 * {
	 * 
	 * private void checkStatement( Statement s )
	 * {
	 * if (indexes != null && s.getSubject().equals( holder.getBaseItem()))
	 * {
	 * resetIndexes();
	 * }
	 * }
	 * 
	 * private void checkStatements( Iterator<Statement> iter )
	 * {
	 * while( indexes != null && iter.hasNext())
	 * {
	 * checkStatement( iter.next() );
	 * }
	 * }
	 * 
	 * @Override
	 * public void addedStatement( Statement s )
	 * {
	 * checkStatement( s );
	 * }
	 * 
	 * @Override
	 * public void addedStatements( Statement[] statements )
	 * {
	 * checkStatements( Arrays.asList(statements).iterator() );
	 * }
	 * 
	 * @Override
	 * public void addedStatements( List<Statement> statements )
	 * {
	 * checkStatements( statements.iterator() );
	 * }
	 * 
	 * @Override
	 * public void addedStatements( StmtIterator statements )
	 * {
	 * try {
	 * checkStatements( statements );
	 * }
	 * finally {
	 * statements.close();
	 * }
	 * }
	 * 
	 * @Override
	 * public void addedStatements( Model baseModel )
	 * {
	 * addedStatements( baseModel.listStatements() );
	 * }
	 * 
	 * @Override
	 * public void removedStatement( Statement s )
	 * {
	 * checkStatement( s );
	 * }
	 * 
	 * @Override
	 * public void removedStatements( Statement[] statements )
	 * {
	 * checkStatements( Arrays.asList(statements).iterator() );
	 * }
	 * 
	 * @Override
	 * public void removedStatements( List<Statement> statements )
	 * {
	 * checkStatements( statements.iterator() );
	 * }
	 * 
	 * @Override
	 * public void removedStatements( StmtIterator statements )
	 * {
	 * try {
	 * checkStatements( statements );
	 * }
	 * finally {
	 * statements.close();
	 * }
	 * }
	 * 
	 * @Override
	 * public void removedStatements( Model baseModel )
	 * {
	 * removedStatements( baseModel.listStatements() );
	 * }
	 * 
	 * @Override
	 * public void notifyEvent( Model baseModel, Object event )
	 * {
	 * // do nothing
	 * }
	 * 
	 * }
	 */
	
	static class NodeMap implements Map1<Statement,RDFNode>
	{

		@Override
		public RDFNode map1( Statement o )
		{
			return o.getObject();
		}
		
	}
	
	static class ContainerComparator implements Comparator<Statement>
	{

		@Override
		public int compare( Statement arg0, Statement arg1 )
		{
			return Integer.valueOf(arg0.getPredicate().getOrdinal()).compareTo( arg1.getPredicate().getOrdinal());
		}
		
	}
	
	static class StatementRemovingIterator extends WrappedIterator<Statement>
	{
		private Statement stmt;
		
		public StatementRemovingIterator( Iterator<? extends Statement> base )
		{
			super(base);
		}

		@Override
		public Statement next()
		{
			stmt = super.next();
			return stmt;
		}

		@Override
		public void remove()
		{
			stmt.remove();
			super.remove();
		}
		
		
	}
}
