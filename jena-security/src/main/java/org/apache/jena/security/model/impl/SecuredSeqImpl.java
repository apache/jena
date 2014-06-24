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
import org.apache.jena.security.impl.SecuredItemInvoker ;
import org.apache.jena.security.model.* ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.Filter ;
import com.hp.hpl.jena.vocabulary.RDF ;

/**
 * Implementation of SecuredSeq to be used by a SecuredItemInvoker proxy.
 * 
 * Sequence may have breaks in the order.
 * http://www.w3.org/TR/2004/REC-rdf-mt-20040210/#Containers
 * 
 */
public class SecuredSeqImpl extends SecuredContainerImpl implements SecuredSeq
{
	private class RDFNodeFilter extends Filter<Statement>
	{
		private final RDFNode n;

		public RDFNodeFilter( final RDFNode n )
		{
			this.n = n;
		}

		@Override
		public boolean accept( final Statement o )
		{
			return (o.getPredicate().getOrdinal() != 0)
					&& n.equals(o.getObject());
		}

	}

	/**
	 * get a SecuredSeq.
	 * 
	 * @param securedModel
	 *            The secured model that provides the security context
	 * @param seq
	 *            The Seq to secure.
	 * @return the SecuredSeq
	 */
	public static SecuredSeq getInstance( final SecuredModel securedModel,
			final Seq seq )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (seq == null)
		{
			throw new IllegalArgumentException("Seq may not be null");
		}
		final ItemHolder<Seq, SecuredSeq> holder = new ItemHolder<Seq, SecuredSeq>(
				seq);
		final SecuredSeqImpl checker = new SecuredSeqImpl(securedModel, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (seq instanceof SecuredSeq)
		{
			if (checker.isEquivalent((SecuredSeq) seq))
			{
				return (SecuredSeq) seq;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(seq.getClass(),
				checker));
	}

	// the item holder that contains this SecuredSeq.
	private final ItemHolder<? extends Seq, ? extends SecuredSeq> holder;

	/**
	 * Constructor.
	 * 
	 * @param securedModel
	 *            The secured model that provides the security context
	 * @param holder
	 *            The item holder that will contain this SecuredSeq.
	 */
	protected SecuredSeqImpl( final SecuredModel securedModel,
			final ItemHolder<? extends Seq, ? extends SecuredSeq> holder )
	{
		super(securedModel, holder);
		this.holder = holder;
	}

	@Override
	public SecuredSeq add( final int index, final boolean o )
	{
		return add( index, asObject( o ));
	}

	@Override
	public SecuredSeq add( final int index, final char o )
	{
		return add( index, asObject( o ));
	}

	@Override
	public SecuredSeq add( final int index, final double o )
	{
		return add( index, asObject( o ));
	}

	@Override
	public SecuredSeq add( final int index, final float o )
	{
		return add( index, asObject( o ));
	}

	@Override
	public SecuredSeq add( final int index, final long o )
	{
		return add( index, asObject( o ));
	}

	@Override
	public SecuredSeq add( final int index, final Object o )
	{
		return add( index, asObject( o ));
	}

	@Override
	public SecuredSeq add( final int index, final RDFNode o )
	{
		checkUpdate();
		final Literal l = holder.getBaseItem().getModel().createTypedLiteral(o);
		checkCreate(index, l);
		holder.getBaseItem().add(index, o);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredSeq add( final int index, final String o )
	{
		return add( index, o, "" );
	}

	@Override
	public SecuredSeq add( final int index, final String o, final String l )
	{
		return add( index, holder.getBaseItem().getModel().createLiteral(o, l));
	}

	private void checkCreate( final int index, final Literal l )
	{
		checkCreate(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), l.asNode()));
	}

