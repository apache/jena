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

import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.web.HttpOp ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class LocatorHTTP extends LocatorURL {
    private static Logger         log         = LoggerFactory.getLogger(LocatorHTTP.class) ;
    private static final String[] schemeNames = {"http", "https"} ;

    public LocatorHTTP() {
        super(schemeNames) ;
    }

    @Override
    protected Logger log() { return log ; }

    @Override
    public TypedInputStream performOpen(String uri) {
        if ( uri.startsWith("http://") || uri.startsWith("https://") )
            return HttpOp.execHttpGet(uri, WebContent.defaultGraphAcceptHeader) ;
        return null ;
    }

    @Override
    public String getName() {
        return "LocatorHTTP" ;
    }

    @Override
    public int hashCode() {
        return 57 ;
    }
}
