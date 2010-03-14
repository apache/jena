/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.openjena.atlas.io.BufferingWriter ;
import org.openjena.atlas.logging.Log ;


import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFWriter;

public abstract class JenaWriterBase implements RDFWriter
{
    protected Map<String, String> writerPropertyMap = new HashMap<String, String>() ;
    private RDFErrorHandler errorHandler = null ;
    
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler)
    {
        RDFErrorHandler old = errorHandler;
        errorHandler = errHandler;
        return old;
    }

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
            writerPropertyMap = new HashMap<String, String>() ;
        String oldValue = writerPropertyMap.get(propName);
        writerPropertyMap.put(propName,(String)propValue);
        return oldValue;
    }

    protected String absolutePropName(String propName)
    {
        // See abbreviate comand line args.
        return propName ;
    }
    
    public void write(Model model, Writer out, String base)
    {
        write(model.getGraph(), out, base) ;
    }

    public void write(Model model, OutputStream out, String base)
    {
        BufferingWriter buff = BufferingWriter.create(out, 1023) ;
        write(model.getGraph(), buff, base) ;
        buff.flush() ;
    }

    protected abstract void write(Graph graph, Writer out, String base) ;  
    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */