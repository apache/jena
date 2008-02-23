/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Utils;


public class AlgebraGenerator 
{
    // Fixed filter position means leave exactly where it is syntactically (illegal SPARQL)
    // Helpful only to write exactly what you mean and test the full query compiler.
    boolean fixedFilterPosition = false ;
    private Context context ;
    
    // SimplifyEarly=true is the alternative reading of the DAWG
    // algebra translation algorithm
    boolean simplifyEarly = false ;         // False is correct setting. 

    public AlgebraGenerator(Context context)
    { 
        if ( context == null )
            context = ARQ.getContext().copy() ;
        this.context = context ;
    }
    
    public AlgebraGenerator() { this(null) ; } 
    
    //-- Public operations.  Do not call recursively (call compileElement).
    // These operations apply the simplification step which is done, once, at the end.
    
    public Op compile(Query query)
    {
        Op pattern = compile(query.getQueryPattern()) ;     // Not compileElement - may need to apply simplification.
        Op op = compileModifiers(query, pattern) ;
        return op ;
    }
    
    static Transform simplify = new TransformSimplify() ;
    // Compile any structural element
    public Op compile(Element elt)
    {
        Op op = compileElement(elt) ;
        if ( ! simplifyEarly && simplify != null )
            op = Transformer.transform(simplify, op) ;
        return op ;
    }

    // This is the operation to call for recursive application of step 4.
    protected Op compileElement(Element elt)
    {
        if ( elt instanceof ElementUnion )
            return compileElementUnion((ElementUnion)elt) ;
      
        if ( elt instanceof ElementGroup )
            return compileElementGroup((ElementGroup)elt) ;
      
        if ( elt instanceof ElementNamedGraph )
            return compileElementGraph((ElementNamedGraph)elt) ; 
      
        if ( elt instanceof ElementService )
            return compileElementService((ElementService)elt) ; 

        // This is only here for queries built programmatically
        // (triple patterns not in a group) 
        if ( elt instanceof ElementTriplesBlock )
            return compileBasicPattern(((ElementTriplesBlock)elt).getTriples()) ;

        if ( elt == null )
            return new OpNull() ;

        broken("compile(Element)/Not a structural element: "+Utils.className(elt)) ;
        return null ;
        
    }
    
    protected Op compileElementUnion(ElementUnion el)
    { 
        if ( el.getElements().size() == 1 )
        {
            Element subElt = (Element)el.getElements().get(0) ;
            ElementGroup elg = (ElementGroup)subElt ;
            return compileElement(elg) ;
        }
        
        Op current = null ;
        
        for (Iterator iter = el.getElements().listIterator() ; iter.hasNext() ; )
        {
            Element subElt = (Element)iter.next() ;
            ElementGroup elg = (ElementGroup)subElt ;
            Op op = compileElement(elg) ;
            if ( current == null )
                current = op ;
            else
                current = new OpUnion(current, op) ;
        }
        return current ;
    }
    
    // Produce the algebra for a single group.
    // http://www.w3.org/TR/rdf-sparql-query/#convertGraphPattern
    //
    // We do some of the steps recursively as we go along. 
    // The only step that must be done after the others to get
    // the right results is simplification.
    //
    // Step 0: (URI resolving and triple pattern syntax forms) was done during parsing
    // Step 1: (BGPs) Done in this code
    // Step 2: (Groups and unions) Was done during parsing to get ElementUnion.
    // Step 3: (GRAPH) Done in this code.
    // Step 4: (Filter extraction and OPTIONAL) Done in this code
    // Simplicifation: Done later 
    // If simplicifation is done now, it changes OPTIONAL { { ?x :p ?w . FILTER(?w>23) } } because it removes the
    //   (join Z (filter...)) that in turn stops the filter getting moved into the LeftJoin.  
    //   It need a depth of 2 or more {{ }} for this to happen. 
    

    protected Op compileElementGroup(ElementGroup groupElt)
    {
        Op current = OpTable.unit() ;
        ExprList exprList = new ExprList() ;
        
        for (Iterator iter = groupElt.getElements().listIterator() ; iter.hasNext() ; )
        {
            Element elt = (Element)iter.next() ;
            current = compileOneInGroup(elt, current, exprList) ;
        }
            
        // Filters collected from the group. 
        if ( ! exprList.isEmpty() )
            current = OpFilter.filter(exprList, current) ;
        
        return current ;
    }

    private Op compileOneInGroup(Element elt, Op current, ExprList exprList)
    {
        // Replace triple patterns by OpBGP (i.e. SPARQL translation step 1)
        if ( elt instanceof ElementTriplesBlock )
        {
            ElementTriplesBlock etb = (ElementTriplesBlock)elt ;
            Op op =  compileBasicPattern(etb.getTriples()) ;
            return join(current, op) ;
        }
        
        // Collect filters
        if (  elt instanceof ElementFilter )
        {
            ElementFilter f = (ElementFilter)elt ;
            if ( fixedFilterPosition )
                // Not SPARQL.
                return OpFilter.filter(f.getExpr(), current) ;
             
            exprList.add(f.getExpr()) ;
            return current ;
        }
    
        // Optional: recurse
        if ( elt instanceof ElementOptional )
        {
            ElementOptional eltOpt = (ElementOptional)elt ;
            return compileElementOptional(eltOpt, current) ;
        }
        
        // All other elements: compile the element and then join on to the current group expression.
        if ( elt instanceof ElementGroup || 
             elt instanceof ElementNamedGraph ||
             elt instanceof ElementService ||
             elt instanceof ElementUnion )
        {
            Op op = compileElement(elt) ;
            return join(current, op) ;
        }
        
        broken("compileDirect/Element not recognized: "+Utils.className(elt)) ;
        return null ;
    }

