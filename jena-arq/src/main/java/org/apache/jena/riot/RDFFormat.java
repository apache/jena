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

import static org.apache.jena.riot.RDFLanguages.THRIFT ;
import org.apache.jena.atlas.lib.Lib ;

/** Constants for writable formats */
public class RDFFormat {
    /** Pretty printing variant */
    public static RDFFormatVariant PRETTY         = new RDFFormatVariant("pretty") ;
    /**
     * Print in blocks, typically all triples with the same subject in an
     * incoming triple/quad stream
     */
    public static RDFFormatVariant BLOCKS         = new RDFFormatVariant("blocks") ;
    /** Print out one per line */
    public static RDFFormatVariant FLAT           = new RDFFormatVariant("flat") ;

    /** Use ASCII output (N-triples, N-Quads) */
    public static RDFFormatVariant ASCII          = new RDFFormatVariant("ascii") ;
    /** Use UTF-8 output (N-triples, N-Quads) */
    public static RDFFormatVariant UTF8           = new RDFFormatVariant("utf-8") ;
    /** Variant for RDF Thrift using values */
    public static final RDFFormatVariant ValueEncoding = new RDFFormatVariant("Value") ;
    
    public static RDFFormat        TURTLE_PRETTY  = new RDFFormat(Lang.TURTLE, PRETTY) ;
    public static RDFFormat        TURTLE         = TURTLE_PRETTY ;
    public static RDFFormat        TTL            = TURTLE_PRETTY ;
    public static RDFFormat        TURTLE_BLOCKS  = new RDFFormat(Lang.TURTLE, BLOCKS) ;
    public static RDFFormat        TURTLE_FLAT    = new RDFFormat(Lang.TURTLE, FLAT) ;

    public static RDFFormat        NTRIPLES_UTF8  = new RDFFormat(Lang.NTRIPLES, UTF8) ;
    public static RDFFormat        NTRIPLES       = NTRIPLES_UTF8 ;
    public static RDFFormat        NT             = NTRIPLES ;
    public static RDFFormat        NTRIPLES_ASCII = new RDFFormat(Lang.NTRIPLES, ASCII) ;

    public static RDFFormat        NQUADS_UTF8    = new RDFFormat(Lang.NQUADS, UTF8) ;
    public static RDFFormat        NQUADS         = NQUADS_UTF8 ;
    public static RDFFormat        NQ             = NQUADS ;
    public static RDFFormat        NQUADS_ASCII   = new RDFFormat(Lang.NQUADS, ASCII) ;

    public static RDFFormat        TRIG_PRETTY    = new RDFFormat(Lang.TRIG, PRETTY) ;
    public static RDFFormat        TRIG           = TRIG_PRETTY ;
    public static RDFFormat        TRIG_BLOCKS    = new RDFFormat(Lang.TRIG, BLOCKS) ;
    public static RDFFormat        TRIG_FLAT      = new RDFFormat(Lang.TRIG, FLAT) ;

    public static RDFFormat        JSONLD_PRETTY  = new RDFFormat(Lang.JSONLD, PRETTY) ;
    public static RDFFormat        JSONLD         = JSONLD_PRETTY ;
    public static RDFFormat        JSONLD_FLAT    = new RDFFormat(Lang.JSONLD, FLAT) ;

    /** RDF/XML ABBREV variant */
    public static RDFFormatVariant ABBREV         = new RDFFormatVariant("pretty") ;
    /** Basic RDF/XML variant */
    public static RDFFormatVariant PLAIN          = new RDFFormatVariant("plain") ;

    public static RDFFormat        RDFXML_PRETTY  = new RDFFormat(Lang.RDFXML, ABBREV) ;
    public static RDFFormat        RDFXML_ABBREV  = RDFXML_PRETTY ;
    public static RDFFormat        RDFXML         = RDFXML_PRETTY ;
    public static RDFFormat        RDFXML_PLAIN   = new RDFFormat(Lang.RDFXML, PLAIN) ;

    public static RDFFormat        RDFJSON        = new RDFFormat(Lang.RDFJSON) ;

    /**
     * RDF Thrift output. This format is faithful representation of RDF writtern
     * and it is suitable for database dumps. It does not encode numeric
     * literals as values (see {@linkplain #RDF_THRIFT_VALUES}).
     *
     * @see #RDF_THRIFT_VALUES
     */
    
    public static final RDFFormat RDF_THRIFT = new RDFFormat(THRIFT) ;
    /**
     * A variant of an an RDFFormat that uses value encoding (e.g. integers,
     * doubles, decimals as binary). This does not preserve exact represenation
     * (+001 is the same value as 1, +1 and 001) which may matter for database
     * dumps. It looses datatype for derived types (xsd;long, xsd:int, xsd:short
     * and xsd:byte become xsd:integer).
     * For large volumes of numeric data, it may provide a significant reduction in size
     * in combination with using prefixes for subjects and predicates.
     *
     * @see #RDF_THRIFT
     */
    public static final RDFFormat RDF_THRIFT_VALUES = new RDFFormat(THRIFT, ValueEncoding) ;

    /**
     * The "null" output format (a sink that prints nothing, usually quite
     * efficiently)
     */
    public static RDFFormat        RDFNULL        = new RDFFormat(Lang.RDFNULL) ;

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
        if ( !Lib.equal(lang, other.lang) )
            return false ;
        if ( !Lib.equal(variant, other.variant) )
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
