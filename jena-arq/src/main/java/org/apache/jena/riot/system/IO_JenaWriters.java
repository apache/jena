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

package org.apache.jena.riot.system;

import java.util.Collection ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_JSONLD ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3 ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3Plain ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3Triples ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3TriplesAlt ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_N3_PP ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_NTriples ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_RDFJSON ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_Turtle ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_Turtle1 ;
import org.apache.jena.riot.adapters.JenaReadersWriters.RDFWriterRIOT_Turtle2 ;

import com.hp.hpl.jena.n3.N3JenaWriter ;
import com.hp.hpl.jena.rdf.model.impl.RDFWriterFImpl ;

public class IO_JenaWriters
{
    private static Map<String, RDFFormat> mapJenaNameToFormat               = new HashMap<>() ;

    /** return the RDFFormat for the existing Jena writer name, or null */ 
    public static RDFFormat getFormatForJenaWriter(String jenaName) { return mapJenaNameToFormat.get(jenaName) ; }

    /** Register an RDFFormat for a Jena writer name */ 
    public static void setFormatForJenaWriter(String jenaName, RDFFormat format) { mapJenaNameToFormat.put(jenaName, format) ; }

    /** Return a collection of jena writer names */ 
    public static Collection<String> getJenaWriterNames() { return mapJenaNameToFormat.keySet() ; }
    
    public static void wireIntoJena()
    {
        setFormatForJenaWriter("RDF/XML",                           RDFFormat.RDFXML_PLAIN) ;
        setFormatForJenaWriter("RDF/XML-ABBREV",                    RDFFormat.RDFXML_ABBREV) ;
        setFormatForJenaWriter("N-TRIPLE",                          RDFFormat.NTRIPLES) ;
        setFormatForJenaWriter("NT",                                RDFFormat.NTRIPLES) ;
        setFormatForJenaWriter("N-TRIPLES",                         RDFFormat.NTRIPLES) ;
        setFormatForJenaWriter("N-Triples",                         RDFFormat.NTRIPLES) ;
        setFormatForJenaWriter("N3",                                RDFFormat.TURTLE) ;
        setFormatForJenaWriter(N3JenaWriter.n3WriterPrettyPrinter,  RDFFormat.TURTLE_PRETTY) ;
        setFormatForJenaWriter(N3JenaWriter.n3WriterPlain,          RDFFormat.TURTLE_BLOCKS) ;
        setFormatForJenaWriter(N3JenaWriter.n3WriterTriples,        RDFFormat.TURTLE_FLAT) ;
        setFormatForJenaWriter(N3JenaWriter.n3WriterTriplesAlt,     RDFFormat.TURTLE_FLAT) ;
        setFormatForJenaWriter(N3JenaWriter.turtleWriter,           RDFFormat.TURTLE) ;
        setFormatForJenaWriter(N3JenaWriter.turtleWriterAlt1,       RDFFormat.TURTLE) ;
        setFormatForJenaWriter(N3JenaWriter.turtleWriterAlt2,       RDFFormat.TURTLE) ;

        setFormatForJenaWriter("JSON-LD",                           RDFFormat.JSONLD) ; 
        setFormatForJenaWriter("JSONLD",                            RDFFormat.JSONLD) ; 
        
        setFormatForJenaWriter("RDF/JSON",                          RDFFormat.RDFJSON) ;
        setFormatForJenaWriter("RDFJSON",                           RDFFormat.RDFJSON) ;
        
        //registerForModelWrite("RDF/XML",                            RDFWriterRIOT_RDFXML.class) ;
        //registerForModelWrite("RDF/XML-ABBREV",                     RDFWriterRIOT_RDFXMLAbbrev.class) ;
        
        // Use the original classes so that setting properties works transparently.
        registerForModelWrite("RDF/XML",                            com.hp.hpl.jena.rdfxml.xmloutput.impl.Basic.class) ;
        registerForModelWrite("RDF/XML-ABBREV",                     com.hp.hpl.jena.rdfxml.xmloutput.impl.Abbreviated.class) ;
        
        registerForModelWrite("N-TRIPLE",                           RDFWriterRIOT_NTriples.class) ;
        registerForModelWrite("N-TRIPLES",                          RDFWriterRIOT_NTriples.class) ;
        registerForModelWrite("N-Triples",                          RDFWriterRIOT_NTriples.class) ;
        registerForModelWrite("NT",                                 RDFWriterRIOT_NTriples.class) ;
        registerForModelWrite("N3",                                 RDFWriterRIOT_N3.class) ;
        
        registerForModelWrite(N3JenaWriter.n3WriterPrettyPrinter,   RDFWriterRIOT_N3_PP.class) ;
        registerForModelWrite(N3JenaWriter.n3WriterPlain,           RDFWriterRIOT_N3Plain.class) ;
        registerForModelWrite(N3JenaWriter.n3WriterTriples,         RDFWriterRIOT_N3Triples.class) ;
        registerForModelWrite(N3JenaWriter.n3WriterTriplesAlt,      RDFWriterRIOT_N3TriplesAlt.class) ;
        
        registerForModelWrite(N3JenaWriter.turtleWriter,            RDFWriterRIOT_Turtle.class) ;
        registerForModelWrite(N3JenaWriter.turtleWriterAlt1,        RDFWriterRIOT_Turtle1.class) ;
        registerForModelWrite(N3JenaWriter.turtleWriterAlt2,        RDFWriterRIOT_Turtle2.class) ;

        registerForModelWrite("JSON-LD",                            RDFWriterRIOT_JSONLD.class) ;
        registerForModelWrite("JSONLD",                             RDFWriterRIOT_JSONLD.class) ;
        
        registerForModelWrite("RDF/JSON",                           RDFWriterRIOT_RDFJSON.class) ;
        registerForModelWrite("RDFJSON",                            RDFWriterRIOT_RDFJSON.class) ;
    }
    
