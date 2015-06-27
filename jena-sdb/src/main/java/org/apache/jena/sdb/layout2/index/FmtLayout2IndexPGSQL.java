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

package org.apache.jena.sdb.layout2.index;

import static org.apache.jena.sdb.sql.SQLUtils.sqlStr ;

import java.sql.SQLException;

import org.apache.jena.sdb.layout2.TableDescNodes ;
import org.apache.jena.sdb.layout2.TableDescQuads ;
import org.apache.jena.sdb.layout2.TableDescTriples ;
import org.apache.jena.sdb.layout2.hash.FmtLayout2HashPGSQL ;
import org.apache.jena.sdb.sql.SDBConnection ;
import org.apache.jena.sdb.sql.SDBExceptionSQL ;


public class FmtLayout2IndexPGSQL extends FmtLayout2HashPGSQL
{
    public FmtLayout2IndexPGSQL(SDBConnection connection)
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
                                 "    s integer NOT NULL,",
                                 "    p integer NOT NULL,",
                                 "    o integer NOT NULL,",
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
                                 "    g integer NOT NULL,",
                                 "    s integer NOT NULL,",
                                 "    p integer NOT NULL,",
                                 "    o integer NOT NULL,",
                                 "    PRIMARY KEY (g, s, p, o)",
                                 ")"                
                    )) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ; }
    }
    
    @Override
    protected void formatTableNodes()
    {
        dropTable(TableDescNodes.name()) ;
        try { 
            connection().exec(sqlStr ("CREATE TABLE "+TableDescNodes.name()+" (",
                                       "   id SERIAL,",
                                       "   hash BIGINT NOT NULL,",
                                       "   lex TEXT NOT NULL,",
                                       "   lang varchar NOT NULL default '',",
                                       "   datatype varchar("+TableDescNodes.DatatypeUriLength+") NOT NULL default '',",
                                       "   type integer NOT NULL default '0',",
                                       "   PRIMARY KEY (id)",
                                       ")"
                    )) ;
            connection().exec("CREATE UNIQUE INDEX Hash ON " + TableDescNodes.name() + " (hash)");
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableDescNodes.name()+"'",ex) ;
        }
    }
}
