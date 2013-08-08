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
/* SAP contribution from Fergal Monaghan (m#)/May 2012 */

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.layout2.FmtLayout2;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TablePrefixes;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SAPStorageType;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;


public class FmtLayout2HashSAP extends FmtLayout2
{
    //static private Logger log = LoggerFactory.getLogger(FormatterTriplesNodesSAP.class) ;
	protected SAPStorageType storageType;
    
    public FmtLayout2HashSAP(SDBConnection connection, SAPStorageType storageType)
    { 
        super(connection) ;
        this.storageType = storageType;
    }

    @Override
    protected void formatTableTriples()
    {
        String tname = TableDescTriples.name() ;
        dropTable(tname) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE " + storageType.getStorageName() + " TABLE "+tname+" (",
                                 "    s BIGINT NOT NULL,",
                                 "    p BIGINT NOT NULL,",
                                 "    o BIGINT NOT NULL",
                                 ")"
                    )) ;
            connection().exec(sqlStr("ALTER TABLE "+tname+" ADD PRIMARY KEY (s, p, o)")) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ; }
    }
    
    @Override
    protected void formatTableQuads()
    {
        String tname = TableDescQuads.name() ;
        dropTable(tname) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE " + storageType.getStorageName() + " TABLE "+tname+" (",
                                 "    g BIGINT NOT NULL,",
                                 "    s BIGINT NOT NULL,",
                                 "    p BIGINT NOT NULL,",
                                 "    o BIGINT NOT NULL",
                                 ")"
                    )) ;
            connection().exec(sqlStr("ALTER TABLE "+tname+" ADD PRIMARY KEY (g, s, p, o)")) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ; }
    }

    @Override
    protected String syntaxDropIndex(String indexName, String table)
    {
        return String.format("DROP INDEX IF EXISTS %s", indexName) ;
    }

    @Override
    protected void formatTableNodes()
    {
        String tname = TableDescNodes.name() ;
        dropTable(tname) ;
        try { 
            connection().exec(sqlStr ("CREATE " + storageType.getStorageName() + " TABLE "+tname+" (",
                                       "   hash BIGINT NOT NULL,",
                                       "   lex nvarchar(5000) NOT NULL,",
                                       "   lang nvarchar (10) NOT NULL default '',",
                                       "   datatype nvarchar("+TableDescNodes.DatatypeUriLength+") NOT NULL default '',",
                                       "   type integer NOT NULL default '0'",
                                       ")"
                    )) ;
            connection().exec(sqlStr ("ALTER TABLE "+tname+" ADD PRIMARY KEY (hash)")) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableDescNodes.name()+"'",ex) ;
        }
    }

    @Override
    protected void formatTablePrefixes()
    {
        String tname = TablePrefixes.name() ;
        dropTable(tname) ;
        try { 
            connection().exec(sqlStr(
                                      "CREATE " + storageType.getStorageName() + " TABLE "+tname+" (",
                                      "    prefix NVARCHAR("+TablePrefixes.prefixColWidth+") NOT NULL ,",
                                      "    uri NVARCHAR("+TablePrefixes.uriColWidth+") NOT NULL",
                                      ")"
                    )) ;
            connection().exec(sqlStr("ALTER TABLE "+tname+" ADD CONSTRAINT "+tname+"_PK PRIMARY KEY (prefix)")) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException resetting table '"+TablePrefixes.name()+"'",ex) ;
        }
    }
}
