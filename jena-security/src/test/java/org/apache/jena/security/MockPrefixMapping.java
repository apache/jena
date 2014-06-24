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
package org.apache.jena.security;

import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.Collections;
import java.util.Map;

public class MockPrefixMapping implements PrefixMapping
{

	@Override
	public String expandPrefix( final String prefixed )
	{
		return prefixed;
	}

	@Override
	public Map<String, String> getNsPrefixMap()
	{
		return Collections.emptyMap();
	}

	@Override
	public String getNsPrefixURI( final String prefix )
	{
		return null;
	}

	@Override
	public String getNsURIPrefix( final String uri )
	{
		return null;
	}

	@Override
	public PrefixMapping lock()
	{
		return this;
	}

	@Override
	public String qnameFor( final String uri )
	{
		return null;
	}

	@Override
	public PrefixMapping removeNsPrefix( final String prefix )
	{
		return this;
	}

	@Override
	public boolean samePrefixMappingAs( final PrefixMapping other )
	{
		return false;
	}

	@Override
	public PrefixMapping setNsPrefix( final String prefix, final String uri )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public PrefixMapping setNsPrefixes( final Map<String, String> map )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public PrefixMapping setNsPrefixes( final PrefixMapping other )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String shortForm( final String uri )
	{
		return uri;
	}

	@Override
	public PrefixMapping withDefaultMappings( final PrefixMapping map )
	{
		throw new UnsupportedOperationException();
	}
}