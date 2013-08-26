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

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RSIterator;
import com.hp.hpl.jena.rdf.model.ResourceF;
import com.hp.hpl.jena.rdf.model.Statement;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.impl.SecuredItem;

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
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( boolean o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( char o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( double o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( float o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( int o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeLiteralObject( long o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeObject( RDFNode o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeObject( String o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeObject( String o, boolean wellFormed )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeObject( String o, String l )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement changeObject( String o, String l, boolean wellFormed )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read, Update
	 * @sec.triple Create
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredReifiedStatement createReifiedStatement()
			throws AccessDeniedException;

	/**
	 * @sec.graph Read, Update
	 * @sec.triple Create
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredReifiedStatement createReifiedStatement( String uri )
			throws AccessDeniedException;

	@Override
	public SecuredAlt getAlt();

	@Override
	public SecuredBag getBag();

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean getBoolean() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public byte getByte() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public char getChar() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public double getDouble() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public float getFloat() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public int getInt() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getLanguage() throws AccessDeniedException;

	@Override
	public SecuredLiteral getLiteral();

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public long getLong() throws AccessDeniedException;

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
	 * @throws AccessDeniedException
	 */
	@Override
	public short getShort() throws AccessDeniedException;

	@Override
	public SecuredStatement getStatementProperty( Property p );

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getString() throws AccessDeniedException;

	@Override
	public SecuredResource getSubject();

	/**
	 * @sec.graph Read
	 * @sec.triple Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean hasWellFormedXML() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean isReified() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read
	 * @throws AccessDeniedException
	 */
	@Override
	public RSIterator listReifiedStatements() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement remove() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws AccessDeniedException
	 */
	@Override
	public void removeReification() throws AccessDeniedException;

}
