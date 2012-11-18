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

import java.io.File ;
import java.io.InputStream ;
import java.io.Reader ;

import com.hp.hpl.jena.rdf.arp.JenaReader ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler ;
import com.hp.hpl.jena.rdf.model.RDFReader ;
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
        String base = chooseBaseURI(url) ;
        reader.read(model, base) ;
    }

    private static String chooseBaseURI(String baseURI)
    {
        String scheme = FileUtils.getScheme(baseURI) ;

        if ( scheme != null )
        {
            if ( scheme.equals("file") )
            {
                if ( ! baseURI.startsWith("file:///") )
                {
                    try {
                        // Fix up file URIs.  Yuk.
                        String tmp = baseURI.substring("file:".length()) ;
                        File f = new File(tmp) ;
                        baseURI = "file:///"+f.getCanonicalPath() ;
                        baseURI = baseURI.replace('\\','/') ;

//                            baseURI = baseURI.replace(" ","%20");
//                            baseURI = baseURI.replace("~","%7E");
                        // Convert to URI.  Except that it removes ///
                        // Could do that and fix up (again)
                        //java.net.URL u = new java.net.URL(baseURI) ;
                        //baseURI = u.toExternalForm() ;
                    } catch (Exception ex) {}
                }
            }
            return baseURI ;
        }

        if ( baseURI.startsWith("/") )
            return "file://"+baseURI ;
        return "file:"+baseURI ;
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

