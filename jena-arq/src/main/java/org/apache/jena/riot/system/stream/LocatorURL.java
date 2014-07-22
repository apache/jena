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

package org.apache.jena.riot.system.stream;

import java.util.Locale ;

import org.apache.jena.atlas.web.TypedInputStream ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.util.FileUtils ;

public abstract class LocatorURL implements Locator
{
    private final String[] schemeNames ;
    
    protected LocatorURL(String[] sNames) {
        schemeNames = sNames ;
    }
    
    protected abstract Logger log() ;
    
    @Override
    public TypedInputStream open(String uri) {
        if ( ! acceptByScheme(uri) ) {
            if ( StreamManager.logAllLookups && log().isTraceEnabled() )
                log().trace("Not found : "+uri) ; 
            return null;
        }
        return performOpen(uri) ;
    }

    protected abstract TypedInputStream performOpen(String uri) ;

    protected boolean acceptByScheme(String filenameOrURI) {
        String uriSchemeName = FileUtils.getScheme(filenameOrURI) ;
        if ( uriSchemeName == null )
            return false ;
        uriSchemeName = uriSchemeName.toLowerCase(Locale.ROOT) ;
        for ( String schemeName : schemeNames )
        {
            if ( uriSchemeName.equals( schemeName ) )
            {
                return true;
            }
        }
        return false ;
    }

    @Override
    public abstract int hashCode() ;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true ;
        if (obj == null) return false ;
        return getClass() == obj.getClass() ;
    }
}

