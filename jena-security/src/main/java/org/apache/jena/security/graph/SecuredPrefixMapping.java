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
package org.apache.jena.security.graph;

import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.Map;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.impl.SecuredItem;

/**
 * The interface for secured PrefixMapping instances.
 * 
 * Use the SecuredPrefixMapping.Factory to create instances
 */
public interface SecuredPrefixMapping extends PrefixMapping, SecuredItem
{
	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String expandPrefix( final String prefixed )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public Map<String, String> getNsPrefixMap() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getNsPrefixURI( final String prefix )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String getNsURIPrefix( final String uri )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredPrefixMapping lock() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String qnameFor( final String uri ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredPrefixMapping removeNsPrefix( final String prefix )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean samePrefixMappingAs( final PrefixMapping other )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredPrefixMapping setNsPrefix( final String prefix,
			final String uri ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredPrefixMapping setNsPrefixes( final Map<String, String> map )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredPrefixMapping setNsPrefixes( final PrefixMapping other )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String shortForm( final String uri ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredPrefixMapping withDefaultMappings( final PrefixMapping map )
			throws AccessDeniedException;

}
