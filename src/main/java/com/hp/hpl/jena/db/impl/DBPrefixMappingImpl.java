/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.db.impl;

import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/**
 *  Implementation of prefix mapping specific to databases.
 *  This extends the base implementation, effectively turning it into
 *  a write-through cache - each new namespace is written to
 *  the database as it is added to the prefix map.
 * 
 *
 	@author csayers
 	@version $Revision: 1.1 $
*/
public class DBPrefixMappingImpl extends PrefixMappingImpl {

	protected DBPropGraph m_graphProperties = null;
	
	/**
	 * Constructor for a persistent prefix mapping.
	 * 
	 * Each GraphRDB has a set of associated properties
	 * which are, themselves, represented as triples in
	 * a system graph.
	 * 
	 * The prefix mapping is persisted by converting it
	 * to triples and storing it along with the other
	 * properties of the GraphRDB in that system graph.
	 * 
	 * @param graphProperties the system properties of a persistent graph.
	 */
    public DBPrefixMappingImpl( DBPropGraph graphProperties) {
		super();
		m_graphProperties = graphProperties;
		
		// Populate the prefix map using data from the 
		// persistent graph properties
        boolean commit = m_graphProperties.begin();
		Iterator<DBPropPrefix> it = m_graphProperties.getAllPrefixes();
		while( it.hasNext()) {
			DBPropPrefix prefix = it.next();
			super.setNsPrefix( prefix.getValue(), prefix.getURI() );
		}
        m_graphProperties.conditionalCommit( commit );
	}

    @Override
    public PrefixMapping removeNsPrefix( String prefix )
        {
        super.removeNsPrefix( prefix );
        m_graphProperties.removePrefix( prefix );
        return this;
        }
    
	/* (non-Javadoc)
	 * Override the default implementation so we can catch the write operation
	 * and update the persistent store.
	 * @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefix(java.lang.String, java.lang.String)
	 */
	@Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        // this avoids touching the database for existing maplets.
        // if (uri.equals( super.getNsPrefixURI( prefix ) )) return this;
		// Ordering is important here - we need to add it to the prefixMappingImpl
		// first since it checks the validity of the prefix (it will throw
		// an exception if there's any problem).
		super.setNsPrefix(prefix, uri);
		
		// All went well, so persist the prefix by adding it to the graph properties
		// (the addPrefix call will overwrite any existing mapping with the same prefix
		// so it matches the behaviour of the prefixMappingImpl).
		m_graphProperties.addPrefix(prefix, uri);
        return this;
	}

	/* (non-Javadoc)
	 * Override the default implementation so we can catch all write operations
	 * @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefixes(com.hp.hpl.jena.shared.PrefixMapping)
	 */
	@Override
    public PrefixMapping setNsPrefixes(PrefixMapping other) {
		return setNsPrefixes(other.getNsPrefixMap());
	}

	/* (non-Javadoc)
	 * Override the default implementation so we can catch all write operations
	 * @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefixes(java.util.Map)
	 */
	@Override
    public PrefixMapping setNsPrefixes(Map<String, String> other) {
        checkUnlocked();
		Iterator<Map.Entry<String, String>> it = other.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> e = it.next();
			setNsPrefix(e.getKey(), e.getValue());
		}
        return this;
	}
}