    protected Op compileElementOptional(ElementOptional eltOpt, Op current)
    {
        Element subElt = eltOpt.getOptionalElement() ;
        Op op = compileElement(subElt) ;
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
    
    public static boolean AlgebraStaging = true ;
    protected Op compileBasicPattern(BasicPattern pattern)
    {
        if ( AlgebraStaging )
        {
            // Sort out property functions.
            Op op = PropertyFunctionGenerator.compile(pattern, context) ;
            return op ;
        }
        return new OpBGP(pattern) ;
    }
    
    protected Op compileElementGraph(ElementNamedGraph eltGraph)
    {
        Node graphNode = eltGraph.getGraphNameNode() ;
        Op sub = compileElement(eltGraph.getElement()) ;
        return new OpGraph(graphNode, sub) ;
    }

    protected Op compileElementService(ElementService eltService)
    {
        Node serviceNode = eltService.getServiceNode() ;
        Op sub = compileElement(eltService.getElement()) ;
        return new OpService(serviceNode, sub) ;
    }
    
    /** Compile query modifiers */
    public Op compileModifiers(Query query, Op pattern)
    {
        Op op = pattern ;
        //Modifiers mods = new Modifiers(query) ;
        
        // ---- ToList
        if ( context.isTrue(ARQ.generateToList) )
            // Listify it.
            op = new OpList(op) ;
        
        // ---- GROUP BY
        if ( query.hasGroupBy() )
        {
            // query.getGroupExprs().size() <  query.getGroupVars().size() ;
            // Wrong.
            // 1 - need to pass in expressions for grouping, not vars.
            // 2 - No aggregates yet
            op = new OpGroupAgg(op, query.getGroupBy(), query.getAggregators()) ;
        }
        else
        {
            if ( query.getAggregators().size() > 0 )
                // No GroupBy but there are some aggregates.
                // This is a group of no variables.
                op = new OpGroupAgg(op,  query.getGroupBy(), query.getAggregators()) ;
            // Fold into above when certainit works
        }
        // ---- HAVING
        if ( query.hasHaving() )
        {
            for ( Iterator iter = query.getHavingExprs().iterator() ; iter.hasNext() ; )
            {
                Expr expr = (Expr)iter.next() ;
                op = OpFilter.filter(expr , op) ;    
            }
        }
        
        // ---- ORDER BY
        if ( query.getOrderBy() != null )
            op = new OpOrder(op, query.getOrderBy()) ;
        
        // ---- PROJECT
        // TODO Need to move assignments (expressions, which add variables) to before ORDER BY and HAVING
        
        // (ORDER may involve an unselected variable)
        // No projection => initial variables are exposed.
        // Needed for CONSTRUCT and initial bindings + SELECT *
        
        VarExprList projectVars = query.getProject() ;
        if ( ! projectVars.isEmpty() && ! query.isQueryResultStar())
        {
            // Don't project for QueryResultStar so initial bindings show through
            // in SELECT *
            if ( projectVars.size() == 0 && query.isSelectType() )
                ALog.warn(this,"No project variables") ;
            
            // Separate assignments and variable projection.
            VarExprList exprs = new VarExprList() ;
            List vars = new ArrayList() ;
            for ( Iterator iter = query.getProject().getVars().iterator() ; iter.hasNext(); )
            {
                Var v = (Var)iter.next() ;
                Expr e = query.getProject().getExpr(v) ;
                if ( e != null )
                    exprs.add(v, e) ;
                // Include in project
                vars.add(v) ;
            }
            
            if ( ! exprs.isEmpty() )
                op = new OpAssign(op, exprs) ;
            if ( vars.size() > 0 )
                op = new OpProject(op, vars) ;
        }
        
        // ---- DISTINCT
        if ( query.isDistinct() )
            op = new OpDistinct(op) ;
        
        // ---- REDUCED
        if ( query.isReduced() )
            op = new OpReduced(op) ;
        
        // ---- LIMIT/OFFSET
        if ( query.hasLimit() || query.hasOffset() )
            op = new OpSlice(op, query.getOffset() /*start*/, query.getLimit()/*length*/) ;
        
        return op ;
    }

    // -------- 
    
    protected Op join(Op current, Op newOp)
    { 
        if ( simplifyEarly )
        {
            if ( OpJoin.isJoinIdentify(current) )
                return newOp ;
            if ( OpJoin.isJoinIdentify(newOp) )
                return current ;
        }
        
        return OpJoin.create(current, newOp) ;
    }

    private void broken(String msg)
    {
        System.err.println("AlgebraCompiler: "+msg) ;
        throw new ARQInternalErrorException(msg) ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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