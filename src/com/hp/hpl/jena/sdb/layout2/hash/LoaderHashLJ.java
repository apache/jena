/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.hash;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.layout2.LoaderTriplesNodes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

/** 
 * @author Damian Steer
 * @version $Id: BulkLoaderLJ.java,v 1.4 2006/04/21 12:40:20 andy_seaborne Exp $
 */

public abstract class LoaderHashLJ extends LoaderTriplesNodes
{
	public LoaderHashLJ(SDBConnection connection)
    {
        super(connection) ;
    }
	
	public String getInsertTripleLoaderTable()
	{
		return "INSERT INTO NTrip VALUES (?,?,?)";
	}
	
	public String getInsertNodeLoaderTable()
	{
		return "INSERT INTO NNode VALUES (?,?,?,?,?,?,?,?)";
	}
	
	public String getInsertNodes()
	{
		return 
			"INSERT INTO Nodes (hash, lex, lang, datatype, type)" +
			"	SELECT NNode.hash, NNode.lex, NNode.lang, NNode.datatype, NNode.type" +
			"	FROM NNode LEFT JOIN Nodes ON " +
			"		(NNode.hash=Nodes.hash)" +
			"WHERE Nodes.hash IS NULL";
	}
	
	public String getInsertTriples()
	{
		return
			"INSERT INTO Triples" +
			"	SELECT DISTINCT NTrip.s, NTrip.p, NTrip.o FROM" +
			"     NTrip LEFT JOIN Triples ON (NTrip.s=Triples.s AND NTrip.p=Triples.p AND NTrip.o=Triples.o)" +
			"     WHERE Triples.s IS NULL OR Triples.p IS NULL OR Triples.o IS NULL";
	}
	
	// This is slow (but mysql & postgres have faster variants)
	public String getDeleteTriples()
	{
		return null;
		/*
		return "DELETE FROM Triples WHERE " +
		" EXISTS (" +
		"SELECT s,p,o FROM " +
		"	  NTrip JOIN Nodes AS S ON (NTrip.s=S.hash)" +
		"     JOIN Nodes AS P ON (NTrip.p=P.hash)" +
		"     JOIN Nodes AS O ON (NTrip.o=O.hash)" +
		" WHERE Triples.s = S.id AND Triples.p = P.id AND Triples.o = O.id)";*/
	}
	
	public String getClearTripleLoaderTable()
	{
		return "DELETE FROM NTrip";
	}
	
	public String getClearNodeLoaderTable()
	{
		return "DELETE FROM NNode";
	}
	
	@Override
	/**
     * Remove one triple
     * 
     * For databases like HSQL which we can't simply bulk delete from easily.
     * @throws SQLException 
     */
    protected void removeOneTriple(PreparedTriple triple) throws SQLException
    {
    	connection().execUpdate("DELETE FROM Triples WHERE " +
    			"s = " + triple.subject.hash + " AND " +
    			"p = " + triple.predicate.hash + " AND " +
    			"o = " + triple.object.hash + "");
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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