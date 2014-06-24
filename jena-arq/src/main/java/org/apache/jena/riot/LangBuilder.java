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
import java.util.List ;



/** Builder for RDF languages (Syntaxes).
 * 
 *  Example usage:
 *  <pre>
 *  LangBuilder.create()
 *             .langName("MyLang")
 *             .contentType("application/wizard")
 *             .addAltContentTypes(...)
 *             .addFileExtensions("ext1", "ext2")
 *             .build()
 *  </pre>
 * 
 * */

public class LangBuilder {
    public String lang ;
    public String officialContentType ; 
    public List<String> altNames = new ArrayList<>() ;
    public List<String> contentTypes = new ArrayList<>() ;
    public List<String> fileExtensions = new ArrayList<>() ;

    /** Create a builder */
    public static LangBuilder create()
    {
        return new LangBuilder() ;
    }
    
    /** Create a builder - convenience operation to 
     * take the language name and content type
     * which should be set if at all possible.
     * @param langname
     * @param officialContentType
     */
    public static LangBuilder create(String langname, String officialContentType)
    {
        return new LangBuilder()
            .langName(langname)
            .contentType(officialContentType) ;
    }
    
    private LangBuilder() {}
    
    /** Add alternative names */
    public LangBuilder addAltNames(String...x)
    { 
        copy(x, altNames) ;
        return this ;
    }
    
    /** Add alternative content types */
    public LangBuilder addAltContentTypes(String...x)
    { 
        copy(x, contentTypes) ;
        return this ;
    }
    
    /** Add file name extensions */
    public LangBuilder addFileExtensions(String...x)
    {
        copy(x, fileExtensions) ;
        return this ;
    }

    private static void copy(String[] src, List<String> dst)
    {
        for ( String str : src )
            if ( !dst.contains(str) )
                dst.add(str) ;
    }
    
    /** Construct the {@link Lang} */
    public Lang build()
    {
        if ( lang == null )
            error("No language name") ;
        if ( officialContentType == null )
            error("No content types") ;
        return new Lang(lang, officialContentType, altNames, contentTypes, fileExtensions) ;
    }

    private void error(String message)
    {
        throw new RiotException(message) ; 
    }

    public String getContentType()
    {
        return officialContentType ;
    }

    /** Set the main content type for this language.
     * If tehre is an officially registers, preferred type, this should be that.  
     */
    public LangBuilder contentType(String officialContentType)
    {
        this.officialContentType = officialContentType ;
        return this ;
    }

    /** Set the language label.  A system wide, unique short name */ 
    public LangBuilder langName(String langname)
    {
        this.lang = langname ;
        return this ;
    }
}
