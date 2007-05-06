/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.hash;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.layout2.FmtLayout2;
import com.hp.hpl.jena.sdb.layout2.TableNodes;
import com.hp.hpl.jena.sdb.layout2.TablePrefixes;
import com.hp.hpl.jena.sdb.layout2.TableTriples;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.sql.TableUtils;


public class FmtLayout2HashHSQL extends FmtLayout2
{
    static private Log log = LogFactory.getLog(FmtLayout2HashHSQL.class) ;
    private MySQLEngineType engineType ;
    
    public FmtLayout2HashHSQL(SDBConnection connection)
    { 
        super(connection) ;
    }

    @Override
    protected void formatTableTriples()
    {
        dropTable(TableTriples.name()) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE TABLE "+TableTriples.name()+" (",
                                 "    s BIGINT NOT NULL ,",
                                 "    p BIGINT NOT NULL ,",
                                 "    o BIGINT NOT NULL ,",
                                 "    PRIMARY KEY (s, p, o)",
                                 ")"               
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableTriples.name()+"'",ex) ;
        }
    }

    @Override
    protected void dropIndexesTableTriples()
    {
        try {
            connection().exec("DROP INDEX PredObj IF EXIST") ;
            connection().exec("DROP INDEX ObjSubj IF EXIST") ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException dropping indexes for table '"+TableTriples.name()+"'",ex) ; }
    }
    

    @Override
    protected void formatTableNodes()
    {
        dropTable(TableNodes.name()) ;
        try { 
            connection().exec(sqlStr (
                     "CREATE TABLE "+TableNodes.name()+" (",
                     "   hash BIGINT NOT NULL ,",
                     "   lex VARCHAR NOT NULL ,",
                     "   lang VARCHAR(10) default '' NOT NULL ,",
                     "   datatype VARCHAR default '' NOT NULL ,",
                     "   type int default 0 NOT NULL ,",
                     "   PRIMARY KEY (hash)",
                     ")"  
                )) ;
            connection().exec("CREATE UNIQUE INDEX Hash ON "+TableNodes.name()+" (hash)") ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException resetting table '"+TableNodes.name()+"'",ex) ;
        }
    }

    @Override
    protected void formatTablePrefixes()
    {
        dropTable(TablePrefixes.name()) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE TABLE "+TablePrefixes.name()+" (",
                                 "    prefix  VARCHAR NOT NULL,",
                                 "    uri     VARCHAR NOT NULL,", 
                                 "    PRIMARY KEY  (prefix)",
                                 ")"            
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException resetting table '"+TablePrefixes.name()+"'",ex) ;
        }
    }
    
    @Override
    protected void truncateTable(String tableName)
    { 
        try { 
            if ( TableUtils.hasTable(connection().getSqlConnection(), tableName) )
                connection().exec("DELETE FROM "+tableName) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException : Can't truncate table: "+tableName, ex) ; }
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