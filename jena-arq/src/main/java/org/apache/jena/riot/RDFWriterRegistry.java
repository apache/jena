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

import java.util.* ;

import org.apache.jena.atlas.lib.CharSpace ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.thrift.WriterDatasetThrift ;
import org.apache.jena.riot.thrift.WriterGraphThrift ;
import org.apache.jena.riot.writer.* ;
import org.apache.jena.sys.JenaSystem ;

public class RDFWriterRegistry
{
    // All mention and use of Writers is purely for compatibility.
    // They are not a good idea (StringWriter is an exception).
    // Let the serializer deal with the character issues.
    // UTF-8 is universal - but UTF-8 is not the default in Java ("platform encoding" is).
    
    static { JenaSystem.init() ; }
    
    private static Map<RDFFormat, WriterGraphRIOTFactory> registryGraph     = new HashMap<>() ;
    private static Map<RDFFormat, WriterDatasetRIOTFactory> registryDataset = new HashMap<>() ;
    private static Map<Lang, RDFFormat> langToFormat                        = new HashMap<>() ;
    private static Map<String, RDFFormat> mapJenaNameToFormat               = new HashMap<>() ;
    
    // Writing a graph
    static WriterGraphRIOTFactory wgfactory = new WriterGraphRIOTFactory() {
        @Override
        public WriterGraphRIOT create(RDFFormat serialization)
        {
            // Built-ins
            
            if ( Objects.equals(RDFFormat.TURTLE_PRETTY, serialization) )
                return new TurtleWriter() ;
            if ( Objects.equals(RDFFormat.TURTLE_BLOCKS, serialization) )
                return new TurtleWriterBlocks() ;
            if ( Objects.equals(RDFFormat.TURTLE_FLAT, serialization) )
                return new TurtleWriterFlat() ;
            
            if ( Objects.equals(RDFFormat.NTRIPLES_UTF8, serialization) )
                return new NTriplesWriter() ;
            if ( Objects.equals(RDFFormat.NTRIPLES_ASCII, serialization) )
                return new NTriplesWriter(CharSpace.ASCII) ;
            
            if ( Objects.equals(RDFFormat.RDFJSON, serialization) )
                return new RDFJSONWriter() ;
            if ( Objects.equals(RDFFormat.RDFXML_PRETTY, serialization) )
                return new RDFXMLAbbrevWriter() ;
            if ( Objects.equals(RDFFormat.RDFXML_PLAIN, serialization) )
                return new RDFXMLPlainWriter() ;
            
            WriterDatasetRIOT dsw = wdsfactory.create(serialization) ;
            if ( dsw != null )
                return RiotLib.adapter(dsw) ;
            return null ;
    }} ;
    
    // Writing a dataset
    static WriterDatasetRIOTFactory wdsfactory = new WriterDatasetRIOTFactory() {
        @Override
        public WriterDatasetRIOT create(RDFFormat serialization)
        {
            if ( Objects.equals(RDFFormat.TRIG_PRETTY, serialization) )
                return new TriGWriter() ;
            if ( Objects.equals(RDFFormat.TRIG_BLOCKS, serialization) )
                return new TriGWriterBlocks() ;
            if ( Objects.equals(RDFFormat.TRIG_FLAT, serialization) )
                return new TriGWriterFlat() ;
            if ( Objects.equals(RDFFormat.NQUADS_UTF8, serialization) )
                return new NQuadsWriter() ;
            if ( Objects.equals(RDFFormat.NQUADS_ASCII, serialization) )
                return new NQuadsWriter(CharSpace.ASCII) ;
            if ( Objects.equals(RDFFormat.RDFNULL, serialization) )
                return NullWriter.factory.create(RDFFormat.RDFNULL) ;
            return null ;
    }} ;
    
    static WriterDatasetRIOTFactory wdsJsonldfactory = new WriterDatasetRIOTFactory() {
    //private static class WriterDatasetJSONLDFactory implements WriterDatasetRIOTFactory {
        @Override
        public WriterDatasetRIOT create(RDFFormat syntaxForm) {
            return new JsonLDWriter(syntaxForm) ;
        }
    } ;
    
