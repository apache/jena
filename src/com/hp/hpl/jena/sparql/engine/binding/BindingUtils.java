/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;
import java.util.Iterator ;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.ResultBinding ;
import com.hp.hpl.jena.sparql.core.Var ;

public class BindingUtils
{
    
//    public static Triple substituteIntoTriple(Triple t, Binding binding)
//    {
//        Node subject = substituteNode(t.getSubject(), binding) ;
//        Node predicate = substituteNode(t.getPredicate(), binding) ;
//        Node object = substituteNode(t.getObject(), binding) ;
//        
//        if ( subject == t.getSubject() &&
//             predicate == t.getPredicate() &&
//             object == t.getObject() )
//             return t ;
//             
//        return new Triple(subject, predicate, object) ;
//    }
//    
//    public static Node substituteNode(Node n, Binding binding)
//    {
//        return Var.lookup(binding, n) ;
//    }
    
    /** Convert a query solution to a binding */ 
    public static Binding asBinding(QuerySolution qSolution)
    {
        if ( qSolution == null )
            return null ;
        if ( qSolution instanceof ResultBinding )
            // Only named variables.
            return new BindingProjectNamed( ((ResultBinding)qSolution).getBinding() ) ;
        Binding binding = new BindingMap(null) ;
        addToBinding(binding, qSolution) ;
        return binding ;
    }
        
    public static void addToBinding(Binding binding, QuerySolution qSolution)
    {
        if ( qSolution == null )
            return ;
        
        for ( Iterator<String> iter = qSolution.varNames() ; iter.hasNext() ; )
        {
            String n = iter.next() ;
            
            RDFNode x = qSolution.get(n) ;
            //XXX
            if ( Var.isBlankNodeVarName(n) )
                continue ;
            try {
                binding.add(Var.alloc(n), x.asNode()) ;
            } catch (ARQInternalErrorException ex)
            {
                // bad binding attempt.
                Log.warn(BindingUtils.class, "Attempt to bind "+n+" when already bound") ;
            }
        }
    }
    
    public static boolean equals(Binding b1, Binding b2)
    {
        return BindingBase.equals(b1, b2) ; 
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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
