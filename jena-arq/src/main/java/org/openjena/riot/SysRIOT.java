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

package org.openjena.riot;

import org.openjena.atlas.event.EventType ;
import org.openjena.riot.system.JenaReaderNTriples2 ;
import org.openjena.riot.system.JenaReaderRdfJson ;
import org.openjena.riot.system.JenaReaderTurtle2 ;
import org.openjena.riot.system.JenaWriterRdfJson ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl ;
import com.hp.hpl.jena.rdf.model.impl.RDFWriterFImpl ;

public class SysRIOT
{
    public static final String riotLoggerName = "org.openjena.riot" ;
    private static Logger riotLogger = LoggerFactory.getLogger(riotLoggerName) ;
    public static final EventType startRead = new EventType(SysRIOT.class, "StartRead") ;
    public static final EventType finishRead = new EventType(SysRIOT.class, "FinishRead") ;
    
    public static boolean StrictXSDLexicialForms = false ;
    public static boolean strictMode             = false ;
    
    public static final String BNodeGenIdPrefix = "genid" ;
    
    static public String fmtMessage(String message, long line, long col)
    {
        if ( col == -1 && line == -1 )
                return message ;
        if ( col == -1 && line != -1 )
            return String.format("[line: %d] %s", line, message) ;
        if ( col != -1 && line == -1 )
            return String.format("[col: %d] %s", col, message) ;
        // Mild attempt to keep some alignment
        return String.format("[line: %d, col: %-2d] %s", line, col, message) ;
    }

    public static Logger getLogger()
    {
        return riotLogger ;
    }
    
    static String jenaNTriplesReader = "com.hp.hpl.jena.rdf.model.impl.NTripleReader" ; 
    static String jenaTurtleReader = "com.hp.hpl.jena.n3.turtle.TurtleReader" ; 
    static String jenaN3Reader = jenaTurtleReader ; 
    
    public static void wireIntoJena()
    {
        RIOT.init() ;
        /* No getter (!!)
         * Standard:
            com.hp.hpl.jena.rdf.model.impl.NTripleReader
            com.hp.hpl.jena.rdf.model.impl.NTripleReader
            
            com.hp.hpl.jena.n3.turtle.TurtleReader 
            com.hp.hpl.jena.n3.turtle.TurtleReader 
            com.hp.hpl.jena.n3.turtle.TurtleReader 
            com.hp.hpl.jena.n3.turtle.TurtleReader 
         */

        // Override N-TRIPLES and Turtle with faster implementations.
        String readerNT = JenaReaderNTriples2.class.getName() ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", readerNT) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE",   readerNT) ;
        
        String readerTTL = JenaReaderTurtle2.class.getName() ;
        RDFReaderFImpl.setBaseReaderClassName("N3",     readerTTL) ;
        RDFReaderFImpl.setBaseReaderClassName("TURTLE", readerTTL) ;
        RDFReaderFImpl.setBaseReaderClassName("Turtle", readerTTL) ;
        RDFReaderFImpl.setBaseReaderClassName("TTL",    readerTTL) ;

        // Add in the RDF/JSON reader and writer
        String readerRdfJson = JenaReaderRdfJson.class.getName() ;
        RDFReaderFImpl.setBaseReaderClassName("RDF/JSON", readerRdfJson) ;
        String writerRdfJson = JenaWriterRdfJson.class.getName() ;
        RDFWriterFImpl.setBaseWriterClassName("RDF/JSON", writerRdfJson) ;
    }
    
    public static void resetJenaReaders()
    {
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", jenaNTriplesReader) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE",  jenaNTriplesReader) ;
        
        RDFReaderFImpl.setBaseReaderClassName("N3",     jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("TURTLE", jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("Turtle", jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("TTL",    jenaTurtleReader) ;

        RDFReaderFImpl.setBaseReaderClassName("RDF/JSON", "") ;
        RDFWriterFImpl.setBaseWriterClassName("RDF/JSON", "") ;
    }

}
