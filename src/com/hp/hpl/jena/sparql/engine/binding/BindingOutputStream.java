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

package com.hp.hpl.jena.sparql.engine.binding;

import java.io.IOException ;
import java.io.OutputStream ;
import java.util.List ;
import java.util.Map ;

import org.openjena.atlas.io.BufferingWriter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.system.PrefixMap ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/** Parser for the RDF Tuples language */
public class BindingOutputStream 
{
    private final BufferingWriter bw ;
    private Binding lastBinding = null ;
    private List<Var> vars = null ;
    private PrefixMap pmap ;
    private boolean needOutputPMap = true ;
    
    public BindingOutputStream(OutputStream out)
    {
        this(out, new PrefixMap()) ;
    }
    
    public BindingOutputStream(OutputStream out, PrefixMap prefixMapping)
    {
        bw = BufferingWriter.create(out) ;
        pmap = prefixMapping ;
    }
    
    public void output(Binding binding)
    {
        try {
            if ( needOutputPMap )
            {
                for ( Map.Entry<String, IRI> e : pmap.getMapping().entrySet() )
                {
                    bw.write("PREFIX ") ;
                    bw.write(e.getKey()) ;
                    bw.write(": <") ;
                    bw.write(e.getValue().toASCIIString()) ;
                    bw.write("> .\n") ;
                }
                needOutputPMap = false ;
            }
            
            // Is the current VARS applicable?
            if ( vars == null || needVars(vars, binding) )
            {
                vars = Iter.toList(binding.vars()) ;
                bw.write("VARS") ;
                for ( Var v2 : vars )
                {
                    bw.write(" ?") ;
                    bw.write(v2.getVarName()) ;
                }
                bw.write(" .\n") ;
            }
            
            for ( Var v : vars )
            {
                Node n = binding.get(v)  ;
                if ( n == null )
                {
                    bw.write("- ") ;
                    continue ;
                }
                if ( n.isURI() )
                {
                    String x = pmap.abbreviate(n.getURI()) ;
                    if ( x != null )
                    {
                        bw.write(x) ;
                        bw.write(" ") ;
                        continue ;
                    }
                }
                bw.write(FmtUtils.stringForNode(n)) ;
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
        for ( Var v : vars )
        {
            if ( ! binding.contains(v) )
                return true ;
        }
        return false ;
    }


    public void flush()
    {
        bw.flush() ;
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

