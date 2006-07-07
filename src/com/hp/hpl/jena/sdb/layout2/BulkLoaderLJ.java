/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.Connection;
import java.sql.SQLException;

import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;

/** 
 * @author Damian Steer
 * @version $Id: BulkLoaderLJ.java,v 1.4 2006/04/21 12:40:20 andy_seaborne Exp $
 */

public abstract class BulkLoaderLJ extends LoaderTriplesNodes
{
	public BulkLoaderLJ(SDBConnection connection)
    {
        super(connection) ;
    }

	public void createPreparedStatements()
	{
		try {
		Connection conn = connection().getSqlConnection();

        super.clearTripleLoaderTable = conn.prepareStatement("DELETE FROM NTrip;");
        super.clearNodeLoaderTable = conn.prepareStatement("DELETE FROM NNode;");
        super.insertTripleLoaderTable = conn.prepareStatement("INSERT INTO NTrip VALUES (?,?,?);");
        super.insertNodeLoaderTable = conn
            .prepareStatement("INSERT INTO NNode VALUES (?,?,?,?,?,?,?,?);");
        
        super.insertNodes = conn.prepareStatement(sqlStr(
        		"INSERT INTO Nodes (hash, lex, lang, datatype, type, vInt, vDouble, vDateTime)",
        		"	SELECT DISTINCT NNode.hash, NNode.lex, NNode.lang, NNode.datatype, NNode.type, NNode.vInt, NNode.vDouble, NNode.vDateTime",
        		"	FROM NNode LEFT JOIN Nodes ON ",
        		"		(NNode.hash=Nodes.hash)",
        		"WHERE Nodes.id IS NULL"
            ));
        
		super.insertTriples = conn.prepareStatement(sqlStr(
				"INSERT INTO Triples",
				"	SELECT DISTINCT S.id, P.id, O.id FROM",
				"	  NTrip JOIN Nodes AS S ON (NTrip.s=S.hash)",
				"     JOIN Nodes AS P ON (NTrip.p=P.hash)",
				"     JOIN Nodes AS O ON (NTrip.o=O.hash)",
				"     LEFT JOIN Triples ON (S.id=Triples.s AND P.id=Triples.p AND O.id=Triples.o)",
				"     WHERE Triples.s IS NULL OR Triples.p IS NULL OR Triples.o IS NULL"
            ));
        } catch (SQLException ex)
        { ex.printStackTrace(); throw new SDBExceptionSQL("Preparing statements",ex) ; }
	}
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