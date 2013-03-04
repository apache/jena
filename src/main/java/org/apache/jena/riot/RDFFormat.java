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

import org.apache.jena.atlas.lib.Lib ;

/** Constants for writable formats */  
public class RDFFormat
{
    /** Pretty printing variant */
    public static RDFFormatVariant PRETTY       = new RDFFormatVariant("pretty" ) ;
    /** Print in blocks, typically all triples with teh same subject in an incoming triple/quad stream */
    public static RDFFormatVariant BLOCKS       = new RDFFormatVariant("blocks" ) ;
    /** Print out one per line */
    public static RDFFormatVariant FLAT         = new RDFFormatVariant("flat" ) ;
    
    public static RDFFormat TURTLE_PRETTY       = new RDFFormat(Lang.TURTLE, PRETTY) ;
    public static RDFFormat TURTLE              = TURTLE_PRETTY ;
    public static RDFFormat TTL                 = TURTLE_PRETTY ;
    public static RDFFormat TURTLE_BLOCKS       = new RDFFormat(Lang.TURTLE, BLOCKS) ;
    public static RDFFormat TURTLE_FLAT         = new RDFFormat(Lang.TURTLE, FLAT) ;
    
    public static RDFFormat NTRIPLES            = new RDFFormat(Lang.NTRIPLES) ;
    public static RDFFormat NT                  = NTRIPLES ;
    
    public static RDFFormat NQUADS              = new RDFFormat(Lang.NQUADS) ;
    public static RDFFormat NQ                  = NQUADS ;
    
    public static RDFFormat TRIG_PRETTY         = new RDFFormat(Lang.TRIG, PRETTY) ;
    public static RDFFormat TRIG                = TRIG_PRETTY ;
    public static RDFFormat TRIG_BLOCKS         = new RDFFormat(Lang.TRIG, BLOCKS) ;
    public static RDFFormat TRIG_FLAT           = new RDFFormat(Lang.TRIG, FLAT) ;
    
    /** RDF/XML ABBREV variant*/
    public static RDFFormatVariant ABBREV       = new RDFFormatVariant("pretty" ) ;
    /** Basic RDF/XML variant */
    public static RDFFormatVariant PLAIN        = new RDFFormatVariant("plain" ) ;
    
    public static RDFFormat RDFXML_PRETTY       = new RDFFormat(Lang.RDFXML, ABBREV) ;
    public static RDFFormat RDFXML_ABBREV       = RDFXML_PRETTY ;
    public static RDFFormat RDFXML              = RDFXML_PRETTY ;
    public static RDFFormat RDFXML_PLAIN        = new RDFFormat(Lang.RDFXML, PLAIN) ;
    
    public static RDFFormat RDFJSON             = new RDFFormat(Lang.RDFJSON) ;
    
    /** The "null" output format (a sink that prints nothing, usually quite efficiently) */
    public static RDFFormat RDFNULL             = new RDFFormat(Lang.RDFNULL) ;

    private final Lang lang ;
    private final RDFFormatVariant variant ;

    public RDFFormat(Lang lang) { this(lang, null) ; }
    
    public RDFFormat(Lang lang, RDFFormatVariant variant)
    {
        this.lang = lang ;
        this.variant = variant ;
    }

    public Lang getLang()
    {
        return lang ;
    }

    public RDFFormatVariant getVariant()
    {
        return variant ;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((lang == null) ? 0 : lang.hashCode()) ;
        result = prime * result + ((variant == null) ? 0 : variant.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        RDFFormat other = (RDFFormat)obj ;
        if ( ! Lib.equal(lang, other.lang) )
            return false ;
        if ( ! Lib.equal(variant, other.variant) )
            return false ;
        return true ;
    }
    
    @Override
    public String toString()
    {
        if ( variant == null )
            return lang.getName() ;
        return lang.getName()+"/"+variant ;
    }
}

