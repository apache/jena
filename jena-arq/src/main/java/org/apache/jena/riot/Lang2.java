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

import org.openjena.atlas.web.ContentType ;

/** A "language" (syntax) */
public class Lang2 
{
    private final String label ;
    private final ContentType contentType ;     // Primary content type.
    private final List<String> altLabels ;
    private final List<String> altContentTypes ;
    private final List<String> fileExtensions ;

    protected Lang2(String langlabel, String mainContentType, List<String> altLangLabels, List<String> otherContentTypes, List<String> fileExt)
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
    
//    public boolean isTriples()  { return RDFLanguages.isTriples(this) ; }
//    public boolean isQuads()    { return RDFLanguages.isQuads(this) ; }
    
    @Override
    public int hashCode() { return label.hashCode() ; } 

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( other == null ) return false ;
        if ( ! ( other instanceof Lang2 ) )
            return false ;

        Lang2 otherLang = (Lang2)other ;
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
            x = " " + getAltNames() ;
        if (getAltContentTypes().size() > 0)
            x = " " + getAltContentTypes() ;
        if (getFileExtensions().size() > 0)
            x = " " + getFileExtensions() ;

        return x ;
    }
}

