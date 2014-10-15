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

package com.hp.hpl.jena.sdb.layout2.index;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.layout2.FmtLayout2;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TablePrefixes;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;


public class FmtLayout2IndexDB2 extends FmtLayout2
{
    //static private Logger log = LoggerFactory.getLogger(FmtLayout2Derby.class) ;

    public FmtLayout2IndexDB2(SDBConnection connection)
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
                                 "    s INT NOT NULL,",
                                 "    p INT NOT NULL,",
                                 "    o INT NOT NULL,",
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
                                 "    g INT NOT NULL,",
                                 "    s INT NOT NULL,",
                                 "    p INT NOT NULL,",
                                 "    o INT NOT NULL,",
                                 "    PRIMARY KEY (g, s, p, o)",
                                 ")"
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ;
        }
    }

    @Override
    protected void formatTableNodes()
    {
        dropTable(TableDescNodes.name()) ;
        try {
            connection().exec(sqlStr ("CREATE TABLE "+TableDescNodes.name()+" (",
                                       "   id INT NOT NULL ,",
                                       "   hash BIGINT NOT NULL,",
                                       "   lex CLOB NOT NULL,",
                                       "   lang VARCHAR(10),",
                                       "   datatype VARCHAR("+TableDescNodes.DatatypeUriLength+"),",
                                       "   type INTEGER  NOT NULL,",
                                       "   PRIMARY KEY (id)",
                                       ")  CCSID UNICODE"
                    )) ;
            //connection().exec("CREATE UNIQUE INDEX Hash ON " + TableNodes.tableName + " (hash)");

            // Urgh. How do we find out if a sequence exists?
            connection().execSilent("DROP SEQUENCE nodeid");

            connection().exec(sqlStr ("CREATE SEQUENCE nodeid AS INT",
                                      "START WITH 1",
                                      "INCREMENT BY 1",
                                      "CACHE 5000",
                                      "ORDER",
                                      "NO MAXVALUE",
                                      "NO CYCLE"
            		));
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
                                      "    prefix VARCHAR("+TablePrefixes.prefixColWidth+") NOT NULL,",
                                      "    uri VARCHAR("+TablePrefixes.uriColWidth+") ,",
                                      "    PRIMARY KEY  (prefix)",
                                      ")"
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException resetting table '"+TablePrefixes.name()+"'",ex) ;
        }
    }

    /* Use truncate */
    @Override
    protected void truncateTable(String tableName)
    {
        try {
        	//Truncate doesnt work for db2 - have to do kludgy delete from
            connection().exec("DELETE FROM "+tableName) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException truncating table: "+tableName,ex) ; }
    }
}
