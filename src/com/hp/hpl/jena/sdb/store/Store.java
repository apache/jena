/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import com.hp.hpl.jena.sdb.sql.SDBConnection;

/** A Store is one RDF dataset instance - it is the unit of query.
 *  The Store class is the SDB specific mechanisms need to implement
 *  an RDF Dataset.  DatasetStore provide the dataset interface.
 *  
 *  A store consists of a number of handlers for different aspects of
 *  the process of setting up and querying a database-backed Store.  This
 *  means Store for new databases can be assmelbed from those standard
 *  components that work, with database-specific code only where necessary.
 *  A common case if the formatting of the database - this is often DB-specific
 *  yet SQL generation of query is more standard.       
 * 
 * @author Andy Seaborne
 * @version $Id: Store.java,v 1.12 2006/04/22 19:51:12 andy_seaborne Exp $
 */

public interface Store
{
    /** Return the connection to the implementing database */
    SDBConnection getConnection() ;
    
    /** Return the processor that turns query Plans (from ARQ) to
     * something that goes to this store.  May still involve ARQ
     * i.e. be a partial translation to SQL, leaving work to be
     * done in the ARQ engine as well. 
     */ 
    PlanTranslator   getPlanTranslator() ;
    
    /** Return the processor that turns SPARQL queries into SQL expressions */
    QueryCompiler    getQueryCompiler() ; 
    
    // Change to a list of customizers
    /** Return the store instance specific modification engine */
    StoreCustomizer  getCustomizer() ;
    
    /** Set the store instance specific modification engine */
    void setCustomizer(StoreCustomizer customizer) ;

    /** Get the SQL-from-realtional algebra generator */ 
    SQLGenerator getSQLGenerator() ;
    
    /** Return the processor that creates the database tables */
    StoreFormatter   getTableFormatter() ;
    
    /** Return the (bulk) loader */
    StoreLoader      getLoader() ;
    
    /** Return the configuration of this Store */
    StoreConfig      getConfiguration() ;
    
    /** Stores should be closed explicitly. 
     *  Some stores may require specific finalization actions (e.g. embedded databases),
     *  and some stores may be able to release system resources.
     */  
    void close() ;
    
//    /** Indicate whether a store is ready to use or
//     * needs setting (typically, formatting)
//     */ 
//    boolean isInitialized() ;
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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