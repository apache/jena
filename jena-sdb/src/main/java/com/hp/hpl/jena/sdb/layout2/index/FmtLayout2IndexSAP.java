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
/* SAP contribution from Fergal Monaghan (m#)/May 2012 */
import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.layout2.hash.FmtLayout2HashSAP;
import com.hp.hpl.jena.sdb.sql.SAPStorageType;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;


public class FmtLayout2IndexSAP extends FmtLayout2HashSAP
{
    public FmtLayout2IndexSAP(SDBConnection connection, SAPStorageType storageType)
    { 
        super(connection, storageType) ;
    }

    @Override
    protected void formatTableTriples()
    {
        dropTable(TableDescTriples.name()) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE " + storageType.getStorageName() + " TABLE "+TableDescTriples.name()+" (",
                                 "    s integer NOT NULL,",
                                 "    p integer NOT NULL,",
                                 "    o integer NOT NULL",
                                 ")"
                    )) ;
            connection().exec(sqlStr("ALTER TABLE "+TableDescTriples.name()+" ADD PRIMARY KEY (s, p, o)")) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ; }
    }
    
    @Override
    protected void formatTableQuads()
    {
        dropTable(TableDescQuads.name()) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE " + storageType.getStorageName() + " TABLE "+TableDescQuads.name()+" (",
                                 "    g integer NOT NULL,",
                                 "    s integer NOT NULL,",
                                 "    p integer NOT NULL,",
                                 "    o integer NOT NULL",
                                 ")"
                    )) ;
            connection().exec(sqlStr("ALTER TABLE "+TableDescQuads.name()+" ADD PRIMARY KEY (g, s, p, o)")) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ; }
    }
    
    @Override
    protected void formatTableNodes()
    {
        dropTable(TableDescNodes.name()) ;
        try { 
            connection().exec(sqlStr ("CREATE " + storageType.getStorageName() + " TABLE "+TableDescNodes.name()+" (",
                                       "   id INTEGER NOT NULL,",
                                       "   hash BIGINT NOT NULL,",
                                       "   lex nvarchar(5000) NOT NULL,",
                                       "   lang nvarchar(10) NOT NULL default '',",
                                       "   datatype nvarchar("+TableDescNodes.DatatypeUriLength+") NOT NULL default '',",
                                       "   type integer NOT NULL default '0'",
                                       ")"
                    )) ;
            connection().exec(sqlStr ("ALTER TABLE "+TableDescNodes.name()+" ADD PRIMARY KEY (id)")) ;
            
            // Urgh. How do we find out if a sequence exists?
            connection().execSilent("DROP SEQUENCE nodeid");
            
            connection().exec(sqlStr ("CREATE SEQUENCE nodeid",
                                      "START WITH 1",
                                      "INCREMENT BY 1",
                                      "NO CYCLE"
            		));
            connection().exec("CREATE UNIQUE INDEX Hash ON " + TableDescNodes.name() + " (hash)");
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableDescNodes.name()+"'",ex) ;
        }
    }
}
