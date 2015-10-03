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

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.ReadDeniedException;

/**
 * The interface for secured Literal instances.
 * 
 * Use the SecuredLiteral.Factory to create instances
 */
public interface SecuredLiteral extends Literal, SecuredRDFNode {

	@Override
	public SecuredLiteral asLiteral();

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public boolean getBoolean() throws ReadDeniedException,
			DatatypeFormatException, AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public byte getByte() throws ReadDeniedException, DatatypeFormatException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public char getChar() throws ReadDeniedException, DatatypeFormatException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public RDFDatatype getDatatype() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public String getDatatypeURI() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public double getDouble() throws ReadDeniedException,
			DatatypeFormatException, AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public float getFloat() throws ReadDeniedException,
			DatatypeFormatException, AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 * @throws DatatypeFormatException
	 */
	@Override
	public int getInt() throws ReadDeniedException, DatatypeFormatException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public String getLanguage() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public String getLexicalForm() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public long getLong() throws ReadDeniedException, DatatypeFormatException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public short getShort() throws ReadDeniedException,
			DatatypeFormatException, AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public String getString() throws ReadDeniedException,
			DatatypeFormatException, AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public Object getValue() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public Literal inModel(final Model m) throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public boolean isWellFormedXML() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public boolean sameValueAs(final Literal other) throws ReadDeniedException,
			AuthenticationRequiredException;

}
