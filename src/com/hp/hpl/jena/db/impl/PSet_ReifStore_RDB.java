/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.LiteralLabel;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.StandardTripleMatch;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

//=======================================================================
/**
* Handles Physical storage for implementing SpecializedGraphs.
* Different PSet classes are needed for different databases and different
* layout schemes.
* <p>
* This class is a base implemention from which database-specific
* drivers can inherit. It is not generic in the sense that it will work
* on any minimal SQL store and so should be treated as if it were
* an abstract class.
* <p>The SQL statements which implement each of the functions are
* loaded in a separate file etc/[layout]_[database].sql from the classpath.
* See {@link SQLCache SQLCache documentation} for more information on the
* format of this file.
* 
* Based on Driver* classes by Dave Reynolds.
*
* @author <a href="mailto:harumi.kuno@hp.com">Harumi Kuno</a>
* @version $Revision: 1.1 $ on $Date: 2003-05-06 05:04:14 $
*/

public  class PSet_ReifStore_RDB extends PSet_TripleStore_RDB {

//=======================================================================
// Cutomization variables

   public static String SYS_AS_TNAME = "JENA_StmtReified";
   
//=======================================================================
// Internal variables

//=======================================================================
// Constructors and accessors

    /**
     * Constructor.
     */
    public PSet_ReifStore_RDB(){
    }
    	    

    
//=======================================================================
// Database operations


	public void storeReifStmt( Node_URI n, Triple t, IDBID my_GID ) {
		storeTripleAR( t, my_GID, n, true, false, null);
	}

	public void deleteReifStmt( Node_URI n, Triple t, IDBID my_GID ) {
		deleteTripleAR( t, my_GID, n, false, null);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.IPSet#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.IDBID)
	 */
	public ExtendedIterator findReifNodes(Node reifNode, boolean hasType, IDBID graphID) {
	   String astName = getASTname();
	   String gid = graphID.getID().toString();
	   ResultSetTripleIterator result= new ResultSetTripleIterator(this, graphID);
	   
	   PreparedStatement ps = null;
		
	   boolean objIsBlankOrURI = false;
	   int args = 1;
	   String stmtStr;
	   boolean findAll = reifNode == null;
	   
	   stmtStr = findAll ? "SelectAllReifStatement" :
	   				hasType ? "SelectReifTypeStatement" : "SelectReifStatement";
	   try {
	   ps = m_sql.getPreparedSQLStatement(stmtStr,getASTname());
	   
	   if ( findAll ) {
	   		String stmt_uri = nodeToRDBString(reifNode);
			ps.setString(1,stmt_uri);
			if ( hasType )
				ps.setInt(2,1);
	   }
	   
	   ps.setString(3,gid);
	   	   
	   } catch (Exception e) {
			   	Log.warning("Getting prepared statement for " + stmtStr + " Caught exception " + e);
			   }
			   
	   try {
		  m_sql.executeSQL(ps, stmtStr, result);
	   } catch (Exception e) {
		 Log.debug("find encountered exception " + e);
	   }
	   return ((ExtendedIterator) result);
   }

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
	
 
