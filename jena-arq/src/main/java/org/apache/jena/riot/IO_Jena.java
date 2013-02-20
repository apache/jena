/**
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

package org.apache.jena.riot;

import org.apache.jena.riot.adapters.JenaReadersWriters ;
import org.apache.jena.riot.system.JenaWriterRdfJson ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl ;
import com.hp.hpl.jena.rdf.model.impl.RDFWriterFImpl ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class IO_Jena
{
    static Logger log = LoggerFactory.getLogger(IO_Jena.class) ;
    
    private static String riotBase = "http://jena.apache.org/riot/" ; 
    private static String streamManagerSymbolStr = riotBase+"streammanager" ; 
    public static Symbol streamManagerSymbol = Symbol.create(streamManagerSymbolStr) ; 

    public static void wireIntoJena()
    {
//        // Wire in generic 
//        String readerRDF = RDFReaderRIOT.class.getName() ;
//        RDFReaderFImpl.setBaseReaderClassName("RDF/XML",    readerRDF) ;           // And default
//        RDFReaderFImpl.setBaseReaderClassName("RDF/XML-ABBREV", readerRDF) ;
//
//        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES",  readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("N-Triples",  readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE",   readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("N3",         readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("TURTLE",     readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("Turtle",     readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("TTL",        readerRDF) ;
//        RDFReaderFImpl.setBaseReaderClassName("RDF/JSON",   readerRDF) ;
        
      RDFReaderFImpl.setBaseReaderClassName("RDF/XML",    JenaReadersWriters.RDFReaderRIOT_RDFXML.class.getName()) ;           // And default
      RDFReaderFImpl.setBaseReaderClassName("RDF/XML-ABBREV", JenaReadersWriters.RDFReaderRIOT_RDFXML.class.getName()) ;

      RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES",  JenaReadersWriters.RDFReaderRIOT_NT.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("N-Triples",  JenaReadersWriters.RDFReaderRIOT_NT.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE",   JenaReadersWriters.RDFReaderRIOT_NT.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("N3",         JenaReadersWriters.RDFReaderRIOT_TTL.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("TURTLE",     JenaReadersWriters.RDFReaderRIOT_TTL.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("Turtle",     JenaReadersWriters.RDFReaderRIOT_TTL.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("TTL",        JenaReadersWriters.RDFReaderRIOT_TTL.class.getName()) ;
      RDFReaderFImpl.setBaseReaderClassName("RDF/JSON",   JenaReadersWriters.RDFReaderRIOT_RDFJSON.class.getName()) ;
      
      // Old style Jena writers.
      String writerRdfJson = JenaWriterRdfJson.class.getName() ;
      RDFWriterFImpl.setBaseWriterClassName("RDF/JSON", writerRdfJson) ;
    }
    
    static String jenaNTriplesReader = "com.hp.hpl.jena.rdf.model.impl.NTripleReader" ; 
    static String jenaTurtleReader = "com.hp.hpl.jena.n3.turtle.TurtleReader" ; 
    static String jenaN3Reader = jenaTurtleReader ; 
    
    public static void resetJenaReaders()
    {
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", jenaNTriplesReader) ;
        RDFReaderFImpl.setBaseReaderClassName("N-Triples",  jenaNTriplesReader) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE",  jenaNTriplesReader) ;
        
        RDFReaderFImpl.setBaseReaderClassName("N3",     jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("TURTLE", jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("Turtle", jenaTurtleReader) ;
        RDFReaderFImpl.setBaseReaderClassName("TTL",    jenaTurtleReader) ;

        RDFReaderFImpl.setBaseReaderClassName("RDF/JSON", "") ;
        RDFWriterFImpl.setBaseWriterClassName("RDF/JSON", "") ;

    }
}

