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

package com.hp.hpl.jena.sdb.layout1;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.layout2.TablePrefixes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.sql.TableUtils;


public class FormatterSimpleDB2 extends FormatterSimple 
{
    private static Logger log = LoggerFactory.getLogger(FormatterSimpleDB2.class) ;
    
    private static final String colDecl = "VARCHAR("+UriWidth+") NOT NULL" ;
    
    public FormatterSimpleDB2(SDBConnection connection)
    { 
        super(connection) ;
    }
    
    @Override
    public void truncate()
    {
        try { 
            connection().exec("DELETE FROM Triples") ;
        } catch (SQLException ex)
        {
            log.warn("Exception truncating tables") ;
            throw new SDBException("SQLException truncating tables",ex) ;
        }
    }
    
    @Override
    public void format()
    {
        reformatPrefixesWorker(false) ;
        reformatDataWorker() ;
    }
    
    private void reformatPrefixesWorker() { reformatPrefixesWorker(false) ; }
    private void reformatPrefixesWorker(boolean loadPrefixes)
    {
        try { 
            dropTable("Prefixes") ;
            connection().exec(sqlStr(
                    "CREATE TABLE Prefixes (",
                    "    prefix VARCHAR ("+TablePrefixes.prefixColWidth+") NOT NULL ,",
                    "    uri VARCHAR("+TablePrefixes.uriColWidth+") NOT NULL ,", 
                    "  PRIMARY KEY(prefix)",
                    ") CCSID UNICODE"
                )) ;
            if ( loadPrefixes )
            {
                connection().execUpdate("INSERT INTO Prefixes VALUES ('x',       'http://example/')") ;
                connection().execUpdate("INSERT INTO Prefixes VALUES ('ex',      'http://example.org/')") ;
                connection().execUpdate("INSERT INTO Prefixes VALUES ('rdf',     'http://www.w3.org/1999/02/22-rdf-syntax-ns#')") ;
                connection().execUpdate("INSERT INTO Prefixes VALUES ('rdfs',    'http://www.w3.org/2000/01/rdf-schema#')") ;
                connection().execUpdate("INSERT INTO Prefixes VALUES ('xsd',     'http://www.w3.org/2001/XMLSchema#')") ;
                connection().execUpdate("INSERT INTO Prefixes VALUES ('owl' ,    'http://www.w3.org/2002/07/owl#')") ;
                connection().execUpdate("INSERT INTO Prefixes VALUES ('foaf',    'http://xmlns.com/foaf/0.1/')") ;
                connection().execUpdate("INSERT INTO Prefixes VALUES ('dc',      'http://purl.org/dc/elements/1.1/')") ;
                connection().execUpdate("INSERT INTO Prefixes VALUES ('dcterms', 'http://purl.org/dc/terms/')") ;
            }
            
        } catch (SQLException ex)
        {
            log.warn("Exception resetting table 'Prefixes'") ; 
            throw new SDBException("SQLException resetting table 'Prefixes'",ex) ;
        }
    }
    
    private void reformatDataWorker()
    {
        
        try {
            dropTable("Triples") ;
            connection().exec(sqlStr(
                    "CREATE TABLE Triples",
                    "(", 
                    "  s "+colDecl+" ,", 
                    "  p "+colDecl+" ,",
                    "  o "+colDecl,
                    // Too long.  Use an SP index below.
                    //"  PRIMARY KEY (s,p,o)",
                    ") CCSID UNICODE"
                )) ;
        } catch (SQLException ex)
        {
            log.warn("Exception resetting table 'Triples'") ; 
            throw new SDBException("SQLException resetting table 'Triples'",ex) ;
        }
    }
    
    protected void dropTable(String tableName)
    {
        TableUtils.dropTable(connection(), tableName) ;
    }
    
    @Override
    public void addIndexes()
    {
        try {
            connection().exec("CREATE INDEX SubjPred ON "+TableDescSPO.name()+" (s,p)") ;
            connection().exec("CREATE INDEX PredObj ON "+TableDescSPO.name()+" (p,o)") ;
            connection().exec("CREATE INDEX ObjSubj ON "+TableDescSPO.name()+" (o,s)") ;
        } catch (SQLException ex)
        {
            throw new SDBException("SQLException indexing table 'Triples'",ex) ;
        }
    }

    @Override
    public void dropIndexes()
    {
        try {
            connection().exec("DROP INDEX "+TableDescSPO.name()+".SubjPred") ;
            connection().exec("DROP INDEX "+TableDescSPO.name()+".PredObj") ;
            connection().exec("DROP INDEX "+TableDescSPO.name()+".ObjSubj") ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException dropping indexes for table 'Triples'",ex) ; }
    }
}
