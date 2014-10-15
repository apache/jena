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

import com.hp.hpl.jena.rdf.model.Alt;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceF;

import org.apache.jena.security.AccessDeniedException;

/**
 * The interface for secured Alt instances.
 * 
 * Use the SecuredAlt.Factory to create instances
 */
@SuppressWarnings("deprecation")
public interface SecuredAlt extends Alt, SecuredContainer
{
	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredRDFNode getDefault() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt getDefaultAlt() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredBag getDefaultBag() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean getDefaultBoolean() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public byte getDefaultByte() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public char getDefaultChar() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public double getDefaultDouble() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public float getDefaultFloat() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public int getDefaultInt() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public String getDefaultLanguage() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredLiteral getDefaultLiteral() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public long getDefaultLong() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource getDefaultResource() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public SecuredResource getDefaultResource( final ResourceF f )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredSeq getDefaultSeq() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public short getDefaultShort() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws AccessDeniedException
	 */
	@Override
	public String getDefaultString() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final boolean o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final char o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final double o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final float o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final long o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final Object o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final RDFNode o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final String o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final String o, final String l )
			throws AccessDeniedException;

}
