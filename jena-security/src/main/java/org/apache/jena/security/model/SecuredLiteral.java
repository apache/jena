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

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;

import org.apache.jena.security.AccessDeniedException;

/**
 * The interface for secured Literal instances.
 * 
 * Use the SecuredLiteral.Factory to create instances
 */
public interface SecuredLiteral extends Literal, SecuredRDFNode
{

	@Override
	public SecuredLiteral asLiteral();

	// @Override
	// public SecuredResource asResource();

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean getBoolean() throws AccessDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public byte getByte() throws AccessDeniedException, DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public char getChar() throws AccessDeniedException, DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public RDFDatatype getDatatype() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getDatatypeURI() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public double getDouble() throws AccessDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public float getFloat() throws AccessDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public int getInt() throws AccessDeniedException, DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getLanguage() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getLexicalForm() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public long getLong() throws AccessDeniedException, DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public short getShort() throws AccessDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getString() throws AccessDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public Object getValue() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public Literal inModel( final Model m ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean isWellFormedXML() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean sameValueAs( final Literal other )
			throws AccessDeniedException;

}
