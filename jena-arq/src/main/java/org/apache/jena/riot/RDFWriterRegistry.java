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
import org.apache.jena.riot.protobuf.WriterDatasetProtobuf;
import org.apache.jena.riot.protobuf.WriterGraphProtobuf;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.thrift.WriterDatasetThrift ;
import org.apache.jena.riot.thrift.WriterGraphThrift ;
import org.apache.jena.riot.writer.* ;
import org.apache.jena.sys.JenaSystem ;

/**
 * Writer registry. This is for writers presenting the functionality to write graphs and datasets, not streams.
 * See {@link StreamRDFWriter} for registration of streaming writers.
 *
 * To register the language: see {@link RDFLanguages}.
 *
 * @see StreamRDFWriter for streaming writers.
 */
public class RDFWriterRegistry
{
    // All mention and use of java.io.Writer is purely for compatibility.
    // They are not a good idea (StringWriter is an exception).
    // Let the serializer deal with the character issues.
    // UTF-8 is universal - but UTF-8 is not the default in Java ("platform encoding" is).

    // System defaults for JSON-LD writing in init$().
    // Also - settings in RDFFormat.

    private static Map<RDFFormat, WriterGraphRIOTFactory> registryGraph     = new HashMap<>() ;
    private static Map<RDFFormat, WriterDatasetRIOTFactory> registryDataset = new HashMap<>() ;
    private static Map<Lang, RDFFormat> langToFormat                        = new HashMap<>() ;

    static { JenaSystem.init() ; }

    // WriterDatasetRIOTFactory as graph writer.
    private static WriterDatasetRIOTFactory wdsfactoryAsGraph = createWriterDatasetFactory();
    private static WriterDatasetRIOTFactory wdsfactoryForGraph() { return wdsfactoryAsGraph; }

    // Graph writers : if none, use a dataset writer
    private static WriterGraphRIOTFactory createWriterGraphFactory() {
        return (RDFFormat serialization) -> {
            // Built-ins
            if ( Objects.equals(RDFFormat.TURTLE_PRETTY, serialization) )
                return new TurtleWriter() ;
            if ( Objects.equals(RDFFormat.TURTLE_BLOCKS, serialization) )
                return new TurtleWriterBlocks() ;
            if ( Objects.equals(RDFFormat.TURTLE_FLAT, serialization) )
                return new TurtleWriterFlat() ;
            if ( Objects.equals(RDFFormat.TURTLE_LONG, serialization) )
                return new TurtleWriterLong() ;

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

            WriterDatasetRIOT dsw = wdsfactoryForGraph().create(serialization) ;
            if ( dsw != null )
                return RiotLib.adapter(dsw) ;
            return null;
        };
    }

    // Dataset writers.
    private static WriterDatasetRIOTFactory createWriterDatasetFactory() {
        return (RDFFormat serialization) -> {
            if ( Objects.equals(RDFFormat.TRIG_PRETTY, serialization) )
                return new TriGWriter() ;
            if ( Objects.equals(RDFFormat.TRIG_BLOCKS, serialization) )
                return new TriGWriterBlocks() ;
            if ( Objects.equals(RDFFormat.TRIG_FLAT, serialization) )
                return new TriGWriterFlat() ;
            if ( Objects.equals(RDFFormat.TRIG_LONG, serialization) )
                return new TriGWriterLong() ;
            if ( Objects.equals(RDFFormat.NQUADS_UTF8, serialization) )
                return new NQuadsWriter() ;
            if ( Objects.equals(RDFFormat.NQUADS_ASCII, serialization) )
                return new NQuadsWriter(CharSpace.ASCII) ;
            if ( Objects.equals(RDFFormat.RDFNULL, serialization) )
                return NullWriter.factory.create(RDFFormat.RDFNULL) ;
            return null ;
        } ;
    }

