/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
 *
 */


package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;

/**
 *
 * @author  hkuno based on GraphMem.java, v 1.4 by Chris Dollin (chris-dollin)
 * @version 
 *
 * TripleStoreGraph_RDB is a table-specific, database-independent 
 * implementation of TripleStoreGraph.
 *
 * The underlying database will be formatted with the following 
 * tables:
 *     RDF_Literals
 *     RDF_StmtAsserted
 *     RDF_Property
 *
 * structure as specified in the Jena2 Persistence Architecture document.
 *
 * TripleStoreGraph_RDB relies upon the underlying database-specific driver
 * (IPSet m_pset) to implement database-dependent code.
 * TripleStoreGraph_RDB interfaces with the driver by passing it operation
 * names for SQL operations (along with arguments).
 * Although the driver is layout-independent, it can use these operations
 * to extract com.hp.hpl.jena.graph.* structures (e.g., Node_Literal, 
 * Node_URI, Triple, etc.) because the operations return results in a known
 * format, regardless of the underlying table layouts.
 * (I.e., the database performs any required joins.)
 */

public class SpecializedGraph_TripleStore_RDB extends SpecializedGraph_TripleStore {
	
	// cache property model
	// hardcode it for now.  
	private Model propertyModel;
	
	// cache maximum literal
	// hardcode it to 1000 for now.  Fix when propertyModel works.
	private int MAX_LITERAL = 1000;
	
	// cache SKIP_DUPLICATE_CHECK
	// hardcode it to "false" for now.  Fix when propertyModel works.
	private boolean SKIP_DUPLICATE_CHECK = false;
	
	// cache SQLCache instance
	private SQLCache sCache;
	
	   
	
	/**
	 * Constructor.
	 * 
	 * Create a new instance of a TripleStore graph, taking an IPSet
	 * as an argument.  Used for bootstrapping, when we don't have a
	 * DBPropLSet yet.
	 */
	public SpecializedGraph_TripleStore_RDB(IPSet pSet, Integer dbGraphId) {
		super(pSet, dbGraphId);
	}

    /** 
     * Compute the number of unique triples added to the Specialized Graph.
     * 
     * @return int count.
     */
    public int tripleCount() {
    	return(m_pset.tripleCount());
    }
    
    /**
     * Tests if a triple is contained in the specialized graph.
     * @param t is the triple to be tested
     * @param complete is true if the graph can guarantee that 
     *  no other specialized graph 
     * could hold any matching triples.
     * @return boolean result to indicte if the tripple was contained
     */
	public boolean contains(Triple t, CompletionFlag complete) {
		complete.setDone(); 
		return (m_pset.statementTableContains(my_GID, t));
	}
       
}

/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
