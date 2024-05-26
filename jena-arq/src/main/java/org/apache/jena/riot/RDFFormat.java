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

package org.apache.jena.riot ;

import java.util.Objects;

/** Constants for writable formats */
public class RDFFormat {
    /** Pretty printing variant */
    public static final RDFFormatVariant PRETTY         = new RDFFormatVariant("pretty") ;
    /** Plain printing variant */
    public static final RDFFormatVariant PLAIN          = new RDFFormatVariant("plain") ;
    /**
     * Print in blocks, typically all triples with the same subject in an
     * incoming triple/quad stream
     */
    public static final RDFFormatVariant BLOCKS         = new RDFFormatVariant("blocks") ;
    /** Print out one per line */
    public static final RDFFormatVariant FLAT           = new RDFFormatVariant("flat") ;
    /** Print with fixed indentation width and linebreaks after each sequence element */
    public static final RDFFormatVariant LONG           = new RDFFormatVariant("long") ;

    /** Use ASCII output (N-triples, N-Quads) */
    public static final RDFFormatVariant ASCII          = new RDFFormatVariant("ascii") ;
    /** Use UTF-8 output (N-triples, N-Quads) */
    public static final RDFFormatVariant UTF8           = new RDFFormatVariant("utf-8") ;
    /** Variant for RDF Thrift using values */
    public static final RDFFormatVariant ValueEncoding  = new RDFFormatVariant("Value") ;

    /** Turtle - pretty form */
    public static final RDFFormat        TURTLE_PRETTY  = new RDFFormat(Lang.TURTLE, PRETTY) ;
    /** Turtle - default form */
    public static final RDFFormat        TURTLE         = TURTLE_PRETTY ;
    /** Turtle - short name */
    public static final RDFFormat        TTL            = TURTLE_PRETTY ;
    /** Turtle - write in blocks of triples, with same subject, no nested object or RDF lists */
    public static final RDFFormat        TURTLE_BLOCKS  = new RDFFormat(Lang.TURTLE, BLOCKS) ;
    /** Turtle - one line per triple  */
    public static final RDFFormat        TURTLE_FLAT    = new RDFFormat(Lang.TURTLE, FLAT) ;
    /** Turtle - with fixed indentation width and linebreaks after each sequence element */
    public static final RDFFormat        TURTLE_LONG    = new RDFFormat(Lang.TURTLE, LONG) ;

    /** N-Triples in UTF-8 */
    public static final RDFFormat        NTRIPLES_UTF8  = new RDFFormat(Lang.NTRIPLES, UTF8) ;
    /** N-Triples - RDF 1.1 form - UTF-8 */
    public static final RDFFormat        NTRIPLES       = NTRIPLES_UTF8 ;
    /** N-Triples - RDF 1.1 form - UTF-8 */
    public static final RDFFormat        NT             = NTRIPLES ;
    /** N-Triples - Use ASCII */
    public static final RDFFormat        NTRIPLES_ASCII = new RDFFormat(Lang.NTRIPLES, ASCII) ;

    /** N-Quads in UTF-8 */
    public static final RDFFormat        NQUADS_UTF8    = new RDFFormat(Lang.NQUADS, UTF8) ;
    /** N-Quads - RDF 1.1 form - UTF-8 */
    public static final RDFFormat        NQUADS         = NQUADS_UTF8 ;
    /** N-Quads - RDF 1.1 form - UTF-8 */
    public static final RDFFormat        NQ             = NQUADS ;
    /** N-Quads - Use ASCII */
    public static final RDFFormat        NQUADS_ASCII   = new RDFFormat(Lang.NQUADS, ASCII) ;

    /** TriG - pretty form */
    public static final RDFFormat        TRIG_PRETTY    = new RDFFormat(Lang.TRIG, PRETTY) ;
    /** TriG - default form */
    public static final RDFFormat        TRIG           = TRIG_PRETTY ;
    /** TriG - write in blocks of triples, with same subject, no nested object or RDF lists */
    public static final RDFFormat        TRIG_BLOCKS    = new RDFFormat(Lang.TRIG, BLOCKS) ;
    /** TriG - one line per triple  */
    public static final RDFFormat        TRIG_FLAT      = new RDFFormat(Lang.TRIG, FLAT) ;
    /** TriG - with fixed indentation width and linebreaks after each sequence element */
    public static final RDFFormat        TRIG_LONG      = new RDFFormat(Lang.TRIG, LONG) ;

