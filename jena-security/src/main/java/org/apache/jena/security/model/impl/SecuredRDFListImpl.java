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
import com.hp.hpl.jena.rdf.model.EmptyListException;
import com.hp.hpl.jena.rdf.model.EmptyListUpdateException;
import com.hp.hpl.jena.rdf.model.InvalidListException;
import com.hp.hpl.jena.rdf.model.ListIndexException;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.WrappedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemImpl;
import org.apache.jena.security.impl.SecuredItemInvoker;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.model.SecuredRDFList;
import org.apache.jena.security.model.SecuredRDFNode;
import org.apache.jena.security.utils.RDFListIterator;
import org.apache.jena.security.utils.RDFListSecFilter;

public class SecuredRDFListImpl extends SecuredResourceImpl implements
		SecuredRDFList
{
	// called plain node but still returns a secured node
	private class PlainNodeMap implements Map1<RDFList, RDFNode>
	{

		@Override
		public RDFNode map1( final RDFList o )
		{
			return SecuredRDFNodeImpl.getInstance(getModel(), o
					.getRequiredProperty(listFirst()).getObject());
		}

	}

	private class SecuredListMap implements Map1<RDFList, SecuredRDFList>
	{

		@Override
		public SecuredRDFList map1( final RDFList o )
		{
			return SecuredRDFListImpl.getInstance(getModel(), o);
		}

	}

	private class SecuredNodeMap implements Map1<RDFList, SecuredRDFNode>
	{

		private Property p;
		public SecuredNodeMap(Property p)
		{
			this.p=p;
		}
		
		@Override
		public SecuredRDFNode map1( final RDFList o )
		{
			return SecuredRDFNodeImpl.getInstance(getModel(), o
					.getRequiredProperty(p).getObject());
		}

	}

	/*
	 * private class SecuredRDFListIterator implements Iterator<SecuredRDFList>
	 * {
	 * private SecuredRDFList current;
	 * private Boolean found;
	 * private final Set<Action> restrictions;
	 * 
	 * private SecuredRDFListIterator( final Action restriction )
	 * {
	 * this(SecurityEvaluator.Util.asSet(new Action[] { restriction }));
	 * }
	 * 
	 * private SecuredRDFListIterator( final Set<Action> restrictions )
	 * {
	 * this.current = SecuredRDFListImpl.this.holder.getSecuredItem();
	 * this.restrictions = restrictions;
	 * }
	 * 
	 * private boolean checkCandidate()
	 * {
	 * if (!endOfList())
	 * {
	 * final SecNode candidate = current.getRequiredProperty(listFirst())
	 * .getObject().asNode();
	 * return getSecurityEvaluator().evaluate(
	 * restrictions,
	 * getModelNode(),
	 * new SecurityEvaluator.SecTriple(SecuredItemImpl
	 * .convert(current.asNode()), SecuredItemImpl
	 * .convert(RDF.first.asNode()), SecuredItemImpl
	 * .convert(candidate)));
	 * }
	 * return false;
	 * }
	 * 
	 * private boolean endOfList()
	 * {
	 * return current.equals(listNil());
	 * }
	 * 
	 * @Override
	 * public boolean hasNext()
	 * {
	 * if ((found == null) && !endOfList())
	 * {
	 * found = checkCandidate();
	 * while (!found && !endOfList())
	 * {
	 * incrementCurrent();
	 * found = checkCandidate();
	 * }
	 * }
	 * return found == null ? false : found;
	 * }
	 * 
	 * private void incrementCurrent()
	 * {
	 * if (!endOfList())
	 * {
	 * current = (SecuredRDFList) current
	 * .getRequiredProperty(listRest()).getResource()
	 * .as(RDFList.class);
	 * }
	 * }
	 * 
	 * @Override
	 * public SecuredRDFList next()
	 * {
	 * if (hasNext())
	 * {
	 * found = null;
	 * final SecuredRDFList retval = current;
	 * incrementCurrent();
	 * return retval;
	 * }
	 * throw new NoSuchElementException();
	 * }
	 * 
	 * @Override
	 * public void remove()
	 * {
	 * throw new UnsupportedOperationException();
	 * }
	 * 
	 * }
	 */
	/**
	 * Get an instance of SecuredProperty
	 * 
	 * @param securedModel
	 *            the Secured Model to use.
	 * @param rdfList
	 *            The rdfList to secure
	 * @return The SecuredProperty
	 */
	public static <T extends RDFList> SecuredRDFList getInstance( final SecuredModel securedModel,
			final T rdfList )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (rdfList == null)
		{
			throw new IllegalArgumentException("RDFList may not be null");
		}

		// check that property has a securedModel.
		RDFList goodList = rdfList;
		if (goodList.getModel() == null)
		{
			goodList = securedModel.createList(rdfList.asJavaList().iterator());
		}

		final ItemHolder<RDFList, SecuredRDFList> holder = new ItemHolder<RDFList, SecuredRDFList>(
				goodList);
		final SecuredRDFListImpl checker = new SecuredRDFListImpl(securedModel,
				holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (goodList instanceof SecuredRDFList)
		{
			if (checker.isEquivalent((SecuredRDFList) goodList))
			{
				return (SecuredRDFList) goodList;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(rdfList.getClass(),
				checker));
	}

	/** Error message if validity check fails */
	protected String m_errorMsg = null;

	/** Pointer to the node that is the tail of the list */
	protected RDFList m_tail = null;

	/** The URI for the 'first' property in this list */
	protected Property m_listFirst = RDF.first;

	/** The URI for the 'rest' property in this list */
	protected Property m_listRest = RDF.rest;

	/** The URI for the 'nil' Resource in this list */
	protected Resource m_listNil = RDF.nil;

	/** The URI for the rdf:type of this list */
	protected Resource m_listType = RDF.List;

	private final ItemHolder<RDFList, SecuredRDFList> holder;

	protected SecuredRDFListImpl( final SecuredModel securedModel,
			final ItemHolder<RDFList, SecuredRDFList> holder )
	{
		super(securedModel, holder);
		this.holder = holder;
	}

	@Override
	public void add( final RDFNode value )
	{
		checkUpdate();
		checkCreateNewList(value, listNil());
		holder.getBaseItem().add(value);
	}

	@Override
	public SecuredRDFList append( final Iterator<? extends RDFNode> nodes )
	{
		SecuredRDFList copy = copy();
		if (nodes.hasNext())
		{
			if (((RDFList)copy.getBaseItem()).size()>0)
//			if (copy.size() > 0)
			{
				copy.concatenate(copy.getModel().createList(nodes));
			}
			else
			{
				copy = copy.getModel().createList(nodes);
			}
		}
		return copy;
	}

	@Override
	public RDFList append( final RDFList list )
	{
		if (holder.getBaseItem().isEmpty())
		{
			return list.size() == 0 ? ModelFactory.createDefaultModel()
					.createList() : list.copy();
		}
		else
		{
			final RDFList copy = copy();
			if (list.size() > 0)
			{
				copy.concatenate(list.copy());
			}
			return copy;
		}
	}

	@Override
	public void apply( final ApplyFn fn )
	{
		// iterator() checks Read
		final ExtendedIterator<RDFNode> i = iterator();
		try
		{
			while (i.hasNext())
			{
				fn.apply(i.next());
			}
		}
		finally
		{
			i.close();
		}
	}

	@Override
	public void apply( final Set<Action> perms, final ApplyFn fn )
	{
		// iterator() checks Read
		final ExtendedIterator<RDFNode> i = iterator(perms);
		try
		{
			while (i.hasNext())
			{
				fn.apply(i.next());
			}
		}
		finally
		{
			i.close();
		}
	}

	@Override
	public List<RDFNode> asJavaList()
	{
		// iterator() checks Read
		return iterator().toList();
	}

	/**
	 * Removes val from underlying list.
	 * 
	 * @param val
	 * @return the modified RDFList.
	 */
	private RDFList baseRemove( final RDFList val )
	{

		RDFList prev = null;
		RDFList cell = holder.getBaseItem();
		final boolean searching = true;

		while (searching && !cell.isEmpty())
		{
			if (cell.equals(val))
			{
				// found the value to be removed
				final RDFList tail = cell.getTail();
				if (prev != null)
				{
					prev.setTail(tail);
				}

				cell.removeProperties();

				// return this unless we have removed the head element
				return (prev == null) ? tail : this;
			}
			else
			{
				// not found yet
				prev = cell;
				cell = cell.getTail();
			}
		}

		// not found
		return this;
	}

	private void checkCreateNewList( final RDFNode value, final Resource tail )
	{
		checkCreate(new SecurityEvaluator.SecTriple(
				SecurityEvaluator.SecNode.FUTURE,
				SecuredItemImpl.convert(listFirst().asNode()),
				SecuredItemImpl.convert(value.asNode())));
		checkCreate(new SecurityEvaluator.SecTriple(
				SecurityEvaluator.SecNode.FUTURE,
				SecuredItemImpl.convert(listRest().asNode()),
				SecuredItemImpl.convert(tail.asNode())));
	}

	private Set<Statement> collectStatements( final Set<Action> actions )
	{
		final Set<Statement> stmts = new HashSet<Statement>();
		final ExtendedIterator<RDFList> iter = WrappedIterator.create(
				new RDFListIterator(holder.getBaseItem())).filterKeep(
				new RDFListSecFilter<RDFList>(this, actions));
		try
		{
			while (iter.hasNext())
			{
				stmts.addAll(iter.next().listProperties().toSet());
			}
			return stmts;
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public void concatenate( final Iterator<? extends RDFNode> nodes )
	{
		checkUpdate();
		if (holder.getBaseItem().isEmpty())
		{
			// concatenating list onto the empty list is an error
			throw new EmptyListUpdateException(
					"Tried to concatenate onto the empty list");
		}
		else
		{
			final org.apache.jena.security.SecurityEvaluator.SecNode p = SecuredItemImpl
					.convert(listFirst().asNode());
			org.apache.jena.security.SecurityEvaluator.SecTriple t = new org.apache.jena.security.SecurityEvaluator.SecTriple(
					org.apache.jena.security.SecurityEvaluator.SecNode.FUTURE,
					p, org.apache.jena.security.SecurityEvaluator.SecNode.ANY);
			if (!canCreate(t))
			{
				final List<RDFNode> list = new ArrayList<RDFNode>();
				while (nodes.hasNext())
				{
					final RDFNode n = nodes.next();
					t = new org.apache.jena.security.SecurityEvaluator.SecTriple(
							org.apache.jena.security.SecurityEvaluator.SecNode.FUTURE,
							p, SecuredItemImpl.convert(n.asNode()));
					checkCreate(t);
					list.add(n);
				}
				holder.getBaseItem().concatenate(list.iterator());

			}
			else
			{
				holder.getBaseItem().concatenate(nodes);
			}
		}
	}

	@Override
	public void concatenate( final RDFList list )
	{
		checkUpdate();
		if (holder.getBaseItem().isEmpty())
		{
			// concatenating list onto the empty list is an error
			throw new EmptyListUpdateException(
					"Tried to concatenate onto the empty list");
		}
		else
		{
			final org.apache.jena.security.SecurityEvaluator.SecNode p = SecuredItemImpl
					.convert(listFirst().asNode());
			org.apache.jena.security.SecurityEvaluator.SecTriple t = new org.apache.jena.security.SecurityEvaluator.SecTriple(
					org.apache.jena.security.SecurityEvaluator.SecNode.FUTURE,
					p, org.apache.jena.security.SecurityEvaluator.SecNode.ANY);
			if (!canCreate(t))
			{
				final ExtendedIterator<RDFNode> iter = list.iterator();
				try
				{
					while (iter.hasNext())
					{
						t = new org.apache.jena.security.SecurityEvaluator.SecTriple(
								org.apache.jena.security.SecurityEvaluator.SecNode.FUTURE,
								p, SecuredItemImpl
										.convert(iter.next().asNode()));
						checkCreate(t);
					}
				}
				finally
				{
					iter.close();
				}
			}
			holder.getBaseItem().concatenate(list);
		}
	}

	@Override
	public SecuredRDFList cons( final RDFNode value )
	{
		checkUpdate();
		checkCreateNewList(value, holder.getBaseItem());
		return SecuredRDFListImpl.getInstance(getModel(), holder.getBaseItem()
				.cons(value));
	}

	@Override
	public boolean contains( final RDFNode value )
	{
		// iterator() checks Read
		final ExtendedIterator<RDFNode> iter = iterator();
		try
		{
			while (iter.hasNext())
			{
				if (value.equals(iter.next()))
				{
					return true;
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
	public SecuredRDFList copy()
	{
		SecuredRDFList retval = null;
		if (canRead())
		{
			final ExtendedIterator<RDFNode> iter = getSecuredRDFListIterator(Action.Read)
					.mapWith( new Map1<RDFList, RDFNode>()
					{

						@Override
						public RDFNode map1( final RDFList o )
						{
							return  o.getRequiredProperty(listFirst()).getObject();
						}

					});
			if (iter.hasNext())
			{
				retval = getModel().createList(iter);
			}
			else
			{
				retval = getModel().createList();
			}
		}
		else
		{
			retval = getModel().createList();
		}
		return retval;
	}

	@Override
	public SecuredRDFNode get( final int i )
	{
		checkRead();
		final ExtendedIterator<SecuredRDFNode> iter = getSecuredRDFListIterator(
				Action.Read).mapWith(new SecuredNodeMap(listFirst()));
		int idx = 0;
		try
		{
			while (iter.hasNext())
			{
				if (i == idx)
				{
					return iter.next();
				}
				else
				{
					idx++;
					iter.next();
				}

			}
			throw new ListIndexException();
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public SecuredRDFNode getHead()
	{
		checkRead();
		Statement s = holder.getBaseItem().getRequiredProperty( listFirst() );
		checkRead( s );
		return SecuredRDFNodeImpl.getInstance(getModel(), s.getObject());
	}

	private ExtendedIterator<RDFList> getSecuredRDFListIterator(
			final Action perm )
	{
		return WrappedIterator
				.create(new RDFListIterator(holder.getBaseItem())).filterKeep(
						new RDFListSecFilter<RDFList>(this, perm));
	}

	private ExtendedIterator<RDFList> getSecuredRDFListIterator(
			final Set<Action> perm )
	{
		return WrappedIterator
				.create(new RDFListIterator(holder.getBaseItem())).filterKeep(
						new RDFListSecFilter<RDFList>(this, perm));
	}

	@Override
	public boolean getStrict()
	{
		return holder.getBaseItem().getStrict();
	}

	@Override
	public SecuredRDFList getTail()
	{
		checkRead();
		Statement s = holder.getBaseItem().getRequiredProperty( listRest() );
		checkRead( s );
		return SecuredRDFListImpl.getInstance(getModel(), s.getObject().as(RDFList.class));
	}

	@Override
	public String getValidityErrorMessage()
	{
		checkRead();
		return holder.getBaseItem().getValidityErrorMessage();
	}

	@Override
	public int indexOf( final RDFNode value )
	{
		checkRead();
		final ExtendedIterator<SecuredRDFNode> iter = getSecuredRDFListIterator(
				Action.Read).mapWith(new SecuredNodeMap(listFirst()));
		try
		{
			int retval = 0;
			while (iter.hasNext())
			{
				if (value.equals(iter.next()))
				{
					return retval;
				}
				else
				{
					retval++;
				}
			}
			return -1;
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public int indexOf( final RDFNode value, final int start )
	{
		checkRead();
		final ExtendedIterator<SecuredRDFNode> iter = getSecuredRDFListIterator(
				Action.Read).mapWith(new SecuredNodeMap(listFirst()));
		try
		{
			int retval = 0;
			while (iter.hasNext() && (retval < start))
			{
				iter.next();
				retval++;
			}
			while (iter.hasNext())
			{
				if (value.equals(iter.next()))
				{
					return retval;
				}
				else
				{
					retval++;
				}
			}
			return -1;
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public boolean isEmpty()
	{
		checkRead();
		final ExtendedIterator<RDFNode> iter = iterator();
		try
		{
			return !iter.hasNext();
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public boolean isValid()
	{
		checkRead();
		return holder.getBaseItem().isValid();
	}

	@Override
	public ExtendedIterator<RDFNode> iterator()
	{
		checkRead();
		return getSecuredRDFListIterator(Action.Read).mapWith(
				new PlainNodeMap());

	}

	@Override
	public ExtendedIterator<RDFNode> iterator( final Set<Action> constraints )
	{
		checkRead();
		final Set<Action> req = new HashSet<Action>(constraints);
		req.add(Action.Read);
		return getSecuredRDFListIterator(req).mapWith(new PlainNodeMap());

	}

	public Class<? extends RDFList> listAbstractionClass()
	{
		return RDFList.class;
	}

	public Property listFirst()
	{
		return m_listFirst;
	}

	public Resource listNil()
	{
		return m_listNil;
	}

	public Property listRest()
	{
		return m_listRest;
	}

	public Resource listType()
	{
		return m_listType;
	}

	@Override
	public <T> ExtendedIterator<T> mapWith( final Map1<RDFNode, T> fn )
	{
		return iterator().mapWith(fn);
	}

	@Override
	public Object reduce( final ReduceFn fn, final Object initial )
	{
		Object acc = initial;

		for (final Iterator<RDFNode> i = iterator(); i.hasNext();)
		{
			acc = fn.reduce(i.next(), acc);
		}

		return acc;
	}

	@Override
	public Object reduce( final Set<Action> requiredActions, final ReduceFn fn,
			final Object initial ) throws AccessDeniedException,
			EmptyListException, ListIndexException, InvalidListException
	{
		Object acc = initial;
		final Set<Action> perms = new HashSet<Action>(requiredActions);
		perms.add(Action.Read);
		for (final Iterator<RDFNode> i = iterator(perms); i.hasNext();)
		{
			acc = fn.reduce(i.next(), acc);
		}

		return acc;
	}

	@Override
	public RDFList remove( final RDFNode val )
	{
		checkUpdate();
		RDFList cell = null;
		boolean denied = false;

		if (!canDelete(new Triple(Node.ANY, listFirst().asNode(), val.asNode())))
		{
			// iterate over the deletable items
			final ExtendedIterator<RDFList> iter = getSecuredRDFListIterator(Action.Delete);// .mapWith(new
																							// SecuredListMap());
			while (iter.hasNext())
			{
				cell = iter.next();

				if (val.equals(cell.getRequiredProperty(listFirst())
						.getObject()))
				{
					if (canDelete(new Triple(cell.asNode(), listFirst()
							.asNode(), val.asNode())))
					{
						return SecuredRDFListImpl.getInstance(getModel(),
								baseRemove(cell));

					}
					else
					{
						denied = true;
					}
				}
			}
			if (denied)
			{
				throw new AccessDeniedException(getModelNode(), Action.Delete);
			}
			else
			{
				return this;
			}
		}
		else
		{
			return SecuredRDFListImpl.getInstance(getModel(), holder
					.getBaseItem().remove(val));
		}
	}

	@Override
	@Deprecated
	public void removeAll()
	{
		removeList();
	}

	@Override
	public SecuredRDFList removeHead()
	{
		checkUpdate();
		final ExtendedIterator<SecuredRDFList> iter = getSecuredRDFListIterator(
				Action.Read).mapWith(new SecuredListMap());
		try
		{
			if (!iter.hasNext())
			{
				throw new EmptyListException(
						"Attempted to delete the head of a nil list");
			}
			final SecuredRDFList cell = iter.next();
			final Statement s = cell.getRequiredProperty(RDF.first);
			checkDelete(s);
			return SecuredRDFListImpl.getInstance(getModel(), baseRemove(cell));

		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public void removeList()
	{
		checkUpdate();
		final Triple t = new Triple(Node.ANY, listFirst().asNode(), Node.ANY);
		/*
		 * if (!canRead(t))
		 * {
		 * throw new EmptyListException(
		 * "Attempted to delete the head of a nil list" );
		 * }
		 */
		// have to be able to read and delete to delete all.
		final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Delete, Action.Read });
		if (getSecurityEvaluator().evaluate(perms, this.getModelNode(),
				SecuredItemImpl.convert(t)))
		{
			holder.getBaseItem().removeList();
		}
		else
		{
			for (final Statement s : collectStatements(perms))
			{
				if (canDelete(s.asTriple()))
				{
					s.remove();
				}
			}
		}
	}

	@Override
	public SecuredRDFNode replace( final int i, final RDFNode value )
	{
		checkUpdate();
		final SecuredNodeMap map = new SecuredNodeMap(listFirst());
		final ExtendedIterator<SecuredRDFList> iter = getSecuredRDFListIterator(
				Action.Read).mapWith(new SecuredListMap());
		int idx = 0;
		try
		{
			while (iter.hasNext())
			{
				if (i == idx)
				{
					final SecuredRDFList list = iter.next();
					final SecuredRDFNode retval = map.map1(list);
					final Triple t = new Triple(list.asNode(), listFirst()
							.asNode(), retval.asNode());
					final Triple t2 = new Triple(list.asNode(), listFirst()
							.asNode(), value.asNode());
					checkUpdate(t, t2);
					final RDFList base = (RDFList) list.getBaseItem();
					base.getRequiredProperty(listFirst()).changeObject(value);
					return retval;
				}
				else
				{
					idx++;
					iter.next();
				}

			}
			throw new ListIndexException();
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public boolean sameListAs( final RDFList list )
	{
		checkRead();
		ExtendedIterator<RDFNode> thisIter = null;
		ExtendedIterator<RDFNode> thatIter = null;
		try
		{
			thisIter = iterator();
			thatIter = list.iterator();
			while (thisIter.hasNext() && thatIter.hasNext())
			{
				final RDFNode thisN = thisIter.next();
				final RDFNode thatN = thatIter.next();
				if ((thisN == null) || !thisN.equals(thatN))
				{
					// not equal at this position
					return false;
				}
			}
			return !(thisIter.hasNext() || thatIter.hasNext());
		}
		finally
		{
			if (thisIter != null)
			{
				thisIter.close();
			}
			if (thatIter != null)
			{
				thatIter.close();
			}
		}
	}

	@Override
	public SecuredRDFNode setHead( final RDFNode value )
	{
		final ExtendedIterator<SecuredRDFList> iter = getSecuredRDFListIterator(
				Action.Read).mapWith(new SecuredListMap());
		try
		{
			if (iter.hasNext())
			{
				return replace(0, value);
			}
			else
			{
				throw new EmptyListException(
						"Tried to set the head of an empty list");
			}
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public void setStrict( final boolean strict )
	{
		checkUpdate();
		holder.getBaseItem().setStrict(strict);
	}

	@Override
	public SecuredRDFList setTail( final RDFList tail )
	{
		checkUpdate();

		final Statement rest = holder.getBaseItem().getRequiredProperty(
				listRest());
		final RDFNode retval = rest.getObject();
		final Triple t = new Triple(holder.getBaseItem().asNode(), listRest()
				.asNode(), retval.asNode());
		final Triple t2 = new Triple(holder.getBaseItem().asNode(), listRest()
				.asNode(), tail.asNode());
		checkUpdate(t, t2);
		rest.changeObject(tail);
		return SecuredRDFListImpl.getInstance(getModel(),
				retval.as(RDFList.class));
	}

	@Override
	public int size()
	{
		checkRead();
		final Triple t = new Triple(Node.ANY, listFirst().asNode(), Node.ANY);
		if (canRead(t))
		{
			return holder.getBaseItem().size();
		}
		final ExtendedIterator<RDFNode> iter = iterator();
		int i = 0;
		while (iter.hasNext())
		{
			i++;
			iter.next();
		}
		return i;
	}

	@Override
	public SecuredRDFList with( final RDFNode value )
	{
		checkUpdate();
		checkCreate(new SecurityEvaluator.SecTriple(
				SecurityEvaluator.SecNode.FUTURE,
				SecuredItemImpl.convert(listFirst().asNode()),
				SecuredItemImpl.convert(value.asNode())));
		return SecuredRDFListImpl.getInstance(getModel(), holder.getBaseItem()
				.with(value));
	}
}
