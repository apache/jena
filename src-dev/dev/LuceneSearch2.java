/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator;
import java.util.NoSuchElementException;

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
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.pfunction.PFLib;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArgType;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionEval;
import com.hp.hpl.jena.sparql.util.NodeUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.Map1Iterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/** Base class for searching a IndexLARQ */
// V2

public abstract class LuceneSearch2 extends PropertyFunctionEval
{
    private static Log log = LogFactory.getLog(LuceneSearch2.class) ;
    
    // TODO 2/3 arg object form
    // TODO Score limit
    // TODO Testing limit and scoreLimit are valid (-ve numbers etc
    
    
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

        if ( argSubject.isList() && argSubject.getArgListSize() != 2 )
                throw new QueryBuildException("Subject has "+argSubject.getArgList().size()+" elements, not 2: "+argSubject) ;
        
        if ( argObject.isList() && (argObject.getArgListSize() != 2 && argObject.getArgListSize() != 3) )
                throw new QueryBuildException("Object has "+argObject.getArgList().size()+" elements, not 2 or 3: "+argObject) ;
        
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
        float scoreLimit = -1.0f ;
        
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
            searchString = argObject.getArg(0) ;
            
            for ( int i = 1 ; i < argObject.getArgListSize() ; i++ )
            {
                Node n = argObject.getArg(i) ;
                int nInt = asInteger(n) ;
                if ( isInteger(nInt) )
                {
                    if ( limit > 0 )
                        throw new ExprEvalException("2 potential limits to Lucene search: "+argObject) ;
                    limit = nInt ;
                    if ( limit < 0 )
                        limit = Query.NOLIMIT ;
                    continue ;
                }
                
                float nFloat = asFloat(n) ;
                if ( isFloat(nFloat) )
                {
                    if ( scoreLimit > 0 )
                        throw new ExprEvalException("2 potential score limits to Lucene search: "+argObject) ;
                    if ( nFloat < 0 )
                        throw new ExprEvalException("Negative score limit to Lucene search: "+argObject) ;
                    scoreLimit = nFloat ;
                    continue ;
                }
                throw new ExprEvalException("Bad argument to Lucene search: "+argObject) ;
            }
            
            if ( scoreLimit < 0 )
                scoreLimit = 0.0f ;

            if ( ! isValidSearchString(searchString) )
                return new QueryIterNullIterator(execCxt) ;
        }
        else
        {
            searchString = argObject.getArg() ;
            limit = Query.NOLIMIT ;
            scoreLimit = 0.0f ;
        }
        
        return execEvaluatedSimple(binding, 
                                   subject, score,
                                   predicate,
                                   searchString, limit, scoreLimit,
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
                                             Node searchString, long limit, float scoreLimit,
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
                              qs, limit, scoreLimit,
                              execCxt) ;
        else
            return boundSubject(binding, 
                                subject, score, 
                                qs, limit, scoreLimit,
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
                                    String searchString, long limit, float scoreLimit,
                                    ExecutionContext execCxt)
    {
        //TODO - made public - reverse?
        Iterator iter = getIndex(execCxt).search(searchString) ;
        
        // Score limit. Truncating Iterator.
        if ( scoreLimit > 0 )
            iter = new IteratorTruncate(new ScoreTest(scoreLimit), iter) ;
        
        HitConverter converter = new HitConverter(binding, subject, score) ;
        
        iter =  new Map1Iterator(converter, iter) ;
        QueryIterator qIter = new QueryIterPlainWrapper(iter, execCxt) ;

        if ( limit >= 0 )
            qIter = new QueryIterSlice(qIter, 0, limit, execCxt) ;
        return qIter ;
    }
    
    static class ScoreTest implements IteratorTruncate.Test
    {
        private float scoreLimit ;
        ScoreTest(float scoreLimit) { this.scoreLimit = scoreLimit ; }
        public boolean accept(Object object)
        {
            HitLARQ hit = (HitLARQ)object ;
            return hit.getScore() >= scoreLimit ;
        }
    }
    
    static class IteratorTruncate implements ClosableIterator
    {
        static interface Test { boolean accept(Object object) ; }
        Test test ;
        Object slot = null ;
        boolean active = true ;
        Iterator iter ;
        
        IteratorTruncate (Test test, Iterator iter)
        { this.test = test ; this.iter = iter ; }

        public boolean hasNext()
        {
            if ( ! active ) return false ;
            if ( slot != null )
                return true ;

            if ( ! iter.hasNext() )
            {
                active = false ;
                return false ;
            }
            
            slot = iter.next() ;
            if ( test.accept(slot) )
                return true ;
            // Once the test goes false, no longer yield anything.
            NiceIterator.close(iter) ;
            active = false ;
            iter = null ;
            slot = null ;
            return false ;
        }

        public Object next()
        {
            if ( ! hasNext() )
                throw new NoSuchElementException("IteratorTruncate.next") ;    
            Object x = slot ;
            slot = null ;
            return x ;
        }

        public void remove()
        { throw new UnsupportedOperationException("IteratorTruncate.remove"); }

        public void close()
        { if ( iter != null ) NiceIterator.close(iter) ; }
        
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
    
    // TODO Score.
    public QueryIterator boundSubject(Binding binding, 
                                      Node subject, Node score,
                                      String searchString, long limit, float scoreLimit,
                                      ExecutionContext execCxt)
    {
        //TODO - made public - reverse?
        if ( getIndex(execCxt).contains(subject, searchString) )
            return new QueryIterSingleton(binding, execCxt) ;
        else
            return new QueryIterNullIterator(execCxt) ;
    }

    static private String asString(Node node)
    {
        if ( node.getLiteralDatatype() != null
            && ! node.getLiteralDatatype().equals("") 
            && ! node.getLiteralDatatype().equals(XSDDatatype.XSDstring) )
            return null ;
        return node.getLiteralLexicalForm() ;
    }

    static private float asFloat(Node n)
    {
        if ( n == null ) return Float.MIN_VALUE ;
        NodeValue nv = NodeValue.makeNode(n) ;
        if ( nv.isFloat() )
            return nv.getFloat() ;
        return Float.MIN_VALUE ;
    }

    static private int asInteger(Node n)
    {
        if ( n == null ) return Integer.MIN_VALUE ;
        return NodeUtils.nodeToInt(n) ;
    }
    
    static private boolean isInteger(int i) { return i != Integer.MIN_VALUE ; }
    static private boolean isFloat(float f) { return f != Float.MIN_VALUE ; }
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