    public static void init() {}
    static { init$() ; }
    private static void init$()
    {
        WriterGraphRIOTFactory wgfactory = createWriterGraphFactory();
        WriterDatasetRIOTFactory wdsfactory = createWriterDatasetFactory();

        // Safer here than as statics due to class initialization ordering effects.
        WriterDatasetRIOTFactory wdsJsonldFactory11 = syntaxForm -> new JsonLD11Writer(syntaxForm);
        WriterGraphRIOTFactory wgJsonldFactory11    = syntaxForm -> RiotLib.adapter(new JsonLD11Writer(syntaxForm));
        WriterGraphRIOTFactory wgProtoFactory       = syntaxForm -> new WriterGraphProtobuf(syntaxForm);
        WriterDatasetRIOTFactory wdsProtoFactory    = syntaxForm -> new WriterDatasetProtobuf(syntaxForm);
        WriterGraphRIOTFactory wgThriftFactory      = syntaxForm -> new WriterGraphThrift(syntaxForm);
        WriterDatasetRIOTFactory wdsThriftFactory   = syntaxForm -> new WriterDatasetThrift(syntaxForm);
        WriterGraphRIOTFactory wgTriXFactory        = syntaxForm -> new WriterTriX();
        WriterDatasetRIOTFactory wdsTriXFactory     = syntaxForm -> new WriterTriX() ;

        // ==== System defaults for JSON-LD writing.
        // ** Coordinate with RDFFormat definitions of JSONLD RDFFormats:
        //    JSONLD_PRETTY, JSONLD_PLAIN, JSONLD, JSONLD_FLAT

        WriterGraphRIOTFactory jsonldWriterGraphDefault      = wgJsonldFactory11;
        WriterDatasetRIOTFactory jsonldWriterDatasetDefault  = wdsJsonldFactory11;

        // -----------------------

        //  Language to format.
        register(Lang.TURTLE,      RDFFormat.TURTLE) ;
        register(Lang.N3,          RDFFormat.TURTLE) ;
        register(Lang.NTRIPLES,    RDFFormat.NTRIPLES) ;
        register(Lang.RDFXML,      RDFFormat.RDFXML) ;

        register(Lang.JSONLD,      RDFFormat.JSONLD) ;
        register(Lang.JSONLD11,    RDFFormat.JSONLD11) ;
        register(Lang.RDFJSON,     RDFFormat.RDFJSON) ;

        register(Lang.TRIG,        RDFFormat.TRIG) ;
        register(Lang.NQUADS,      RDFFormat.NQUADS) ;
        register(Lang.RDFNULL,     RDFFormat.RDFNULL) ;
        register(Lang.RDFPROTO,    RDFFormat.RDF_PROTO) ;
        register(Lang.RDFTHRIFT,   RDFFormat.RDF_THRIFT) ;

        register(Lang.TRIX,        RDFFormat.TRIX) ;

        // Writer factories - graph.

        register(RDFFormat.TURTLE_PRETTY,  wgfactory) ;
        register(RDFFormat.TURTLE_BLOCKS,  wgfactory) ;
        register(RDFFormat.TURTLE_FLAT,    wgfactory) ;
        register(RDFFormat.TURTLE_LONG,    wgfactory) ;

        register(RDFFormat.NTRIPLES,       wgfactory) ;
        register(RDFFormat.NTRIPLES_ASCII, wgfactory) ;

        // JSON-LD 1.1
        register(RDFFormat.JSONLD11,                    wgJsonldFactory11) ;
        register(RDFFormat.JSONLD11_PRETTY,             wgJsonldFactory11) ;
        register(RDFFormat.JSONLD11_PLAIN,              wgJsonldFactory11) ;
        register(RDFFormat.JSONLD11_FLAT,               wgJsonldFactory11) ;

        register(RDFFormat.JSONLD11,                    wdsJsonldFactory11) ;
        register(RDFFormat.JSONLD11_PRETTY,             wdsJsonldFactory11) ;
        register(RDFFormat.JSONLD11_PLAIN,              wdsJsonldFactory11) ;
        register(RDFFormat.JSONLD11_FLAT,               wdsJsonldFactory11) ;

        // JSON-LD System defaults.
        register(RDFFormat.JSONLD,                      jsonldWriterGraphDefault) ;
        register(RDFFormat.JSONLD_PRETTY,               jsonldWriterGraphDefault) ;
        register(RDFFormat.JSONLD_PLAIN,                jsonldWriterGraphDefault) ;
        register(RDFFormat.JSONLD_FLAT,                 jsonldWriterGraphDefault) ;

        register(RDFFormat.JSONLD,                      jsonldWriterDatasetDefault) ;
        register(RDFFormat.JSONLD_PRETTY,               jsonldWriterDatasetDefault) ;
        register(RDFFormat.JSONLD_PLAIN,                jsonldWriterDatasetDefault) ;
        register(RDFFormat.JSONLD_FLAT,                 jsonldWriterDatasetDefault) ;

        register(RDFFormat.RDFJSON,        wgfactory) ;

        register(RDFFormat.RDFXML_PRETTY,  wgfactory) ;
        register(RDFFormat.RDFXML_PLAIN,   wgfactory) ;

        // Graphs in a quad format.
        register(RDFFormat.TRIG_PRETTY,    wgfactory) ;
        register(RDFFormat.TRIG_BLOCKS,    wgfactory) ;
        register(RDFFormat.TRIG_FLAT,      wgfactory) ;
        register(RDFFormat.TRIG_LONG,      wgfactory) ;

        register(RDFFormat.NQUADS,         wgfactory) ;
        register(RDFFormat.NQUADS_ASCII,   wgfactory) ;
        register(RDFFormat.RDFNULL,        wgfactory) ;

        register(RDFFormat.RDF_PROTO,           wgProtoFactory) ;
        register(RDFFormat.RDF_PROTO_VALUES,    wgProtoFactory) ;
        register(RDFFormat.RDF_THRIFT,          wgThriftFactory) ;
        register(RDFFormat.RDF_THRIFT_VALUES,   wgThriftFactory) ;

        register(RDFFormat.TRIX, wgTriXFactory) ;

        // Writer factories - datasets.

        register(RDFFormat.TRIG_PRETTY,    wdsfactory) ;
        register(RDFFormat.TRIG_BLOCKS,    wdsfactory) ;
        register(RDFFormat.TRIG_FLAT,      wdsfactory) ;

        register(RDFFormat.NQUADS,         wdsfactory) ;
        register(RDFFormat.NQUADS_ASCII,   wdsfactory) ;
        register(RDFFormat.RDFNULL,        wdsfactory) ;

        register(RDFFormat.RDF_PROTO,           wdsProtoFactory) ;
        register(RDFFormat.RDF_PROTO_VALUES,    wdsProtoFactory) ;
        register(RDFFormat.RDF_THRIFT,          wdsThriftFactory) ;
        register(RDFFormat.RDF_THRIFT_VALUES,   wdsThriftFactory) ;

        register(RDFFormat.TRIX, wdsTriXFactory) ;
    }