    static WriterGraphRIOTFactory wgJsonldfactory = new WriterGraphRIOTFactory() {
    //private static class WriterGraphJSONLDFactory implements WriterGraphRIOTFactory {
        @Override
        public WriterGraphRIOT create(RDFFormat syntaxForm) {
            return RiotLib.adapter(new JsonLDWriter(syntaxForm)) ;
        }
    } ;
    
    static WriterGraphRIOTFactory wgThriftFactory = new WriterGraphRIOTFactory(){
        @Override
        public WriterGraphRIOT create(RDFFormat syntaxForm) {
            return new WriterGraphThrift(syntaxForm) ;
        }
    } ;

    static WriterDatasetRIOTFactory wdsThriftFactory = new WriterDatasetRIOTFactory(){
        @Override
        public WriterDatasetRIOT create(RDFFormat syntaxForm) {
            return new WriterDatasetThrift(syntaxForm) ;
        }
    } ;
    
    static WriterGraphRIOTFactory wgTriXFactory = new WriterGraphRIOTFactory() {

        @Override
        public WriterGraphRIOT create(RDFFormat syntaxForm) {
            return new WriterTriX() ;
        } 
    } ;
        
    static WriterDatasetRIOTFactory wdsTriXFactory = new WriterDatasetRIOTFactory() {

        @Override
        public WriterDatasetRIOT create(RDFFormat syntaxForm) {
            return new WriterTriX() ;
        } 
    } ;
        
     public static void init() {}
     static { init$() ; }
     private static void init$()
     {
         // Language to format.
         register(Lang.TURTLE,      RDFFormat.TURTLE) ;
         register(Lang.N3,          RDFFormat.TURTLE) ;
         register(Lang.NTRIPLES,    RDFFormat.NTRIPLES) ;
         register(Lang.RDFXML,      RDFFormat.RDFXML) ;
         
         register(Lang.JSONLD,      RDFFormat.JSONLD) ;
         register(Lang.RDFJSON,     RDFFormat.RDFJSON) ;

         register(Lang.TRIG,        RDFFormat.TRIG) ;
         register(Lang.NQUADS,      RDFFormat.NQUADS) ;
         register(Lang.RDFNULL,     RDFFormat.RDFNULL) ;
         register(Lang.RDFTHRIFT,   RDFFormat.RDF_THRIFT) ;
         register(Lang.TRIX,        RDFFormat.TRIX) ;

         // Writer factories.
         register(RDFFormat.TURTLE_PRETTY,  wgfactory) ;
         register(RDFFormat.TURTLE_BLOCKS,  wgfactory) ;
         register(RDFFormat.TURTLE_FLAT,    wgfactory) ;

         register(RDFFormat.NTRIPLES,       wgfactory) ;
         register(RDFFormat.NTRIPLES_ASCII, wgfactory) ;
         
         register(RDFFormat.JSONLD,                      wgJsonldfactory) ;
         register(RDFFormat.JSONLD_FLAT,                 wgJsonldfactory) ;
         register(RDFFormat.JSONLD_PRETTY,               wgJsonldfactory) ;
         register(RDFFormat.JSONLD_COMPACT_PRETTY,       wgJsonldfactory) ;
         register(RDFFormat.JSONLD_FLATTEN_PRETTY,       wgJsonldfactory) ;
         register(RDFFormat.JSONLD_EXPAND_PRETTY,        wgJsonldfactory) ;
         register(RDFFormat.JSONLD_FRAME_PRETTY,         wgJsonldfactory) ;
         register(RDFFormat.JSONLD_COMPACT_FLAT,         wgJsonldfactory) ;
         register(RDFFormat.JSONLD_FLATTEN_FLAT,         wgJsonldfactory) ;
         register(RDFFormat.JSONLD_EXPAND_FLAT,          wgJsonldfactory) ;
         register(RDFFormat.JSONLD_FRAME_FLAT,           wgJsonldfactory) ;
        
         register(RDFFormat.RDFJSON,        wgfactory) ;

         register(RDFFormat.RDFXML_PRETTY,  wgfactory) ;
         register(RDFFormat.RDFXML_PLAIN,   wgfactory) ;

         // Graphs in a quad format.
         register(RDFFormat.TRIG_PRETTY,    wgfactory) ;
         register(RDFFormat.TRIG_BLOCKS,    wgfactory) ;
         register(RDFFormat.TRIG_FLAT,      wgfactory) ;

         register(RDFFormat.NQUADS,         wgfactory) ;
         register(RDFFormat.NQUADS_ASCII,   wgfactory) ;
         register(RDFFormat.RDFNULL,        wgfactory) ;
         
         register(RDFFormat.RDF_THRIFT,     wgThriftFactory) ;
         register(RDFFormat.RDF_THRIFT_VALUES, wgThriftFactory) ;

         register(RDFFormat.TRIX, wgTriXFactory) ;

         // Datasets
         register(RDFFormat.TRIG_PRETTY,    wdsfactory) ;
         register(RDFFormat.TRIG_BLOCKS,    wdsfactory) ;
         register(RDFFormat.TRIG_FLAT,      wdsfactory) ;

         register(RDFFormat.NQUADS,         wdsfactory) ;
         register(RDFFormat.NQUADS_ASCII,   wdsfactory) ;
         register(RDFFormat.RDFNULL,        wdsfactory) ;
         
         register(RDFFormat.JSONLD,                      wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_FLAT,                 wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_PRETTY,               wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_COMPACT_PRETTY,       wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_FLATTEN_PRETTY,       wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_EXPAND_PRETTY,        wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_FRAME_PRETTY,         wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_COMPACT_FLAT,         wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_FLATTEN_FLAT,         wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_EXPAND_FLAT,          wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_FRAME_FLAT,           wdsJsonldfactory) ;

         register(RDFFormat.RDF_THRIFT,     wdsThriftFactory) ;
         register(RDFFormat.RDF_THRIFT_VALUES, wdsThriftFactory) ;
         
         register(RDFFormat.TRIX, wdsTriXFactory) ;
     }
    