    public static void resetJena()
    {
        // This is the old Jena configuration (bugs and all) 
        RDFWriterFImpl.setBaseWriterClassName("RDF/XML",        com.hp.hpl.jena.rdfxml.xmloutput.impl.Basic.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("RDF/XML-ABBREV", com.hp.hpl.jena.rdfxml.xmloutput.impl.Abbreviated.class.getName()) ;

        RDFWriterFImpl.setBaseWriterClassName("N-TRIPLE",       com.hp.hpl.jena.rdf.model.impl.NTripleWriter.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("NT",             "") ;
        RDFWriterFImpl.setBaseWriterClassName("N-TRIPLES",      com.hp.hpl.jena.rdf.model.impl.NTripleWriter.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("N-Triples",      com.hp.hpl.jena.rdf.model.impl.NTripleWriter.class.getName()) ;

        RDFWriterFImpl.setBaseWriterClassName("N3",             com.hp.hpl.jena.n3.N3JenaWriter.class.getName()) ;         
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterPrettyPrinter,    com.hp.hpl.jena.n3.N3JenaWriterPP.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterPlain,           com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterTriples,         com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterTriplesAlt,      com.hp.hpl.jena.n3.N3JenaWriterTriples.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.turtleWriter,            com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.turtleWriterAlt1,        com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.turtleWriterAlt2,        com.hp.hpl.jena.n3.N3TurtleJenaWriter.class.getName()) ;
        
        RDFWriterFImpl.setBaseWriterClassName("JSON-LD",    "");
        RDFWriterFImpl.setBaseWriterClassName("JSON",       "");

        RDFWriterFImpl.setBaseWriterClassName("RDF/JSON",   "");
        RDFWriterFImpl.setBaseWriterClassName("RDFJSON",    "");
    }
    
    /** Register for use with Model.write  (old style compatibility) */ 
    public static void registerForModelWrite(String name, Class<?> cls)
    {
        RDFWriterFImpl.setBaseWriterClassName(name, cls.getName()) ;
    }
}

