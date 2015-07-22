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

import org.apache.jena.datatypes.DatatypeFormatException ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.shared.ReadDeniedException;

/**
 * The interface for secured Literal instances.
 * 
 * Use the SecuredLiteral.Factory to create instances
 */
public interface SecuredLiteral extends Literal, SecuredRDFNode
{

	@Override
	public SecuredLiteral asLiteral();

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public boolean getBoolean() throws ReadDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public byte getByte() throws ReadDeniedException, DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public char getChar() throws ReadDeniedException, DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public RDFDatatype getDatatype() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String getDatatypeURI() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public double getDouble() throws ReadDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public float getFloat() throws ReadDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public int getInt() throws ReadDeniedException, DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String getLanguage() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public String getLexicalForm() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public long getLong() throws ReadDeniedException, DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public short getShort() throws ReadDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws DatatypeFormatException
	 */
	@Override
	public String getString() throws ReadDeniedException,
			DatatypeFormatException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public Object getValue() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public Literal inModel( final Model m ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean isWellFormedXML() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean sameValueAs( final Literal other )
			throws ReadDeniedException;

}
