/*
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

package org.apache.jena.riot.lang;

import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.system.*;
import org.apache.jena.sparql.util.Context ;

public class ReaderRDFXML implements ReaderRIOT {
    private /*final*/ ErrorHandler errorHandler;
    private /*final*/ MakerRDF maker;
    
    public ReaderRDFXML(MakerRDF maker, ErrorHandler errorHandler) {
        this.maker = maker;
        this.errorHandler = errorHandler;
    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        baseURI = baseURI_RDFXML(baseURI) ;
        LangRDFXML parser = LangRDFXML.create(in, baseURI, baseURI, ErrorHandlerFactory.getDefaultErrorHandler(), output) ;
        parser.parse();
    }
        
    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        baseURI = baseURI_RDFXML(baseURI) ;
        LangRDFXML parser = LangRDFXML.create(reader, baseURI, baseURI, ErrorHandlerFactory.getDefaultErrorHandler(), output) ;
        parser.parse();
    }
    
    @Override
    public ParserProfile getParserProfile() {
        if ( maker instanceof ParserProfile )
            return (ParserProfile)maker;
        throw new UnsupportedOperationException() ;
    }

    @Override
    public void setParserProfile(ParserProfile profile) {
        maker = profile ;
    }
    
    /** Sort out the base URI for RDF/XML parsing. */
    private static String baseURI_RDFXML(String baseIRI) {
        // LangRIOT derived from LangEngine do this in ParserProfile 
        if ( baseIRI == null )
            return SysRIOT.chooseBaseIRI() ;
        else
            // This normalizes the URI.
            return SysRIOT.chooseBaseIRI(baseIRI) ;
    }
}

