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

package org.apache.jena.riot.system.stream ;

import java.io.IOException ;
import java.io.InputStream ;
import java.net.MalformedURLException ;
import java.net.URL ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class LocatorFTP extends LocatorURL {
    private static Logger         log         = LoggerFactory.getLogger(LocatorFTP.class) ;
    private static final String[] schemeNames = { "ftp" } ;

    public LocatorFTP() {
        super(schemeNames) ;
    }

    @Override
    protected Logger log() { return log ; }

    @Override
    public TypedInputStream performOpen(String uri) {
        if ( uri.startsWith("ftp://") ) {
            try {
                URL url = new URL(uri) ;
                InputStream in = url.openStream() ;
                ContentType ct = RDFLanguages.guessContentType(uri) ;
                return new TypedInputStream(in, ct) ;
            } 
            catch (MalformedURLException ex) {
                throw new RiotException("Bad FTP URL: "+uri, ex) ;
            }
            catch (IOException ex) {
                // This includes variations on "not found"
                IO.exception(ex) ;
            }
        }
        return null ;
    }

    @Override
    public String getName() {
        return "LocatorFTP" ;
    }

    @Override
    public int hashCode() {
        return 57 ;
    }
}
