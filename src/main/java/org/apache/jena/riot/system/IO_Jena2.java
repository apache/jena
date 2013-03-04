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
import org.apache.jena.riot.adapters.RDFWriterRIOT ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.n3.N3JenaWriter ;
import com.hp.hpl.jena.rdf.model.impl.RDFWriterFImpl ;

public class IO_Jena2
{
    // Jena writer adapters
    // To adapters.JenaReadersWriters
    public static class RDFWriterRIOT_RDFXML        extends RDFWriterRIOT   { public RDFWriterRIOT_RDFXML()         { super("RDF/XML") ; } }
    public static class RDFWriterRIOT_RDFXMLAbbrev  extends RDFWriterRIOT   { public RDFWriterRIOT_RDFXMLAbbrev()   { super("RDF/XML-ABBREV") ; } }
    public static class RDFWriterRIOT_NTriples      extends RDFWriterRIOT   { public RDFWriterRIOT_NTriples()       { super("N-TRIPLES") ; } }
    public static class RDFWriterRIOT_N3            extends RDFWriterRIOT   { public RDFWriterRIOT_N3()             { super("N3") ; } }
    public static class RDFWriterRIOT_N3_PP         extends RDFWriterRIOT   { public RDFWriterRIOT_N3_PP()          { super(N3JenaWriter.n3WriterPrettyPrinter) ; } }
    public static class RDFWriterRIOT_N3Plain       extends RDFWriterRIOT   { public RDFWriterRIOT_N3Plain()        { super(N3JenaWriter.n3WriterPlain) ; } }
    public static class RDFWriterRIOT_N3Triples     extends RDFWriterRIOT   { public RDFWriterRIOT_N3Triples()      { super(N3JenaWriter.n3WriterTriples) ; } }
    public static class RDFWriterRIOT_N3TriplesAlt  extends RDFWriterRIOT   { public RDFWriterRIOT_N3TriplesAlt()   { super(N3JenaWriter.n3WriterTriplesAlt) ; } }
    public static class RDFWriterRIOT_Turtle        extends RDFWriterRIOT   { public RDFWriterRIOT_Turtle()         { super(N3JenaWriter.turtleWriter) ; } }
    public static class RDFWriterRIOT_Turtle1       extends RDFWriterRIOT   { public RDFWriterRIOT_Turtle1()        { super(N3JenaWriter.turtleWriterAlt1) ; } }
    public static class RDFWriterRIOT_Turtle2       extends RDFWriterRIOT   { public RDFWriterRIOT_Turtle2()        { super(N3JenaWriter.turtleWriterAlt2) ; } }
    public static class RDFWriterRIOT_RDFJSON       extends RDFWriterRIOT   { public RDFWriterRIOT_RDFJSON()        { super("RDF/JSON") ; } }
    
    private static Map<String, RDFFormat> mapJenaNameToFormat               = new HashMap<String, RDFFormat>() ;

    /** return the RDFFormat for the existing Jena writer name, or null */ 
    public static RDFFormat getFormatForJenaWriter(String jenaName) { return mapJenaNameToFormat.get(jenaName) ; }

    /** Register an RDFFormat for a Jena writer name */ 
    public static void setFormatForJenaWriter(String jenaName, RDFFormat format) { mapJenaNameToFormat.put(jenaName, format) ; }

    /** Return a collection of jena writer names */ 
    public static Collection<String> getJenaWriterNames() { return mapJenaNameToFormat.keySet() ; }
    
    public static void wireIntoJenaW()
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
        
        setFormatForJenaWriter("RDF/JSON",                          RDFFormat.RDFJSON) ;
        setFormatForJenaWriter("RDFJSON",                           RDFFormat.RDFJSON) ;
        
//        RDFWriterFImpl.LANGS
//        RDFWriterFImpl.DEFAULTWRITER
        RDFWriterFImpl.setBaseWriterClassName("RDF/XML",            RDFWriterRIOT_RDFXML.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("RDF/XML-ABBREV",     RDFWriterRIOT_RDFXMLAbbrev.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("N-TRIPLE",           RDFWriterRIOT_NTriples.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("N-TRIPLES",          RDFWriterRIOT_NTriples.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("N-Triples",          RDFWriterRIOT_NTriples.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("NT",                 RDFWriterRIOT_NTriples.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("N3",                 RDFWriterRIOT_N3.class.getName()) ;
        
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterPrettyPrinter,   RDFWriterRIOT_N3_PP.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterPlain,           RDFWriterRIOT_N3Plain.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterTriples,         RDFWriterRIOT_N3Triples.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterTriplesAlt,      RDFWriterRIOT_N3TriplesAlt.class.getName()) ;
        
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.turtleWriter,            RDFWriterRIOT_Turtle.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.turtleWriterAlt1,        RDFWriterRIOT_Turtle1.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.turtleWriterAlt2,        RDFWriterRIOT_Turtle2.class.getName()) ;
        
        RDFWriterFImpl.setBaseWriterClassName("RDF/JSON",       RDFWriterRIOT_RDFJSON.class.getName()) ;
        RDFWriterFImpl.setBaseWriterClassName("RDFJSON",        RDFWriterRIOT_RDFJSON.class.getName()) ;
    }
    
    public static void resetJenaW()
    {
        // This is the old Jena configuration (bugs and all) 
        RDFWriterFImpl.setBaseWriterClassName("RDF/XML",        Jena.PATH + ".xmloutput.impl.Basic") ;
        RDFWriterFImpl.setBaseWriterClassName("RDF/XML-ABBREV", Jena.PATH + ".xmloutput.impl.Abbreviated") ;

        RDFWriterFImpl.setBaseWriterClassName("N-TRIPLE",       Jena.PATH + ".rdf.model.impl.NTripleWriter") ;
        RDFWriterFImpl.setBaseWriterClassName("N-TRIPLES",      Jena.PATH + ".rdf.model.impl.NTripleWriter") ;
        RDFWriterFImpl.setBaseWriterClassName("N-Triples",      Jena.PATH + ".rdf.model.impl.NTripleWriter") ;
        
        RDFWriterFImpl.setBaseWriterClassName("N3",             Jena.PATH + ".n3.N3JenaWriter") ;         
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterPrettyPrinter,    Jena.PATH + ".n3.N3JenaWriterPP") ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterPlain,           Jena.PATH + ".n3.N3TurtleJenaWriter") ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterTriples,         Jena.PATH + ".n3.N3TurtleJenaWriter") ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.n3WriterTriplesAlt,      Jena.PATH + ".n3.N3JenaWriterTriples") ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.turtleWriter,            Jena.PATH + ".n3.N3TurtleJenaWriter") ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.turtleWriterAlt1,        Jena.PATH + ".n3.N3TurtleJenaWriter") ;
        RDFWriterFImpl.setBaseWriterClassName(N3JenaWriter.turtleWriterAlt2,        Jena.PATH + ".n3.N3TurtleJenaWriter") ;
        
    }
}

