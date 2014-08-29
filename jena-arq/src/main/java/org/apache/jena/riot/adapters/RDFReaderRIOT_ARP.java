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

import org.apache.jena.riot.SysRIOT ;
import org.apache.jena.riot.system.IRILib ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler ;
import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.rdfxml.xmlinput.JenaReader ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.FileUtils ;

/** Adapter from Jena2 original style adapter for ARP - adds the file: */

public class RDFReaderRIOT_ARP implements RDFReader
{
    // A FileManager sort out baseURIs for files.
    static FileManager fileMgr = new FileManager() ;
    static {
        fileMgr.addLocatorFile() ;
        fileMgr.addLocatorURL() ;
    }
    
    JenaReader reader = new JenaReader() ;
    
    public RDFReaderRIOT_ARP() {}

    @Override
    public void read(Model model, Reader r, String base)
    { reader.read(model, r, base) ; }

    @Override
    public void read(Model model, InputStream r, String base)
    { reader.read(model, r, base) ; }
    
    @Override
    public void read(Model model, String url)
    {
        url = fixupURL(url) ;
        reader.read(model, url) ;
    }

    /** Sort out filename-like URLs: file:, X: and plain filename */ 
    private static String fixupURL(String url)
    {
        String scheme = FileUtils.getScheme(url) ;

        if ( scheme != null )
        {
            // Skip any scheme that is not file: and not a windows drive letter.
            if ( ! scheme.equals("file") && ! isWindowsDrive(scheme) )
                return url ;
        }
        return IRILib.filenameToIRI(url) ;
    }
    
    private static boolean isWindowsDrive(String scheme)
    {
        return  (SysRIOT.isWindows && scheme.length() == 1) ;
    }

    @Override
    public Object setProperty(String propName, Object propValue)
    {
        return reader.setProperty(propName, propValue) ;
    }
    
    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
    {
        return reader.setErrorHandler(errHandler) ;
    }
}

