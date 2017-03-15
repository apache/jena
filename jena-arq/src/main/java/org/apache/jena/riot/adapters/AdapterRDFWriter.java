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
import java.util.Map ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.RDFWriter ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.SysRIOT ;
import org.apache.jena.riot.WriterGraphRIOT ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.writer.WriterGraphRIOTBase ;
import org.apache.jena.sparql.util.Context ;

/**
 * Adapter providing RIOT interface {@link WriterGraphRIOT} over an old-style
 * Jena {@link RDFWriter}. Subclasses of this class are used for RDF/XML
 * (basic and abbrevated) in RIOT.
 * <p>
 * See {@link RDFWriterRIOT} for the class plugged into RIOT that provides the
 * {@link RDFWriter} interface to Jena core operations. It is {@link RDFWriter} over
 * a {@link WriterGraphRIOT}.
 */
public abstract class AdapterRDFWriter extends WriterGraphRIOTBase
{
    protected abstract RDFWriter create() ;
    
    @Override public abstract Lang getLang() ;

    @Override
    public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        RDFWriter w = create() ;
        setProperties(w, context) ;
        w.write(ModelFactory.createModelForGraph(graph), out, baseURI) ;
    }

    @Override
    public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        RDFWriter w = create() ;
        setProperties(w, context) ;
        w.write(ModelFactory.createModelForGraph(graph), out, baseURI) ;
    }
    
    private static void setProperties(RDFWriter w, Context context) {
        try { 
            @SuppressWarnings("unchecked")
            Map<String, Object> p = (Map<String, Object>)(context.get(SysRIOT.sysRdfWriterProperties)) ;
            if ( p != null )
                p.forEach((k,v) -> w.setProperty(k, v)) ;
        } catch (Throwable ex) {
            Log.warn(AdapterRDFWriter.class, "Problem setting properties", ex);
        }
    }


}

