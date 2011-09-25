/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
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
        List<String> x = new ArrayList<String>() ;
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


/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
