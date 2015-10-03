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

package org.apache.jena.sdb.layout1;

import static org.apache.jena.sdb.sql.SQLUtils.sqlStr ;

import java.sql.SQLException;

import org.apache.jena.sdb.SDBException ;
import org.apache.jena.sdb.layout2.TablePrefixes ;
import org.apache.jena.sdb.sql.MySQLEngineType ;
import org.apache.jena.sdb.sql.SDBConnection ;
import org.apache.jena.sdb.sql.SDBExceptionSQL ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FormatterSimpleMySQL extends FormatterSimple
{
    private static Logger log = LoggerFactory.getLogger(FormatterSimpleMySQL.class) ;
    private MySQLEngineType engineType ;
    
    // MySQL keys are limited to 765 bytes, and a key is S/P/O
    private static final String colDecl = "VARCHAR(200) BINARY NOT NULL" ;
    
    public FormatterSimpleMySQL(SDBConnection connection, MySQLEngineType tableType)
    { 
        super(connection) ;
        engineType = tableType ;
        if ( engineType == null )
        {
            log.error("Engine type is null") ;
            throw new SDBException("Engine type is null") ;
        }
    }
    
    @Override
    public void truncate()
    {
        try { 
            connection().exec("TRUNCATE TABLE Triples") ;
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
            connection().exec("DROP TABLE IF EXISTS Prefixes") ;
            connection().exec(sqlStr(
                    "CREATE TABLE Prefixes (",
                    "    prefix VARCHAR("+TablePrefixes.prefixColWidth+") BINARY NOT NULL ,",
                    "    uri VARCHAR("+TablePrefixes.uriColWidth+") BINARY NOT NULL ,", 
                    "  PRIMARY KEY(prefix)",
                    ") ENGINE="+engineType.getEngineName()
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
            connection().exec("DROP TABLE IF EXISTS Triples") ;
            connection().exec(sqlStr(
                    "CREATE TABLE Triples",
                    "(", 
                    "  s "+colDecl+" ,",
                    "  p "+colDecl+" ,",
                    "  o "+colDecl+" ,",
                    "  PRIMARY KEY (s,p,o)",
                    ")",
                    "ENGINE="+engineType.getEngineName()+" CHARSET=utf8"
                )) ;
        } catch (SQLException ex)
        {
            log.warn("Exception resetting table 'Triples'") ; 
            throw new SDBException("SQLException resetting table 'Triples'",ex) ;
        }
    }
    
    // Need this if we need to limit the index lengths 
//    @Override
//    public void addIndexes()
//    {
//        try {
//            connection().exec("CREATE INDEX SubjObj ON "+TableTriples.tableName+" (s(100),o(100))") ;
//            connection().exec("CREATE INDEX ObjPred ON "+TableTriples.tableName+" (o(100),p(100))") ;
//            connection().exec("CREATE INDEX Pred    ON "+TableTriples.tableName+" (p(100))") ;
//        } catch (SQLException ex)
//        {
//            throw new SDBException("SQLException indexing table 'Triples'",ex) ;
//        }
//    }
    
    @Override
    public void dropIndexes()
    {
        try {
            connection().exec("DROP INDEX PredObj ON "+TableDescSPO.name()) ;
            connection().exec("DROP INDEX ObjSubj ON "+TableDescSPO.name()) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException dropping indexes for table 'Triples'",ex) ; }
    }
}
