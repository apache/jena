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

package org.apache.jena.riot.adapters;

import static org.apache.jena.riot.RDFLanguages.JSONLD ;

import com.hp.hpl.jena.n3.N3JenaWriter ;

public class JenaReadersWriters
{
    // Temporary - eventually, replace all model.read calls with the correct call to RIOT
    // and then the common RDFReaderRIOT can be used.
    
    public static class RDFReaderRIOT_RDFXML        extends RDFReaderRIOT   { public RDFReaderRIOT_RDFXML()     { super("RDF/XML") ; } }
    public static class RDFReaderRIOT_TTL           extends RDFReaderRIOT   { public RDFReaderRIOT_TTL()        { super("TTL") ; } }
    public static class RDFReaderRIOT_NT            extends RDFReaderRIOT   { public RDFReaderRIOT_NT()         { super("N-TRIPLE") ; } }
    public static class RDFReaderRIOT_JSONLD        extends RDFReaderRIOT   { public RDFReaderRIOT_JSONLD()  { super(JSONLD.getName()) ; } }
    public static class RDFReaderRIOT_RDFJSON       extends RDFReaderRIOT   { public RDFReaderRIOT_RDFJSON()    { super("RDF/JSON") ; } }
    
    // Unused - we use the original RDF/XML writers directly to preserve property setting.  
//    public static class RDFWriterRIOT_RDFXML        extends RDFWriterRIOT   { public RDFWriterRIOT_RDFXML()         { super("RDF/XML") ; } }
//    public static class RDFWriterRIOT_RDFXMLAbbrev  extends RDFWriterRIOT   { public RDFWriterRIOT_RDFXMLAbbrev()   { super("RDF/XML-ABBREV") ; } }
    public static class RDFWriterRIOT_NTriples      extends RDFWriterRIOT   { public RDFWriterRIOT_NTriples()       { super("N-TRIPLES") ; } }
    public static class RDFWriterRIOT_N3            extends RDFWriterRIOT   { public RDFWriterRIOT_N3()             { super("N3") ; } }
    public static class RDFWriterRIOT_N3_PP         extends RDFWriterRIOT   { public RDFWriterRIOT_N3_PP()          { super(N3JenaWriter.n3WriterPrettyPrinter) ; } }
    public static class RDFWriterRIOT_N3Plain       extends RDFWriterRIOT   { public RDFWriterRIOT_N3Plain()        { super(N3JenaWriter.n3WriterPlain) ; } }
    public static class RDFWriterRIOT_N3Triples     extends RDFWriterRIOT   { public RDFWriterRIOT_N3Triples()      { super(N3JenaWriter.n3WriterTriples) ; } }
    public static class RDFWriterRIOT_N3TriplesAlt  extends RDFWriterRIOT   { public RDFWriterRIOT_N3TriplesAlt()   { super(N3JenaWriter.n3WriterTriplesAlt) ; } }
    public static class RDFWriterRIOT_Turtle        extends RDFWriterRIOT   { public RDFWriterRIOT_Turtle()         { super(N3JenaWriter.turtleWriter) ; } }
    public static class RDFWriterRIOT_Turtle1       extends RDFWriterRIOT   { public RDFWriterRIOT_Turtle1()        { super(N3JenaWriter.turtleWriterAlt1) ; } }
    public static class RDFWriterRIOT_Turtle2       extends RDFWriterRIOT   { public RDFWriterRIOT_Turtle2()        { super(N3JenaWriter.turtleWriterAlt2) ; } }

    public static class RDFWriterRIOT_JSONLD        extends RDFWriterRIOT   { public RDFWriterRIOT_JSONLD()         { super("JSON-LD") ; } }
    public static class RDFWriterRIOT_JSONLDAlt     extends RDFWriterRIOT   { public RDFWriterRIOT_JSONLDAlt()      { super("JSONLD") ; } }
    
    public static class RDFWriterRIOT_RDFJSON       extends RDFWriterRIOT   { public RDFWriterRIOT_RDFJSON()        { super("RDF/JSON") ; } }
}

