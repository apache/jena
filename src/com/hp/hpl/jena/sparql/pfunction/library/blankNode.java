/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction.library;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimple;
import com.hp.hpl.jena.sparql.util.IterLib;

/** Relationship between a node (subject) and it's bNode label (object/string) */ 

public class blankNode extends PFuncSimple
{
    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt)
    {
        if ( Var.isVar(subject) )
            throw new ExprEvalException("bnode: subject is an unbound variable") ;
        if ( ! subject.isBlank() )
            return IterLib.noResults(execCxt) ;
        String str = subject.getBlankNodeLabel() ;
        Node obj = Node.createLiteral(str) ;
        if ( Var.isVar(object) )
            return IterLib.oneResult(binding, Var.alloc(object), obj, execCxt) ;
        
        // Subject and object are concrete 
        if ( object.sameValueAs(obj) )
            return IterLib.result(binding, execCxt) ;
        return IterLib.noResults(execCxt) ;
    }
}

// Code to create bNodes from strings.
//            // Subject a variable : we're try to create a bNode ... :-)
//            
//            if ( Var.isVar(object) )
//                throw new ExprEvalException("bnode: subject and object are both unbound variables") ;
//            
//            if ( ! object.isLiteral() ) return PFLib.noResults(execCxt) ;
//            
//            RDFDatatype dt = object.getLiteralDatatype() ;
//            if ( dt != null &&  !dt.equals(XSDDatatype.XSDstring) )
//                return PFLib.noResults(execCxt) ;
//            
//            String str = object.getLiteralLexicalForm() ;
//            Node n = Node.createAnon(new AnonId(str)) ;
//            return PFLib.oneResult(binding, Var.alloc(subject), n, execCxt) ;

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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