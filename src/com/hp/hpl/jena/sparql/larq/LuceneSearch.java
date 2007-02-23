/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.larq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimple;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;

/** Base class for searching a IndexLARQ */

public abstract class LuceneSearch extends PFuncSimple
{
    protected abstract IndexLARQ getIndex(ExecutionContext execCxt) ;
    
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        super.build(argSubject, predicate, argObject, execCxt) ;
        
        Node obj = argObject.getArg() ;
        if ( getIndex(execCxt) == null )
            throw new QueryBuildException("Index not found") ;
        
        if ( !obj.isLiteral() )
            throw new QueryBuildException("Not a string: "+argObject.getArg()) ;
        
        if ( obj.getLiteralDatatypeURI() != null )
            throw new QueryBuildException("Not a plain string: "+argObject.getArg()) ;
        
        if ( obj.getLiteralLanguage() != null && ! obj.getLiteralLanguage().equals("") )
            throw new QueryBuildException("Not a plain string (has lang tag): "+argObject.getArg()) ;
        
    }
    
    public QueryIterator execEvaluated(Binding binding, 
                                       Node subject, Node predicate, Node searchString, 
                                       ExecutionContext execCxt)
    {
        // check searchString : use build()
        String queryString = null ;
        if ( ! searchString.isLiteral() )
        {
            System.err.println("Not a literal: "+subject) ;
            return new QueryIterNullIterator(execCxt) ;
        }

        String qs = asString(searchString) ;
        
        if ( qs == null )
        {
                System.err.println("Not a string: "+subject) ;
                return new QueryIterNullIterator(execCxt) ;
        }
        
        if ( subject.isVariable() )
            return varSubject(binding,subject,qs, execCxt) ;
        else
            return boundSubject(binding,subject,qs, execCxt) ;
        //throw new ARQNotImplemented("Bound subject to luceneSearch") ;
    }
    
    public QueryIterator varSubject(Binding binding, 
                                       Node subject, String searchString, 
                                       ExecutionContext execCxt)
    {
        Iterator iter = getIndex(execCxt).search(searchString) ;
        // Better a wrapper-converted iterator
        //new QueryIterConvert()
        List results = new ArrayList() ;
        for ( ; iter.hasNext(); )
        {
            Node x = (Node)iter.next();
            results.add(new Binding1(binding, Var.alloc(subject), x)) ;
        }
        // To binding.
        return new QueryIterPlainWrapper(results.iterator(), execCxt) ;
    }
    
    public QueryIterator boundSubject(Binding binding, 
                                      Node subject, String searchString, 
                                      ExecutionContext execCxt)
    {
        if ( getIndex(execCxt).contains(subject, searchString) )
            return new QueryIterSingleton(binding, execCxt) ;
        else
            return new QueryIterNullIterator(execCxt) ;
    }

    private String asString(Node node)
    {
        if ( node.getLiteralDatatype() != null
            && ! node.getLiteralDatatype().equals("") 
            && ! node.getLiteralDatatype().equals(XSDDatatype.XSDstring) )
            return null ;
        return node.getLiteralLexicalForm() ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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