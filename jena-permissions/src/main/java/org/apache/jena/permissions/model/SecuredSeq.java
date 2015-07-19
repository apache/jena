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
package org.apache.jena.permissions.model;


import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.ResourceF ;
import org.apache.jena.rdf.model.Seq ;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;

/**
 * The interface for secured Seq instances.
 * 
 * Use the SecuredSeq.Factory to create instances
 * 
 * Sequence may have breaks in the order.
 * http://www.w3.org/TR/2004/REC-rdf-mt-20040210/#Containers
 * 
 */
@SuppressWarnings("deprecation")
public interface SecuredSeq extends Seq, SecuredContainer
{
	/**	 * 
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq add( final int index, final boolean o )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq add( final int index, final char o )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq add( final int index, final double o )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq add( final int index, final float o )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq add( final int index, final long o )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq add( final int index, final Object o )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq add( final int index, final RDFNode o )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq add( final int index, final String o )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq add( final int index, final String o, final String l )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredAlt getAlt( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredBag getBag( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean getBoolean( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public byte getByte( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public char getChar( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public double getDouble( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public float getFloat( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int getInt( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public String getLanguage( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredLiteral getLiteral( final int index )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public long getLong( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredRDFNode getObject( final int index )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResource getResource( final int index )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	@Deprecated
	public SecuredResource getResource( final int index, final ResourceF f )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredSeq getSeq( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public short getShort( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public String getString( final int index ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int indexOf( final boolean o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int indexOf( final char o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int indexOf( final double o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int indexOf( final float o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int indexOf( final long o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int indexOf( final Object o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int indexOf( final RDFNode o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int indexOf( final String o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int indexOf( final String o, final String l )
			throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete SecTriple( this, RDF.li(1), o )
	 * @sec.triple Update Triples after index
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredSeq remove( final int index ) throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredSeq set( final int index, final boolean o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredSeq set( final int index, final char o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredSeq set( final int index, final double o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredSeq set( final int index, final float o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredSeq set( final int index, final long o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredSeq set( final int index, final Object o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredSeq set( final int index, final RDFNode o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredSeq set( final int index, final String o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredSeq set( final int index, final String o, final String l )
			throws UpdateDeniedException;

}
