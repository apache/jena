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

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.model.impl.SecuredStatementIterator;

/**
 * The interface for secured Resource instances.
 * 
 * Use the SecuredResource.Factory to create instances
 */
public interface SecuredResource extends Resource, SecuredRDFNode
{

	@Override
	public SecuredResource abort();

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource addLiteral( final Property p, final boolean o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addLiteral( final Property p, final char o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, value, d )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addLiteral( final Property value, final double d )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, value, d )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addLiteral( final Property value, final float d )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addLiteral( final Property p, final Literal o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addLiteral( final Property p, final long o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addLiteral( final Property p, final Object o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addProperty( final Property p, final RDFNode o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addProperty( final Property p, final String o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, literal(lexicalForm,datatype) )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addProperty( final Property p, final String lexicalForm,
			final RDFDatatype datatype ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public Resource addProperty( final Property p, final String o,
			final String l ) throws AccessDeniedException;

	@Override
	public SecuredResource asResource();

	@Override
	public SecuredResource begin();

	@Override
	public SecuredResource commit();

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean equals( final Object o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public AnonId getId() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getLocalName() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getNameSpace() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement getProperty( final Property p )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource getPropertyResourceValue( final Property p )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement getRequiredProperty( final Property p )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getURI() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final boolean o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final char o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final double o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final float o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final long o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final Object o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasProperty( final Property p ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasProperty( final Property p, final RDFNode o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasProperty( final Property p, final String o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,literal(o,l))
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasProperty( final Property p, final String o, final String l )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasURI( final String uri ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read on returned Statements
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatementIterator listProperties()
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read on returned Statements
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatementIterator listProperties( final Property p )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete on associated Statements
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource removeAll( final Property p )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete on all Statements
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource removeProperties() throws AccessDeniedException;
}