     // ---- Compatibility

    /** Register the serialization for graphs and it's associated factory
     * @param serialization         RDFFormat for the output format.
     * @param graphWriterFactory    Source of writer engines
     */
    public static void register(RDFFormat serialization, WriterGraphRIOTFactory graphWriterFactory) {
        registryGraph.put(serialization, graphWriterFactory) ;
    }

    /** Register the serialization for datasets and it's associated factory
     * @param serialization         RDFFormat for the output format.
     * @param datasetWriterFactory    Source of writer engines
     */
    public static void register(RDFFormat serialization, WriterDatasetRIOTFactory datasetWriterFactory) {
        registryDataset.put(serialization, datasetWriterFactory);
    }

    /** Register an RDFFormat */
    private static void register(RDFFormat serialization)
    { /*no-op*/ }

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
        return Set.copyOf(registryGraph.keySet()) ;
    }

    /** All registered dataset formats */
    public static Collection<RDFFormat> registeredDatasetFormats() {
        return Set.copyOf(registryDataset.keySet()) ;
    }

    /** All registered formats */
    public static Collection<RDFFormat> registeredFormats() {
        Set<RDFFormat> x = new HashSet<>() ;
        x.addAll(registryGraph.keySet()) ;
        x.addAll(registryDataset.keySet()) ;
        return Set.copyOf(x) ;
    }

    /** All registered languages */
    public static Collection<Lang> registeredLangs() {
        return Set.copyOf(langToFormat.keySet());
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

