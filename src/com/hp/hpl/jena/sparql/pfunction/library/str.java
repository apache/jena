/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction.library;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimple;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.util.IterLib;

/** Property function to turn an RDF term (but not a blank node) into a string
      <pre>
      ?x :str "foo"@en
      </pre>
*/ 

public class str extends PFuncSimple
{
    public str() {}

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    { }
    
    @Override
    public QueryIterator execEvaluated(Binding binding,
                                       Node subject, Node predicate, Node object,
                                       ExecutionContext execCxt)
    {
        // Subject bound to something other a literal. 
        if ( subject.isURI() || subject.isBlank() )
            return IterLib.noResults(execCxt) ;

        if ( Var.isVar(subject) && Var.isVar(object) )
            throw new QueryExecException("str: Both subject and object are unbound variables") ;
        
        if ( Var.isVar(object) )
            throw new QueryExecException("str: Object is an unbound variables") ;
        
        if ( object.isBlank() )
            throw new QueryExecException("str: object is a blank node") ;
        
        Node strValue =  Node.createLiteral(NodeFunctions.str(object)) ;
        
        if ( Var.isVar(subject) )
            return IterLib.oneResult(binding, Var.alloc(subject), strValue, execCxt) ;
        else
        {
            // Subject bound : check it.
            if ( subject.equals(strValue) )
                return IterLib.result(binding, execCxt) ;
            return IterLib.noResults(execCxt) ;
        }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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