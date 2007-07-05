/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.hash;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.layout2.FmtLayout2;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TablePrefixes;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;


public class FmtLayout2HashSQLServer extends FmtLayout2
{
    //static private Log log = LogFactory.getLog(FormatterTriplesNodesSQLServer.class) ;
    
    public FmtLayout2HashSQLServer(SDBConnection connection)
    { 
        super(connection) ;
    }

    @Override
    protected void formatTableTriples()
    {
        dropTable(TableDescTriples.name()) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE TABLE "+TableDescTriples.name()+" (",
                                 "    s BIGINT NOT NULL,",
                                 "    p BIGINT NOT NULL,",
                                 "    o BIGINT NOT NULL,",
                                 "    PRIMARY KEY (s, p, o)",
                                 ")"                
                    )) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ; }
    }
    
    @Override
    protected void formatTableQuads()
    {
        dropTable(TableDescQuads.name()) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE TABLE "+TableDescQuads.name()+" (",
                                 "    g BIGINT NOT NULL,",
                                 "    s BIGINT NOT NULL,",
                                 "    p BIGINT NOT NULL,",
                                 "    o BIGINT NOT NULL,",
                                 "    PRIMARY KEY (g, s, p, o)",
                                 ")"                
                    )) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ; }
    }
    
    @Override
    protected void addIndexesTableTriples()
    {
        try {
            connection().exec("CREATE INDEX PredObj ON "+TableDescTriples.name()+" (p, o);") ;
            connection().exec("CREATE INDEX ObjSubj ON "+TableDescTriples.name()+" (o, s);") ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException indexing table '"+TableDescTriples.name()+"'",ex) ; }
    }
    
    @Override
    protected void dropIndexesTableTriples()
    {
        try {
            connection().exec("DROP INDEX "+TableDescTriples.name()+".PredObj") ;
            connection().exec("DROP INDEX "+TableDescTriples.name()+".ObjSubj") ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException indexing table '"+TableDescTriples.name()+"'",ex) ; }
    }

    @Override
    protected void formatTableNodes()
    {
        dropTable(TableDescNodes.name()) ;
        try { 
            connection().exec(sqlStr ("CREATE TABLE "+TableDescNodes.name()+" (",
                                       "   hash BIGINT NOT NULL,",
                                       "   lex NTEXT NOT NULL,",   // NVARCHAR(max) better but not in SQL Server 2000 
                                       "   lang NVARCHAR(10) NOT NULL DEFAULT '',",
                                       "   datatype NVARCHAR("+TableDescNodes.DatatypeUriLength+") NOT NULL default '',",
                                       "   type INT NOT NULL DEFAULT '0',",
                                       "   PRIMARY KEY (hash)",
                                       ")"
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableDescNodes.name()+"'",ex) ;
        }
    }

    @Override
    protected void formatTablePrefixes()
    {
        dropTable(TablePrefixes.name()) ;
        try { 
            connection().exec(sqlStr(
                                      "CREATE TABLE "+TablePrefixes.name()+" (",
                                      "    prefix NVARCHAR("+TablePrefixes.prefixColWidth+") NOT NULL ,",
                                      "    uri NVARCHAR("+TablePrefixes.uriColWidth+") NOT NULL ,", 
                                      "    PRIMARY KEY  (prefix)",
                                      ")"            
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException resetting table '"+TablePrefixes.name()+"'",ex) ;
        }
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