/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.rdf.model.RDFException;
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

	public void setMaxLiteral(int value);
	public void setSQLType(String value);
	public void setSkipDuplicateCheck(boolean value);
	public void setSkipAllocateId(boolean value );
	public void setEmptyLiteralMarker(String value );
	public void setInsertByProcedure(boolean value);
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
     * Return boolean indicating whether or not the given table
     * exists in the database.
     */
    public boolean tableExists(String tName);
    	
    
	/**
	 * Check if database contains given table. 
	 * If not, run specified command to create it.
	 */
	public void initializeStatementTable();

    	
	/**
	 * General ID allocate stub.
	 * Calls the given SQL operation to perform the allocation.
	 */
	public IDBID allocateID(String opname);

		
	/**
	 * Return the database ID for the literal and allocate one of necessary
	 */
	public IDBID getLiteralID(Node_Literal l);
	
	/**
	 * get index for this literal.
	 */
	public String getLiteralIdx(String literal);
	
	/**
	 * Add a literal to the Literal table
	 */
	public IDBID addLiteral(Node_Literal l);

	/**
	 * @param subj_uri
	 * @param pred_uri
	 * @param o
	 * @param my_ASTName
	 */
	public void storeTriple(Triple t, IDBID gid);

	/**
	 * @param subj_uri
	 * @param pred_uri
	 * @param o
	 * @param my_ASTName
	 */
	public void deleteTriple(Triple t, IDBID gid);
	
	
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
		String subjURI,
		String predURI,
		String objURI,
		String objVal,
		String objRef)throws RDFException;
		
	/**
	 * Method find matching entries
	 * @param t tripleMatch pattern
	 * @param graphID of the graph to search
	 * @return ExtendedIterator holding results
	 */
	public ExtendedIterator find(TripleMatch t, 
	IDBID graphID);
	
	/**
	 * Return a count of the rows in a given table
	 * 
	 * @param tName
	 * @return int
	 */
	public int rowCount(String tName);

	/**
	 * Remove the statements associated with this PStore
	 * from the database tables.  Leave Literals.
	 * @param pProp properties
	 */
	public void removeStatementsFromDB(IDBID graphID);

	/**
	 * @return number of triples in AssertedStatement table
	 */
	public int tripleCount();

	/**
	 * @param SYSTEM_PROP_TNAME
	 */
	public void setASTname(String SYSTEM_PROP_TNAME);

}

/*
 *  (c) Copyright Hewlett-Packard Company 2000-2003
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
	
 
