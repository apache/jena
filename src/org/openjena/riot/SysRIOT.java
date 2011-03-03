/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import org.openjena.atlas.event.EventType ;
import org.openjena.riot.system.JenaReaderNTriples2 ;
import org.openjena.riot.system.JenaReaderTurtle2 ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl ;

public class SysRIOT
{
    public static final String riotLoggerName = "org.openjena.riot" ;
    private static Logger riotLogger = LoggerFactory.getLogger(riotLoggerName) ;
    public static final EventType startRead = new EventType(SysRIOT.class, "StartRead") ;
    public static final EventType finishRead = new EventType(SysRIOT.class, "FinishRead") ;
    
    public static boolean StrictXSDLexicialForms = false ;
    
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

    }
    
    public static void resetJenaReaders()
    {
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", jenaNTriplesReader) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE",  jenaNTriplesReader) ;
        
        RDFReaderFImpl.setBaseReaderClassName("N3",     jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("TURTLE", jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("Turtle", jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("TTL",    jenaTurtleReader) ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd
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
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
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