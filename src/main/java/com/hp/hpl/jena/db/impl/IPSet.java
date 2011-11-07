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

import java.util.List;

import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
* Generic database interface used for implementing PStore
* Different database table layouts and different SQL dialects should all
* be supportable via this generic interface. 
* 
* Based on the Jena1 version of IRDBDriver by Dave Reynolds
* 
* @author hkuno
* @version $Revision: 1.1 $
*/

public interface IPSet {

	/**
	 * Link an existing instance of the IPSet to a specific driver
	 */
	public void setDriver(IRDBDriver driver) throws RDFRDBException;

	/**
	 * Pass the SQL cache to the IPSet
	 */
	public void setSQLCache(SQLCache cache);
	public SQLCache getSQLCache();
	public void setSQLType(String value);
	public void setSkipDuplicateCheck(boolean value);
	public void setCachePreparedStatements(boolean value);

    /**
     * Close this PSet
     */
    public void close();

    /**
     * Remove all RDF information associated with this PSet
     * from a database.
     */
    public void cleanDB();
    
    /**
     * Return boolean indicating whether or not statement
     * table for specified statement table contains
     * the specified triple for the specified graph.
     */
    public boolean statementTableContains(IDBID graphID, Triple t);
    	
    
	/**
	 * @param t the triple to be added
	 * @param gid the id of the graph
	 */
	public void storeTriple(Triple t, IDBID gid);
	
	/** 
	 * Attempt to add a list of triples to the specialized graph.
	 * 
	 * As each triple is successfully added it is removed from the List.
	 * If complete is true then the entire List was added and the List will 
	 * be empty upon return.  if complete is false, then at least one triple 
	 * remains in the List.
	 * 
	 * If a triple can't be stored for any reason other than incompatability
	 * (for example, a lack of disk space) then the implemenation should throw
	 * a runtime exception.
	 * 
	 * @param triples List of triples to be added.  This is modified by the call.
	 * @param my_GID  ID of the graph.
	 */
	public void storeTripleList(List<Triple> triples, IDBID my_GID);




	/**
	 * @param t the triple to be added
	 * @param gid the id of the graph
	 */
	public void deleteTriple(Triple t, IDBID gid);
	
	/**
		 * @param t the triple to be added
		 * @param gid the id of the graph
		 */
	public void deleteTripleList(List<Triple> triples, IDBID gid);
	
	
	/**
	 * Method extractTripleFromRowData.
	 * @param subjURI
	 * @param predURI
	 * @param objURI may be null
	 * @param objVal may be null
	 * @param objRef may be null
	 * @return Triple
	 */
	Triple extractTripleFromRowData(
		String subj,
		String pred,
		String obj);
		
	/**
	 * Method find matching entries
	 * @param t tripleMatch pattern
	 * @param graphID of the graph to search
	 * @return ExtendedIterator holding results
	 */
	public ExtendedIterator<Triple>  find(TripleMatch t, IDBID graphID);
	
	/**
	 * Return a count of the rows in a given table
	 * 
	 * @param tName
	 * @return int
	 */
	public int rowCount(int graphId);

	/**
	 * Remove the statements associated with this PStore
	 * from the database tables.  Leave Literals.
	 * @param pProp properties
	 */
	public void removeStatementsFromDB(IDBID graphID);

	/**
	 * @param graphId TODO
	 * @return number of triples in AssertedStatement table
	 */
	public int tripleCount(IDBID graphId);

	/**
	 * @param tblName
	 */
	public void setTblName(String tblName);
	
	/**
	 * @return String the name of the table that stores the PSet.
	 */
	public String getTblName();

	/**
	 * @return the driver for the PSet
	 */
	public IRDBDriver driver();

}