	private Statement containerIndexOf( final RDFNode n )
	{
		final ExtendedIterator<Statement> iter = listProperties().filterKeep(
				new RDFNodeFilter(n));
		try
		{
			if (iter.hasNext())
			{
				return iter.next();
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

	@Override
	public SecuredAlt getAlt( final int index )
	{
		checkRead();
		final Alt a = holder.getBaseItem().getAlt(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), a.asNode()));
		return SecuredAltImpl.getInstance(getModel(), a);
	}

	@Override
	public SecuredBag getBag( final int index )
	{
		checkRead();
		final Bag b = holder.getBaseItem().getBag(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), b.asNode()));
		return SecuredBagImpl.getInstance(getModel(), b);
	}

	@Override
	public boolean getBoolean( final int index )
	{
		checkRead();
		final boolean retval = holder.getBaseItem().getBoolean(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), asObject(retval).asNode()));
		return retval;
	}

	@Override
	public byte getByte( final int index )
	{
		checkRead();
		final byte retval = holder.getBaseItem().getByte(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), asObject(retval).asNode()));
		return retval;
	}

	@Override
	public char getChar( final int index )
	{
		checkRead();
		final char retval = holder.getBaseItem().getChar(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), asObject(retval).asNode()));
		return retval;

	}

	@Override
	public double getDouble( final int index )
	{
		checkRead();
		final double retval = holder.getBaseItem().getDouble(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), asObject(retval).asNode()));
		return retval;
	}

	@Override
	public float getFloat( final int index )
	{
		checkRead();
		final float retval = holder.getBaseItem().getFloat(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), asObject(retval).asNode()));
		return retval;
	}

	@Override
	public int getInt( final int index )
	{
		checkRead();
		final int retval = holder.getBaseItem().getInt(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), asObject(retval).asNode()));
		return retval;
	}

	@Override
	public String getLanguage( final int index )
	{
		checkRead();
		final Literal literal = holder.getBaseItem().getLiteral(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), literal.asNode()));
		return literal.getLanguage();
	}

	@Override
	public SecuredLiteral getLiteral( final int index )
	{
		checkRead();
		final Literal literal = holder.getBaseItem().getLiteral(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), literal.asNode()));
		return SecuredLiteralImpl.getInstance(getModel(), literal);
	}

	@Override
	public long getLong( final int index )
	{
		checkRead();
		final long retval = holder.getBaseItem().getLong(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), asObject(retval).asNode()));
		return retval;
	}

	@Override
	public SecuredRDFNode getObject( final int index )
	{
		checkRead();
		final RDFNode retval = holder.getBaseItem().getObject(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), retval.asNode()));
		return SecuredRDFNodeImpl.getInstance(getModel(), retval);
	}

	@Override
	public SecuredResource getResource( final int index )
	{
		checkRead();
		final Resource retval = holder.getBaseItem().getResource(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), retval.asNode()));
		return SecuredResourceImpl.getInstance(getModel(), retval);
	}

	@Override
	@Deprecated
	public SecuredResource getResource( final int index, final ResourceF f )
	{
		checkRead();
		final Resource retval = holder.getBaseItem().getResource(index, f);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), retval.asNode()));
		return SecuredResourceImpl.getInstance(getModel(), retval);
	}

	@Override
	public SecuredSeq getSeq( final int index )
	{
		checkRead();
		final Seq retval = holder.getBaseItem().getSeq(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), retval.asNode()));
		return SecuredSeqImpl.getInstance(getModel(), retval);
	}

	@Override
	public short getShort( final int index )
	{
		checkRead();
		final short retval = holder.getBaseItem().getShort(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), ResourceFactory.createTypedLiteral(retval).asNode()));
		return retval;
	}

	@Override
	public String getString( final int index )
	{
		checkRead();
		final String retval = holder.getBaseItem().getString(index);
		checkRead(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
				.asNode(), ResourceFactory.createTypedLiteral(retval).asNode()));
		return retval;
	}

	@Override
	public int indexOf( final boolean o )
	{
		return indexOf( asObject( o ));
	}

	@Override
	public int indexOf( final char o )
	{
		return indexOf( asObject( o ));
	}

	@Override
	public int indexOf( final double o )
	{
		return indexOf( asObject( o ));
	}

	@Override
	public int indexOf( final float o )
	{
		return indexOf( asObject( o ));
	}

	@Override
	public int indexOf( final long o )
	{
		return indexOf( asObject( o ));
	}

	@Override
	public int indexOf( final Object o )
	{
		return indexOf( asObject( o ));
	}

	@Override
	public int indexOf( final RDFNode o )
	{
		checkRead();
		final Statement stmt = containerIndexOf(o);
		if (stmt == null)
		{
			return 0;
		}
		checkRead(stmt);
		return stmt.getPredicate().getOrdinal();
	}

	@Override
	public int indexOf( final String o )
	{
		return indexOf( asLiteral( o, "" ));
	}

	@Override
	public int indexOf( final String o, final String l )
	{
		return indexOf( asLiteral( o, l));
	}

	@Override
	public SecuredSeq remove( final int index )
	{
		checkUpdate();
		final RDFNode rdfNode = holder.getBaseItem().getObject(index);
		if (rdfNode != null)
		{
			checkDelete(new Triple(holder.getBaseItem().asNode(), RDF.li(index)
					.asNode(), rdfNode.asNode()));
			holder.getBaseItem().remove(index);
		}
		return holder.getSecuredItem();
	}

	@Override
	public SecuredSeq set( final int index, final boolean o )
	{
		return set( index, asObject( o ));
	}

	@Override
	public SecuredSeq set( final int index, final char o )
	{
		return set( index, asObject( o ));
	}

	@Override
	public SecuredSeq set( final int index, final double o )
	{
		return set( index, asObject( o ));
	}

	@Override
	public SecuredSeq set( final int index, final float o )
	{
		return set( index, asObject( o ));
	}

	@Override
	public SecuredSeq set( final int index, final long o )
	{
		return set( index, asObject( o ));
	}

	@Override
	public SecuredSeq set( final int index, final Object o )
	{
		return set( index, asObject( o ));
	}

	@Override
	public SecuredSeq set( final int index, final RDFNode o )
	{
		checkUpdate();
		final Triple t2 = new Triple(holder.getBaseItem().asNode(), RDF.li(
				index).asNode(), o.asNode());
		final RDFNode rdfNode = holder.getBaseItem().getObject(index);
		if (rdfNode != null)
		{
			final Triple t1 = new Triple(holder.getBaseItem().asNode(), RDF.li(
					index).asNode(), rdfNode.asNode());
			checkUpdate(t1, t2);
		}
		else
		{
			checkCreate(t2);
		}
		holder.getBaseItem().set(index, o);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredSeq set( final int index, final String o )
	{
		return set( index, asLiteral( o, "" ));
	}

	@Override
	public SecuredSeq set( final int index, final String o, final String l )
	{
		return set( index, asLiteral(o, l));
	}
}
