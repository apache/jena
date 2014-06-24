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

package org.apache.jena.riot.system;

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;

public abstract class JenaWriterBase implements RDFWriter
{
    protected Map<String, String> writerPropertyMap = new HashMap<>() ;
    private RDFErrorHandler errorHandler = null ;
    
    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
    {
        RDFErrorHandler old = errorHandler;
        errorHandler = errHandler;
        return old;
    }

    @Override
    public Object setProperty(String propName, Object propValue)
    {
        if ( ! ( propValue instanceof String ) )
        {
            Log.warn(this, "setProperty: Property for '"+propName+"' is not a string") ;
            propValue = propValue.toString() ;
        }
        
        // Store absolute name of property 
        propName = absolutePropName(propName) ;
        if ( writerPropertyMap == null )
            writerPropertyMap = new HashMap<>() ;
        String oldValue = writerPropertyMap.get(propName);
        writerPropertyMap.put(propName,(String)propValue);
        return oldValue;
    }

    protected String absolutePropName(String propName)
    {
        // See abbreviate comand line args.
        return propName ;
    }
    
    @Override
    public void write(Model model, Writer out, String base)
    {
        write(model.getGraph(), out, base) ;
    }

    @Override
    public void write(Model model, OutputStream out, String base)
    {
        Writer w = IO.asBufferedUTF8(out) ; 
        write(model.getGraph(), w, base) ;
        try { w.flush() ; } catch (Exception e) {}
    }

    protected abstract void write(Graph graph, Writer out, String base) ;  
    
}
