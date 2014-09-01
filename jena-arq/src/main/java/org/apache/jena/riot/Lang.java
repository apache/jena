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

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

import org.apache.jena.atlas.web.ContentType ;

/** A "language" (syntax).
 *  A language has a name, some alternative names,
 *  a content type,  some alternative content types,
 *  and a list of associated file extensions.
 *  Names, content types and file extensions must be unique to one language. 
 *  To create a Lang constant, use {@link LangBuilder} and 
 *  register with {@link RDFLanguages}.
 */
public class Lang 
{
    //  public static final Lang RDFXML = RDFLanguages.RDFXML ; 
    //  public static final Lang NTRIPLES = RDFLanguages.NTriples ; 
    //  public static final Lang N3 = RDFLanguages.N3 ; 
    //  public static final Lang TURTLE = RDFLanguages.Turtle ; 
    //  public static final Lang RDFJSON = RDFLanguages.RDFJSON ; 
    //  public static final Lang NQUADS = RDFLanguages.NQuads ; 
    //  public static final Lang TRIG = RDFLanguages.TriG ; 

    // To avoid an initialization circularity, these are set by RDFLanguages.
    static { RDFLanguages.init() ; }

    /** <a href="http://www.w3.org/TR/REC-rdf-syntax/">RDF/XML</a> */
    public static Lang RDFXML ;
    
    /** <a href="http://www.w3.org/TR/turtle/">Turtle</a>*/
    public static Lang TURTLE ;
    
    /** Alternative constant for {@linkplain #TURTLE} */
    public static Lang TTL ;
    
    /** N3 (treat as Turtle) */
    public static Lang N3 ;
    
    /** <a href="http://www.w3.org/TR/n-triples/">N-Triples</a>*/
    public static Lang NTRIPLES ;
    
    /** Alternative constant for {@linkplain #NTRIPLES} */
    public static Lang NT ;

    /** <a href="http://www.w3.org/TR/json-ld/">JSON-LD</a>. */
    public static Lang JSONLD ;
    
    /** <a href="http://www.w3.org/TR/rdf-json/">RDF/JSON</a>.  This is not <a href="http://www.w3.org/TR/json-ld/">JSON-LD</a>. */
    public static Lang RDFJSON ;
    
    /** <a href="http://www.w3.org/TR/trig/">TriG</a> */
    public static Lang TRIG ;
    
    /** <a href="http://www.w3.org/TR/n-quads">N-Quads</a> */
    public static Lang NQUADS ;
    
    /** Alternative constant {@linkplain #NQUADS} */
    public static Lang NQ ;

    //** The RDF syntax "RDF Thrift" : see http://jena.apache.org/documentation/io */ 
    public static Lang RDFTHRIFT ;

    /** "CSV" - CSV data read into an RDF model with simple conversion : See Jena-625 */
    public static Lang CSV ;

    /** The "null" language */
    public static Lang RDFNULL ;

    private final String label ;                    // Primary name
    private final ContentType contentType ;         // Primary content type.
    private final List<String> altLabels ;
    private final List<String> altContentTypes ;
    private final List<String> fileExtensions ;

    // NOT public. This is to force use of the RDFLanguages which makes
    // languages symbols and so == works
    protected Lang(String langlabel, String mainContentType, List<String> altLangLabels,
                   List<String> otherContentTypes, List<String> fileExt) {
        if ( langlabel == null )
            throw new IllegalArgumentException("Null not allowed for language name") ;
        else
            langlabel = langlabel.intern() ;
        label = langlabel ;

        String mediaType = mainContentType ;

        contentType = mediaType == null ? null : ContentType.create(mediaType) ;

        List<String> _altContentTypes = copy(otherContentTypes) ;
        if ( !_altContentTypes.contains(mainContentType) )
            _altContentTypes.add(mainContentType) ;
        altContentTypes = Collections.unmodifiableList(_altContentTypes) ;

        List<String> _altLabels = copy(altLangLabels) ;
        if ( !_altLabels.contains(label) )
            _altLabels.add(label) ;
        altLabels = Collections.unmodifiableList(_altLabels) ;

        List<String> _fileExtensions = copy(fileExt) ;
        fileExtensions = Collections.unmodifiableList(_fileExtensions) ;
    }
    
    static <T> List<T> copy(List<T> original) {
        List<T> x = new ArrayList<>() ;
        x.addAll(original) ;
        return x ;
    }
    
    @Override
    public int hashCode() { return label.hashCode() ; } 

    @Override
    public boolean equals(Object other) {
        if ( this == other ) return true ;
        if ( other == null ) return false ;
        if ( ! ( other instanceof Lang ) )
            return false ;

        Lang otherLang = (Lang)other ;
        // Just label should be enough.
        return 
            this.label == otherLang.label &&
            this.contentType.equals(otherLang.contentType) &&
            this.altContentTypes.equals(otherLang.altContentTypes) &&
            this.fileExtensions.equals(otherLang.fileExtensions) ;
        // File extensions and alt 
    }

    public String getName()                     { return label ; }
    public ContentType getContentType()         { return contentType ; }
    
    /** As an HTTP Content-Type field value */ 
    public String getHeaderString()             { return contentType.toHeaderString() ; }
    public String getLabel()                    { return label ; }
    public List<String> getAltNames()           { return altLabels ; }
    public List<String> getAltContentTypes()    { return altContentTypes ; }
    public List<String> getFileExtensions()     { return fileExtensions ; }

    @Override
    public String toString()  { return "Lang:"+label ; }
    
    public String toLongString() { 
        String x = "Lang:" + label + " " + getContentType() ;
        if (getAltNames().size() > 0)
            x = x + " " + getAltNames() ;
        if (getAltContentTypes().size() > 0)
            x = x + " " + getAltContentTypes() ;
        if (getFileExtensions().size() > 0)
            x = x + " " + getFileExtensions() ;

        return x ;
    }
}

