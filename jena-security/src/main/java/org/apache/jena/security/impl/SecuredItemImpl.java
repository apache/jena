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
package org.apache.jena.security.impl;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import java.lang.reflect.Proxy;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.SecurityEvaluator.SecNode;
import org.apache.jena.security.SecurityEvaluator.SecTriple;
import org.apache.jena.security.SecurityEvaluator.SecNode.Type;

/**
 * An abstract implementation of SecuredItem that caches security checks.
 * <p>
 * Security checks are performed at multiple locations.  This implementation ensures that 
 * during a single operation the specific check is only evaluated once by caching the result.
 * </p>
 * 
 */
public abstract class SecuredItemImpl implements SecuredItem
{
	// a key for the secured item.
	private class CacheKey implements Comparable<CacheKey>
	{
		private final Action action;
		private final SecNode modelNode;
		private final SecTriple from;
		private final SecTriple to;
		private Integer hashCode;

		public CacheKey( final Action action, final SecNode modelNode )
		{
			this(action, modelNode, null, null);
		}

		public CacheKey( final Action action, final SecNode modelNode,
				final SecTriple to )
		{
			this(action, modelNode, to, null);
		}

		public CacheKey( final Action action, final SecNode modelNode,
				final SecTriple to, final SecTriple from )
		{
			this.action = action;
			this.modelNode = modelNode;
			this.to = to;
			this.from = from;
		}

		@Override
		public int compareTo( final CacheKey other )
		{
			int retval = this.action.compareTo(other.action);
			if (retval == 0)
			{
				retval = this.modelNode.compareTo(other.modelNode);
			}
			if (retval == 0)
			{
				if (this.to == null)
				{
					if (other.to == null)
					{
						return 0;
					}
					return -1;
				}
				retval = this.to.compareTo(other.to);
			}
			if (retval == 0)
			{
				if (this.from == null)
				{
					if (other.from == null)
					{
						return 0;
					}
					return -1;
				}
				retval = this.from.compareTo(other.from);
			}
			return retval;
		}

		@Override
		public boolean equals( final Object o )
		{
			if (o instanceof CacheKey)
			{
				return this.compareTo((CacheKey) o) == 0;
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			if (hashCode == null)
			{
				hashCode = new HashCodeBuilder().append(action)
						.append(modelNode).append(from).append(to).toHashCode();
			}
			return hashCode;
		}
	}

	// the maximum size of the cache
	public static int MAX_CACHE = 100;
	// the cache for this thread.
	public static final ThreadLocal<LRUMap> CACHE = new ThreadLocal<LRUMap>();
	// the number of times this thread has recursively called the constructor.
	public static final ThreadLocal<Integer> COUNT = new ThreadLocal<Integer>();
	
	/**
	 * Convert a Jena Node object into a SecNode object.
	 * @param jenaNode The Jena node to convert.
	 * @return The SecNode that represents the jenaNode.
	 */
	public static SecNode convert( final com.hp.hpl.jena.graph.Node jenaNode )
	{
		if (com.hp.hpl.jena.graph.Node.ANY.equals(jenaNode))
		{
			return SecNode.ANY;
		}
		if (jenaNode.isLiteral())
		{
			return new SecNode(Type.Literal, jenaNode.getLiteral().toString());
		}
		if (jenaNode.isBlank())
		{
			return new SecNode(Type.Anonymous, jenaNode.getBlankNodeLabel());
		}
		if (jenaNode.isVariable())
		{
			return SecNode.VARIABLE;
		}
		return new SecNode(Type.URI, jenaNode.getURI());
	}

	/**
	 * Convert a Jena Triple into a SecTriple.
	 * @param jenaTriple The Jena Triple to convert.
	 * @return The SecTriple that represents the jenaTriple.
	 */
	public static SecTriple convert(
			final com.hp.hpl.jena.graph.Triple jenaTriple )
	{
		return new SecTriple(SecuredItemImpl.convert(jenaTriple.getSubject()),
				SecuredItemImpl.convert(jenaTriple.getPredicate()),
				SecuredItemImpl.convert(jenaTriple.getObject()));
	}

