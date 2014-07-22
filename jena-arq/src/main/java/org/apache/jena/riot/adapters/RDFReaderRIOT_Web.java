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

package org.apache.jena.riot.adapters;

import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.system.stream.StreamManager ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.NotFoundException ;

/** This is a reader primarily for model.read(url)
 */ 
public class RDFReaderRIOT_Web extends RDFReaderRIOT
{
    private static final String defaultSyntax = "RDF/XML" ;
    
    public RDFReaderRIOT_Web()
    {
        super(defaultSyntax) ;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void read(Model model, Reader r, String base)
    { 
        // model.read(Reader, baseURI)
        startRead(model) ; 
        RDFDataMgr.read(model, r, base, hintlang) ;
        finishRead(model) ;
    }

    
    @Override
    public void read(Model model, InputStream r, String base)
    {
        // model.read(InputStream, baseURI)
        startRead(model) ; 
        RDFDataMgr.read(model, r, base, hintlang) ;
        finishRead(model) ;
    }
    
    @Override
    public void read(Model model, String url)
    {
        // model.read(url)
        TypedInputStream in = StreamManager.get().open(url) ;
        if ( in == null )
            throw new NotFoundException(url) ;
        String contentType = in.getContentType() ;
        
        // Reading a URL, no hint language provided.
        // Use the URL structure as the hint.
        Lang lang = null ;
        if ( ! Lib.equal(contentType, WebContent.contentTypeTextPlain) )
            lang = RDFLanguages.contentTypeToLang(contentType) ; 
        
        if ( lang == null )
            lang = RDFLanguages.filenameToLang(url) ;
        
        if ( lang == null )
            lang = super.hintlang ;
        
        // Here, we want syntax determination to be:
        // ctLang > fileExtLang > RDF/XML
        //
        // whereas RDFDataMgr.read(.. , lang) ;
        // treats lang to override.
                        
        // ** 
        
        startRead(model) ;
        RDFDataMgr.read(model, in, url, lang) ;
        finishRead(model) ;
    }
}

