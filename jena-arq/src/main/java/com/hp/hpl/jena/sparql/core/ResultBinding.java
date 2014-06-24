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

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingBase ;
import com.hp.hpl.jena.sparql.util.ModelUtils ;


/** A mapping from variable name to an RDF value.
 *  A wrapper around the graph level Binding. */


public class ResultBinding extends QuerySolutionBase
{
    Binding binding ;
    Model model ;

    public ResultBinding( Model _model, Binding _binding)
    {
        model = _model ;
        binding = _binding ;
    }
    
//    private ResultBinding( Model _model )
//    {
//        model = _model ;
//        binding = BindingFactory.create() ;
//    }
    
    @Override
    protected RDFNode _get(String varName)
    {
        Node n = binding.get(Var.alloc(varName)) ;
        if ( n == null )
            return null;
        return ModelUtils.convertGraphNodeToRDFNode(n, model) ;
    }
    
    @Override
    protected boolean _contains(String varName)
    {
        return binding.contains(Var.alloc(varName)) ;
    }

    @Override
    public Iterator<String> varNames()
    { 
        List<String> x = new ArrayList<>() ;
        for ( Iterator<Var> iter = binding.vars() ; iter.hasNext(); )
        {
            Var v = iter.next();
            x.add(v.getVarName()) ;
        }
        return x.iterator() ;
    }
    
//    public void setModel(Model m) { model = m ; }
//    public Model getModel() { return model ; }

    public Binding getBinding() { return binding ; }
    
    @Override
    public String toString()
    {
        if ( binding == null )
            return "<no binding>" ;
        return binding.toString() ;
    }

    public static boolean equals(ResultBinding rb1, ResultBinding rb2)
    {
        return BindingBase.equals(rb1.getBinding(), rb2.getBinding() ) ; 
    }
}
