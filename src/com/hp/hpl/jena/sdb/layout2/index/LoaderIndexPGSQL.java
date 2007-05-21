/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.index;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.TableUtils;

/** Interface to setting up the bulk loader environment.
 * 
 * @author Andy Seaborne
 * @version $Id: LoaderHSQL.java,v 1.1 2006/04/21 16:53:32 andy_seaborne Exp $
 */

public class LoaderIndexPGSQL extends LoaderIndexLJ
{
    public LoaderIndexPGSQL(SDBConnection connection) { super(connection) ; }
    
    public void createLoaderTable() throws SQLException
    {
    	if (!TableUtils.hasTable(this, getNodeLoader()))
        	connection().exec(sqlStr(
        			"CREATE TEMPORARY TABLE " + getNodeLoader(),
                    "(",
                    "  hash BIGINT NOT NULL ,",
                    "  lex TEXT NOT NULL ,",
                    "  lang VARCHAR(10) NOT NULL ,",
                    "  datatype VARCHAR("+ TableDescNodes.DatatypeUriLength+ ") NOT NULL ,",
                    "  type int NOT NULL ,",
                    "  vInt int,",
                    "  vDouble double precision,",
                    "  vDateTime timestamp",
                    ") ON COMMIT DELETE ROWS"
        	));
        
        if (!TableUtils.hasTable(this, getTripleLoader()))
        	connection().exec(sqlStr(
        			"CREATE TEMPORARY TABLE " + getTripleLoader(),
        			"(",
        			"  s BIGINT NOT NULL,",
        			"  p BIGINT NOT NULL,",
        			"  o BIGINT NOT NULL",
        			") ON COMMIT DELETE ROWS"
        	));
    }
    
    @Override
    public String getClearTripleLoaderTable()
	{
		return null;
	}
	
    @Override
	public String getClearNodeLoaderTable()
	{
		return null;
	}
    
    /* This is slower than deleting one at a time */
    /*@Override
    public String getDeleteTriples()
	{
		return "DELETE FROM Triples USING " +
		"	  NTrip JOIN Nodes AS S ON (NTrip.s=S.hash)" +
		"     JOIN Nodes AS P ON (NTrip.p=P.hash)" +
		"     JOIN Nodes AS O ON (NTrip.o=O.hash)" +
		" WHERE Triples.s = S.id AND Triples.p = P.id AND Triples.o = O.id";
	}*/
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
