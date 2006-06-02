/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.sql.SQLUtils;

/** Interface to setting up the bulk loader environment.
 * 
 * @author Andy Seaborne
 * @version $Id: LoaderMySQL.java,v 1.1 2006/04/21 12:40:20 andy_seaborne Exp $
 */

public class LoaderMySQL extends BulkLoaderLJ
{
    public LoaderMySQL(SDBConnection connection) { super(connection) ; }
    
    public void createLoaderTable()
    {
        try {
            Connection conn = connection().getSqlConnection();

            if (SQLUtils.hasTable(conn, "NTrip"))
                return;

            PreparedStatement createLoaderTable = conn.prepareStatement(sqlStr(
                    "CREATE TEMPORARY TABLE IF NOT EXISTS NTrip",
                    "(",
                    "  shash BIGINT NOT NULL default 0,",
                    "  slex TEXT BINARY CHARACTER SET utf8 NOT NULL default '',",
                    "  stype int unsigned NOT NULL default '0',",
                    "  phash BIGINT NOT NULL default 0,",
                    "  plex TEXT BINARY CHARACTER SET utf8 NOT NULL default '',",
                    "  ptype int unsigned NOT NULL default '0',",
                    "  hash BIGINT NOT NULL default 0,",
                    "  lex TEXT BINARY CHARACTER SET utf8 NOT NULL default '',",
                    "  lang VARCHAR(10) BINARY CHARACTER SET utf8 NOT NULL default '',",
                    "  datatype VARCHAR("+ TableNodes.UriLength+ ") BINARY CHARACTER SET utf8 NOT NULL default '',",
                    "  type int unsigned NOT NULL default '0',",
                    "  vInt int NOT NULL default '0',",
                    "  vDouble double NOT NULL default '0',",
                    "  vDateTime datetime NOT NULL default '0000-00-00 00:00:00'",
                    ") DEFAULT CHARSET=utf8;"
            ));
            createLoaderTable.execute();
        }
        catch (SQLException ex)
        { throw new SDBExceptionSQL("Making loader table",ex) ; }
    }

    @Override
    String truncateTable(String tableName) throws SQLException
    {
        // MySQL note: DELETE FROM is trnasactional, TRUNCATE is not.
        return "TRUNCATE "+tableName ;
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