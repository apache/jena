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
package org.apache.jena.security.graph.impl;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.security.graph.SecuredPrefixMapping;
import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemImpl;

/**
 * Implementation of SecuredPrefixMapping to be used by a SecuredItemInvoker
 * proxy.
 */
public class SecuredPrefixMappingImpl extends SecuredItemImpl implements
		SecuredPrefixMapping
{
	// the item holder that holds this SecuredPrefixMapping
	private final ItemHolder<PrefixMapping, SecuredPrefixMapping> holder;

	/**
	 * Constructor
	 * 
	 * @param graph
	 *            The Secured graph this mapping is for.
	 * @param holder
	 *            The item holder that will contain this SecuredPrefixMapping.
	 */
	SecuredPrefixMappingImpl( final SecuredGraphImpl graph,
			final ItemHolder<PrefixMapping, SecuredPrefixMapping> holder )
	{
		super(graph, holder);
		this.holder = holder;
	}

	@Override
	public String expandPrefix( final String prefixed )
	{
		checkRead();
		return holder.getBaseItem().expandPrefix(prefixed);
	}

	@Override
	public Map<String, String> getNsPrefixMap()
	{
		checkRead();
		return holder.getBaseItem().getNsPrefixMap();
	}

	@Override
	public String getNsPrefixURI( final String prefix )
	{
		checkRead();
		return holder.getBaseItem().getNsPrefixURI(prefix);
	}

	@Override
	public String getNsURIPrefix( final String uri )
	{
		checkRead();
		return holder.getBaseItem().getNsURIPrefix(uri);
	}

	@Override
	public SecuredPrefixMapping lock()
	{
		checkUpdate();
		holder.getBaseItem().lock();
		return holder.getSecuredItem();
	}

	@Override
	public String qnameFor( final String uri )
	{
		checkRead();
		return holder.getBaseItem().qnameFor(uri);
	}

	@Override
	public SecuredPrefixMapping removeNsPrefix( final String prefix )
	{
		checkUpdate();
		holder.getBaseItem().removeNsPrefix(prefix);
		return holder.getSecuredItem();
	}

	@Override
	public boolean samePrefixMappingAs( final PrefixMapping other )
	{
		checkRead();
		return holder.getBaseItem().samePrefixMappingAs(other);
	}

	@Override
	public SecuredPrefixMapping setNsPrefix( final String prefix,
			final String uri )
	{
		checkUpdate();
		holder.getBaseItem().setNsPrefix(prefix, uri);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredPrefixMapping setNsPrefixes( final Map<String, String> map )
	{
		checkUpdate();
		holder.getBaseItem().setNsPrefixes(map);
		return holder.getSecuredItem();
	}

	@Override
	public SecuredPrefixMapping setNsPrefixes( final PrefixMapping other )
	{
		checkUpdate();
		holder.getBaseItem().setNsPrefixes(other);
		return holder.getSecuredItem();
	}

	@Override
	public String shortForm( final String uri )
	{
		checkRead();
		return holder.getBaseItem().shortForm(uri);
	}

	@Override
	public SecuredPrefixMapping withDefaultMappings( final PrefixMapping map )
	{
		// mapping only updates if there are map entries to add.  Since this gets called
		// when we are doing deep triple checks while writing we need to attempt the 
		// update only if there are new updates to add.
		
		PrefixMapping m = holder.getBaseItem();
		PrefixMappingImpl pm = new PrefixMappingImpl();
		for ( Entry<String, String> e : map.getNsPrefixMap().entrySet())
		{
			if (m.getNsPrefixURI(e.getKey()) == null && m.getNsURIPrefix(e.getValue()) == null )
			{
				pm.setNsPrefix( e.getKey(), e.getValue() );
			}
		}
		if ( !pm.getNsPrefixMap().isEmpty())
		{
			checkUpdate();
			holder.getBaseItem().withDefaultMappings(pm);
		}
		return holder.getSecuredItem();
	}
}