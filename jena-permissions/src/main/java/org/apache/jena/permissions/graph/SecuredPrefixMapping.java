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
package org.apache.jena.permissions.graph;

import java.util.Map;

import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;

/**
 * The interface for secured PrefixMapping instances.
 * 
 * Use the SecuredPrefixMapping.Factory to create instances
 */
public interface SecuredPrefixMapping extends PrefixMapping, SecuredItem
{
	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String expandPrefix( final String prefixed )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public Map<String, String> getNsPrefixMap() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String getNsPrefixURI( final String prefix )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String getNsURIPrefix( final String uri )
			throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredPrefixMapping lock() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String qnameFor( final String uri ) throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredPrefixMapping removeNsPrefix( final String prefix )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean samePrefixMappingAs( final PrefixMapping other )
			throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredPrefixMapping setNsPrefix( final String prefix,
			final String uri ) throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredPrefixMapping setNsPrefixes( final Map<String, String> map )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredPrefixMapping setNsPrefixes( final PrefixMapping other )
			throws UpdateDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String shortForm( final String uri ) throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredPrefixMapping withDefaultMappings( final PrefixMapping map )
			throws UpdateDeniedException;

}
