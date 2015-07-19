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


import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;

/**
 * The interface for secured Statement instances.
 * 
 * Use the SecuredStatement.Factory to create instances
 */
public interface SecuredStatement extends Statement, SecuredItem
{
	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( boolean o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( char o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( double o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( float o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( int o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( long o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeObject( RDFNode o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeObject( String o )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeObject( String o, boolean wellFormed )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeObject( String o, String l )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement changeObject( String o, String l, boolean wellFormed )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Read, Update
	 * @sec.triple Create
	 * @throws ReadDeniedException
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredReifiedStatement createReifiedStatement()
			throws ReadDeniedException, UpdateDeniedException, AddDeniedException;

	/**
	 * @sec.graph Read, Update
	 * @sec.triple Create
	 * @throws ReadDeniedException
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredReifiedStatement createReifiedStatement( String uri )
			throws ReadDeniedException, UpdateDeniedException, AddDeniedException;

	@Override
	public SecuredAlt getAlt();

	@Override
	public SecuredBag getBag();

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean getBoolean() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public byte getByte() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public char getChar() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public double getDouble() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public float getFloat() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public int getInt() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String getLanguage() throws ReadDeniedException;

	@Override
	public SecuredLiteral getLiteral();

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public long getLong() throws ReadDeniedException;

	@Override
	public SecuredModel getModel();

	@Override
	public SecuredRDFNode getObject();

	@Override
	public SecuredProperty getPredicate();

	@Override
	public SecuredStatement getProperty( Property p );

	@Override
	public SecuredResource getResource();

	@Override
	@Deprecated
	public SecuredResource getResource( ResourceF f );

	@Override
	public SecuredSeq getSeq();

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public short getShort() throws ReadDeniedException;

	@Override
	public SecuredStatement getStatementProperty( Property p );

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String getString() throws ReadDeniedException;

	@Override
	public SecuredResource getSubject();

	/**
	 * @sec.graph Read
	 * @sec.triple Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean hasWellFormedXML() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean isReified() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read
	 * @throws ReadDeniedException
	 */
	@Override
	public RSIterator listReifiedStatements() throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredStatement remove() throws UpdateDeniedException, DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public void removeReification() throws UpdateDeniedException, DeleteDeniedException;

}
