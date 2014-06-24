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

import org.apache.jena.security.AccessDeniedException ;
import org.apache.jena.security.SecurityEvaluator.Action ;
import org.apache.jena.security.impl.ItemHolder ;
import org.apache.jena.security.impl.SecuredItemImpl ;
import org.apache.jena.security.impl.SecuredItemInvoker ;
import org.apache.jena.security.model.SecuredModel ;
import org.apache.jena.security.model.SecuredResource ;
import org.apache.jena.security.model.SecuredStatement ;
import org.apache.jena.security.utils.PermStatementFilter ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.PropertyNotFoundException ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/**
 * Implementation of SecuredResource to be used by a SecuredItemInvoker proxy.
 */
public class SecuredResourceImpl extends SecuredRDFNodeImpl implements
		SecuredResource
{
	/**
	 * Get a SecuredResource.
	 * 
	 * @param securedModel
	 *            the securedItem that provides the security context.
	 * @param resource
	 *            The resource to secure.
	 * @return The SecuredResource
	 */
	public static SecuredResource getInstance( final SecuredModel securedModel,
			final Resource resource )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (resource == null)
		{
			throw new IllegalArgumentException("Resource may not be null");
		}
		if (resource.isLiteral())
		{
			throw new IllegalArgumentException("Resource may not be a literal");
		}
		// check that resource has a securedModel.
		Resource goodResource = resource;
		if (goodResource.getModel() == null)
		{
			final Node n = resource.asNode();
			if (resource.isAnon())
			{
				goodResource = securedModel.createResource(n.getBlankNodeId());
			}
			else
			{
				goodResource = securedModel.createResource(n.getURI());
			}
		}

		final ItemHolder<Resource, SecuredResource> holder = new ItemHolder<Resource, SecuredResource>(
				goodResource);

		final SecuredResourceImpl checker = new SecuredResourceImpl(
				securedModel, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (goodResource instanceof SecuredResource)
		{
			if (checker.isEquivalent((SecuredResource) goodResource))
			{
				return (SecuredResource) goodResource;
			}
		}

		return holder.setSecuredItem(new SecuredItemInvoker(
				resource.getClass(), checker));

	}

	// the item holder that contains this SecuredResource
	private final ItemHolder<? extends Resource, ? extends SecuredResource> holder;

	/**
	 * Constructor.
	 * 
	 * @param securedModel
	 *            The secured model to use
	 * @param holder
	 *            the item holder that will contain this SecuredResource.
	 */
	protected SecuredResourceImpl(
			final SecuredModel securedModel,
			final ItemHolder<? extends Resource, ? extends SecuredResource> holder )
	{
		super(securedModel, holder);
		this.holder = holder;
	}

	/**
	 * Abort the transaction in the associated securedModel.
	 * 
	 * @return This resource to permit cascading.
	 */
	@Override
	public SecuredResource abort()
	{
		holder.getBaseItem().abort();
		return holder.getSecuredItem();
	}

	/**
	 * Add the property <code>p</code> with the typed-literal value
	 * <code>o</code> to this resource, <i>ie</i> add (this, p, typed(o)) to
	 * this's securedModel. Answer
	 * this resource. The typed literal is equal to one constructed by using
	 * <code>this.getModel().createTypedLiteral(o)</code>.
	 */
	@Override
	public SecuredResource addLiteral( final Property p, final boolean o )
	{
		return addProperty( p, ResourceFactory.createTypedLiteral(o) );
	}

	/**
	 * Add the property <code>p</code> with the typed-literal value
	 * <code>o</code> to this resource, <i>ie</i> add (this, p, typed(o)) to
	 * this's securedModel. Answer
	 * this resource. The typed literal is equal to one constructed by using
	 * <code>this.getModel().createTypedLiteral(o)</code>.
	 */
	@Override
	public SecuredResource addLiteral( final Property p, final char o )
	{
		return addProperty( p, ResourceFactory.createTypedLiteral(o));
	}

	/**
	 * Add the property <code>p</code> with the typed-literal value
	 * <code>o</code> to this resource, <i>ie</i> add (this, p, typed(o)) to
	 * this's securedModel. Answer
	 * this resource. The typed literal is equal to one constructed by using
	 * <code>this.getModel().createTypedLiteral(o)</code>.
	 */
	@Override
	public SecuredResource addLiteral( final Property value, final double d )
	{
		return addProperty( value, ResourceFactory.createTypedLiteral(d));
	}

	/**
	 * Add the property <code>p</code> with the typed-literal value
	 * <code>o</code> to this resource, <i>ie</i> add (this, p, typed(o)) to
	 * this's securedModel. Answer
	 * this resource. The typed literal is equal to one constructed by using
	 * <code>this.getModel().createTypedLiteral(o)</code>.
	 */
	@Override
	public SecuredResource addLiteral( final Property value, final float d )
	{
		return addProperty( value, ResourceFactory.createTypedLiteral(d) );
	}

	/**
	 * Add the property <code>p</code> with the pre-constructed Literal value
	 * <code>o</code> to this resource, <i>ie</i> add (this, p, o) to this's
	 * securedModel. Answer this resource. <b>NOTE</b> thjat this is distinct
	 * from the
	 * other addLiteral methods in that the Literal is not turned into a
	 * Literal.
	 */
	@Override
	public SecuredResource addLiteral( final Property p, final Literal o )
	{
		return addProperty( p, o );
	}

	/**
	 * Add the property <code>p</code> with the typed-literal value
	 * <code>o</code> to this resource, <i>ie</i> add (this, p, typed(o)) to
	 * this's securedModel. Answer
	 * this resource. The typed literal is equal to one constructed by using
	 * <code>this.getModel().createTypedLiteral(o)</code>.
	 */
	@Override
	public SecuredResource addLiteral( final Property p, final long o )
	{
		return addProperty( p, ResourceFactory.createTypedLiteral(o));
	}

	/**
	 * Add the property <code>p</code> with the typed-literal value
	 * <code>o</code> to this resource, <i>ie</i> add (this, p, typed(o)) to
	 * this's securedModel. Answer
	 * this resource. The typed literal is equal to one constructed by using
	 * <code>this.getModel().createTypedLiteral(o)</code>.
	 */
	@Override
	public SecuredResource addLiteral( final Property p, final Object o )
	{
		return addProperty( p, ResourceFactory.createTypedLiteral(o));
	}

	/**
	 * Add a property to this resource.
	 * 
	 * <p>
	 * A statement with this resource as the subject, p as the predicate and o
	 * as the object is added to the securedModel associated with this resource.
	 * </p>
	 * 
	 * @param p
	 *            The property to be added.
	 * @param o
	 *            The value of the property to be added.
	 * @return This resource to allow cascading calls.
	 */
	@Override
	public SecuredResource addProperty( final Property p, final RDFNode o )
	{
		checkUpdate();
		checkCreate(new Triple(holder.getBaseItem().asNode(), p.asNode(),
				o.asNode()));
		holder.getBaseItem().addProperty(p, o);
		return holder.getSecuredItem();
	}

	/**
	 * Add a property to this resource.
	 * 
	 * <p>
	 * A statement with this resource as the subject, p as the predicate and o
	 * as the object is added to the securedModel associated with this resource.
	 * </p>
	 * 
	 * @param p
	 *            The property to be added.
	 * @param o
	 *            The value of the property to be added.
	 * @return This resource to allow cascading calls.
	 */
	@Override
	public SecuredResource addProperty( final Property p, final String o )
	{
		return addProperty( p, o, "");
	}

	/**
	 * Add a property to this resource.
	 * 
	 * <p>
	 * A statement with this resource as the subject, p as the predicate and o
	 * as the object is added to the securedModel associated with this resource.
	 * </p>
	 * 
	 * @param p
	 *            The property to be added.
	 * @param lexicalForm
	 *            The lexical form of the literal
	 * @param datatype
	 *            The datatype
	 * @return This resource to allow cascading calls.
	 */
	@Override
	public SecuredResource addProperty( final Property p, final String lexicalForm,
			final RDFDatatype datatype )
	{
		checkUpdate();
		final Literal l = ResourceFactory.createTypedLiteral(lexicalForm,
				datatype);
		checkCreate(new Triple(holder.getBaseItem().asNode(), p.asNode(),
				l.asNode()));
		holder.getBaseItem().addProperty(p, l);
		return holder.getSecuredItem();
	}

	/**
	 * Add a property to this resource.
	 * 
	 * <p>
	 * A statement with this resource as the subject, p as the predicate and o
	 * as the object is added to the securedModel associated with this resource.
	 * </p>
	 * 
	 * @param p
	 *            The property to be added.
	 * @param o
	 *            The value of the property to be added.
	 * @param l
	 *            the language of the property
	 * @return This resource to allow cascading calls.
	 */
	@Override
	public SecuredResource addProperty( final Property p, final String o,
			final String l )
	{
		checkUpdate();
		checkCreate(new Triple(holder.getBaseItem().asNode(), p.asNode(),
		                       NodeFactory.createLiteral(o, l, false)));
		holder.getBaseItem().addProperty(p, o, l);
		return holder.getSecuredItem();
	}

	@Override
	public Literal asLiteral()
	{
		throw new LiteralRequiredException(asNode());
	}

	@Override
	public SecuredResource asResource()
	{
		return holder.getSecuredItem();
	}

	/**
	 * Begin a transaction in the associated securedModel.
	 * 
	 * @return This resource to permit cascading.
	 */
	@Override
	public SecuredResource begin()
	{
		holder.getBaseItem().begin();
		return holder.getSecuredItem();
	}

	public boolean canReadProperty( final Node p )
	{
		return canRead(new Triple(holder.getBaseItem().asNode(), p, Node.ANY));
	}

	protected void checkReadProperty( final Node p )
	{
		if (!canReadProperty(p))
		{
			throw new AccessDeniedException(getModelNode(), SecuredItemImpl
					.convert(
							new Triple(holder.getBaseItem().asNode(), p,
									Node.ANY)).toString(), Action.Read);
		}
	}

	/**
	 * Commit the transaction in the associated securedModel.
	 * 
	 * @return This resource to permit cascading.
	 */
	@Override
	public SecuredResource commit()
	{
		holder.getBaseItem().commit();
		return holder.getSecuredItem();
	}

	/**
	 * Returns an a unique identifier for anonymous resources.
	 * 
	 * <p>
	 * The id is unique within the scope of a particular implementation. All
	 * models within an implementation will use the same id for the same
	 * anonymous resource.
	 * </p>
	 * 
	 * <p>
	 * This method is undefined if called on resources which are not anonymous
	 * and may raise an exception.
	 * </p>
	 * 
	 * @return A unique id for an anonymous resource.
	 */
	@Override
	public AnonId getId()
	{
		checkRead();
		return holder.getBaseItem().getId();

	}

	/**
	 * Returns the name of this resource within its namespace.
	 * 
	 * @return The name of this property within its namespace.
	 */
	@Override
	public String getLocalName()
	{
		checkRead();
		return holder.getBaseItem().getLocalName();
	}

	/**
	 * Returns the namespace associated with this resource.
	 * 
	 * @return The namespace for this property.
	 */
	@Override
	public String getNameSpace()
	{
		checkRead();
		return holder.getBaseItem().getNameSpace();
	}

	/**
	 * Answer some statement (this, p, O) in the associated securedModel. If
	 * there are
	 * several
	 * such statements, any one of them may be returned. If no such statements
	 * exist,
	 * null is returned - in this is differs from getRequiredProperty.
	 * 
	 * @param p
	 *            the property sought
	 * @return a statement (this, p, O), or null if no such statements exist
	 *         here
	 */
	@Override
	public SecuredStatement getProperty( final Property p )
	{
		checkRead();
		final ExtendedIterator<Statement> iter = holder.getBaseItem()
				.listProperties(p)
				.filterKeep(new PermStatementFilter(Action.Read, this));
		try
		{
			if (iter.hasNext())
			{
				return org.apache.jena.security.model.impl.SecuredStatementImpl
						.getInstance(getModel(), iter.next());
			}
			else
			{
				return null;
			}
		}
		finally
		{
			iter.close();
		}
	}

	/**
	 * Answer some resource R for which this.hasProperty( p, R ),
	 * or null if no such R exists.
	 */
	@Override
	public SecuredResource getPropertyResourceValue( final Property p )
	{
		final SecuredStatementIterator iter = listProperties(p);
		try
		{
			while (iter.hasNext())
			{
				final Statement s = iter.next();
				if (s.getObject().isResource())
				{
					return SecuredResourceImpl.getInstance(getModel(), s
							.getObject().asResource());
				}
			}
			return null;
		}
		finally
		{
			iter.close();
		}
	}

	/**
	 * Get a property value of this resource.
	 * 
	 * <p>
	 * The securedModel associated with the resource instance is searched for
	 * statements whose subject is this resource and whose predicate is p. If
	 * such a statement is found, it is returned. If several such statements are
	 * found, any one may be returned. If no such statements are found, an
	 * exception is thrown.
	 * </p>
	 * 
	 * @param p
	 *            The property sought.
	 * @return some (this, p, ?O) statement if one exists
	 * @throws PropertyNotFoundException
	 *             if no such statement found
	 */
	@Override
	public SecuredStatement getRequiredProperty( final Property p )
			throws PropertyNotFoundException
	{
		checkRead();
		final ExtendedIterator<Statement> iter = holder.getBaseItem()
				.listProperties(p)
				.filterKeep(new PermStatementFilter(Action.Read, this));
		try
		{
			if (iter.hasNext())
			{
				return org.apache.jena.security.model.impl.SecuredStatementImpl
						.getInstance(getModel(), iter.next());
			}
			else
			{
				throw new PropertyNotFoundException(p);
			}
		}
		finally
		{
			iter.close();
		}
		// return org.apache.jena.security.model.impl.Factory.getInstance(
		// this, holder.getBaseItem().getRequiredProperty(p));
	}

	/**
	 * Return the URI of the resource, or null if it's a bnode.
	 * 
	 * @return The URI of the resource, or null if it's a bnode.
	 */
	@Override
	public String getURI()
	{
		checkRead();
		return holder.getBaseItem().getURI();
	}

	/**
	 * Answer true iff this resource has the value <code>o</code> for
	 * property <code>p</code>. <code>o</code> is interpreted as
	 * a typed literal with the appropriate RDF type.
	 */
	@Override
	public boolean hasLiteral( final Property p, final boolean o )
	{
		checkRead();
		checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
				ResourceFactory.createTypedLiteral(o).asNode()));
		return holder.getBaseItem().hasLiteral(p, o);
	}

	/**
	 * Answer true iff this resource has the value <code>o</code> for
	 * property <code>p</code>. <code>o</code> is interpreted as
	 * a typed literal with the appropriate RDF type.
	 */
	@Override
	public boolean hasLiteral( final Property p, final char o )
	{
		checkRead();
		checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
				ResourceFactory.createTypedLiteral(o).asNode()));
		return holder.getBaseItem().hasLiteral(p, o);
	}

	/**
	 * Answer true iff this resource has the value <code>o</code> for
	 * property <code>p</code>. <code>o</code> is interpreted as
	 * a typed literal with the appropriate RDF type.
	 */
	@Override
	public boolean hasLiteral( final Property p, final double o )
	{
		checkRead();
		checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
				ResourceFactory.createTypedLiteral(o).asNode()));
		return holder.getBaseItem().hasLiteral(p, o);
	}

	/**
	 * Answer true iff this resource has the value <code>o</code> for
	 * property <code>p</code>. <code>o</code> is interpreted as
	 * a typed literal with the appropriate RDF type.
	 */
	@Override
	public boolean hasLiteral( final Property p, final float o )
	{
		checkRead();
		checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
				ResourceFactory.createTypedLiteral(o).asNode()));
		return holder.getBaseItem().hasLiteral(p, o);
	}

	/**
	 * Answer true iff this resource has the value <code>o</code> for
	 * property <code>p</code>. <code>o</code> is interpreted as
	 * a typed literal with the appropriate RDF type.
	 */
	@Override
	public boolean hasLiteral( final Property p, final long o )
	{
		checkRead();
		checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
				ResourceFactory.createTypedLiteral(o).asNode()));
		return holder.getBaseItem().hasLiteral(p, o);
	}

	/**
	 * Answer true iff this resource has the value <code>o</code> for
	 * property <code>p</code>. <code>o</code> is interpreted as
	 * a typed literal with the appropriate RDF type.
	 */
	@Override
	public boolean hasLiteral( final Property p, final Object o )
	{
		checkRead();
		checkRead(new Triple(holder.getBaseItem().asNode(), p.asNode(),
				ResourceFactory.createTypedLiteral(o).asNode()));
		return holder.getBaseItem().hasLiteral(p, o);
	}

	/**
	 * Determine whether this resource has any values for a given property.
	 * 
	 * @param p
	 *            The property sought.
	 * @return true if and only if this resource has at least one
	 *         value for the property.
	 */
	@Override
	public boolean hasProperty( final Property p )
	{
		checkRead();
		final ExtendedIterator<Statement> iter = holder.getBaseItem()
				.listProperties(p)
				.filterKeep(new PermStatementFilter(Action.Read, this));
		try
		{
			return iter.hasNext();
		}
		finally
		{
			iter.close();
		}
	}

	/**
	 * Test if this resource has a given property with a given value.
	 * 
	 * @param p
	 *            The property sought.
	 * @param o
	 *            The value of the property sought.
	 * @return true if and only if this resource has property p with
	 *         value o.
	 */
	@Override
	public boolean hasProperty( final Property p, final RDFNode o )
	{
		checkRead();
		final ExtendedIterator<Statement> iter = holder.getBaseItem()
				.getModel().listStatements(this, p, o)
				.filterKeep(new PermStatementFilter(Action.Read, this));
		try
		{
			return iter.hasNext();
		}
		finally
		{
			iter.close();
		}
	}

	/**
	 * Test if this resource has a given property with a given value.
	 * 
	 * @param p
	 *            The property sought.
	 * @param o
	 *            The value of the property sought.
	 * @return true if and only if this resource has property p with
	 *         value o.
	 */
	@Override
	public boolean hasProperty( final Property p, final String o )
	{
		checkRead();
		final ExtendedIterator<Statement> iter = holder.getBaseItem()
				.getModel().listStatements(this, p, o)
				.filterKeep(new PermStatementFilter(Action.Read, this));
		try
		{
			return iter.hasNext();
		}
		finally
		{
			iter.close();
		}
	}

	/**
	 * Test if this resource has a given property with a given value.
	 * 
	 * @param p
	 *            The property sought.
	 * @param o
	 *            The value of the property sought.
	 * @param l
	 *            The language of the property sought.
	 * @return true if and only if this resource has property p with
	 *         value o.
	 */
	@Override
	public boolean hasProperty( final Property p, final String o, final String l )
	{
		checkRead();
		final Literal ll = holder.getBaseItem().getModel().createLiteral(o, l);
		final ExtendedIterator<Statement> iter = holder.getBaseItem()
				.getModel().listStatements(this, p, ll)
				.filterKeep(new PermStatementFilter(Action.Read, this));
		try
		{
			return iter.hasNext();
		}
		finally
		{
			iter.close();
		}
	}

	/**
	 * Answer true iff this Resource is a URI resource with the given URI.
	 * Using this is preferred to using getURI() and .equals().
	 */
	@Override
	public boolean hasURI( final String uri )
	{
		checkRead();
		return holder.getBaseItem().hasURI(uri);
	}

	@Override
	public Resource inModel( final Model m )
	{
		return (Resource) super.inModel(m);
	}

	/**
	 * Return an iterator over all the properties of this resource.
	 * 
	 * <p>
	 * The securedModel associated with this resource is search and an iterator
	 * is returned which iterates over all the statements which have this
	 * resource as a subject.
	 * </p>
	 * 
	 * @return An iterator over all the statements about this object.
	 */
	@Override
	public SecuredStatementIterator listProperties()
	{
		checkRead();
		return new SecuredStatementIterator(getModel(), holder.getBaseItem()
				.listProperties());
	}

	/**
	 * List all the values of the property p.
	 * 
	 * <p>
	 * Returns an iterator over all the statements in the associated
	 * securedModel whose subject is this resource and whose predicate is p.
	 * </p>
	 * 
	 * @param p
	 *            The predicate sought.
	 * @return An iterator over the statements.
	 */
	@Override
	public SecuredStatementIterator listProperties( final Property p )
	{
		checkRead();
		return new SecuredStatementIterator(getModel(), holder.getBaseItem()
				.listProperties(p));

	}

	/**
	 * Delete all the statements with predicate <code>p</code> for this resource
	 * from its associated securedModel.
	 * 
	 * @param p
	 *            the property to remove
	 * @return this resource, to permit cascading
	 */
	@Override
	public SecuredResource removeAll( final Property p )
	{
		checkUpdate();
		if (!canDelete(new Triple(holder.getBaseItem().asNode(), p.asNode(),
				Node.ANY)))
		{
			final StmtIterator iter = holder.getBaseItem().listProperties(p);
			try
			{
				if (!iter.hasNext())
				{
					// thre arn't any to delete -- so return
					return holder.getSecuredItem();
				}
				while (iter.hasNext())
				{
					checkDelete(iter.next().asTriple());
				}
			}
			finally
			{
				iter.close();
			}
		}
		holder.getBaseItem().removeAll(p);
		return holder.getSecuredItem();
	}

	/**
	 * Delete all the properties for this resource from the associated
	 * securedModel.
	 * 
	 * @return This resource to permit cascading.
	 */
	@Override
	public SecuredResource removeProperties()
	{
		checkUpdate();
		if (!canDelete(new Triple(holder.getBaseItem().asNode(), Node.ANY,
				Node.ANY)))
		{
			final StmtIterator iter = holder.getBaseItem().listProperties();
			try
			{
				if (!iter.hasNext())
				{
					// thre arn't any to delete -- so return
					return holder.getSecuredItem();
				}
				while (iter.hasNext())
				{
					checkDelete(iter.next().asTriple());
				}
			}
			finally
			{
				iter.close();
			}
		}
		holder.getBaseItem().removeProperties();
		return holder.getSecuredItem();
	}

	/**
	 * Return a string representation of the resource.
	 * 
	 * Returns the URI of the resource unless the resource is anonymous
	 * in which case it returns the id of the resource enclosed in square
	 * brackets.
	 * 
	 * @return Return a string representation of the resource.
	 *         if it is anonymous.
	 */
	@Override
	public String toString()
	{
		return holder.getBaseItem().toString();
	}

	@Override
	public Object visitWith( final RDFVisitor rv )
	{
		return isAnon() ? rv.visitBlank(this, getId()) : rv.visitURI(this,
				getURI());
	}

}
