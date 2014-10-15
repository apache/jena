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

import org.apache.jena.security.impl.ItemHolder ;
import org.apache.jena.security.impl.SecuredItemImpl ;
import org.apache.jena.security.impl.SecuredItemInvoker ;
import org.apache.jena.security.model.* ;

import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.PropertyNotFoundException ;

/**
 * Implementation of SecuredStatement to be used by a SecuredItemInvoker proxy.
 */
public class SecuredStatementImpl extends SecuredItemImpl implements
		SecuredStatement
{
	/**
	 * get a SecuredStatement
	 * 
	 * @param securedModel
	 *            The secured model that provides the security context
	 * @param stmt
	 *            The statement to secure.
	 * @return the SecuredStatement
	 */
	public static SecuredStatement getInstance(
			final SecuredModel securedModel, final Statement stmt )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (stmt == null)
		{
			throw new IllegalArgumentException("Statement may not be null");
		}

		final ItemHolder<Statement, SecuredStatement> holder = new ItemHolder<Statement, SecuredStatement>(
				stmt);

		final SecuredStatementImpl checker = new SecuredStatementImpl(
				securedModel, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (stmt instanceof SecuredStatement)
		{
			if (checker.isEquivalent((SecuredStatement) stmt))
			{
				return (SecuredStatement) stmt;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(holder
				.getBaseItem().getClass(), checker));
	}

	// the item holder that contains this SecuredStatement.
	private final ItemHolder<Statement, SecuredStatement> holder;

	private final SecuredModel securedModel;

	/**
	 * Constructor.
	 * 
	 * @param securityEvaluator
	 *            The security evaluator to use.
	 * @param graphIRI
	 *            the graph IRI to verify against.
	 * @param holder
	 *            The item holder that will contain this SecuredStatement.
	 */
	private SecuredStatementImpl( final SecuredModel securedModel,
			final ItemHolder<Statement, SecuredStatement> holder )
	{
		super(securedModel, holder);
		this.holder = holder;
		this.securedModel = securedModel;
	}

	@Override
	public Triple asTriple()
	{
		checkRead();
		final Triple retval = holder.getBaseItem().asTriple();
		checkRead(retval);
		return retval;
	}

	@Override
	public boolean canCreate()
	{
		return super.canCreate() ? canCreate(holder.getBaseItem()) : false;
	}

	@Override
	public boolean canDelete()
	{
		return super.canDelete() ? canDelete(holder.getBaseItem()) : false;
	}

	@Override
	public boolean canRead()
	{
		return super.canRead() ? canRead(holder.getBaseItem()) : false;
	}

	@Override
	public SecuredStatement changeLiteralObject( final boolean o )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = getNewTriple(base, o);
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeLiteralObject(o));
	}

	@Override
	public SecuredStatement changeLiteralObject( final char o )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = getNewTriple(base, o);
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeLiteralObject(o));
	}

	@Override
	public SecuredStatement changeLiteralObject( final double o )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = getNewTriple(base, o);
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeLiteralObject(o));
	}

	@Override
	public SecuredStatement changeLiteralObject( final float o )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = getNewTriple(base, o);
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeLiteralObject(o));
	}

	@Override
	public SecuredStatement changeLiteralObject( final int o )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = getNewTriple(base, o);
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeLiteralObject(o));
	}

	@Override
	public SecuredStatement changeLiteralObject( final long o )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = getNewTriple(base, o);
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeLiteralObject(o));
	}

	@Override
	public SecuredStatement changeObject( final RDFNode o )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = new Triple(base.getSubject(),
				base.getPredicate(), o.asNode());
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeObject(o));
	}

	@Override
	public SecuredStatement changeObject( final String o )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = getNewTriple(base, o);
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeObject(o));
	}

	@Override
	public SecuredStatement changeObject( final String o,
			final boolean wellFormed )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = new Triple(base.getSubject(),
				base.getPredicate(), NodeFactory.createLiteral(o, "", wellFormed));
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeObject(o));
	}

	@Override
	public SecuredStatement changeObject( final String o, final String l )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = new Triple(base.getSubject(),
				base.getPredicate(), NodeFactory.createLiteral(o, l, false));
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeObject(o, l));
	}

	@Override
	public SecuredStatement changeObject( final String o, final String l,
			final boolean wellFormed )
	{
		checkUpdate();
		final Triple base = holder.getBaseItem().asTriple();
		final Triple newBase = new Triple(base.getSubject(),
				base.getPredicate(), NodeFactory.createLiteral(o, l, wellFormed));
		checkUpdate(base, newBase);
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().changeObject(o, l, wellFormed));
	}

	@Override
	public SecuredReifiedStatement createReifiedStatement()
	{
		checkUpdate();
		checkCreateReified(null,
				SecuredItemImpl.convert(holder.getBaseItem().asTriple()));
		return SecuredReifiedStatementImpl.getInstance(getModel(), holder
				.getBaseItem().createReifiedStatement());
	}

	@Override
	public SecuredReifiedStatement createReifiedStatement( final String uri )
	{
		checkUpdate();
		checkCreateReified(uri,
				SecuredItemImpl.convert(holder.getBaseItem().asTriple()));
		return SecuredReifiedStatementImpl.getInstance(getModel(), holder
				.getBaseItem().createReifiedStatement(uri));
	}

	@Override
	public SecuredAlt getAlt()
	{
		return SecuredAltImpl.getInstance(getModel(), holder.getBaseItem()
				.getAlt());
	}

	@Override
	public SecuredBag getBag()
	{
		return SecuredBagImpl.getInstance(getModel(), holder.getBaseItem()
				.getBag());
	}

	@Override
	public boolean getBoolean()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getBoolean();
	}

	@Override
	public byte getByte()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getByte();
	}

	@Override
	public char getChar()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getChar();

	}

	@Override
	public double getDouble()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getDouble();
	}

	@Override
	public float getFloat()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getFloat();
	}

	@Override
	public int getInt()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getInt();
	}

	@Override
	public String getLanguage()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getLiteral().getLanguage();
	}

	@Override
	public SecuredLiteral getLiteral()
	{
		return SecuredLiteralImpl.getInstance(getModel(), holder.getBaseItem()
				.getLiteral());
	}

	@Override
	public long getLong()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getLong();
	}

	@Override
	public SecuredModel getModel()
	{
		return securedModel;
	}

	private Triple getNewTriple( final Triple t, final Object o )
	{
		return new Triple(t.getSubject(), t.getPredicate(), 
		                  NodeFactory.createLiteral(String.valueOf(o), "", false));
	}

	@Override
	public SecuredRDFNode getObject()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		final RDFNode rdfNode = holder.getBaseItem().getObject();
		return SecuredRDFNodeImpl.getInstance(getModel(), rdfNode);

	}

	@Override
	public SecuredProperty getPredicate()
	{
		return SecuredPropertyImpl.getInstance(getModel(), holder.getBaseItem()
				.getPredicate());
	}

	@Override
	public SecuredStatement getProperty( final Property p )
	{
		final StmtIterator s = holder
				.getBaseItem()
				.getModel()
				.listStatements(holder.getBaseItem().getObject().asResource(),
						p, (RDFNode) null);
		final SecuredStatementIterator iter = new SecuredStatementIterator(
				getModel(), s);
		try
		{
			if (iter.hasNext())
			{
				return SecuredStatementImpl
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
	}

	@Override
	public SecuredResource getResource()
	{
		return SecuredResourceImpl.getInstance(getModel(), holder.getBaseItem()
				.getResource());
	}

	@Override
	@Deprecated
	public SecuredResource getResource( final ResourceF f )
	{
		return SecuredResourceImpl.getInstance(getModel(), holder.getBaseItem()
				.getResource(f));
	}

	@Override
	public SecuredSeq getSeq()
	{
		return SecuredSeqImpl.getInstance(getModel(), holder.getBaseItem()
				.getSeq());
	}

	@Override
	public short getShort()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getShort();
	}

	@Override
	public SecuredStatement getStatementProperty( final Property p )
	{
		final RSIterator rsIter = holder.getBaseItem().listReifiedStatements();
		try
		{
			while (rsIter.hasNext())
			{
				final ReifiedStatement s = rsIter.next();
				if (s.hasProperty(p))
				{
					return SecuredStatementImpl.getInstance(getModel(),
							s.getProperty(p));
				}
			}
			throw new PropertyNotFoundException(p);
		}
		finally
		{
			rsIter.close();
		}
	}

	@Override
	public String toString()
	{
		if (canRead() && canRead(holder.getBaseItem().asTriple()))
		{
			return holder.getBaseItem().toString();
		}
		else
		{
			return super.toString();
		}
	}
	
	@Override
	public String getString()
	{ return getLiteral().getLexicalForm(); }

	@Override
	public SecuredResource getSubject()
	{
		return SecuredResourceImpl.getInstance(getModel(), holder.getBaseItem()
				.getSubject());
	}

	@Override
	public boolean hasWellFormedXML()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().getLiteral().isWellFormedXML();
	}

	@Override
	public boolean isReified()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return holder.getBaseItem().isReified();
	}

	@Override
	public RSIterator listReifiedStatements()
	{
		checkRead();
		checkRead(holder.getBaseItem().asTriple());
		return new SecuredRSIterator(getModel(), holder.getBaseItem()
				.listReifiedStatements());
	}

	@Override
	public SecuredStatement remove()
	{
		checkUpdate();
		checkDelete(holder.getBaseItem());
		holder.getBaseItem().remove();
		return holder.getSecuredItem();
	}

	@Override
	public void removeReification()
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			StmtIterator iter = null;
			final RSIterator rsIter = holder.getBaseItem()
					.listReifiedStatements();
			try
			{
				while (rsIter.hasNext())
				{
					final ReifiedStatement stmt = rsIter.next();
					iter = stmt.listProperties();
					while (iter.hasNext())
					{
						final Statement s = iter.next();
						checkDelete(s);
					}
				}
			}
			finally
			{
				rsIter.close();
				if (iter != null)
				{
					iter.close();
				}
			}
		}
		holder.getBaseItem().removeReification();
	}

}
