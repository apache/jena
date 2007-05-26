/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Utils;


public class AlgebraGenerator 
{
    // Fixed filter position means leave exactly where it is syntactically (illegal SPARQL)
    // Helpful only to write exactly what you mean and test the full query compiler).
    boolean fixedFilterPosition = false ;
    private Context context ;

    public AlgebraGenerator(Context context)
    { 
        if ( context == null )
            context = ARQ.getContext().copy() ;
        this.context = context ;
    }
    
    public AlgebraGenerator() { this(null) ; } 
    
    public Op compile(Query query)
    {
        Op pattern = compile(query.getQueryPattern()) ;
        Op op = compileModifiers(query, pattern) ;
        return op ;
    }
    
    // Compile any structural element
    public Op compile(Element elt)
    {
      if ( elt instanceof ElementUnion )
          return compile((ElementUnion)elt) ;
    
      if ( elt instanceof ElementGroup )
          return compile((ElementGroup)elt) ;
    
      if ( elt instanceof ElementNamedGraph )
          return compile((ElementNamedGraph)elt) ; 
    
      // This is only here for queries built programmatically
      // (triple patterns not in a group) 
      if ( elt instanceof ElementTriplesBlock )
          return compile(((ElementTriplesBlock)elt).getTriples()) ;

      if ( elt == null )
          return new OpNull() ;

      broken("compile(Element)/Not a structural element: "+Utils.className(elt)) ;
      return null ;
    }

    protected Op compile(ElementUnion el)
    { 
        if ( el.getElements().size() == 1 )
        {
            Element subElt = (Element)el.getElements().get(0) ;
            ElementGroup elg = (ElementGroup)subElt ;
            return compile(elg) ;
        }
        
        Op current = null ;
        
        for (Iterator iter = el.getElements().listIterator() ; iter.hasNext() ; )
        {
            Element subElt = (Element)iter.next() ;
            ElementGroup elg = (ElementGroup)subElt ;
            Op op = compile(elg) ;
            if ( current == null )
                current = op ;
            else
                current = new OpUnion(current, op) ;
        }
        return current ;
    }
    
    protected Op compile(ElementGroup groupElt)
    {
        if ( fixedFilterPosition )
            return compileFixed(groupElt) ;
        
        ExprList filters = new ExprList() ;
        Op current = OpTable.unit() ;
        
        // TriplesBlock => BGP
        // If adjacent triples blocks after filters extracted, then merge.
        // Simpler : extract filters first?
        
        BasicPattern currentPattern = null ;
        
        for (Iterator iter = groupElt.getElements().listIterator() ; iter.hasNext() ; )
        {
            Element elt = (Element)iter.next() ;
            
            if ( elt instanceof ElementTriplesBlock )
            {
                ElementTriplesBlock etb = (ElementTriplesBlock)elt ;
                // Accumulate triples.
                if  ( currentPattern == null )
                    currentPattern = new BasicPattern() ;
                currentPattern.addAll(etb.getTriples()) ;
                continue ;
            }
            
            if (  elt instanceof ElementFilter )
            {
                ElementFilter f = (ElementFilter)elt ; 
                filters.add(f.getExpr()) ;
                continue ;
            }
            
            // At this point, whatever it is, break up any BGP in progress

            if ( currentPattern != null )
            {
                Op op = compile(currentPattern) ;
                current = join(current, op) ;
            }

            currentPattern = null ;

            if ( elt instanceof ElementOptional )
            {
                ElementOptional eltOpt = (ElementOptional)elt ;
                current = compile(eltOpt, current) ;
                continue ;
            }
            
            if ( elt instanceof ElementGroup || 
                 elt instanceof ElementNamedGraph ||
                 elt instanceof ElementUnion )
            {
                Op op = compile(elt) ;
                current = join(current, op) ;
                continue ;
            }
            
            broken("compile/group: not a fixed element, optional or filter: "+Utils.className(elt)) ;
        }
        
        // Any trailing basic pattern
        if ( currentPattern != null )
        {
            Op op = compile(currentPattern) ;
            current = join(current, op) ;
        }
        
        if ( ! filters.isEmpty() )
        {
            if ( current == null )
                current = OpTable.unit() ;
            current = OpFilter.filter(filters, current) ;
        }
        
        return current ;
    }

