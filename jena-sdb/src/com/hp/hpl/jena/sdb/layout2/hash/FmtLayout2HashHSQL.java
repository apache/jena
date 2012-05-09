/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sdb.layout2.hash;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.layout2.FmtLayout2;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TablePrefixes;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.sql.TableUtils;


public class FmtLayout2HashHSQL extends FmtLayout2
{
    static private Logger log = LoggerFactory.getLogger(FmtLayout2HashHSQL.class) ;
    private MySQLEngineType engineType ;
    
    public FmtLayout2HashHSQL(SDBConnection connection)
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
                                 "    s BIGINT NOT NULL ,",
                                 "    p BIGINT NOT NULL ,",
                                 "    o BIGINT NOT NULL ,",
                                 "    PRIMARY KEY (s, p, o)",
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
                                 "    g BIGINT NOT NULL ,",
                                 "    s BIGINT NOT NULL ,",
                                 "    p BIGINT NOT NULL ,",
                                 "    o BIGINT NOT NULL ,",
                                 "    PRIMARY KEY (g, s, p, o)",
                                 ")"               
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableDescQuads.name()+"'",ex) ;
        }
    }
    
//    @Override
//    protected String syntaxCreateIndex(String indexName, String cols, String table)
//    {
//        return String.format("CREATE INDEX %s ON %s %s", indexName, table, cols) ;
//    }
    
    @Override
    protected String syntaxDropIndex(String indexName, String table)
    {
        return String.format("DROP INDEX %s IF EXIST", indexName) ;
    }

    @Override
    protected void formatTableNodes()
    {
        dropTable(TableDescNodes.name()) ;
        try { 
            connection().exec(sqlStr (
                     "CREATE TABLE "+TableDescNodes.name()+" (",
                     "   hash BIGINT NOT NULL ,",
                     "   lex VARCHAR NOT NULL ,",
                     "   lang VARCHAR(10) default '' NOT NULL ,",
                     "   datatype VARCHAR default '' NOT NULL ,",
                     "   type int default 0 NOT NULL ,",
                     "   PRIMARY KEY (hash)",
                     ")"  
                )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException resetting table '"+TableDescNodes.name()+"'",ex) ;
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