	/**
	 * Decrement the number of instances of SecuredItem.
	 */
	public static void decrementUse()
	{
		final Integer i = SecuredItemImpl.COUNT.get();
		if (i == null)
		{
			throw new IllegalStateException("No count on exit");
		}
		if (i < 1)
		{
			throw new IllegalStateException("No count less than 1");
		}
		if (i == 1)
		{
			SecuredItemImpl.CACHE.remove();
			SecuredItemImpl.COUNT.remove();
		}
		else
		{
			SecuredItemImpl.COUNT.set( i - 1 );
		}
	}

	/**
	 * Increment the number of instances of SecuredItem.
	 */
	public static void incrementUse()
	{
		final Integer i = SecuredItemImpl.COUNT.get();
		if (i == null)
		{
			SecuredItemImpl.CACHE.set(new LRUMap(Math.max(
					SecuredItemImpl.MAX_CACHE, 100)));
			SecuredItemImpl.COUNT.set( 1 );
		}
		else
		{
			SecuredItemImpl.COUNT.set( i + 1 );
		}
	}

	// the evaluator we are using 
	private final SecurityEvaluator securityEvaluator;

	// the secured node for that names the graph.
	private final SecNode modelNode;

	// the item holder that we are evaluating.
	private final ItemHolder<?, ?> itemHolder;

	/**
	 * Create the SecuredItemImpl.
	 * @param securedItem The securedItem.
	 * @param holder The Item holder for the securedItem.
	 * @throws IllegalArgumentException if securedItem is null or securedItem.getSecurityEvaluator() 
	 * returns null, or the holder is null.
	 */
	protected SecuredItemImpl( final SecuredItem securedItem,
			final ItemHolder<?, ?> holder )
	{
		if (securedItem == null)
		{
			throw new IllegalArgumentException("Secured item may not be null");
		}
		if (securedItem.getSecurityEvaluator() == null)
		{
			throw new IllegalArgumentException(
					"Security evaluator in secured item may not be null");
		}
		if (holder == null)
		{
			throw new IllegalArgumentException("ItemHolder may not be null");
		}
		this.securityEvaluator = securedItem.getSecurityEvaluator();
		this.modelNode = new SecurityEvaluator.SecNode(
				SecurityEvaluator.SecNode.Type.URI, securedItem.getModelIRI());
		this.itemHolder = holder;
	}

	/**
	 * Create the SecuredItemImpl.
	 * @param securityEvaluator the secured evaluator to use.
	 * @param modelURI the URI for the model.
	 * @param holder The holder to use.
	 * @throws IllegalArgumentException if security evaluator is null, modelURI is null or empty,
	 * or holder is null.
	 */
	protected SecuredItemImpl( final SecurityEvaluator securityEvaluator,
			final String modelURI, final ItemHolder<?, ?> holder )
	{
		if (securityEvaluator == null)
		{
			throw new IllegalArgumentException(
					"Security evaluator may not be null");
		}
		if (StringUtils.isEmpty(modelURI))
		{
			throw new IllegalArgumentException(
					"ModelURI may not be empty or null");
		}
		if (holder == null)
		{
			throw new IllegalArgumentException("ItemHolder may not be null");
		}
		this.securityEvaluator = securityEvaluator;
		this.modelNode = new SecurityEvaluator.SecNode(
				SecurityEvaluator.SecNode.Type.URI, modelURI);
		this.itemHolder = holder;
	}

	@Override
	public String toString() {
		if (canRead())
		{
			return itemHolder.getBaseItem().toString();
		}
		return super.toString();
	}
	
	/**
	 * get the cached value.
	 * @param key The key to look for.
	 * @return the value of the security check or <code>null</code> if the value has not been cached.
	 */
	private Boolean cacheGet( final CacheKey key )
	{
		final LRUMap cache = SecuredItemImpl.CACHE.get();
		return (cache == null) ? null : (Boolean) cache.get(key);
	}

