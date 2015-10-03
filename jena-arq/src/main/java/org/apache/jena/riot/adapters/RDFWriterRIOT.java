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

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.Locale ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFErrorHandler ;
import org.apache.jena.rdf.model.RDFWriter ;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol ;

/** Adapter from RIOT to old style Jena RDFWriter. */
public class RDFWriterRIOT implements RDFWriter 
{
    // ---- Compatibility
    private final String basename ; 
    private final String jenaName ; 
    private Context context = new Context() ;
    private WriterGraphRIOT writer ;
    private RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();
    
//    public RDFWriterRIOT() {
//        this.basename = "org.apache.jena.riot.writer.generic" ;
//        this.jenaName = null ;
//        writer = writer() ;
//    }
    
    public RDFWriterRIOT(String jenaName) {
        this.basename = "org.apache.jena.riot.writer." + jenaName.toLowerCase(Locale.ROOT);
        this.jenaName = jenaName;
    }

    // Initialize late to avoid confusing exceptions during newInstance.
    private WriterGraphRIOT writer() {
        if ( writer != null )
            return writer;
        if ( jenaName == null )
            throw new IllegalArgumentException("Jena writer name is null");
        writer = setWriter();
        return writer;
    }

    private WriterGraphRIOT setWriter() {
        if ( writer != null )
            return writer;
        if ( jenaName == null )
            throw new IllegalArgumentException("Jena writer name is null");
        // For writing via model.write(), use the old names for jena writers.
        RDFFormat format = RDFWriterRegistry.getFormatForJenaWriter(jenaName) ;
        if ( format != null )
            return RDFDataMgr.createGraphWriter(format) ;
        Lang lang = RDFLanguages.nameToLang(jenaName);
        if ( lang != null )
            return RDFDataMgr.createGraphWriter(lang);
        throw new RiotException("No graph writer for '" + jenaName + "'");
    }

    @Override
    public void write(Model model, Writer out, String base) {
        if ( base != null && base.equals("") )
            base = null;
        Graph graph = model.getGraph();
        writer().write(out, graph, RiotLib.prefixMap(graph), base, context);
    }
    
    @Override
    public void write(Model model, OutputStream out, String base) {
        if ( base != null && base.equals("") )
            base = null;
        Graph graph = model.getGraph();
        writer().write(out, graph, RiotLib.prefixMap(graph), base, context);
    }

    @Override
    public Object setProperty(String propName, Object propValue) {
        Symbol sym = Symbol.create(basename + propName);
        Object oldObj = context.get(sym);
        return oldObj;
    }

    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
        RDFErrorHandler old = errorHandler;
        errorHandler = errHandler;
        return old;
    }
}
