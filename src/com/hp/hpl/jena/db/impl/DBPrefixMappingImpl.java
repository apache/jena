/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: DBPrefixMappingImpl.java,v 1.10 2005-02-21 12:02:45 andy_seaborne Exp $
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import java.util.*;

/**
 *  Implementation of prefix mapping specific to databases.
 *  This extends the base implementation, effectively turning it into
 *  a write-through cache - each new namespace is written to
 *  the database as it is added to the prefix map.
 * 
 *
 	@author csayers
 	@version $Revision: 1.10 $
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
		Iterator it = m_graphProperties.getAllPrefixes();
		while( it.hasNext()) {
			DBPropPrefix prefix = (DBPropPrefix)it.next();
			super.setNsPrefix( prefix.getValue(), prefix.getURI() );
		}
	}

    public PrefixMapping removeNsPrefix( String prefix )
        {
        String uri = getNsPrefixURI( prefix );
        super.removeNsPrefix( prefix );
        if (uri != null) m_graphProperties.removePrefix( prefix );
        return this;
        }
    
	/* (non-Javadoc)
	 * Override the default implementation so we can catch the write operation
	 * and update the persistent store.
	 * @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefix(java.lang.String, java.lang.String)
	 */
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
	public PrefixMapping setNsPrefixes(PrefixMapping other) {
		return setNsPrefixes(other.getNsPrefixMap());
	}

	/* (non-Javadoc)
	 * Override the default implementation so we can catch all write operations
	 * @see com.hp.hpl.jena.shared.PrefixMapping#setNsPrefixes(java.util.Map)
	 */
	public PrefixMapping setNsPrefixes(Map other) {
        checkUnlocked();
		Iterator it = other.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry) it.next();
			setNsPrefix((String) e.getKey(), (String) e.getValue());
		}
        return this;
	}
}

/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/