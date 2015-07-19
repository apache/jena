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

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.permissions.model.impl.SecuredStatementIterator;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;

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
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredResource addLiteral( final Property p, final boolean o )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addLiteral( final Property p, final char o )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, value, d )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addLiteral( final Property value, final double d )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, value, d )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addLiteral( final Property value, final float d )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addLiteral( final Property p, final Literal o )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addLiteral( final Property p, final long o )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addLiteral( final Property p, final Object o )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addProperty( final Property p, final RDFNode o )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addProperty( final Property p, final String o )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, literal(lexicalForm,datatype) )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addProperty( final Property p, final String lexicalForm,
			final RDFDatatype datatype ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create (this, p, o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Resource addProperty( final Property p, final String o,
			final String l ) throws UpdateDeniedException, AddDeniedException;

	@Override
	public SecuredResource asResource();

	@Override
	public SecuredResource begin();

	@Override
	public SecuredResource commit();

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean equals( final Object o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public AnonId getId() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String getLocalName() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String getNameSpace() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatement getProperty( final Property p )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResource getPropertyResourceValue( final Property p )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatement getRequiredProperty( final Property p )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String getURI() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final boolean o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final char o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final double o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final float o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final long o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasLiteral( final Property p, final Object o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasProperty( final Property p ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasProperty( final Property p, final RDFNode o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,o)
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasProperty( final Property p, final String o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple(this,p,literal(o,l))
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasProperty( final Property p, final String o, final String l )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasURI( final String uri ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read on returned Statements
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatementIterator listProperties()
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read on returned Statements
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatementIterator listProperties( final Property p )
			throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete on associated Statements
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredResource removeAll( final Property p )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete on all Statements
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredResource removeProperties() throws UpdateDeniedException, DeleteDeniedException;
}
