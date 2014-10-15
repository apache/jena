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
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.layout2.hash.FmtLayout2HashDerby;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;

public class FmtLayout2IndexDerby extends FmtLayout2HashDerby
{
    public FmtLayout2IndexDerby(SDBConnection connection)
    { 
        super(connection) ;
    }

    @Override
    protected void formatTableTriples()
    {
        // TODO Generalize : return a template
        TableDescTriples desc = new TableDescTriples() ;
        dropTable(desc.getTableName()) ;
        try { 
            String x = sqlStr(
                              "CREATE TABLE %s (",
                              "    %2$s int NOT NULL,",
                              "    %3$s int NOT NULL,",
                              "    %4$s int NOT NULL,",
                              "    PRIMARY KEY (%2$s, %3$s, %4$s)",
                              ")") ;
            x = String.format(x, desc.getTableName(),
                              desc.getSubjectColName(),
                              desc.getPredicateColName(),
                              desc.getObjectColName()) ;
            
            connection().exec(sqlStr(
                                 "CREATE TABLE "+desc.getTableName()+" (",
                                 "    s int NOT NULL,",
                                 "    p int NOT NULL,",
                                 "    o int NOT NULL,",
                                 "    PRIMARY KEY (s, p, o)",
                                 ")"                
                    )) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException formatting table '"+TableDescTriples.name()+"'",ex) ; }
    }
    
    @Override
    protected void formatTableQuads()
    {
        TableDescQuads desc = new TableDescQuads() ;
        dropTable(desc.getTableName()) ;
        try { 
            String x = sqlStr(
                              "CREATE TABLE %s (",
                              "    %2$s int NOT NULL,",
                              "    %3$s int NOT NULL,",
                              "    %4$s int NOT NULL,",
                              "    %5$s int NOT NULL,",
                              "    PRIMARY KEY (%2$s, %3$s, %4$s, %5$s)",
                              ")") ;
            x = String.format(x, desc.getTableName(),
            		          desc.getGraphColName(),
                              desc.getSubjectColName(),
                              desc.getPredicateColName(),
                              desc.getObjectColName()) ;
            
            connection().exec(sqlStr(
                                 "CREATE TABLE "+desc.getTableName()+" (",
                                 "    g int NOT NULL,",
                                 "    s int NOT NULL,",
                                 "    p int NOT NULL,",
                                 "    o int NOT NULL,",
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
                                       "   id int generated always as identity ,",
                                       "   hash BIGINT NOT NULL ,",
                                       "   lex CLOB NOT NULL ,",
                                       "   lang LONG VARCHAR NOT NULL ,",
                                       "   datatype varchar("+TableDescNodes.DatatypeUriLength+") NOT NULL ,",
                                       "   type integer NOT NULL ,",
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
