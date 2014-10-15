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

import com.hp.hpl.jena.rdf.model.Container;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

import java.util.Set;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.model.impl.SecuredNodeIterator;

/**
 * The interface for secured Container instances.
 * 
 * Use one of the SecuredContainer derived class Factory methods to create
 * instances
 */
public interface SecuredContainer extends Container, SecuredResource
{

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer add( final boolean o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer add( final char o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer add( final double o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer add( final float o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer add( final long o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer add( final Object o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer add( final RDFNode o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer add( final String o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer add( final String o, final String l )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final boolean o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final char o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final double o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final float o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final long o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final Object o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final RDFNode o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final String o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final String o, final String l )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read on each triple ( this, rdf:li_? node ) returned by
	 *            iterator;
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredNodeIterator<RDFNode> iterator() throws AccessDeniedException;

	/**
	 * @param perms the Permissions required on each node returned
	 * @sec.graph Read
	 * @sec.triple Read + perms on each triple ( this, rdf:li_? node ) returned
	 *            by iterator;
	 * @throws AccessDeniedException
	 */
	public SecuredNodeIterator<RDFNode> iterator( Set<Action> perms )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete s as triple;
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredContainer remove( final Statement s )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public int size() throws AccessDeniedException;
}
