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

package org.apache.jena.riot.stream;

import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.web.HttpOp ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.util.FileUtils ;

public class LocatorURL implements Locator
{
    private static Logger log = LoggerFactory.getLogger(LocatorURL.class) ;
    private static final String[] schemeNames = { "http" , "https" } ;    // Must be lower case and not include the ":"
    
    public final String acceptTriples = "text/turtle,application/rdf+xml;q=0.9,application/xml;q=0.8,*/*;q=0.5" ; 

    @Override
    public TypedInputStream open(String uri)
    {
        if ( ! acceptByScheme(uri) )
        {
            if ( StreamManager.logAllLookups && log.isTraceEnabled() )
                log.trace("Not found : "+uri) ; 
            return null;
        }
        if ( uri.startsWith("http://") || uri.startsWith("https://"))
            return HttpOp.execHttpGet(uri, acceptTriples) ;
        return null ;
    }

    @Override
    public String getName()
    {
        return "LocatorURL" ;
    }
    
    private static boolean acceptByScheme(String filenameOrURI)
    {
        String uriSchemeName = FileUtils.getScheme(filenameOrURI) ;
        if ( uriSchemeName == null )
            return false ;
        uriSchemeName = uriSchemeName.toLowerCase() ; 
        for ( int i = 0 ; i < schemeNames.length ; i++ )
        {
            if ( uriSchemeName.equals(schemeNames[i]) )
                return true ;
        }
        return false ;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((acceptTriples == null) ? 0 : acceptTriples.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        LocatorURL other = (LocatorURL)obj ;
        if (acceptTriples == null)
        {
            if (other.acceptTriples != null) return false ;
        } else
            if (!acceptTriples.equals(other.acceptTriples)) return false ;
        return true ;
    }
}

