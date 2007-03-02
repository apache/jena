/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.layout2.TablePrefixes;
import com.hp.hpl.jena.sdb.layout2.TableTriples;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SDBConnection;


public class FormatterSimpleMySQL extends FormatterSimple
{
    private static Log log = LogFactory.getLog(FormatterSimpleMySQL.class) ;
    private MySQLEngineType engineType ;
    
    public FormatterSimpleMySQL(SDBConnection connection, MySQLEngineType tableType)
    { 
        super(connection) ;
        engineType = tableType ;
        if ( engineType == null )
        {
            log.fatal("Engine type is null") ;
            throw new SDBException("Engine type is null") ;
        }
    }
    
    // -------- Formatting
    
    public void create() { format() ; }

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
                    "  s VARCHAR(200) BINARY NOT NULL ,",
                    "  p VARCHAR(200) BINARY NOT NULL ,",
                    "  o VARCHAR(200) BINARY NOT NULL  ",
                    ",  PRIMARY KEY (s,p,o)",
                    ")",
                    "ENGINE="+engineType.getEngineName()+" CHARSET=utf8"
                )) ;
            // ?? Indexes limited to 1000 bytes (MyISAM?)
            //connection().execAny("CREATE INDEX SPO     ON "+TableTriples.tableName+" (s,p,o)") ;
            connection().exec("CREATE INDEX SubjObj ON "+TableTriples.tableName+" (s(100),o(100))") ;
            connection().exec("CREATE INDEX ObjPred ON "+TableTriples.tableName+" (o(100),p(100))") ;
            connection().exec("CREATE INDEX Pred    ON "+TableTriples.tableName+" (p(100))") ;
        } catch (SQLException ex)
        {
            log.warn("Exception resetting table 'Triples'") ; 
            throw new SDBException("SQLException resetting table 'Triples'",ex) ;
        }
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
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AS IS'' AND ANY EXPRESS OR
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