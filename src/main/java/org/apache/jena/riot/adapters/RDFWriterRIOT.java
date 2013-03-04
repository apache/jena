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

import static org.apache.jena.riot.system.RiotWriterLib.prefixMap ;

import java.io.OutputStream ;
import java.io.Writer ;

import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.IO_Jena2 ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Adapter from RIOT to old style Jena RDFWriter. */
public class RDFWriterRIOT implements RDFWriter 
{
    private final String jenaName ; 
    private Context context = new Context() ;
    
    public RDFWriterRIOT(String jenaName)
    { 
        this.jenaName = jenaName ;
    }
    
    //Initial late to avoid confusing exceptions during newInstance. 
    private WriterGraphRIOT writer()
    {
        RDFFormat format = IO_Jena2.getFormatForJenaWriter(jenaName) ;
        if ( format != null )
            return RDFWriterMgr.createGraphWriter(format) ;
        // Try lang instead.
        Lang lang = RDFLanguages.nameToLang(jenaName) ;
        if ( lang != null )
            return RDFWriterMgr.createGraphWriter(lang) ;
        throw new RiotException("No graph writer for '"+jenaName+"'") ;
    }
    
    @Override
    public void write(Model model, Writer out, String base)
    {
        if (  base != null && base.equals("") )
            base = null ;
        Graph graph = model.getGraph() ;
        writer().write(out, graph, prefixMap(graph), base, context) ;
    }

    @Override
    public void write(Model model, OutputStream out, String base)
    {
        if ( base != null && base.equals("") )
            base = null ;
        Graph graph = model.getGraph() ;
        writer().write(out, graph, prefixMap(graph), base, context) ;
    }

    @Override
    public Object setProperty(String propName, Object propValue)
    {
        return null ;
    }

    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
    {
        return null ;
    }
}