	/**
	 * set teh cache value.
	 * @param key The key to set the value for.
	 * @param value The value to set.
	 */
	void cachePut( final CacheKey key, final boolean value )
	{
		final LRUMap cache = SecuredItemImpl.CACHE.get();
		if (cache != null)
		{
			cache.put(key, value);
			SecuredItemImpl.CACHE.set(cache);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.security.SecuredItem#canCreate()
	 */
	@Override
	public boolean canCreate()
	{
		final CacheKey key = new CacheKey(Action.Create, modelNode);
		Boolean retval = cacheGet(key);
		if (retval == null)
		{
			retval = securityEvaluator.evaluate(Action.Create, modelNode);
			cachePut(key, retval);
		}
		return retval;
	}

	public boolean canCreate( final com.hp.hpl.jena.graph.Triple t )
	{
		return canCreate(SecuredItemImpl.convert(t));
	}

	@Override
	public boolean canCreate( final SecTriple t )
	{
		final CacheKey key = new CacheKey(Action.Create, modelNode, t);
		Boolean retval = cacheGet(key);
		if (retval == null)
		{
			retval = securityEvaluator.evaluate(Action.Create, modelNode, t);
			cachePut(key, retval);
		}
		return retval;
	}

	public boolean canCreate( final Statement s )
	{
		return canCreate(s.asTriple());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.security.SecuredItem#canDelete()
	 */
	@Override
	public boolean canDelete()
	{
		final CacheKey key = new CacheKey(Action.Delete, modelNode);
		Boolean retval = cacheGet(key);
		if (retval == null)
		{
			retval = securityEvaluator.evaluate(Action.Delete, modelNode);
			cachePut(key, retval);
		}
		return retval;
	}

	public boolean canDelete( final com.hp.hpl.jena.graph.Triple t )
	{
		return canDelete(SecuredItemImpl.convert(t));
	}

	@Override
	public boolean canDelete( final SecTriple t )
	{
		final CacheKey key = new CacheKey(Action.Delete, modelNode, t);
		Boolean retval = cacheGet(key);
		if (retval == null)
		{
			retval = securityEvaluator.evaluate(Action.Delete, modelNode, t);
			cachePut(key, retval);
		}
		return retval;
	}

	public boolean canDelete( final Statement s )
	{
		return canDelete(s.asTriple());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.security.SecuredItem#canRead()
	 */
	@Override
	public boolean canRead()
	{
		final CacheKey key = new CacheKey(Action.Read, modelNode);
		Boolean retval = cacheGet(key);
		if (retval == null)
		{
			retval = securityEvaluator.evaluate(Action.Read, modelNode);
			cachePut(key, retval);
		}
		return retval;
	}

	public boolean canRead( final com.hp.hpl.jena.graph.Triple t )
	{
		return canRead(SecuredItemImpl.convert(t));
	}

	@Override
	public boolean canRead( final SecTriple t )
	{
		final CacheKey key = new CacheKey(Action.Read, modelNode, t);
		Boolean retval = cacheGet(key);
		if (retval == null)
		{
			retval = securityEvaluator.evaluate(Action.Read, modelNode, t);
			cachePut(key, retval);
		}
		return retval;
	}

	public boolean canRead( final Statement s )
	{
		return canRead(s.asTriple());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.security.SecuredItem#canUpdate()
	 */
	@Override
	public boolean canUpdate()
	{
		final CacheKey key = new CacheKey(Action.Update, modelNode);
		Boolean retval = cacheGet(key);
		if (retval == null)
		{
			retval = securityEvaluator.evaluate(Action.Update, modelNode);
			cachePut(key, retval);
		}
		return retval;
	}

	public boolean canUpdate( final com.hp.hpl.jena.graph.Triple from,
			final com.hp.hpl.jena.graph.Triple to )
	{
		return canUpdate(SecuredItemImpl.convert(from),
				SecuredItemImpl.convert(to));
	}

	@Override
	public boolean canUpdate( final SecTriple from, final SecTriple to )
	{
		final CacheKey key = new CacheKey(Action.Update, modelNode, from, to);
		Boolean retval = cacheGet(key);
		if (retval == null)
		{
			retval = securityEvaluator.evaluateUpdate(modelNode, from, to);
			cachePut(key, retval);
		}
		return retval;
	}

	public boolean canUpdate( final Statement from, final Statement to )
	{
		return canUpdate(from.asTriple(), to.asTriple());
	}

	/**
	 * check that create on the securedModel is allowed,
	 * 
	 * @throws AccessDeniedException
	 *             on failure
	 */
	protected void checkCreate()
	{
		if (!canCreate())
		{
			throw new AccessDeniedException(modelNode, Action.Create);
		}
	}

	protected void checkCreate( final com.hp.hpl.jena.graph.Triple t )
	{
		checkCreate(SecuredItemImpl.convert(t));
	}

	/**
	 * check that the triple can be created in the securedModel.,
	 * 
	 * @throws AccessDeniedException
	 *             on failure
	 */
	protected void checkCreate( final SecTriple t )
	{
		if (!canCreate(t))
		{
			throw new AccessDeniedException(modelNode, t.toString(),
					Action.Create);
		}
	}

	protected void checkCreate( final Statement s )
	{
		checkCreate(s.asTriple());
	}

	protected void checkCreateReified( final String uri, final SecTriple t )
	{
		checkUpdate();
		final SecNode n = uri == null ? SecNode.FUTURE : new SecNode(Type.URI,
				uri);
		checkCreate(new SecTriple(n, SecuredItemImpl.convert(RDF.subject
				.asNode()), t.getSubject()));
		checkCreate(new SecTriple(n, SecuredItemImpl.convert(RDF.predicate
				.asNode()), t.getPredicate()));
		checkCreate(new SecTriple(n, SecuredItemImpl.convert(RDF.object
				.asNode()), t.getObject()));
	}

	protected void checkCreateStatement( final ExtendedIterator<Statement> stmts )
	{
		if (!canCreate(SecTriple.ANY))
		{
			try
			{
				while (stmts.hasNext())
				{
					checkCreate(stmts.next());
				}
			}
			finally
			{
				stmts.close();
			}
		}
	}

	protected void checkCreateTriples(
			final ExtendedIterator<com.hp.hpl.jena.graph.Triple> triples )
	{
		if (!canCreate(SecTriple.ANY))
		{
			try
			{
				while (triples.hasNext())
				{
					checkCreate(triples.next());
				}
			}
			finally
			{
				triples.close();
			}
		}
	}

	/**
	 * check that delete on the securedModel is allowed,
	 * 
	 * @throws AccessDeniedException
	 *             on failure
	 */
	protected void checkDelete()
	{
		if (!canDelete())
		{
			throw new AccessDeniedException(modelNode, Action.Delete);
		}
	}

	protected void checkDelete( final com.hp.hpl.jena.graph.Triple t )
	{
		checkDelete(SecuredItemImpl.convert(t));
	}

	/**
	 * check that the triple can be deleted in the securedModel.,
	 * 
	 * @throws AccessDeniedException
	 *             on failure
	 */
	protected void checkDelete( final SecTriple t )
	{
		if (!canDelete(t))
		{
			throw new AccessDeniedException(modelNode, t.toString(),
					Action.Delete);
		}
	}

	protected void checkDelete( final Statement s )
	{
		checkDelete(s.asTriple());
	}

	protected void checkDeleteStatements(
			final ExtendedIterator<Statement> stmts )
	{
		if (!canDelete(SecTriple.ANY))
		{
			try
			{
				while (stmts.hasNext())
				{
					checkDelete(stmts.next());
				}
			}
			finally
			{
				stmts.close();
			}
		}
	}

	protected void checkDeleteTriples(
			final ExtendedIterator<com.hp.hpl.jena.graph.Triple> triples )
	{
		if (!canDelete(SecTriple.ANY))
		{
			try
			{
				while (triples.hasNext())
				{
					checkDelete(triples.next());
				}
			}
			finally
			{
				triples.close();
			}
		}
	}

	/**
	 * check that read on the securedModel is allowed,
	 * 
	 * @throws AccessDeniedException
	 *             on failure
	 */
	protected void checkRead()
	{
		if (!canRead())
		{
			throw new AccessDeniedException(modelNode, Action.Read);
		}
	}

	protected void checkRead( final com.hp.hpl.jena.graph.Triple t )
	{
		checkRead(SecuredItemImpl.convert(t));
	}

	/**
	 * check that the triple can be read in the securedModel.,
	 * 
	 * @throws AccessDeniedException
	 *             on failure
	 */
	protected void checkRead( final SecTriple t )
	{
		if (!canRead(t))
		{
			throw new AccessDeniedException(modelNode, t.toString(),
					Action.Read);
		}
	}

	protected void checkRead( final Statement s )
	{
		checkRead(s.asTriple());
	}

	protected void checkReadStatement( final ExtendedIterator<Statement> stmts )
	{
		try
		{
			while (stmts.hasNext())
			{
				checkRead(stmts.next());
			}
		}
		finally
		{
			stmts.close();
		}
	}

	protected void checkReadTriples(
			final ExtendedIterator<com.hp.hpl.jena.graph.Triple> triples )
	{
		try
		{
			while (triples.hasNext())
			{
				checkRead(triples.next());
			}
		}
		finally
		{
			triples.close();
		}
	}

	/**
	 * check that update on the securedModel is allowed,
	 * 
	 * @throws AccessDeniedException
	 *             on failure
	 */
	protected void checkUpdate()
	{
		if (!canUpdate())
		{
			throw new AccessDeniedException(modelNode, Action.Update);
		}
	}

	protected void checkUpdate( final com.hp.hpl.jena.graph.Triple from,
			final com.hp.hpl.jena.graph.Triple to )
	{
		checkUpdate(SecuredItemImpl.convert(from), SecuredItemImpl.convert(to));
	}

	/**
	 * check that the triple can be updated in the securedModel.,
	 * 
	 * @param from the starting triple
	 * @param to the final triple.
	 * @throws AccessDeniedException
	 *             on failure
	 */
	protected void checkUpdate( final SecTriple from, final SecTriple to )
	{
		if (!canUpdate(from, to))
		{
			throw new AccessDeniedException(modelNode, String.format(
					"%s to %s", from, to), Action.Update);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.security.SecuredItem#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( final Object o )
	{
		if (Proxy.isProxyClass(o.getClass()))
		{
			return o.equals(itemHolder.getSecuredItem());
		}
		else
		{
			if (o instanceof SecuredItemImpl)
			{
				return itemHolder.getBaseItem().equals( ((SecuredItemImpl)o).getBaseItem());
			}
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return itemHolder.getBaseItem().hashCode();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.security.SecuredItem#getBaseItem()
	 */
	@Override
	public Object getBaseItem()
	{
		return itemHolder.getBaseItem();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.security.SecuredItem#getModelIRI()
	 */
	@Override
	public String getModelIRI()
	{
		return modelNode.getValue();
	}

	/**
	 * get the name of the model.
	 */
	@Override
	public SecNode getModelNode()
	{
		return modelNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.security.SecuredItem#getSecurityEvaluator()
	 */
	@Override
	public SecurityEvaluator getSecurityEvaluator()
	{
		return securityEvaluator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.security.isEquivalent()
	 */
	@Override
	public boolean isEquivalent( final SecuredItem securedItem )
	{
		return SecuredItem.Util.isEquivalent(this, securedItem);
	}
}