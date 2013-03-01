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
//        Class<?> readerRDF = RDFReaderRIOT.class ;
//        registerForModelRead("RDF/XML",    readerRDF) ;
//        registerForModelRead("RDF/XML-ABBREV", readerRDF) ;
//
//        registerForModelRead("N-TRIPLES",  readerRDF) ;
//        registerForModelRead("N-Triples",  readerRDF) ;
//        registerForModelRead("N-TRIPLE",   readerRDF) ;
//        registerForModelRead("N3",         readerRDF) ;
//        registerForModelRead("TURTLE",     readerRDF) ;
//        registerForModelRead("Turtle",     readerRDF) ;
//        registerForModelRead("TTL",        readerRDF) ;
//        registerForModelRead("RDF/JSON",   readerRDF) ;
        
      registerForModelRead("RDF/XML",    JenaReadersWriters.RDFReaderRIOT_RDFXML.class) ;           // And default
      registerForModelRead("RDF/XML-ABBREV", JenaReadersWriters.RDFReaderRIOT_RDFXML.class) ;

      registerForModelRead("N-TRIPLES",  JenaReadersWriters.RDFReaderRIOT_NT.class) ;
      registerForModelRead("N-Triples",  JenaReadersWriters.RDFReaderRIOT_NT.class) ;
      registerForModelRead("N-TRIPLE",   JenaReadersWriters.RDFReaderRIOT_NT.class) ;
      registerForModelRead("N3",         JenaReadersWriters.RDFReaderRIOT_TTL.class) ;
      registerForModelRead("TURTLE",     JenaReadersWriters.RDFReaderRIOT_TTL.class) ;
      registerForModelRead("Turtle",     JenaReadersWriters.RDFReaderRIOT_TTL.class) ;
      registerForModelRead("TTL",        JenaReadersWriters.RDFReaderRIOT_TTL.class) ;
      registerForModelRead("RDF/JSON",   JenaReadersWriters.RDFReaderRIOT_RDFJSON.class) ;
      
      // Old style Jena writers.
      // TODO Remove when riot-output arrives.
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
    
    /** Register for use with Model.read **/ 
    public static void registerForModelRead(String name, Class<?> cls)
    {
        RDFReaderFImpl.setBaseReaderClassName(name, cls.getName()) ;
    }
    
    /** Register for use with Model.write **/ 
    public static void registerForModelWrite(String name, Class<?> cls)
    {
        RDFWriterFImpl.setBaseWriterClassName(name, cls.getName()) ;
    }

}