    /** SHACL Compact Syntax */
    public static final RDFFormat        SHACLC         = new RDFFormat(Lang.SHACLC);
    //
    // JSONLD related
    //

    // ---- JSONLD 1.1 / Titanium

    /** JSON LD 1.1 - multi-line JSON - prefixes and native types. */
    public static RDFFormat             JSONLD11_PRETTY  = new RDFFormat(Lang.JSONLD11, RDFFormat.PRETTY);
    /** JSON LD 1.1 - multi-line JSON */
    public static RDFFormat             JSONLD11_PLAIN  = new RDFFormat(Lang.JSONLD11, RDFFormat.PLAIN);
    /** JSON LD 1.1 - single-line JSON */
    public static RDFFormat             JSONLD11_FLAT   = new RDFFormat(Lang.JSONLD11, RDFFormat.FLAT);
    /** JSON LD 1.1 default form - multi-line JSON */
    public static RDFFormat             JSONLD11        = JSONLD11_PRETTY;


// ---- JSONLD 1.0 / jsonld-java -- support removed in Jena 5
// Constants left in the source in case someone makes a future contribution.
//
//    public static class JSONLDVariant extends RDFFormatVariant {
//        private static enum JSONLD_FORMAT {
//            COMPACT,
//            FLATTEN,
//            EXPAND,
//            FRAME
//        }
//
//        private JSONLD_FORMAT format ;
//        private boolean prettyJson ;
//
//        JSONLDVariant(String name, boolean prettyJson, JSONLD_FORMAT format) {
//            super(name) ;
//            this.format = format ;
//            this.prettyJson = prettyJson ;
//        }
//
//        public boolean isPretty() { return prettyJson ; }
//
//        private boolean isFormat(JSONLD_FORMAT fmt) {
//            return (fmt == format);
//        }
//
//        public boolean isCompact() { return isFormat(JSONLD_FORMAT.COMPACT); }
//        public boolean isFlatten() { return isFormat(JSONLD_FORMAT.FLATTEN); }
//        public boolean isExpand() { return isFormat(JSONLD_FORMAT.EXPAND); }
//        public boolean isFrame() { return isFormat(JSONLD_FORMAT.FRAME); }
//    }
//
//    // Variants for the JsonLD 1.0 outputs.
//    // because of the pre-existing JSONLD_PRETTY and JSONLD_FLAT,
//    // we're more or less obliged to create all of these
//    // Assumes jsonld-java.
//
//    private static final RDFFormatVariant EXPAND_PRETTY      = new JSONLDVariant("expand pretty", true, JSONLDVariant.JSONLD_FORMAT.EXPAND) ;
//    private static final RDFFormatVariant EXPAND_FLAT        = new JSONLDVariant("expand flat", false, JSONLDVariant.JSONLD_FORMAT.EXPAND) ;
//    private static final RDFFormatVariant COMPACT_PRETTY     = new JSONLDVariant("compact pretty", true, JSONLDVariant.JSONLD_FORMAT.COMPACT) ;
//    private static final RDFFormatVariant COMPACT_FLAT       = new JSONLDVariant("compact flat", false, JSONLDVariant.JSONLD_FORMAT.COMPACT) ;
//    private static final RDFFormatVariant FLATTEN_PRETTY     = new JSONLDVariant("flatten pretty", true, JSONLDVariant.JSONLD_FORMAT.FLATTEN) ;
//    private static final RDFFormatVariant FLATTEN_FLAT       = new JSONLDVariant("flatten flat", false, JSONLDVariant.JSONLD_FORMAT.FLATTEN) ;
//    private static final RDFFormatVariant FRAME_PRETTY       = new JSONLDVariant("frame pretty", true, JSONLDVariant.JSONLD_FORMAT.FRAME) ;
//    private static final RDFFormatVariant FRAME_FLAT         = new JSONLDVariant("frame flat", false, JSONLDVariant.JSONLD_FORMAT.FRAME) ;

    // JSON-LD 1.1
    public static final RDFFormat        JSONLD_PRETTY  = new RDFFormat(Lang.JSONLD, PRETTY);
    public static final RDFFormat        JSONLD_PLAIN   = new RDFFormat(Lang.JSONLD, PLAIN);
    public static final RDFFormat        JSONLD         = JSONLD_PRETTY;
    public static final RDFFormat        JSONLD_FLAT    = new RDFFormat(Lang.JSONLD, FLAT);