     // ---- Compatibility
     
     /** return the RDFFormat for the existing Jena writer name, or null */
     public static RDFFormat getFormatForJenaWriter(String jenaName) {
         return mapJenaNameToFormat.get(jenaName);
     }

     /** Register an RDFFormat for a Jena writer name */
     private /*public*/ static void setFormatForJenaWriter(String jenaName, RDFFormat format) {
         mapJenaNameToFormat.put(jenaName, format);
     }

     /** Return a collection of Jena writer names */
     public static Collection<String> getJenaWriterNames() {
         return mapJenaNameToFormat.keySet();
     }

     // Unused; not called from init$()
     // These settings are used by RDFWriterRIOT to override RDFWriterRegistrations.
     
     // The only difference is "RDF/XML" being plain here but pretty
     // as registered.
     // 2017-03:
     // The number of user questions on format of RDF/XML not being pretty
     // has dropped off since Jena2.  
     
//     private static void setupJenaNames() {
//         setFormatForJenaWriter("RDF/XML",                           RDFFormat.RDFXML_PLAIN) ;
//         setFormatForJenaWriter("RDF/XML-ABBREV",                    RDFFormat.RDFXML_ABBREV) ;
//         setFormatForJenaWriter("N-TRIPLE",                          RDFFormat.NTRIPLES) ;
//         setFormatForJenaWriter("NT",                                RDFFormat.NTRIPLES) ;
//         setFormatForJenaWriter("N-TRIPLES",                         RDFFormat.NTRIPLES) ;
//         setFormatForJenaWriter("N-Triples",                         RDFFormat.NTRIPLES) ;
//         setFormatForJenaWriter("N3",                                RDFFormat.TURTLE) ;
//         setFormatForJenaWriter(N3JenaWriter.n3WriterPrettyPrinter,  RDFFormat.TURTLE_PRETTY) ;
//         setFormatForJenaWriter(N3JenaWriter.n3WriterPlain,          RDFFormat.TURTLE_BLOCKS) ;
//         setFormatForJenaWriter(N3JenaWriter.n3WriterTriples,        RDFFormat.TURTLE_FLAT) ;
//         setFormatForJenaWriter(N3JenaWriter.n3WriterTriplesAlt,     RDFFormat.TURTLE_FLAT) ;
//         setFormatForJenaWriter(N3JenaWriter.turtleWriter,           RDFFormat.TURTLE) ;
//         setFormatForJenaWriter(N3JenaWriter.turtleWriterAlt1,       RDFFormat.TURTLE) ;
//         setFormatForJenaWriter(N3JenaWriter.turtleWriterAlt2,       RDFFormat.TURTLE) ;
//     }

     
    /** Register the serialization for graphs and it's associated factory
     * @param serialization         RDFFormat for the output format.
     * @param graphWriterFactory    Source of writer engines
     */
    public static void register(RDFFormat serialization, WriterGraphRIOTFactory graphWriterFactory)
    {
        registryGraph.put(serialization, graphWriterFactory) ;
    }

