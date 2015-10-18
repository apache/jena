/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.riot.lang;

import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.sparql.util.Context ;

public class ReaderRDFNULL implements ReaderRIOT {
    public ReaderRDFNULL() {}
    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {}

    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {}

    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {}

    @Override
    public ParserProfile getParserProfile() {
        return null;
    }

    @Override
    public void setParserProfile(ParserProfile profile) {}
}