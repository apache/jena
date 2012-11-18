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

import org.openjena.atlas.web.ContentType ;

public class Lang2 
{
    public static final Lang2 RDFXML    = Langs.langRDFXML ;
    public static final Lang2 NTRIPLES  = Langs.langNTriples ;
    public static final Lang2 N3        = Langs.langN3 ;
    public static final Lang2 TURTLE    = Langs.langTurtle ;
    public static final Lang2 RDFJSON   = Langs.langRDFJSON ;
   
    public static final Lang2 NQUADS    = Langs.langNQuads ;
    public static final Lang2 TRIG      = Langs.langTriG ;
    
    private final String label ;
    private final ContentType contentType ;

    /** Create a language with a well-known name,
     * All languages with the same name will be treated as the same language.
     */
    static public Lang2 create(String label, String mediaType)
    {
        return new Lang2(label, mediaType) ;
    }
    
    protected Lang2(String label, String mediaType)
    { 
        if ( label == null )
            throw new IllegalArgumentException("Null not allowed for language name") ;
        else
            label = label.intern();
        this.label = label ;
        this.contentType = mediaType==null ? null : ContentType.parse(mediaType) ;
    }
    
    public boolean isTriples()  { return Langs.isTriples(this) ; }
    public boolean isQuads()    { return Langs.isQuads(this) ; }
    
    @Override
    public int hashCode() { return label.hashCode() ; } 

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! ( other instanceof Lang2 ) )
            return false ;

        Lang2 otherLang = (Lang2)other ;
        return this.label == otherLang.label ; // String interning.
    }

    public String getName()             { return label ; }
    public ContentType getContentType() { return contentType ; }
    @Override
    public String toString()  { return "Lang:"+label ; }
}

