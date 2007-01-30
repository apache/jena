/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.hash;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.layout2.TableNodes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

/** Interface to setting up the bulk loader environment.
 * 
 * @author Andy Seaborne
 * @version $Id: LoaderMySQL.java,v 1.1 2006/04/21 12:40:20 andy_seaborne Exp $
 */

public class LoaderHashMySQL extends LoaderHashLJ
{
    public LoaderHashMySQL(SDBConnection connection) { super(connection) ; }
    
    public void createLoaderTable() throws SQLException
    {
        connection().exec(sqlStr(
        		"CREATE TEMPORARY TABLE IF NOT EXISTS " + getNodeLoader(),
        		"(",
        		"  hash BIGINT NOT NULL,",
        		"  lex TEXT BINARY CHARACTER SET utf8 NOT NULL,",
        		"  lang VARCHAR(10) BINARY CHARACTER SET utf8 NOT NULL,",
        		"  datatype VARCHAR("+ TableNodes.UriLength+ ") BINARY CHARACTER SET utf8 NOT NULL,",
        		"  type int unsigned NOT NULL,",
        		"  vInt int,",
        		"  vDouble double,",
        		"  vDateTime datetime",
        		") ENGINE=MYISAM DEFAULT CHARSET=utf8;"
        ));
        connection().exec(sqlStr(
        		"CREATE TEMPORARY TABLE IF NOT EXISTS " + getTripleLoader(),
        		"(",
        		"  s BIGINT NOT NULL,",
        		"  p BIGINT NOT NULL,",
        		"  o BIGINT NOT NULL",
        		") ENGINE=MYISAM;"
        ));
    }
    
    @Override
	public String getInsertNodes()
	{
		return 
			"INSERT IGNORE INTO Nodes (hash, lex, lang, datatype, type)" +
			"	SELECT " + getNodeLoader() + ".hash, " + getNodeLoader() + ".lex, " + getNodeLoader() + ".lang, " + getNodeLoader() + ".datatype, " + getNodeLoader() + ".type" +
			"	FROM " + getNodeLoader();
	}
	
    @Override
	public String getInsertTriples()
	{
		return
			"INSERT IGNORE INTO Triples" +
			"	SELECT s, p, o FROM " + getTripleLoader();
	}
	
	@Override
    public String getClearTripleLoaderTable()
	{
		return "TRUNCATE " + getTripleLoader();
	}
	
	@Override
    public String getClearNodeLoaderTable()
	{
		return "TRUNCATE " + getNodeLoader();
	}
	
	@Override
	public String getDeleteTriples()
	{
		return "DELETE FROM Triples USING " +
		"	  Triples, " + getTripleLoader() + " JOIN Nodes AS S ON (" + getTripleLoader() + ".s=S.hash)" +
		"     JOIN Nodes AS P ON (" + getTripleLoader() + ".p=P.hash)" +
		"     JOIN Nodes AS O ON (" + getTripleLoader() + ".o=O.hash)" +
		" WHERE Triples.s = S.hash AND Triples.p = P.hash AND Triples.o = O.hash";
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