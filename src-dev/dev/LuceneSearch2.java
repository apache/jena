/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.larq.HitLARQ;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSlice;
import com.hp.hpl.jena.sparql.pfunction.PFLib;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArgType;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionEval;
import com.hp.hpl.jena.sparql.util.NodeUtils;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.Map1Iterator;

/** Base class for searching a IndexLARQ */
// V2

public abstract class LuceneSearch2 extends PropertyFunctionEval
{
    private static Log log = LogFactory.getLog(LuceneSearch2.class) ;
    
    protected LuceneSearch2()
    {
        super(PropFuncArgType.PF_ARG_EITHER,
              PropFuncArgType.PF_ARG_EITHER) ;
    }

    protected abstract IndexLARQ getIndex(ExecutionContext execCxt) ;
    
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        super.build(argSubject, predicate, argObject, execCxt) ;
        if ( getIndex(execCxt) == null )
            throw new QueryBuildException("Index not found") ;

        if ( argSubject.isList() && argSubject.getArgList().size() != 2 )
                throw new QueryBuildException("Subject has "+argSubject.getArgList().size()+" elements, not 2: "+argSubject) ;
        
        if ( argObject.isList() && argObject.getArgList().size() != 2 )
                throw new QueryBuildException("Object has "+argObject.getArgList().size()+" elements, not 2: "+argObject) ;
        
//        
//        Node obj = argObject.getArg() ;
//        if ( getIndex(execCxt) == null )
//            throw new QueryBuildException("Index not found") ;
//        
//        if ( !obj.isLiteral() )
//            throw new QueryBuildException("Not a string: "+argObject.getArg()) ;
//        
//        if ( obj.getLiteralDatatypeURI() != null )
//            throw new QueryBuildException("Not a plain string: "+argObject.getArg()) ;
//        
//        if ( obj.getLiteralLanguage() != null && ! obj.getLiteralLanguage().equals("") )
//            throw new QueryBuildException("Not a plain string (has lang tag): "+argObject.getArg()) ;
        
    }
    
    public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        Node subject = null ;
        Node score = null ;
        
        Node searchString = null ;
        long limit = Query.NOLIMIT ;
        
        
        if ( argSubject.isList() )
        {
            // Length checked in build
            subject = argSubject.getArg(0) ;
            score = argSubject.getArg(1) ;
            
            if ( subject.isVariable() && ! score.isVariable() )
                throw new QueryExecException("Subject is a variable but the score is not: ("+argSubject) ;
        }
        else
        {
            subject = argSubject.getArg() ;
            score = null ;
        }
        
        if ( argObject.isList() )
        {
            // Length checked in build
            searchString= argObject.getArg(0) ;
            
            Node limitN = argObject.getArg(1) ;
            if ( ! isValidSearchString(searchString) )
                return new QueryIterNullIterator(execCxt) ;
            limit = NodeUtils.nodeToInt(limitN) ; 
            if ( limit < 0 )
            {
                log.warn("Can't grok limit: "+limitN) ;
                return PFLib.noResults(execCxt) ;
            }
        }
        else
        {
            searchString = argObject.getArg() ;
            limit = Query.NOLIMIT ;
        }
        
        return execEvaluatedSimple(binding, 
                                   subject, score,
                                   predicate,
                                   searchString, limit,
                                   execCxt) ;
        /*
        + Access to other fields
          e.g. +((text:(foo bar)^2)(title:(foo bar)^4)(description:(foo bar)^3))
          Use cases and examples?

        + Extend to: (?node ?score) textMatch ('query' 100) 
          Also ARQ.luceneLimit context parameter
        */
    }

    public QueryIterator execEvaluatedSimple(Binding binding, 
                                             Node subject, Node score,  
                                             Node predicate, 
                                             Node searchString, long limit,
                                             ExecutionContext execCxt)
    {
        if ( !isValidSearchString(searchString) )
            return PFLib.noResults(execCxt) ;

        String qs = asString(searchString) ;
        
        if ( qs == null )
        {
            log.warn("Not a string (it was a moment ago!): "+searchString) ;
            return new QueryIterNullIterator(execCxt) ;
        }
        
        if ( subject.isVariable() )
            return varSubject(binding, 
                              Var.alloc(subject), (score==null)?null:Var.alloc(score),
                              qs, limit,
                              execCxt) ;
        else
            return boundSubject(binding, 
                                subject, score, 
                                qs, limit,
                                execCxt) ;
    }
    
    private static boolean isValidSearchString(Node searchString)
    {
        if ( !searchString.isLiteral() )
        {
            log.warn("Not a string: "+searchString) ;
            return false ;
        }

        if ( searchString.getLiteralDatatypeURI() != null )
        {
            log.warn("Not a plain string: "+searchString) ;
            return false ;
        }

        if ( searchString.getLiteralLanguage() != null && ! searchString.getLiteralLanguage().equals("") )
        {
            log.warn("Not a plain string (has lang tag): "+searchString) ;
            return false ;
        }
        return true ;
    }
    
    public QueryIterator varSubject(Binding binding, 
                                    Var subject, Var score,
                                    String searchString, long limit, 
                                    ExecutionContext execCxt)
    {
        //TODO - made public - reverse?
        Iterator iter = getIndex(execCxt).search(searchString) ;
        
        HitConverter converter = new HitConverter(binding, subject, score) ;
        iter =  new Map1Iterator(converter, iter) ;
        QueryIterator qIter = new QueryIterPlainWrapper(iter, execCxt) ;

        if ( limit >= 0 )
            qIter = new QueryIterSlice(qIter, 0, limit, execCxt) ;
        return qIter ;
    }
    
    static class HitConverter implements Map1
    {
        private Binding binding ;
        private Var subject ;
        private Var score ;
        
        HitConverter(Binding binding, Var subject, Var score)
        {
            this.binding = binding ;
            this.subject = subject ;
            this.score = score ;
        }
        
        public Object map1(Object thing)
        {
            HitLARQ hit = (HitLARQ)thing ;
            Binding b = new BindingMap(binding) ;
            b.add(Var.alloc(subject), hit.getNode()) ;
            if ( score != null )
                b.add(Var.alloc(score), NodeUtils.floatToNode(hit.getScore())) ;
            return b ;
        }
        
    }
    
    public QueryIterator boundSubject(Binding binding, 
                                      Node subject, Node score,
                                      String searchString, long limit, 
                                      ExecutionContext execCxt)
    {
        //TODO - made public - reverse?
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