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

    // To avoid an initialization circularity, these are set by RDFLaguages.
    // Using the RDFLanguages constants is preferred style.

    public static Lang RDFXML ; 
    public static Lang NTRIPLES ; 
    public static Lang N3 ; 
    public static Lang TURTLE ; 
    public static Lang RDFJSON ; 
    public static Lang NQUADS ; 
    public static Lang TRIG ; 

    private final String label ;                    // Primary name
    private final ContentType contentType ;         // Primary content type.
    private final List<String> altLabels ;
    private final List<String> altContentTypes ;
    private final List<String> fileExtensions ;

    // NOT public.  This is to force use of the RDFLanguages which makes languges symbols and so == works
    protected Lang(String langlabel, String mainContentType, List<String> altLangLabels, List<String> otherContentTypes, List<String> fileExt)
    {
        if ( langlabel == null )
            throw new IllegalArgumentException("Null not allowed for language name") ;
        else
            langlabel = langlabel.intern();
        label = langlabel ;
        
        String mediaType = mainContentType ;

        contentType = mediaType==null ? null : ContentType.parse(mediaType) ;
        
        List<String> _altContentTypes = copy(otherContentTypes) ;
        if ( ! _altContentTypes.contains(mainContentType) )
            _altContentTypes.add(mainContentType) ;
        altContentTypes = Collections.unmodifiableList(_altContentTypes) ;
        
        List<String> _altLabels = copy(altLangLabels) ;
        if ( ! _altLabels.contains(label) )
            _altLabels.add(label) ;
        altLabels = Collections.unmodifiableList(_altLabels) ;
        
        List<String> _fileExtensions = copy(fileExt) ;
        fileExtensions = Collections.unmodifiableList(_fileExtensions) ;
    }
    
    static <T> List<T> copy(List<T> original)
    {
        List<T> x = new ArrayList<T>() ;
        x.addAll(original) ;
        return x ;
    }
    
    @Override
    public int hashCode() { return label.hashCode() ; } 

    @Override
    public boolean equals(Object other)
    {
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
    public String getLabel()                    { return label ; }
    public List<String> getAltNames()           { return altLabels ; }
    public List<String> getAltContentTypes()    { return altContentTypes ; }
    public List<String> getFileExtensions()     { return fileExtensions ; }

    @Override
    public String toString()  { return "Lang:"+label ; }
    
    public String toLongString()
    { 
        String x = "Lang:" + label + " " + getContentType() ;
        if (getAltNames().size() > 0)
            x = x + " " + getAltNames() ;
        if (getAltContentTypes().size() > 0)
            x = x + " " + getAltContentTypes() ;
        if (getFileExtensions().size() > 0)
            x = x + " " + getFileExtensions() ;

        return x ;
    }
    
    // ----
    /** Translate a name into a Lang
     * Throws RiotException if the name is not recognized.
     * @deprecated Use {@link RDFLanguages#nameToLang(String)}
     */
    @Deprecated 
    public static Lang get(String name)
    {
        Lang lang =  RDFLanguages.nameToLang(name) ;
        if ( lang == null )
            throw new RiotException("No such language: "+name) ;
        return lang ;
    }
    
    /** Translate a name into a Lang, return the default if no match found.
     * @deprecated Use {@link RDFLanguages#nameToLang(String)}
     */
    @Deprecated 
    public static Lang get(String name, Lang dftLang)
    {
        Lang lang =  RDFLanguages.nameToLang(name) ;
        if ( lang == null )
            return dftLang ;
        return lang ;
    }

    /** Guess the language, based on filename, or URL, extenstion.
     * Returns default if there isn't a guess available
     * @deprecated Use {@link RDFLanguages#filenameToLang(String,Lang)}
     */
    @Deprecated 
    public static Lang guess(String resourceIRI, Lang dftLang)
    {
        return RDFLanguages.filenameToLang(resourceIRI, dftLang) ;
    }
    
    /** Guess the language, based on filename, or URL, extenstion.
     * Returns null if there isn't a guess available
     * @deprecated Use {@link RDFLanguages#filenameToLang(String)}
     */
    @Deprecated
    public static Lang guess(String resourceIRI)
    {
        return RDFLanguages.filenameToLang(resourceIRI) ;
    }
}