    /** RDF/XML ABBREV variant */
    public static final RDFFormatVariant ABBREV         = PRETTY;

    public static final RDFFormat        RDFXML_PRETTY  = new RDFFormat(Lang.RDFXML, PRETTY) ;
    public static final RDFFormat        RDFXML_ABBREV  = RDFXML_PRETTY ;
    public static final RDFFormat        RDFXML         = RDFXML_PRETTY ;
    public static final RDFFormat        RDFXML_PLAIN   = new RDFFormat(Lang.RDFXML, PLAIN) ;

    public static final RDFFormat        RDFJSON        = new RDFFormat(Lang.RDFJSON) ;
    public static final RDFFormat        TRIX           = new RDFFormat(Lang.TRIX) ;

    /**
     * RDF Protobuf output. This format is faithful representation of RDF written
     * and it is suitable for database dumps. It does not encode numeric
     * literals as values (see {@link #RDF_PROTO_VALUES}).
     *
     * @see #RDF_PROTO_VALUES
     */

    public static final RDFFormat RDF_PROTO            = new RDFFormat(Lang.RDFPROTO) ;
    /**
     * A variant of an an RDFFormat that uses value encoding (e.g. integers,
     * doubles, decimals as binary). This does not preserve exact representation
     * (+001 is the same value as 1, +1 and 001) which may matter for database
     * dumps. It looses datatype for derived types (xsd:long, xsd:int, xsd:short
     * and xsd:byte become xsd:integer).
     * For large volumes of numeric data, it may provide a significant reduction in size
     * in combination with using prefixes for subjects and predicates.
     *
     * @see #RDF_PROTO
     */
    public static final RDFFormat RDF_PROTO_VALUES     = new RDFFormat(Lang.RDFPROTO, RDFFormat.ValueEncoding) ;

    /**
     * RDF Thrift output. This format is faithful representation of RDF written
     * and it is suitable for database dumps. It does not encode numeric
     * literals as values (see {@link #RDF_THRIFT_VALUES}).
     *
     * @see #RDF_THRIFT_VALUES
     */

    public static final RDFFormat RDF_THRIFT            = new RDFFormat(Lang.RDFTHRIFT) ;
    /**
     * A variant of an an RDFFormat that uses value encoding (e.g. integers,
     * doubles, decimals as binary). This does not preserve exact representation
     * (+001 is the same value as 1, +1 and 001) which may matter for database
     * dumps. It looses datatype for derived types (xsd:long, xsd:int, xsd:short
     * and xsd:byte become xsd:integer).
     * For large volumes of numeric data, it may provide a significant reduction in size
     * in combination with using prefixes for subjects and predicates.
     *
     * @see #RDF_THRIFT
     */
    public static final RDFFormat RDF_THRIFT_VALUES     = new RDFFormat(Lang.RDFTHRIFT, ValueEncoding) ;

    /**
     * The "null" output format (a sink that prints nothing, usually quite
     * efficiently)
     */
    public static final RDFFormat        RDFNULL        = new RDFFormat(Lang.RDFNULL) ;

    /**
     * Stream-only output format for development - flushes every line.
     */
    public static final RDFFormat        RDFRAW        = new RDFFormat(Lang.RDFRAW) ;


    private final Lang             lang ;
    private final RDFFormatVariant variant ;

    public RDFFormat(Lang lang) {
        this(lang, null) ;
    }

    public RDFFormat(Lang lang, RDFFormatVariant variant) {
        this.lang = lang ;
        this.variant = variant ;
    }

    public Lang getLang() {
        return lang ;
    }

    public RDFFormatVariant getVariant() {
        return variant ;
    }

    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((lang == null) ? 0 : lang.hashCode()) ;
        result = prime * result + ((variant == null) ? 0 : variant.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( getClass() != obj.getClass() )
            return false ;
        RDFFormat other = (RDFFormat)obj ;
        if ( !Objects.equals(lang, other.lang) )
            return false ;
        if ( !Objects.equals(variant, other.variant) )
            return false ;
        return true ;
    }

    @Override
    public String toString() {
        if ( variant == null )
            return lang.getName() ;
        return lang.getName() + "/" + variant ;
    }
}
