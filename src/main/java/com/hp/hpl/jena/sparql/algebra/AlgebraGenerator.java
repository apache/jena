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

package com.hp.hpl.jena.sparql.algebra;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel ;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpList ;
import com.hp.hpl.jena.sparql.algebra.op.OpMinus ;
import com.hp.hpl.jena.sparql.algebra.op.OpNull ;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced ;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence ;
import com.hp.hpl.jena.sparql.algebra.op.OpService ;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformSimplify ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.PathBlock ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.E_Exists ;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprLib ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.path.PathCompiler ;
import com.hp.hpl.jena.sparql.path.PathLib ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.syntax.ElementAssign ;
import com.hp.hpl.jena.sparql.syntax.ElementBind ;
import com.hp.hpl.jena.sparql.syntax.ElementExists ;
import com.hp.hpl.jena.sparql.syntax.ElementFetch ;
import com.hp.hpl.jena.sparql.syntax.ElementFilter ;
import com.hp.hpl.jena.sparql.syntax.ElementGroup ;
import com.hp.hpl.jena.sparql.syntax.ElementMinus ;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph ;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists ;
import com.hp.hpl.jena.sparql.syntax.ElementOptional ;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock ;
import com.hp.hpl.jena.sparql.syntax.ElementService ;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery ;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock ;
import com.hp.hpl.jena.sparql.syntax.ElementUnion ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class AlgebraGenerator 
{
    // Fixed filter position means leave exactly where it is syntactically (illegal SPARQL)
    // Helpful only to write exactly what you mean and test the full query compiler.
    private boolean fixedFilterPosition = false ;
    private Context context ;
    private PathCompiler pathCompiler = new PathCompiler() ;
    private int subQueryDepth = 0 ;
    
    // simplifyInAlgebraGeneration=true is the alternative reading of
    // the DAWG Algebra translation algorithm. 

    // If we simplify during algebra generation, it changes the SPARQL for OPTIONAL {{ FILTER }}
    // The  {{}} results in (join unit (filter ...)) the filter is not moved
    // into the LeftJoin.  
    
    static final private boolean applySimplification = true ;              // Allows raw algebra to be generated (testing) 
    static final private boolean simplifyTooEarlyInAlgebraGeneration = false ;   // False is the correct setting. 

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
        Op op = compile(query.getQueryPattern()) ;     // Not compileElement - may need to apply simplification.
        
        op = compileModifiers(query, op) ;
        return op ;
    }
    
    protected static Transform simplify = new TransformSimplify() ;
    // Compile any structural element
    public Op compile(Element elt)
    {
        Op op = compileElement(elt) ;
        Op op2 = op ;
        if ( ! simplifyTooEarlyInAlgebraGeneration && applySimplification && simplify != null )
            op2 = simplify(op) ;
        return op2;
    }
    
    private static Op simplify(Op op)
    {
        return Transformer.transform(simplify, op) ;
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
        
        if ( elt instanceof ElementFetch )
            return compileElementFetch((ElementFetch)elt) ; 

        // This is only here for queries built programmatically
        // (triple patterns not in a group) 
        if ( elt instanceof ElementTriplesBlock )
            return compileBasicPattern(((ElementTriplesBlock)elt).getPattern()) ;
        
        // Ditto.
        if ( elt instanceof ElementPathBlock )
            return compilePathBlock(((ElementPathBlock)elt).getPattern()) ;

        if ( elt instanceof ElementSubQuery )
            return compileElementSubquery((ElementSubQuery)elt) ; 
        
        if ( elt == null )
            return OpNull.create() ;

        broken("compile(Element)/Not a structural element: "+Utils.className(elt)) ;
        return null ;
        
    }
    
    protected Op compileElementUnion(ElementUnion el)
    { 
//        if ( el.getElements().size() == 1 )
//        {
//            // SPARQL 1.0 but never happens in a legal syntax query.
//            Element subElt = el.getElements().get(0) ;
//            return compileElement(subElt) ;
//        }
        
        Op current = null ;
        
        for ( Element subElt: el.getElements() )
        {
            Op op = compileElement(subElt) ;
            current = union(current, op) ;
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
    // Simplification: Done later 
    // If simplicifation is done now, it changes OPTIONAL { { ?x :p ?w . FILTER(?w>23) } } because it removes the
    //   (join Z (filter...)) that in turn stops the filter getting moved into the LeftJoin.  
    //   It need a depth of 2 or more {{ }} for this to happen. 

    protected Op compileElementGroup(ElementGroup groupElt)
    {
        Op current = OpTable.unit() ;
        
        // First: get all filters, merge adjacent BGPs. This includes BGP-FILTER-BGP
        // This is done in finalizeSyntax after which the new ElementGroup is in
        // the right order w.r.t. BGPs and filters. 
        // 
        // This is a delay from parsing time so a printed query
        // keeps filters where the query author put them.
        
        List<Element> groupElts = finalizeSyntax(groupElt) ;

        // Second: compile the consolidated group elements.
        // Asumes that filters moved to end.
        for (Iterator<Element> iter = groupElts.listIterator() ; iter.hasNext() ; )
        {
            Element elt = iter.next() ;
            current = compileOneInGroup(elt, current) ;
        }
            
        return current ;
    }

    /* Extract filters, merge adjacent BGPs.
     * Return a list of elements: update the exprList
     */
    
    private List<Element> finalizeSyntax(ElementGroup groupElt)
    {
        if ( fixedFilterPosition )
            // Illegal SPARQL
            return groupElt.getElements() ;
        
        List<Element> groupElts = new ArrayList<Element>() ;
        BasicPattern prev = null ;
        List<ElementFilter> filters = null ;
        PathBlock prev2 = null ;
        
        for (Element elt : groupElt.getElements() )
        {
            if ( elt instanceof ElementFilter )
            {
                ElementFilter f = (ElementFilter)elt ;
                if ( filters == null )
                    filters = new ArrayList<ElementFilter>() ;
                filters.add(f) ;
                // Collect filters but do not place them yet.
                continue ;
            }
            
            if ( elt instanceof ElementTriplesBlock )
            {
                if ( prev2 != null )
                    throw new ARQInternalErrorException("Mixed ElementTriplesBlock and ElementPathBlock (case 1)") ;
                
                ElementTriplesBlock etb = (ElementTriplesBlock)elt ;

                if ( prev != null )
                {
                    // Previous was an ElementTriplesBlock.
                    // Merge because they were adjacent in a group
                    // in syntax, so it must have been BGP, Filter, BGP.
                    // Or someone constructed a non-serializable query. 
                    prev.addAll(etb.getPattern()) ;
                    continue ;
                }
                // New BGP.
                // Copy - so that any later mergings do not change the original query. 

                ElementTriplesBlock etb2 = new ElementTriplesBlock() ;
                etb2.getPattern().addAll(etb.getPattern()) ;
                prev = etb2.getPattern() ;
                groupElts.add(etb2) ;
                continue ;
            }
            
            // TIDY UP - grr this is duplication.
            // Can't mix ElementTriplesBlock and ElementPathBlock (which subsumes ElementTriplesBlock)
            if ( elt instanceof ElementPathBlock )
            {
                if ( prev != null )
                    throw new ARQInternalErrorException("Mixed ElementTriplesBlock and ElementPathBlock (case 2)") ;
                
                ElementPathBlock epb = (ElementPathBlock)elt ;
                if ( prev2 != null )
                {
                    prev2.addAll(epb.getPattern()) ;
                    continue ;
                }
                
                ElementPathBlock epb2 = new ElementPathBlock() ;
                epb2.getPattern().addAll(epb.getPattern()) ;
                prev2 = epb2.getPattern() ;
                groupElts.add(epb2) ;
                continue ;
            }
            
            // Anything else.  End of BGP - put in any accumulated filters 

            //[Old:BGP-scoped filter]
//            endBGP(groupElts, filters) ;
//            // Clear any BGP-related accumulators.
//            filters = null ;
            
            // Clear any BGP-related triple accumulators.
            prev = null ;
            prev2 = null ;
            
            // Add this element (not BGP/Filter related).
            groupElts.add(elt) ;
        }
        //End of group - put in any accumulated filters
        if ( filters != null )
            groupElts.addAll(filters) ;
        return groupElts ;
    }
    
// [Old:BGP-scoped filter]
//    private void endBGP(List<Element> groupElts, List<ElementFilter> filters)
//    {
//        if ( filters != null )
//            groupElts.addAll(filters) ;
//    }
    
    private Op compileOneInGroup(Element elt, Op current)
    {
        // Replace triple patterns by OpBGP (i.e. SPARQL translation step 1)
        if ( elt instanceof ElementTriplesBlock )
        {
            ElementTriplesBlock etb = (ElementTriplesBlock)elt ;
            Op op = compileBasicPattern(etb.getPattern()) ;
            return join(current, op) ;
        }
        
        if ( elt instanceof ElementPathBlock )
        {
            ElementPathBlock epb = (ElementPathBlock)elt ;
            Op op = compilePathBlock(epb.getPattern()) ;
            return join(current, op) ;
        }
        
        // Filters were collected together by finalizeSyntax.
        // So they are in the right place.
        if ( elt instanceof ElementFilter )
        {
            ElementFilter f = (ElementFilter)elt ;
            return OpFilter.filter(f.getExpr(), current) ;
        }
    
        if ( elt instanceof ElementOptional )
        {
            ElementOptional eltOpt = (ElementOptional)elt ;
            return compileElementOptional(eltOpt, current) ;
        }
        
        if ( elt instanceof ElementSubQuery )
        {
            ElementSubQuery elQuery = (ElementSubQuery)elt ;
            Op op = compileElementSubquery(elQuery) ;
            return join(current, op) ;
        }
        
        if ( elt instanceof ElementAssign )
        {
            ElementAssign assign = (ElementAssign)elt ;
            Op op = OpAssign.assign(current, assign.getVar(), assign.getExpr()) ;
            return op ;
        }
        
        if ( elt instanceof ElementBind )
        {
            ElementBind bind = (ElementBind)elt ;
            Op op = OpExtend.extend(current, bind.getVar(), bind.getExpr()) ;
            return op ;
        }
        
        if ( elt instanceof ElementExists )
        {
            ElementExists elt2 = (ElementExists)elt ;
            Op op = compileElementExists(current, elt2) ;
            return op ;
        }
        
        if ( elt instanceof ElementNotExists )
        {
            ElementNotExists elt2 = (ElementNotExists)elt ;
            Op op = compileElementNotExists(current, elt2) ;
            return op ;
        }
        
        if ( elt instanceof ElementMinus )
        {
            ElementMinus elt2 = (ElementMinus)elt ;
            Op op = compileElementMinus(current, elt2) ;
            return op ;
        }
        
//        // SPARQL 1.1 UNION -- did no tmake SPARQL 
//        if ( elt instanceof ElementUnion )
//        {
//            ElementUnion elt2 = (ElementUnion)elt ;
//            if ( elt2.getElements().size() == 1 )
//            {
//                Op op = compileElementUnion(current, elt2) ;
//                return op ;
//            }
//        }
        
        // All other elements: compile the element and then join on to the current group expression.
        if ( elt instanceof ElementGroup || 
             elt instanceof ElementNamedGraph ||
             elt instanceof ElementService ||
             elt instanceof ElementFetch ||
             elt instanceof ElementUnion )
        {
            Op op = compileElement(elt) ;
            return join(current, op) ;
        }
        
        broken("compile/Element not recognized: "+Utils.className(elt)) ;
        return null ;
    }

    private Op compileElementNotExists(Op current, ElementNotExists elt2)
    {
        Op op = compile(elt2.getElement()) ;    // "compile", not "compileElement" -- do simpliifcation  
        Expr expr = new E_Exists(elt2, op) ;
        expr = new E_LogicalNot(expr) ;
        return OpFilter.filter(expr, current) ;
    }

    private Op compileElementExists(Op current, ElementExists elt2)
    {
        Op op = compile(elt2.getElement()) ;    // "compile", not "compileElement" -- do simpliifcation 
        Expr expr = new E_Exists(elt2, op) ;
        return OpFilter.filter(expr, current) ;
    }

    private Op compileElementMinus(Op current, ElementMinus elt2)
    {
        Op op = compile(elt2.getMinusElement()) ;
        Op opMinus = OpMinus.create(current, op) ;
        return opMinus ;
    }

    private Op compileElementUnion(Op current, ElementUnion elt2)
    {
        // Special SPARQL 1.1 case.
        Op op = compile(elt2.getElements().get(0)) ;
        Op opUnion = OpUnion.create(current, op) ;
        return opUnion ;
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
    
    protected Op compileBasicPattern(BasicPattern pattern)
    {
        return new OpBGP(pattern) ;
    }
    
    protected Op compilePathBlock(PathBlock pathBlock)
    {
        // Empty path block : the parser does not generate this case.
        if ( pathBlock.size() == 0 )
            return OpTable.unit() ;

        // Always turns the most basic paths to triples.
        return PathLib.pathToTriples(pathBlock) ;
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
        return new OpService(serviceNode, sub, eltService, eltService.getSilent()) ;
    }
    
    private Op compileElementFetch(ElementFetch elt)
    {
        Node serviceNode = elt.getFetchNode() ;
        
        // Probe to see if enabled.
        OpExtBuilder builder = OpExtRegistry.builder("fetch") ;
        if ( builder == null )
        {
            Log.warn(this, "Attempt to use OpFetch - need to enable first with a call to OpFetch.enable()") ; 
            return OpLabel.create("fetch/"+serviceNode, OpTable.unit()) ;
        }
        Item item = Item.createNode(elt.getFetchNode()) ;
        ItemList args = new ItemList() ;
        args.add(item) ;
        return builder.make(args) ;
    }

    protected Op compileElementSubquery(ElementSubQuery eltSubQuery)
    {
        subQueryDepth++ ;
        Op sub = this.compile(eltSubQuery.getQuery()) ;
        subQueryDepth-- ;
        return sub ;
    }
    
    /** Compile query modifiers */
    private Op compileModifiers(Query query, Op pattern)
    {
         /* The modifier order in algebra is:
          * 
          * Limit/Offset
          *   Distinct/reduce
          *     project
          *       OrderBy
          *         Bindings
          *           having
          *             select expressions
          *               group
          */
        
        // Preparation: sort SELECT clause into assignments and projects.
        VarExprList projectVars = query.getProject() ;
        
        VarExprList exprs = new VarExprList() ;     // Assignments to be done.
        List<Var> vars = new ArrayList<Var>() ;     // projection variables
        
        Op op = pattern ;
        
        // ---- GROUP BY
        
        if ( query.hasGroupBy() )
        {
            // When there is no GroupBy but there are some aggregates, it's a group of no variables.
            op = new OpGroup(op, query.getGroupBy(), query.getAggregators()) ;
        }
        
        //---- Assignments from SELECT and other places (so available to ORDER and HAVING)
        // Now do assignments from expressions 
        // Must be after "group by" has introduced it's variables.
        
        // Look for assignments in SELECT expressions.
        if ( ! projectVars.isEmpty() && ! query.isQueryResultStar())
        {
            // Don't project for QueryResultStar so initial bindings show
            // through in SELECT *
            if ( projectVars.size() == 0 && query.isSelectType() )
                Log.warn(this,"No project variables") ;
            // Separate assignments and variable projection.
            for ( Var v : query.getProject().getVars() )
            {
                Expr e = query.getProject().getExpr(v) ;
                if ( e != null )
                {
                    Expr e2 = ExprLib.replaceAggregateByVariable(e) ;
                    exprs.add(v, e2) ;
                }
                // Include in project
                vars.add(v) ;
            }
        }
        
        // ---- Assignments from SELECT and other places (so available to ORDER and HAVING)
        if ( ! exprs.isEmpty() )
            // Potential rewrites based of assign introducing aliases.
            op = OpExtend.extend(op, exprs) ;

        // ---- HAVING
        if ( query.hasHaving() )
        {
            for (Expr expr : query.getHavingExprs())
            {
                // HAVING expression to refer to the aggregate via the variable.
                Expr expr2 = ExprLib.replaceAggregateByVariable(expr) ; 
                op = OpFilter.filter(expr2 , op) ;
            }
        }
        // ---- BINDINGS
        if ( query.hasBindings() )
        {
            Table table = TableFactory.create() ;
            for ( Binding binding : query.getBindingValues() )
                table.addBinding(binding) ;
            OpTable opTable = OpTable.create(table) ;
            op = OpJoin.create(op, opTable) ;
        }
        
        // ---- ToList
        if ( context.isTrue(ARQ.generateToList) )
            // Listify it.
            op = new OpList(op) ;
        
        // ---- ORDER BY
        if ( query.getOrderBy() != null )
        {
            List<SortCondition> scList = new ArrayList<SortCondition>() ;

            // Aggregates in ORDER BY
            for ( SortCondition sc : query.getOrderBy() )
            {
                Expr e = sc.getExpression() ;
                e = ExprLib.replaceAggregateByVariable(e) ;
                scList.add(new SortCondition(e, sc.getDirection())) ;
                
            }
            op = new OpOrder(op, scList) ;
        }
        
        // ---- PROJECT
        // No projection => initial variables are exposed.
        // Needed for CONSTRUCT and initial bindings + SELECT *
        
        if ( vars.size() > 0 )
            op = new OpProject(op, vars) ;
        
        // ---- DISTINCT
        if ( query.isDistinct() )
            op = OpDistinct.create(op) ;
        
        // ---- REDUCED
        if ( query.isReduced() )
            op = OpReduced.create(op) ;
        
        // ---- LIMIT/OFFSET
        if ( query.hasLimit() || query.hasOffset() )
            op = new OpSlice(op, query.getOffset() /*start*/, query.getLimit()/*length*/) ;
        
        return op ;
    }

    // -------- 
    
    protected Op join(Op current, Op newOp)
    { 
//        if ( current instanceof OpBGP && newOp instanceof OpBGP )
//        {
//            OpBGP opBGP = (OpBGP)current ;
//            opBGP.getPattern().addAll( ((OpBGP)newOp).getPattern() ) ;
//            return current ;
//        }
        
        if ( simplifyTooEarlyInAlgebraGeneration && applySimplification )
        {
            if ( OpJoin.isJoinIdentify(current) )
                return newOp ;
            if ( OpJoin.isJoinIdentify(newOp) )
                return current ;
        }
        
        return OpJoin.create(current, newOp) ;
    }

    protected Op sequence(Op current, Op newOp)
    {
        return OpSequence.create(current, newOp) ;
    }
    
    protected Op union(Op current, Op newOp)
    {
        return OpUnion.create(current, newOp) ;
    }
    
    private void broken(String msg)
    {
        //System.err.println("AlgebraGenerator: "+msg) ;
        throw new ARQInternalErrorException(msg) ;
    }
}
