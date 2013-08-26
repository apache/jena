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

import com.hp.hpl.jena.enhanced.UnsupportedPolymorphismException;
import com.hp.hpl.jena.graph.FrontsNode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemImpl;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.model.SecuredRDFNode;
import org.apache.jena.security.model.SecuredUnsupportedPolymorphismException;

/**
 * Implementation of SecuredRDFNode to be used by a SecuredItemInvoker proxy.
 */
public abstract class SecuredRDFNodeImpl extends SecuredItemImpl implements
		SecuredRDFNode
{
	/**
	 * 
	 * @param securedModel
	 *            the Secured Model to use.
	 * @param rdfNode the node to secure.
	 * @return the secured RDFNode
	 */
	public static SecuredRDFNode getInstance( final SecuredModel securedModel,
			final RDFNode rdfNode )
	{
		if (rdfNode instanceof Literal)
		{
			return SecuredLiteralImpl.getInstance(securedModel,
					(Literal) rdfNode);
		}
		else
		{
			return SecuredResourceImpl.getInstance(securedModel,
					(Resource) rdfNode);
		}
	}

	// the item holder that contains this SecuredRDFNode
	private final ItemHolder<? extends RDFNode, ? extends SecuredRDFNode> holder;

	// the secured securedModel that contains this node.
	private final SecuredModel securedModel;

	/**
	 * Constructor
	 * 
	 * @param securedModel
	 *            the Secured Model to use.
	 * @param holder
	 *            the item holder that will contain this SecuredRDFNode.
	 */
	protected SecuredRDFNodeImpl( final SecuredModel securedModel,
			final ItemHolder<? extends RDFNode, ? extends SecuredRDFNode> holder )
	{
		super(securedModel, holder);
		if (holder.getBaseItem().getModel() == null)
		{
			throw new IllegalArgumentException(String.format(
					"Holder base item (%s) must have a securedModel", holder
							.getBaseItem().getClass()));
		}
		this.securedModel = securedModel;
		this.holder = holder;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T extends RDFNode> T as( final Class<T> view )
	{
		checkRead();
		// see if the base Item can as
		T baseAs = holder.getBaseItem().as(view);
		
			if (view.equals(SecuredRDFNodeImpl.class)
					|| view.equals(RDFNode.class))
			{
				return (T) this;
			}
			final Method m = getConstructor(view);
			if (m == null)
			{
				throw new SecuredUnsupportedPolymorphismException(this, view);
			}
			try
			{
				return (T) m.invoke(null, securedModel, holder.getBaseItem()
						.as(view));
			}
			catch (final UnsupportedPolymorphismException e)
			{
				throw new SecuredUnsupportedPolymorphismException(this, view);
			}
			catch (final IllegalArgumentException e)
			{
				throw new RuntimeException(e);
			}
			catch (final IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
			catch (final InvocationTargetException e)
			{
				throw new RuntimeException(e);
			}
		/*
		else
		{
			throw new SecuredUnsupportedPolymorphismException(this, view);
		}
		*/
	}

	@Override
	public Node asNode()
	{
		checkRead();
		return holder.getBaseItem().asNode();
	}

	@Override
	public <T extends RDFNode> boolean canAs( final Class<T> view )
	{
		checkRead();
		// see if the base Item can as
		if (holder.getBaseItem().canAs(view))
		{
			return getConstructor(view) != null;
		}
		return false;
	}

	private <T extends RDFNode> Method getConstructor( final Class<T> view )
	{
		String classNm = SecuredRDFNodeImpl.class.getName();
		classNm = String.format("%s.Secured%sImpl",
				classNm.substring(0, classNm.lastIndexOf(".")),
				view.getSimpleName());
		try
		{
			final Class<?> c = Class.forName(classNm);
			return c.getDeclaredMethod("getInstance", SecuredModel.class, view);
		}
		catch (final ClassNotFoundException e)
		{
			return null;
		}
		catch (final SecurityException e)
		{
			return null;
		}
		catch (final NoSuchMethodException e)
		{
			return null;
		}
	}

	@Override
	public SecuredModel getModel()
	{
		return securedModel;
	}

	@Override
	public RDFNode inModel( final Model m )
	{
		checkRead();
		if (securedModel.equals(m))
		{
			return this;
		}
		if (m instanceof SecuredModel)
		{
			return SecuredRDFNodeImpl.getInstance((SecuredModel) m, holder
					.getBaseItem().inModel(m));
		}
		return holder.getBaseItem().inModel(m);
	}

	@Override
	public boolean isAnon()
	{
		return holder.getBaseItem().isAnon();
	}

	@Override
	public boolean isLiteral()
	{
		return holder.getBaseItem().isLiteral();
	}

	@Override
	public boolean isResource()
	{
		return holder.getBaseItem().isResource();
	}

	@Override
	public boolean isURIResource()
	{
		return holder.getBaseItem().isURIResource();
	}

	/**
     * An RDFNode is equal to another enhanced node n iff the underlying 
     * nodes are equal. We generalise to allow the other object to be any class
     * implementing asNode, because we allow other implemementations of
     * Resource, at least in principle.
     * This is deemed to be a complete and correct interpretation of RDFNode
     * equality, which is why this method has been marked final.
     * 
     * @param o An object to test for equality with this node
     * @return True if o is equal to this node.
     */
    @Override final public boolean equals( Object o )
        { 
    	checkRead();
    	return o instanceof FrontsNode && asNode().equals(((FrontsNode) o).asNode()); 
    	}
    
    /**
     * The hash code of an RDFnode is defined to be the same as the underlying node.
     * @return The hashcode as an int
     */
    @Override final public int hashCode() {
     	return holder.getBaseItem().asNode().hashCode();
    }
}