    /** Register the serialization for datasets and it's associated factory
     * @param serialization         RDFFormat for the output format.
     * @param datasetWriterFactory    Source of writer engines
     */
    public static void register(RDFFormat serialization, WriterDatasetRIOTFactory datasetWriterFactory)
    {
        registryDataset.put(serialization, datasetWriterFactory) ;
    }

    /** Register an RDFFormat */
    private static void register(RDFFormat serialization)
    { }
    
    /**
     * Register the default serialization for the language (replace any existing
     * registration).
     * 
     * @param lang
     *            Languages
     * @param format
     *            The serialization format to use when the language is used for
     *            writing.
     */
    public static void register(Lang lang, RDFFormat format) {
        register(format) ;
        langToFormat.put(lang, format) ;
    }

    /** Return the format registered as the default for the language */
    public static RDFFormat defaultSerialization(Lang lang) {
        return langToFormat.get(lang) ;
    }

    /** Does the language have a registered output format? */
    public static boolean contains(Lang lang) {
        if ( !langToFormat.containsKey(lang) )
            return false ;

        RDFFormat fmt = langToFormat.get(lang) ;
        return contains(fmt) ;
    }

    /** Is the RDFFormat registered for use? */ 
    public static boolean contains(RDFFormat format)
    { return langToFormat.containsKey(format.getLang()) && (registryGraph.containsKey(format) || registryDataset.containsKey(format)); }


    /** All registered graph formats */ 
    public static Collection<RDFFormat> registeredGraphFormats() {
        return Collections.unmodifiableSet(registryGraph.keySet()) ;
    }

    /** All registered dataset formats */ 
    public static Collection<RDFFormat> registeredDatasetFormats() {
        return Collections.unmodifiableSet(registryDataset.keySet()) ;
    }
    
    /** All registered formats */
    public static Collection<RDFFormat> registered() {
        Set<RDFFormat> x = new HashSet<>() ;
        x.addAll(registryGraph.keySet()) ;
        x.addAll(registryDataset.keySet()) ;
        return Collections.unmodifiableSet(x) ;
    }
    
    
    /** Get the graph writer factory associated with the language */
    public static WriterGraphRIOTFactory getWriterGraphFactory(Lang lang) {
        RDFFormat serialization = defaultSerialization(lang) ;
        if ( serialization == null )
            throw new RiotException("No default serialization for language " + lang) ;
        return getWriterGraphFactory(serialization) ;
    }

    /** Get the graph writer factory associated with the output format */
    public static WriterGraphRIOTFactory getWriterGraphFactory(RDFFormat serialization) {
        return registryGraph.get(serialization) ;
    }

    /** Get the dataset writer factory associated with the language */
    public static WriterDatasetRIOTFactory getWriterDatasetFactory(Lang lang) {
        RDFFormat serialization = defaultSerialization(lang) ;
        if ( serialization == null )
            throw new RiotException("No default serialization for language " + lang) ;
        return getWriterDatasetFactory(serialization) ;
    }

    /** Get the dataset writer factory associated with the output format */
    public static WriterDatasetRIOTFactory getWriterDatasetFactory(RDFFormat serialization) {
        if ( serialization == null )
            return null ;
        return registryDataset.get(serialization) ;
    }
}

