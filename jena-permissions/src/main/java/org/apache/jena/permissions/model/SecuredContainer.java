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

import java.util.Set;

import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.model.impl.SecuredNodeIterator;
import org.apache.jena.rdf.model.Container ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Statement ;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;

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
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredContainer add( final boolean o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredContainer add( final char o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredContainer add( final double o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredContainer add( final float o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredContainer add( final long o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredContainer add( final Object o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredContainer add( final RDFNode o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredContainer add( final String o ) throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( this, RDF.li, o );
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredContainer add( final String o, final String l )
			throws UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final boolean o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final char o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final double o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final float o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final long o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final Object o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final RDFNode o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final String o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( this, RDF.li, o );
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final String o, final String l )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read on each triple ( this, rdf:li_? node ) returned by
	 *            iterator;
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredNodeIterator<RDFNode> iterator() throws ReadDeniedException;

	/**
	 * @param perms the Permissions required on each node returned
	 * @sec.graph Read
	 * @sec.triple Read + perms on each triple ( this, rdf:li_? node ) returned
	 *            by iterator;
	 * @throws ReadDeniedException
	 */
	public SecuredNodeIterator<RDFNode> iterator( Set<Action> perms )
			throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete s as triple;
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredContainer remove( final Statement s )
			throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public int size() throws ReadDeniedException;
}
