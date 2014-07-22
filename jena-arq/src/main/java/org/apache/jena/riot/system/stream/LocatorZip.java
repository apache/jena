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

package org.apache.jena.riot.system.stream;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.zip.ZipEntry ;
import java.util.zip.ZipFile ;

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.RDFLanguages ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.shared.JenaException ;


/** Location files in a zip file */

public class LocatorZip implements Locator
{
    private static Logger log = LoggerFactory.getLogger(LocatorZip.class) ;
    private final String zipFileName ; 
    private final ZipFile zipFile ;
    
    public LocatorZip(String zfn)
    {
        try {
            zipFileName = zfn ;
            zipFile = new ZipFile(zipFileName) ;
        } catch  (IOException ex)
        { 
            throw new JenaException("Problems accessing "+zfn, ex) ;
        }
    }
    
    @Override
    public TypedInputStream open(String filenameOrURI)
    {
        ZipEntry entry = zipFile.getEntry(filenameOrURI) ;
        if ( entry == null )
        {
            if ( StreamManager.logAllLookups && log.isDebugEnabled() )
                log.debug("Not found: "+zipFileName+" : "+filenameOrURI) ; 
            return null ;
            
        }
        try
        {
            InputStream in = zipFile.getInputStream(entry) ;
            
            if ( in == null )
            {
                if ( StreamManager.logAllLookups && log.isTraceEnabled() )
                    log.trace("Not found: "+filenameOrURI) ; 
                return null ;
            }
            
            if ( StreamManager.logAllLookups  && log.isTraceEnabled() )
                log.trace("Found: "+filenameOrURI) ;
            
            ContentType ct = RDFLanguages.guessContentType(filenameOrURI) ;
            return new TypedInputStream(in, ct, filenameOrURI) ;
        }
        catch (IOException ex)
        {
            log.warn("IO Exception opening zip entry: " + filenameOrURI);
            return null;
        }
    }

    public String getZipFileName()
    {
        return zipFileName ;
    }

    @Override
    public String getName() { return "LocatorZip("+zipFileName+")" ; } 

    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((zipFileName == null) ? 0 : zipFileName.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        LocatorZip other = (LocatorZip)obj ;
        if (zipFileName == null)
        {
            if (other.zipFileName != null) return false ;
        } else
            if (!zipFileName.equals(other.zipFileName)) return false ;
        return true ;
    }

}
