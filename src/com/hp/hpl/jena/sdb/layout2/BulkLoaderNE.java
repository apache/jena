/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */
package com.hp.hpl.jena.sdb.layout2;

import java.sql.Connection;
import java.sql.SQLException;

import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr ;

/** 
 * @author Damian Steer
 * @version $Id: BulkLoaderNE.java,v 1.3 2006/04/21 12:40:20 andy_seaborne Exp $
 */

public abstract class BulkLoaderNE extends BulkLoader
{
    
    public BulkLoaderNE(SDBConnection connection)
    {
        super(connection) ;
    }

    public void createPreparedStatements()
    {
        try {
            Connection conn = connection().getSqlConnection();
            
            super.clearLoaderTable = conn.prepareStatement("DELETE FROM NTrip;");
            super.insertLoaderTable = conn
                .prepareStatement("INSERT INTO NTrip VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            
            super.insertObjects = conn.prepareStatement(sqlStr(
                    "INSERT INTO Nodes (hash, lex, lang, datatype, type, vInt, vDouble, vDateTime)",
                    "  SELECT DISTINCT hash, lex, lang, datatype, type, vInt, vDouble, vDateTime FROM NTrip",
                    "  WHERE NOT EXISTS",
                    "  (",
                    "     SELECT * FROM Nodes WHERE",
                    "     NTrip.hash=Nodes.hash",
                    "  );"
                ));
            super.insertSubjects = conn.prepareStatement(sqlStr(
                    "INSERT INTO Nodes (hash, lex, type)",
                    "  SELECT DISTINCT shash, slex, stype FROM NTrip",
                    "  WHERE NOT EXISTS",
                    "  (",
                    "     SELECT * FROM Nodes WHERE",
                    "     NTrip.shash=Nodes.hash",
                    "  );"
                ));
            super.insertPredicates = conn.prepareStatement(sqlStr(
                    "INSERT INTO Nodes (hash, lex, type)",
                    "  SELECT DISTINCT phash, plex, ptype FROM NTrip",
                    "  WHERE NOT EXISTS",
                    "  (",
                    "     SELECT * FROM Nodes WHERE",
                    "     NTrip.phash=Nodes.hash",
                    "  );"
                ));
            super.insertTriples = conn.prepareStatement(sqlStr(
                    "INSERT INTO Triples",
                    "  SELECT DISTINCT s.id, p.id, o.id FROM NTrip, Nodes s, Nodes p, Nodes o",
                    "  WHERE",
                    "     NTrip.shash=s.hash AND",
                    "     NTrip.phash=p.hash AND",
                    "     NTrip.hash=o.hash AND",
                    "     NOT EXISTS",
                    "     (",
                    "        SELECT * FROM Triples WHERE",
                    "        s.id=Triples.s AND p.id=Triples.p AND o.id=Triples.o",
                    "     );"
                ));
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("Preparing statements",ex) ; }
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