/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;

import static dev.DBSyntax.* ;

public class FmtStdHashDerby extends StoreFormatterStd
{
    private DBSyntax syntax ;
    private static final boolean NOT_NULL = false ;
    

    public FmtStdHashDerby(SDBConnection conn, DBSyntax syntax)
    {
        super(conn) ;
        this.syntax = syntax ;
    }

    @Override
    protected void formatTableNodes()
    {
        dropTable(TableDescNodes.name()) ;
        try { 
            connection().exec(sqlStr ("CREATE TABLE "+TableDescNodes.name()+" (",
                                      "   "+col("hash",     syntax.integer64(),     NOT_NULL),
                                      "   "+col("lex",      syntax.text(),          NOT_NULL),
                                      "   "+col("lang",     syntax.varchar(20),     NOT_NULL),
                                      "   "+col("datatype", syntax.varchar(200),    NOT_NULL),
                                      "   "+col("type",     syntax.integer32(),     NOT_NULL),
                                      "   "+syntax.primaryKey("hash"),
                                      
//                                       "   hash BIGINT NOT NULL ,",
//                                       "   lex CLOB NOT NULL ,",
//                                       "   lang LONG VARCHAR NOT NULL ,",
//                                       "   datatype LONG VARCHAR NOT NULL ,",
//                                       "   type integer NOT NULL ,",
//                                       "   PRIMARY KEY (hash)",
                                       ")"
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException resetting table '"+TableDescNodes.name()+"'",ex) ;
        }
    }

    @Override
    protected void formatTablePrefixes()
    {}

    @Override
    protected void formatTableTriples()
    {
        dropTable(TableDescTriples.name()) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE TABLE "+TableDescTriples.name()+" (",
                                 "    "+col("s", syntax.integer64(), NOT_NULL),
                                 "    "+col("p", syntax.integer64(), NOT_NULL),
                                 "    "+col("o", syntax.integer64(), NOT_NULL),
                                 "    "+syntax.primaryKey("s", "p", "o") ,
                                 ")"                
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ;
        }
    }

    @Override
    protected void formatTableQuads()
    {
        dropTable(TableDescQuads.name()) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE TABLE "+TableDescQuads.name()+" (",
                                 "    "+col("g", syntax.integer64(), NOT_NULL),
                                 "    "+col("s", syntax.integer64(), NOT_NULL),
                                 "    "+col("p", syntax.integer64(), NOT_NULL),
                                 "    "+col("o", syntax.integer64(), NOT_NULL),
                                 "    "+syntax.primaryKey("g", "s", "p", "o") ,
                                 ")"                
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ;
        }

    }


    @Override
    protected void addIndexesTableQuads()
    {}

    @Override
    protected void addIndexesTableTriples()
    {}

    @Override
    protected void dropIndexesTableQuads()
    {}

    @Override
    protected void dropIndexesTableTriples()
    {}

    @Override
    protected void truncateTableNodes()
    {}

    @Override
    protected void truncateTablePrefixes()
    {}

    @Override
    protected void truncateTableQuads()
    {}

    @Override
    protected void truncateTableTriples()
    {}

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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