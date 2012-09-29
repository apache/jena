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

package com.hp.hpl.jena.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.shared.JenaException;

/** Location files in a zip file  */
 

public class LocatorZip implements Locator
{
    static Logger log = LoggerFactory.getLogger(LocatorZip.class) ;
    String zipFileName = null ; 
    ZipFile zipFile = null ;
    
    public LocatorZip(String zfn)
    {
        try {
            zipFileName = zfn ;
            zipFile = new ZipFile(zipFileName) ;
        } catch  (IOException ex)
        { 
            throw new JenaException("Problems accessing "+zipFileName, ex) ;
        }
    }
    
    @Override
    public TypedStream open(String filenameOrURI)
    {
        ZipEntry entry = zipFile.getEntry(filenameOrURI) ;
        if ( entry == null )
        {
            if ( FileManager.logAllLookups && log.isDebugEnabled() )
                log.debug("Not found: "+zipFileName+" : "+filenameOrURI) ; 
            return null ;
            
        }
        try
        {
            InputStream in = zipFile.getInputStream(entry) ;
            
            if ( in == null )
            {
                if ( FileManager.logAllLookups && log.isTraceEnabled() )
                    log.trace("Not found: "+filenameOrURI) ; 
                return null ;
            }
            
            if ( FileManager.logAllLookups  && log.isTraceEnabled() )
                log.trace("Found: "+filenameOrURI) ;
            return new TypedStream(in) ;
        }
        catch (IOException ex)
        {
            log.warn("IO Exception opening zip entry: " + filenameOrURI);
            return null;
        }
    }
    
    public String getZipFileName() { return zipFileName ; }
    
    @Override
    public String getName() { return "LocatorZip("+zipFileName+")" ; } 

}
