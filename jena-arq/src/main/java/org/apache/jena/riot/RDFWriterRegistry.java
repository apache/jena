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

import java.util.Collection ;
import java.util.Collections ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.riot.out.CharSpace ;
import org.apache.jena.riot.out.JsonLDWriter ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.thrift.WriterDatasetThrift ;
import org.apache.jena.riot.thrift.WriterGraphThrift ;
import org.apache.jena.riot.writer.* ;

public class RDFWriterRegistry
{
    // All mention and use of Writers is purely for compatibility.
    // They are not a good idea (StringWriter is an exception).
    // Let the serializer deal with the character issues.
    // UTF-8 is universal - but UTF-8 is not the default in Java ("platform encoding" is).
    
    static { RIOT.init() ; }

    private static Map<RDFFormat, WriterGraphRIOTFactory> registryGraph     = new HashMap<>() ;
    private static Map<RDFFormat, WriterDatasetRIOTFactory> registryDataset = new HashMap<>() ;
    private static Map<Lang, RDFFormat> langToFormat                        = new HashMap<>() ;
    
    // Writing a graph
    static WriterGraphRIOTFactory wgfactory = new WriterGraphRIOTFactory() {
        @Override
        public WriterGraphRIOT create(RDFFormat serialization)
        {
            // Built-ins
            
            if ( Lib.equal(RDFFormat.TURTLE_PRETTY, serialization) )
                return new TurtleWriter() ;
            if ( Lib.equal(RDFFormat.TURTLE_BLOCKS, serialization) )
                return new TurtleWriterBlocks() ;
            if ( Lib.equal(RDFFormat.TURTLE_FLAT, serialization) )
                return new TurtleWriterFlat() ;
            
            if ( Lib.equal(RDFFormat.NTRIPLES_UTF8, serialization) )
                return new NTriplesWriter() ;
            if ( Lib.equal(RDFFormat.NTRIPLES_ASCII, serialization) )
                return new NTriplesWriter(CharSpace.ASCII) ;
            
            if ( Lib.equal(RDFFormat.RDFJSON, serialization) )
                return new RDFJSONWriter() ;
            if ( Lib.equal(RDFFormat.RDFXML_PRETTY, serialization) )
                return new RDFXMLAbbrevWriter() ;
            if ( Lib.equal(RDFFormat.RDFXML_PLAIN, serialization) )
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
            if ( Lib.equal(RDFFormat.TRIG_PRETTY, serialization) )
                return new TriGWriter() ;
            if ( Lib.equal(RDFFormat.TRIG_BLOCKS, serialization) )
                return new TriGWriterBlocks() ;
            if ( Lib.equal(RDFFormat.TRIG_FLAT, serialization) )
                return new TriGWriterFlat() ;
            if ( Lib.equal(RDFFormat.NQUADS_UTF8, serialization) )
                return new NQuadsWriter() ;
            if ( Lib.equal(RDFFormat.NQUADS_ASCII, serialization) )
                return new NQuadsWriter(CharSpace.ASCII) ;
            if ( Lib.equal(RDFFormat.RDFNULL, serialization) )
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
         register(Lang.RDFTHRIFT,      RDFFormat.RDF_THRIFT) ;

         // Writer factories.
         register(RDFFormat.TURTLE_PRETTY,  wgfactory) ;
         register(RDFFormat.TURTLE_BLOCKS,  wgfactory) ;
         register(RDFFormat.TURTLE_FLAT,    wgfactory) ;

         register(RDFFormat.NTRIPLES,       wgfactory) ;
         register(RDFFormat.NTRIPLES_ASCII, wgfactory) ;
         
         register(RDFFormat.JSONLD,         wgJsonldfactory) ;
         register(RDFFormat.JSONLD_FLAT,    wgJsonldfactory) ;
         register(RDFFormat.JSONLD_PRETTY,  wgJsonldfactory) ;
         
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

         // Datasets
         register(RDFFormat.TRIG_PRETTY,    wdsfactory) ;
         register(RDFFormat.TRIG_BLOCKS,    wdsfactory) ;
         register(RDFFormat.TRIG_FLAT,      wdsfactory) ;

         register(RDFFormat.NQUADS,         wdsfactory) ;
         register(RDFFormat.NQUADS_ASCII,   wdsfactory) ;
         register(RDFFormat.RDFNULL,        wdsfactory) ;
         
         register(RDFFormat.JSONLD,         wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_FLAT,    wdsJsonldfactory) ;
         register(RDFFormat.JSONLD_PRETTY,  wdsJsonldfactory) ;
         
         register(RDFFormat.RDF_THRIFT,     wdsThriftFactory) ;
         register(RDFFormat.RDF_THRIFT_VALUES, wdsThriftFactory) ;

     }
    
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
    
    /** Register the default serialization for the language (replace any existing registration).
     * @param lang      Languages
     * @param format    The serialization forma to use when the language is used for writing.
     */
    public static void register(Lang lang, RDFFormat format)
    {
        register(format) ;
        langToFormat.put(lang, format) ;
    }
    
    /** Return the format registered as the default for the language */ 
    public static RDFFormat defaultSerialization(Lang lang)
    {
        return langToFormat.get(lang) ;
    }

    /** Does the language have a registered output format? */
    public static boolean contains(Lang lang)
    { 
        if ( ! langToFormat.containsKey(lang) ) return false ;
        
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
    
    /** Get the graph writer factory asscoiated with the language */
    public static WriterGraphRIOTFactory getWriterGraphFactory(Lang lang)
    {
        RDFFormat serialization = defaultSerialization(lang) ;
        if ( serialization == null )
            throw new RiotException("No default serialization for language "+lang) ;
        return getWriterGraphFactory(serialization) ;
    }

    /** Get the graph writer factory asscoiated with the output format */
    public static WriterGraphRIOTFactory getWriterGraphFactory(RDFFormat serialization)
    {
        return registryGraph.get(serialization) ;
    }
    
    /** Get the dataset writer factory asscoiated with the language */
    public static WriterDatasetRIOTFactory getWriterDatasetFactory(Lang lang)
    {
        RDFFormat serialization = defaultSerialization(lang) ;
        if ( serialization == null )
            throw new RiotException("No default serialization for language "+lang) ;
        return getWriterDatasetFactory(serialization) ;  
    }

    /** Get the dataset writer factory asscoiated with the output format */
    public static WriterDatasetRIOTFactory getWriterDatasetFactory(RDFFormat serialization)
    {
        if ( serialization == null )
            return null ; 
        return registryDataset.get(serialization) ;
    }
}

