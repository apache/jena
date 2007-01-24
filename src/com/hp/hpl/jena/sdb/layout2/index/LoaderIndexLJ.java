/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.index;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.sdb.layout2.LoaderTriplesNodes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

/** 
 * @author Damian Steer
 * @version $Id: BulkLoaderLJ.java,v 1.4 2006/04/21 12:40:20 andy_seaborne Exp $
 */

public abstract class LoaderIndexLJ extends LoaderTriplesNodes
{
	public LoaderIndexLJ(SDBConnection connection)
    {
        super(connection) ;
    }
	
	public String getInsertTripleLoaderTable()
	{
		return "INSERT INTO " + getTripleLoader() + " VALUES (?,?,?)";
	}
	
	public String getInsertNodeLoaderTable()
	{
		return "INSERT INTO " + getNodeLoader() + " VALUES (?,?,?,?,?,?,?,?)";
	}
	
	public String getInsertNodes()
	{
		return 
			"INSERT INTO Nodes (hash, lex, lang, datatype, type)" +
			"	SELECT " + getNodeLoader() + ".hash, " + getNodeLoader() + ".lex, " + getNodeLoader() + ".lang, " + getNodeLoader() + ".datatype, " + getNodeLoader() + ".type" +
			"	FROM " + getNodeLoader() + " LEFT JOIN Nodes ON " +
			"		(" + getNodeLoader() + ".hash=Nodes.hash)" +
			"WHERE Nodes.id IS NULL";
	}
	
	public String getInsertTriples()
	{
		return
			"INSERT INTO Triples" +
			"	SELECT DISTINCT S.id, P.id, O.id FROM" +
			"	  " + getTripleLoader() + " JOIN Nodes AS S ON (" + getTripleLoader() + ".s=S.hash)" +
			"     JOIN Nodes AS P ON (" + getTripleLoader() + ".p=P.hash)" +
			"     JOIN Nodes AS O ON (" + getTripleLoader() + ".o=O.hash)" +
			"     LEFT JOIN Triples ON (S.id=Triples.s AND P.id=Triples.p AND O.id=Triples.o)" +
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
		return "DELETE FROM " + getTripleLoader();
	}
	
	public String getClearNodeLoaderTable()
	{
		return "DELETE FROM " + getNodeLoader();
	}
	
	@Override
	protected void removeOneTriple(PreparedTriple triple) throws SQLException
    {
    	int s,p,o;
    	if ((s = getIdFromHash(triple.subject.hash)) == -1) return;
    	if ((p = getIdFromHash(triple.predicate.hash)) == -1) return;
    	if ((o = getIdFromHash(triple.object.hash)) == -1) return;
    	
    	connection().execUpdate("DELETE FROM Triples WHERE " +
    			"s = " + s + " AND " +
    			"p = " + p + " AND " +
    			"o = " + o);
    }

	private int getIdFromHash(long hash) throws SQLException
	{
		int id = -1;
		ResultSet result = connection().execQuery("SELECT id FROM Nodes WHERE hash = " + hash);
		
		if (result.next())
			id = result.getInt(1);
		result.close();
		return id;
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