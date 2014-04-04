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

import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.sparql.util.Context ;

/** Interface to parsing processes that takes an input stream and emit items.
 *  The "read" operation may be called repeatedly for a single ReaderRIOT, with different
 *  arguments.  The StreamRDF destination would have to cope with concurrent operation
 *  if these read operations overlap. 
 */

public interface ReaderRIOT
{
    /** Read from an InputStream and output RDF on the StreamRDF.
     * @param in        InputStream
     * @param baseURI   Base URI (or null)
     * @param ct        Content-Type if available.  Routing to the right
     *                   parser will have already been done so this only useful to get
     *                   addition Content-Type information or if this ReaderRIOT can
     *                   handle multiple media types.  
     * @param output    Destintation for the parser output.
     * @param context   Environment settings.
     */
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) ;

    /** Read from an InputStream and output RDF on the StreamRDF.
     * @param reader    Reader. InputStreams are preferred because Reader do not allow RIOT to set the character set. 
     * @param baseURI   Base URI (or null)
     * @param ct        Content-Type if available.  Routing to the right
     *                   parser will have already been done so this only useful to get
     *                   addition Content-Type information or if this ReaderRIOT can
     *                   handle multiple media types.  
     * @param output    Destintation for the parser output.
     * @param context   Environment settings.
     */
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) ;
    
    public ErrorHandler getErrorHandler() ;
    public void setErrorHandler(ErrorHandler errorHandler) ;
    
    /** Get the parser profile.  Not all parser have parser profiles so this may be null */
    public ParserProfile getParserProfile() ;
    /** Set the parser profile.  Not all parser have parser profiles so this may be a no-op */
    public void setParserProfile(ParserProfile profile) ;
}
