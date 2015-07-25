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

import org.apache.jena.rdf.model.Alt ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.ResourceF ;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;

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
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredRDFNode getDefault() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredAlt getDefaultAlt() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredBag getDefaultBag() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean getDefaultBoolean() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public byte getDefaultByte() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public char getDefaultChar() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public double getDefaultDouble() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public float getDefaultFloat() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public int getDefaultInt() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public String getDefaultLanguage() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredLiteral getDefaultLiteral() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public long getDefaultLong() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResource getDefaultResource() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	@Deprecated
	public SecuredResource getDefaultResource( final ResourceF f )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredSeq getDefaultSeq() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public short getDefaultShort() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this, RDF.li(1), o )
	 * @throws ReadDeniedException
	 */
	@Override
	public String getDefaultString() throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final boolean o )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final char o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final double o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final float o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final long o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final Object o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final RDFNode o )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final String o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update SecTriple(this, RDF.li(1), existing ), SecTriple(this,
	 *            RDF.li(1), o )
	 * @sec.triple Create SecTriple(this, RDF.li(1), o ) if no current default
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt setDefault( final String o, final String l )
			throws UpdateDeniedException, AddDeniedException;

}