    protected Op compile(ElementOptional eltOpt, Op current)
    {
        Element subElt = eltOpt.getOptionalElement() ;
        Op op = compile(subElt) ;
        ExprList exprs = null ;
        if ( op instanceof OpFilter )
        {
            OpFilter f = (OpFilter)op ;
            //f = OpFilter.tidy(f) ;  // Collapse filter(filter(..))
            Op sub = f.getSubOp() ;
            if ( sub instanceof OpFilter )
                broken("compile/Optional/nested filters - unfinished") ; 
            exprs = f.getExprs() ;
            op = sub ;
        }
        current = OpLeftJoin.create(current, op, exprs) ;
        return current ;
    }
    
    protected Op compile(BasicPattern pattern)
    {
        return new OpBGP(pattern) ;
    }
    
    protected Op compile(ElementNamedGraph eltGraph)
    {
        Node graphNode = eltGraph.getGraphNameNode() ;
        Op sub = compile(eltGraph.getElement()) ;
        return new OpGraph(graphNode, sub) ;
    }

    private Op compileFixed(ElementGroup groupElt)
    {
        Op current = OpTable.unit() ;
        for (Iterator iter = groupElt.getElements().listIterator() ; iter.hasNext() ; )
        {
            Element elt = (Element)iter.next() ;
            current = compileDirect(elt, current) ;
        }
            
        return current ;
    }

    private Op compileDirect(Element elt, Op current)
    {
        if ( elt instanceof ElementTriplesBlock )
        {
            ElementTriplesBlock etb = (ElementTriplesBlock)elt ;
            Op op =  compile(etb.getTriples()) ;
            return join(current, op) ;
        }
        
        if (  elt instanceof ElementFilter )
        {
            ElementFilter f = (ElementFilter)elt ; 
            return OpFilter.filter(new ExprList(f.getExpr()), current) ;
        }
    
        if ( elt instanceof ElementOptional )
        {
            ElementOptional eltOpt = (ElementOptional)elt ;
            return compile(eltOpt, current) ;
        }
        
        if ( elt instanceof ElementGroup || 
             elt instanceof ElementNamedGraph ||
             elt instanceof ElementUnion )
        {
            Op op = compile(elt) ;
            return join(current, op) ;
        }
        
        broken("compileDirect/Element not recognized: "+Utils.className(elt)) ;
        return null ;
    }
    
    /** Compile query modifiers */
    public Op compileModifiers(Query query, Op pattern)
    {
        Op op = pattern ;
        Modifiers mods = new Modifiers(query) ;
        
        // ---- ToList
        if ( context.isTrue(ARQ.generateToList) )
            // Listify it.
            op = new OpList(op) ;
        
        // ---- ORDER BY
        if ( mods.orderConditions != null )
            op = new OpOrder(op, mods.orderConditions) ;
        
        // ---- PROJECT
        // (ORDER may involve an unselected variable)
        // No projection => initial variables are exposed.
        // Needed for CONSTRUCT and initial bindings + SELECT *
        
        if ( mods.projectVars != null && ! query.isQueryResultStar())
        {
            // Don't project for QueryResultStar so initial bindings show through
            // in SELECT *
            if ( mods.projectVars.size() == 0 && query.isSelectType() )
                LogFactory.getLog(AlgebraGenerator.class).warn("No project variables") ;
            if ( mods.projectVars.size() > 0 ) 
                op = new OpProject(op, mods.projectVars) ;
        }
        
        // ---- DISTINCT
        if ( query.isDistinct() )
            op = new OpDistinct(op) ;
        
        // ---- REDUCED
        if ( query.isReduced() )
            op = new OpReduced(op) ;
        
        // ---- LIMIT/OFFSET
        if ( query.hasLimit() || query.hasOffset() )
            op = new OpSlice(op, mods.start, mods.length) ;
        
        return op ;
    }

    // -------- 
    
    protected Op join(Op current, Op newOp)
    { return OpJoin.create(current, newOp) ; }

    private void broken(String msg)
    {
        System.err.println("AlgebraCompiler: "+msg) ;
        throw new ARQInternalErrorException(msg) ;
    }
    
//    protected Modifiers getModifiers()
//    {
//        Modifiers mods = new Modifiers(query) ;
//        if ( query.isConstructType() )
//            // Need to expose the initial bindings - no projection at all. 
//            mods.projectVars = null ;
//        return mods ;
//    }

    private static class Modifiers
    {
        // And construct needs to avoid a projection.
        public long start ;
        public long length ;
        public boolean distinct ;
        public boolean reduced ;
        public List projectVars ;      // Null for no projection
        public List orderConditions ;

        public Modifiers(Query query)
        {
            start = query.getOffset() ;
            length = query.getLimit() ;
            distinct = query.isDistinct() ;
            reduced = query.isReduced() ;
            projectVars = Var.varList(query.getResultVars()) ;
            orderConditions = query.getOrderBy() ;
        }
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