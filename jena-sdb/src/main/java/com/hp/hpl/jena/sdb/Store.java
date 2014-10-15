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

package com.hp.hpl.jena.sdb;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sdb.compiler.QueryCompilerFactory;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.*;


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
 */

public interface Store
{
    /** Return the connection to the implementing database */
    public SDBConnection getConnection() ;
    
    /** Return the producer of processors that turn SPARQL queries into SQL expressions */
    public QueryCompilerFactory    getQueryCompilerFactory() ; 
    
    /** Factory for SQL bridges for this store */ 
    public SQLBridgeFactory getSQLBridgeFactory() ;
    
    /** Get the SQL-from-relational algebra generator */ 
    public SQLGenerator     getSQLGenerator() ;
    
    /** Return the processor that creates the database tables */
    public StoreFormatter   getTableFormatter() ;
    
    /** Return the (bulk) loader */
    public StoreLoader      getLoader() ;
    
    /** Return the configuration of this Store */
    public StoreConfig      getConfiguration() ;
    
    /** Return the database type of the store */
    public DatabaseType     getDatabaseType() ;
    
    /** Return the layout type of the store */
    public LayoutType       getLayoutType() ;
    
    /** Stores should be closed explicitly. 
     *  Some stores may require specific finalization actions (e.g. embedded databases),
     *  and some stores may be able to release system resources.
     */  
    public void  close() ;

    /** Has this store been closed? **/
    public boolean isClosed();
    
    /** Get the size of this store **/
    public long  getSize() ;
    
    /** Get the size of the graph corresponding to graphNode **/
    public long getSize(Node graphNode);
    
    /** Where the default graph is store */ 
    public TableDescTriples     getTripleTableDesc() ;
    
    /** Where the named graphs are in is store */ 
    public TableDescQuads       getQuadTableDesc() ;
    
    /** Location of the nodes in the store (if meaningful) */  
    public TableDescNodes       getNodeTableDesc() ;

    // Use the SPARQL query : SELECT ?g {GRAPH ?g {}}
//    /** List the Nodes of the named graphs */
//    public Iterator<Node> listNamedGraphs() ;
}
