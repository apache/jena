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
package org.apache.jena.security.model;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceF;
import com.hp.hpl.jena.rdf.model.Seq;

import org.apache.jena.security.AccessDeniedException;

/**
 * The interface for secured Seq instances.
 * 
 * Use the SecuredSeq.Factory to create instances
 * 
 * Sequence may have breaks in the order.
 * http://www.w3.org/TR/2004/REC-rdf-mt-20040210/#Containers
 * 
 */
public interface SecuredSeq extends Seq, SecuredContainer
{
	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq add( final int index, final boolean o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq add( final int index, final char o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq add( final int index, final double o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq add( final int index, final float o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq add( final int index, final long o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq add( final int index, final Object o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq add( final int index, final RDFNode o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq add( final int index, final String o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq add( final int index, final String o, final String l )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredAlt getAlt( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredBag getBag( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public boolean getBoolean( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public byte getByte( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public char getChar( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public double getDouble( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public float getFloat( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int getInt( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public String getLanguage( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredLiteral getLiteral( final int index )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public long getLong( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredRDFNode getObject( final int index )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredResource getResource( final int index )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	@Deprecated
	public SecuredResource getResource( final int index, final ResourceF f )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public SecuredSeq getSeq( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public short getShort( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public String getString( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int indexOf( final boolean o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int indexOf( final char o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int indexOf( final double o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int indexOf( final float o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int indexOf( final long o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int indexOf( final Object o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int indexOf( final RDFNode o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int indexOf( final String o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li(1), o )
	 */
	@Override
	public int indexOf( final String o, final String l )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete SecTriple( this, RDF.li(1), o )
	 * @sec.triple Update Triples after index
	 */
	@Override
	public SecuredSeq remove( final int index ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 */
	@Override
	public SecuredSeq set( final int index, final boolean o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 */
	@Override
	public SecuredSeq set( final int index, final char o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 */
	@Override
	public SecuredSeq set( final int index, final double o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 */
	@Override
	public SecuredSeq set( final int index, final float o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 */
	@Override
	public SecuredSeq set( final int index, final long o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 */
	@Override
	public SecuredSeq set( final int index, final Object o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 */
	@Override
	public SecuredSeq set( final int index, final RDFNode o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 */
	@Override
	public SecuredSeq set( final int index, final String o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple( this, RDF.li(index), old ) SecTriple( this,
	 *            RDF.li(index), o )
	 */
	@Override
	public SecuredSeq set( final int index, final String o, final String l )
			throws AccessDeniedException;

}
