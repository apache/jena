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

package org.apache.jena.sparql.engine.binding;

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.Writer ;
import java.util.Iterator;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.graph.Node ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.out.NodeFormatter ;
import org.apache.jena.riot.out.NodeFormatterTTL ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.sparql.core.Var ;

/** Writer for the RDF Tuples language */
public class BindingOutputStream implements Sink<Binding>
{
    private final AWriter bw ;
    private Binding lastBinding = null ;
    private List<Var> vars = null ;
    private PrefixMap pmap ;
    private NodeFormatter nodeFormatter ;
    private boolean needOutputPMap = true ;
    private boolean needOutputVars = true ;
    
    public BindingOutputStream(OutputStream out)
    {
        this(out, null, null) ;
    }
    
    public BindingOutputStream(OutputStream out, PrefixMap prefixMapping)
    {
        this(out, null, prefixMapping) ;
    }
    
    public BindingOutputStream(OutputStream out, List<Var> vars)
    {
        this(out, vars, null) ;
    }

    public BindingOutputStream(OutputStream out, List<Var> vars, PrefixMap prefixMapping)
    {
        //this( BufferingWriter.create(out) , vars, prefixMapping) ;
        this( IO.asBufferedUTF8(out), vars, prefixMapping) ;
    }
    
    private BindingOutputStream(Writer out, List<Var> variables, PrefixMap prefixMapping)
    {
        bw = IO.wrap(out) ;
        vars = variables ;
        pmap = prefixMapping ;
        
        nodeFormatter = new NodeFormatterTTL(null, pmap) ;
        needOutputVars = (vars != null ) && vars.size() > 0 ;
    }
    
    public final void write(Binding binding) { send(binding) ; }
    @Override
    public void send(Binding binding)
    {
        try {
            if ( needOutputPMap )
            {
                if ( pmap != null )
                {
                    for ( Map.Entry<String, IRI> e : pmap.getMapping().entrySet() )
                    {
                        bw.write("PREFIX ") ;
                        bw.write(e.getKey()) ;
                        bw.write(": <") ;
                        bw.write(e.getValue().toASCIIString()) ;
                        bw.write("> .\n") ;
                    }
                }
                needOutputPMap = false ;
            }
            
            // Is the current VARS applicable?
            if ( needVars(vars, binding) )
            {
                if ( vars == null ) {
                    vars = Iter.toList(binding.vars()) ;
                } else {
                    // Order preserving update to vars (nicety)
                    // Adds new vars to end of list, does not remove old ones.
                    Iterator<Var> x = binding.vars();
                    while(x.hasNext()) {
                        Var v = x.next();
                        if ( ! vars.contains(v) )
                            vars.add(v);
                    }
                }
                needOutputVars = true ;
            }
            
            if ( needOutputVars )
            {
                // Odd special case.
                // No vars, empty binding.
                if ( binding.size() == 0 && vars.size() == 0 )
                {
                    bw.write(".\n") ;
                    needOutputVars = false ;
                    return ;
                }
                
                bw.write("VARS") ;
                for ( Var v2 : vars )
                {
                    bw.write(" ?") ;
                    bw.write(v2.getVarName()) ;
                }
                bw.write(" .\n") ;
                needOutputVars = false ;
            }
            
            for ( Var v : vars )
            {
                Node n = binding.get(v)  ;
                if ( n == null )
                {
                    bw.write("- ") ;
                    continue ;
                }
                // NodeFormatters should write safe bNode labels.
                nodeFormatter.format(bw, n) ;
                bw.write(" ") ;
            }
            bw.write(".\n") ;
        } catch (IOException ex)
        {
            throw new RiotException(ex) ;
        }
    }

    private static boolean needVars(List<Var> vars, Binding binding)
    {
        if ( vars == null ) 
            return true ;
        List<Var> x = Iter.toList(binding.vars());
        if ( x.equals(vars) ) 
            return false;
        return true;
    }

    @Override
    public void flush()
    {
        IO.flush(bw) ;
    }
    
    @Override
    public void close()
    {
        IO.close(bw) ;
    }

    public List<Var> getVars()
    {
        return vars ;
    }

    public void setVars(List<Var> vars)
    {
        this.vars = vars ;
    }

    public PrefixMap getPrefixMap()
    {
        return pmap ;
    }

    public void setPrefixMap(PrefixMap pmap)
    {
        this.pmap = pmap ;
        this.needOutputPMap = true ;
    }
}
