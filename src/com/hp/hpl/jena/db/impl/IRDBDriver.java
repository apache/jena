/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
 *
 *
 */

package com.hp.hpl.jena.db.impl;

//=======================================================================

// Imports

import java.util.List;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;


/**
* Generic database interface used for implementing RDF Stores.
* Different database table layouts and different SQL dialects should all
* be supportable via this generic interface. 
* 
* In earlier versions of Jena the Driver was exposed to some applications -
* that is no longer the case, and no application should need to use these
* functions directly.
* 
* Based in part on the Jena 1.0 implementation by der.
* 
* @author csayers
* @version $Revision: 1.8 $
*/

public interface IRDBDriver {
	
	
	/**
	 * Set the database connection
	 */
	public void setConnection( IDBConnection dbcon );
	
	/**
	 * Return the connection
	 */
	public IDBConnection getConnection();
	
	/**
	 * Return the specialized graph containing system properties.
	 * Formats the databaase and constucts a new one if necessary.
	 * 
	 * @return SpecializedGraph holding properties of this database
	 * @since Jena 2.0
	 */
	public SpecializedGraph getSystemSpecializedGraph();
	
	/**
	 * Construct and return a list of specialized graphs.
	 * @param graphProperties A set of customization properties for the graph.
	 * @return List of SpecializedGraphs to store a Graph
	 * 
	 * @since Jena 2.0
	 */
	public List createSpecializedGraphs(DBPropGraph graphProperties);
	
	/**
	 * Reconstruct and return a list of specialized graphs.
	 * @param graphProperties A set of customization properties for the  graph.
	 * @return List of SpecializedGraphs to store a Graph
	 * 
	 * @since Jena 2.0
	 */
	public List recreateSpecializedGraphs(DBPropGraph graphProperties);
	
	/**
	 * Remove the specialized graph, erasing all trace of a Graph.
	 * @param graphProperties The properties for the graph to be removed.
	 * 
	 * @since Jena 2.0
	 */
	public void removeSpecializedGraphs(DBPropGraph graphProperties, List specializedGraphs);
	
	/**
	 * Test if the database has previously been formatted (there's no other
	 * way to easily tell, since getSpecializedGraph will always return
	 * something).
	 * 
	 * @return boolean true if database is correctly formatted, false on any error.
	 */
	public boolean isDBFormatOK();
	
	/**
	 * Method setDatabaseProperties.
	 * 
	 * Sets the current properties for the database.
	 * 
	 * @param databaseProperties is a Graph containing a full set of database properties
	 */
	void setDatabaseProperties(Graph databaseProperties);
		
	/**
	 * Obtain a default set of model properties.
	 * 
	 * Return the default properties for a new model stored in this database
	 * @return DBPropGraph containg the default properties for a new model
	 */
	DBPropGraph getDefaultModelProperties();
	
	/**
	 * Return a string identifying underlying database type.
	 *
	 */
	String getDatabaseType();
	

    /**
     * Remove all RDF information from a database.
     * 
     * There should be no need for an application to call this function
     * directly - instead use DBConnection.cleanDB().
     * 
     */

    public void cleanDB();

    /**
     * Close the databse connection.
     * @throws RDFDBException if there is an access problem
     */

    public void close() throws RDFRDBException;

    /**
     * Initialise a database ready to store RDF tables.
     * Currently the table format depends on the RDBSpec type. In future it
     * may become an explicit part of operations like this.
     * @throws RDFDBException if the is a problem opening the connection or an internal SQL error.
	 * @deprecated Since Jena 2.0 this call is no longer needed - formatting 
	 * happens automatically as a side effect of creating Models - there should
	 * be no need for an application to interact directly with the driver.
     */

    public void formatDB() throws RDFRDBException;
    
	/**
	 * Create a table for storing asserted or reified statements.
	 * 
	 * @param graphId the graph which the table is created.
	 * @param isReif true if table stores reified statements.
	 * @return the name of the new table 
	 * 
	 */
	abstract String createTable( int graphId, boolean isReif);
	   
    /**
     * Aborts the open transaction, then turns autocommit on.
     */
	public void abort() throws  RDFRDBException;
        
	/**
	 * Turns autocommit off, then opens a new transaction.	 *
	 */
	public void begin() throws  RDFRDBException;
        
	/**
	 * Commits the open transaction, then turns autocommit on.
	 */
	public void commit() throws  RDFRDBException;

	/**
	 * Returns true if the underlying database supports transactions.
	 */
	public boolean transactionsSupported();    

    /**
     * Returns true if the database layout supports multiple RDF models in the same database.
     * @return boolean true if the database supports multiple models
     * @deprecated Since Jena 2.0 all databases support multiple models.
     */

    public boolean supportsMultipleModels();

    /**
     * Returns true if the database layout supports implicit reification.
     * of statements (i.e. statements can be treated as resources).
     * @return boolean true if the database supports jena 1.0 reification.
     * @deprecated Since Jena 2.0 the reification API has changed.  The
     * new API is supported in all models, but the old Jena 1 API is no
     * longer supported.  This call will return false to indicate
     * to old code that the old style of jena reification is not supported.
     */

    public boolean supportsJenaReification();
    
	/**
	 * Allocate an identifier for a new graph.
	 * @param graphName The name of a new graph.
	 * @return the identifier of the new graph.
	 */
	 public int graphIdAlloc ( String graphName );
	
	/**
	 * Deallocate an identifier for a new graph.
	 * @param graphId The graph identifier.
	 */
	 public void graphIdDealloc ( int graphId );

	/**
 	* Return the identifier of the most recently inserted auto-incremented row.
 	* @return the identifier of the most recently inserted auto-increment row.
 	*/
	 public int getLastInsertID ();

    
	/**
	* Convert a node to a string to be stored in a statement table.
	* @param Node The node to convert to a string. Must be a concrete node.
	* @param addIfLong If the node is a long object and is not in the database, add it.
	* @return the string.
	*/

	public String nodeToRDBString ( Node node, boolean addIfLong );
	
	/**
	* Convert an RDB string to the node that it encodes. Return null if failure.
	* @param RDBstring The string to convert to a node.
	* @return The node.
	*/
	
	public Node RDBStringToNode ( String RDBString );	


}

/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
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
	
 