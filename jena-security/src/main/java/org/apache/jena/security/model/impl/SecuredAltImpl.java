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

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Alt;
import com.hp.hpl.jena.rdf.model.AltHasNoDefaultException;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceF;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemInvoker;
import org.apache.jena.security.model.SecuredAlt;
import org.apache.jena.security.model.SecuredBag;
import org.apache.jena.security.model.SecuredLiteral;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.model.SecuredRDFNode;
import org.apache.jena.security.model.SecuredResource;
import org.apache.jena.security.model.SecuredSeq;

/**
 * Implementation of SecuredAlt to be used by a SecuredItemInvoker proxy.
 */
public class SecuredAltImpl extends SecuredContainerImpl implements SecuredAlt
{
	/**
	 * Get an instance of SecuredAlt.
	 * 
	 * @param securedModel
	 *            the Secured Model to use.
	 * @param alt
	 *            The Alt to be secured.
	 * @return The secured Alt instance.
	 */
	public static SecuredAlt getInstance( final SecuredModel securedModel,
			final Alt alt )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (alt == null)
		{
			throw new IllegalArgumentException("Alt may not be null");
		}
		final ItemHolder<Alt, SecuredAlt> holder = new ItemHolder<Alt, SecuredAlt>(
				alt);
		final SecuredAltImpl checker = new SecuredAltImpl(securedModel, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (alt instanceof SecuredAlt)
		{
			if (checker.isEquivalent((SecuredAlt) alt))
			{
				return (SecuredAlt) alt;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(alt.getClass(),
				checker));
	}

	// The item holder holding this SecuredAlt
	private final ItemHolder<? extends Alt, ? extends SecuredAlt> holder;

	/**
	 * Constructor.
	 * 
	 * @param securedModel
	 *            the securedModel to use.
	 * @param holder
	 *            The item holder that will hold this SecuredAlt.
	 */
	protected SecuredAltImpl( final SecuredModel securedModel,
			final ItemHolder<? extends Alt, ? extends SecuredAlt> holder )
	{
		super(securedModel, holder);
		this.holder = holder;
	}

	@Override
	public SecuredRDFNode getDefault()
	{
		// getDefaultStatement() calls checkRead
		return SecuredRDFNodeImpl.getInstance(getModel(), getDefaultStatement()
				.getObject());
	}

	@Override
	public SecuredAlt getDefaultAlt()
	{
		// getDefaultStatement() calls checkRead
		return SecuredAltImpl.getInstance(getModel(), getDefaultStatement()
				.getAlt());
	}

	@Override
	public SecuredBag getDefaultBag()
	{
		// getDefaultStatement() calls checkRead
		return SecuredBagImpl.getInstance(getModel(), getDefaultStatement()
				.getBag());
	}

	@Override
	public boolean getDefaultBoolean()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getBoolean();
	}

	@Override
	public byte getDefaultByte()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getByte();
	}

	@Override
	public char getDefaultChar()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getChar();
	}

	@Override
	public double getDefaultDouble()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getDouble();
	}

	@Override
	public float getDefaultFloat()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getFloat();
	}

	@Override
	public int getDefaultInt()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getInt();
	}

	@Override
	public String getDefaultLanguage()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getLanguage();
	}

	@Override
	public SecuredLiteral getDefaultLiteral()
	{
		// getDefaultStatement() calls checkRead
		return SecuredLiteralImpl.getInstance(getModel(), getDefaultStatement()
				.getLiteral());
	}

	@Override
	public long getDefaultLong()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getLong();
	}

	@Override
	public SecuredResource getDefaultResource()
	{
		// getDefaultStatement() calls checkRead
		return SecuredResourceImpl.getInstance(getModel(),
				getDefaultStatement().getResource());
	}

	@Override
	@Deprecated
	public SecuredResource getDefaultResource( final ResourceF f )
	{
		// getDefaultStatement() calls checkRead
		return SecuredResourceImpl.getInstance(getModel(),
				getDefaultStatement().getResource(f));
	}

	@Override
	public SecuredSeq getDefaultSeq()
	{
		// getDefaultStatement() calls checkRead
		return SecuredSeqImpl.getInstance(getModel(), getDefaultStatement()
				.getSeq());
	}

	@Override
	public short getDefaultShort()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getShort();

	}

	private Statement getDefaultStatement()
	{
		checkRead();
		final ExtendedIterator<Statement> iter = getStatementIterator(Action.Read);
		try
		{
			if (iter.hasNext())
			{
				return iter.next();
			}
			throw new AltHasNoDefaultException(this);
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public String getDefaultString()
	{
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getString();

	}

	/*
	 * private SecTriple getDefaultTriple()
	 * {
	 * final StmtIterator iter = holder.getBaseItem().getModel()
	 * .listStatements(this, RDF.li(1), (RDFNode) null);
	 * try
	 * {
	 * return iter.hasNext() ? iter.nextStatement().asTriple() : null;
	 * }
	 * finally
	 * {
	 * iter.close();
	 * }
	 * 
	 * }
	 * 
	 * private SecTriple getNewTriple( final SecTriple t, final Object o )
	 * {
	 * return new SecTriple(t.getSubject(), t.getPredicate(),
	 * SecNode.createLiteral(
	 * String.valueOf(o), "", false));
	 * }
	 */
	@Override
	public SecuredAlt setDefault( final boolean o )
	{
		return setDefault( asObject( o ));
	}

	@Override
	public SecuredAlt setDefault( final char o )
	{
		return setDefault( asObject( o ));
	}

	@Override
	public SecuredAlt setDefault( final double o )
	{
		return setDefault( asObject( o ));
	}

	@Override
	public SecuredAlt setDefault( final float o )
	{
		return setDefault( asObject( o ));
	}

	@Override
	public SecuredAlt setDefault( final long o )
	{
		return setDefault( asObject( o ));
	}

	@Override
	public SecuredAlt setDefault( final Object o )
	{
		return setDefault( asObject( o ));
	}

	@Override
	public SecuredAlt setDefault( final RDFNode o )
	{
		checkUpdate();			
		final ExtendedIterator<Statement> iter = getStatementIterator(Action.Read);
		try {
			if (iter.hasNext())
			{
				final Statement stmt = iter.next();
				final Triple t = stmt.asTriple();
				final Triple t2 = new Triple(t.getSubject(), t.getPredicate(),
						o.asNode());
				checkUpdate(t, t2);
				stmt.changeObject(o);
				return holder.getSecuredItem();
			}
			else
			{
				add( o );
				return holder.getSecuredItem();
			}
		}
		finally {
			iter.close();
		}
		
	}

	@Override
	public SecuredAlt setDefault( final String o )
	{
		return setDefault( asLiteral( o, "" ));
	}

	@Override
	public SecuredAlt setDefault( final String o, final String l )
	{
		return setDefault( asLiteral( o, l) );
	}
}
