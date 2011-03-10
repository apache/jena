/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Stack ;

import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.ExprAggregator ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.ARQNotImplemented ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.syntax.* ;
import com.hp.hpl.jena.sparql.util.graph.GraphList ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** Convert an Op expression in SPARQL syntax, that is, the reverse of algebra generation */   
public class OpAsQuery
{
    public static Query asQuery(Op op)
    {
        Query query = QueryFactory.make() ;
        
        Converter v = new Converter(query) ;
        //OpWalker.walk(op, v) ;
        op.visit(v) ;
        
        List<Var> vars = v.projectVars;
        query.setQueryResultStar(vars.isEmpty()); // SELECT * unless we are projecting
        Iterator<Var> iter = vars.iterator();
        for (; iter.hasNext();) {
            Var var = iter.next();
            if (v.varExpression.containsKey(var))
                query.addResultVar(var, v.varExpression.get(var));
            else
                query.addResultVar(var);
        }
        
        ElementGroup eg = v.currentGroup ;
        query.setQueryPattern(eg) ;
        query.setQuerySelectType() ;
        
        query.setResultVars() ; 
        return query ; 
    }
    
    public static class Converter implements OpVisitor
    {
        private Query query ;
        private Element element = null ;
        private ElementGroup currentGroup = null ;
        private Stack<ElementGroup> stack = new Stack<ElementGroup>() ;
        private List<Var> projectVars = Collections.emptyList() ;
        private Map<Var, Expr> varExpression = new HashMap<Var, Expr>() ;
        
        public Converter(Query query)
        {
            this.query = query ;
            currentGroup = new ElementGroup() ;
        }

        Element asElement(Op op)
        {
            ElementGroup g = asElementGroup(op) ;
            if ( g.getElements().size() == 1 )
                return g.getElements().get(0) ;
            return g ;
        }
        
        ElementGroup asElementGroup(Op op)
        {
            startSubGroup() ;
            op.visit(this) ;
            return endSubGroup() ;
        }

        public void visit(OpBGP opBGP)
        {
            currentGroup().addElement(process(opBGP.getPattern())) ;
        }

//        public void visit(OpPropFunc opPropFunc)
//        {
//            OpBGP opBGP = opPropFunc.getBGP() ;
//            currentGroup().addElement(process(opBGP.getPattern())) ;
//        }
        
        public void visit(OpTriple opTriple)
        { currentGroup().addElement(process(opTriple.getTriple())) ; }

        public void visit(OpProcedure opProcedure)
        {
            throw new ARQNotImplemented("OpProcedure") ;
        }
        
        public void visit(OpPropFunc opPropFunc)
        {
            Node s = processPropFuncArg(opPropFunc.getSubjectArgs()) ;
            Node o = processPropFuncArg(opPropFunc.getObjectArgs()) ;
            Triple t = new Triple(s, opPropFunc.getProperty(), o) ;
            currentGroup().addElement(process(t)) ;
        }
        
        private Node processPropFuncArg(PropFuncArg args)
        {
            if ( args.isNode() )
                return args.getArg() ;

            // List ...
            List<Node> list = args.getArgList() ;
            if ( list.size() == 0 )
                return RDF.Nodes.nil ;
            BasicPattern bgp = new BasicPattern() ;
            Node head = GraphList.listToTriples(list, bgp) ;
            currentGroup().addElement(process(bgp)) ;
            return head ;
        }
        
        public void visit(OpSequence opSequence)
        {
            ElementGroup g = currentGroup() ;
            boolean nestGroup = ! g.isEmpty() ;
            if ( nestGroup )
            {
                startSubGroup() ;
                g = currentGroup() ;
            }
            
            Iterator<Op> iter = opSequence.iterator() ;
            
            for ( ; iter.hasNext() ; )
            {
                Op op = iter.next() ;
                Element e = asElement(op) ;
                g.addElement(e) ;
            }
            if ( nestGroup )
                endSubGroup() ;
            return ;
        }
        
        public void visit(OpDisjunction opDisjunction)
        {
            throw new ARQNotImplemented("OpDisjunction") ;
        }

        private Element process(BasicPattern pattern)
        {
            // The different SPARQL versions use different internal structures for BGPs.
            if ( query.getSyntax() == Syntax.syntaxSPARQL_10 )
            {
                ElementTriplesBlock e = new ElementTriplesBlock() ;
                for (Triple t : pattern)
                    // Leave bNode variables as they are
                    // Query serialization will deal with them. 
                    e.addTriple(t) ;
                return e ;
            }
            
            if ( query.getSyntax() == Syntax.syntaxSPARQL_11 ||
                 query.getSyntax() == Syntax.syntaxARQ )
            {
                ElementPathBlock e = new ElementPathBlock() ;
                for (Triple t : pattern)
                    // Leave bNode variables as they are
                    // Query serialization will deal with them. 
                    e.addTriple(t) ;
                return e ;
            }
            
            throw new ARQInternalErrorException("Unrecognized syntax: "+query.getSyntax()) ;
            
        }
        
        private ElementTriplesBlock process(Triple triple)
        {
            // Unsubtle
            ElementTriplesBlock e = new ElementTriplesBlock() ;
            e.addTriple(triple) ;
            return e ;
        }
        
        public void visit(OpQuadPattern quadPattern)
        { throw new ARQNotImplemented("OpQuadPattern") ; }

        public void visit(OpPath opPath)
        { throw new ARQNotImplemented("OpPath") ; }

        public void visit(OpJoin opJoin)
        {
            // Keep things clearly separated.
            Element eLeft = asElement(opJoin.getLeft()) ;
            Element eRight = asElementGroup(opJoin.getRight()) ;
            
            ElementGroup g = currentGroup() ;
            g.addElement(eLeft) ;
            g.addElement(eRight) ;
            return ;
        }

        private static boolean emptyGroup(Element element)
        {
            if ( ! ( element instanceof ElementGroup ) )
                return false ;
            ElementGroup eg = (ElementGroup)element ;
            return eg.isEmpty() ;
        }
        
        public void visit(OpLeftJoin opLeftJoin)
        {
            Element eLeft = asElement(opLeftJoin.getLeft()) ;
            ElementGroup eRight = asElementGroup(opLeftJoin.getRight()) ;
            
            if ( opLeftJoin.getExprs() != null )
            {
                for ( Expr expr : opLeftJoin.getExprs() )
                {
                    ElementFilter f = new ElementFilter(expr) ;
                    eRight.addElement(f) ;
                }
            }
            ElementGroup g = currentGroup() ;
            if ( ! emptyGroup(eLeft) )
                g.addElement(eLeft) ;
            ElementOptional opt = new ElementOptional(eRight) ;
            g.addElement(opt) ;
        }

        public void visit(OpDiff opDiff)
        { throw new ARQNotImplemented("OpDiff") ; }

        public void visit(OpMinus opMinus)
        { throw new ARQNotImplemented("OpMinus") ; }

        public void visit(OpUnion opUnion)
        {
            Element eLeft = asElementGroup(opUnion.getLeft()) ;
            Element eRight = asElementGroup(opUnion.getRight()) ;
            if ( eLeft instanceof ElementUnion )
            {
                ElementUnion elUnion = (ElementUnion)eLeft ;
                elUnion.addElement(eRight) ;
                return ;
            }
            
//            if ( eRight instanceof ElementUnion )
//            {
//                ElementUnion elUnion = (ElementUnion)eRight ;
//                elUnion.getElements().add(0, eLeft) ;
//                return ;
//            }
            
            ElementUnion elUnion = new ElementUnion() ;
            elUnion.addElement(eLeft) ;
            elUnion.addElement(eRight) ;
            currentGroup().addElement(elUnion) ;
        }

        public void visit(OpConditional opCondition)
        { throw new ARQNotImplemented("OpCondition") ; }

        public void visit(OpFilter opFilter)
        {
            // (filter .. (filter ( ... ))   (non-canonicalizing OpFilters)
            // Inner gets Grouped unnecessarily. 
            Element e = asElement(opFilter.getSubOp()) ;
            if ( currentGroup() != e )
                currentGroup().addElement(e) ;
            element = currentGroup() ;      // Was cleared by asElement. 
            
            ExprList exprs = opFilter.getExprs() ;
            for ( Expr expr : exprs )
            {
                ElementFilter f = new ElementFilter(expr) ;
                currentGroup().addElement(f) ;
            }
        }

        public void visit(OpGraph opGraph)
        {
            startSubGroup() ;
            Element e = asElement(opGraph.getSubOp()) ;
            ElementGroup g = endSubGroup() ;
            
            Element graphElt = new ElementNamedGraph(opGraph.getNode(), e) ;
            currentGroup().addElement(graphElt) ;
        }

        public void visit(OpService opService)
        { 
            // Hmm - if the subnode has been optimized, we may fail.
            Op op = opService.getSubOp() ;
            Element x = asElement(opService.getSubOp()) ; 
            Element elt = new ElementService(opService.getService(), x, opService.getSilent()) ;
            currentGroup().addElement(elt) ;
        }
        
        public void visit(OpDatasetNames dsNames)
        { throw new ARQNotImplemented("OpDatasetNames") ; }

        public void visit(OpTable opTable)
        { 
            // This will go in a group so simply forget it. 
            if ( opTable.isJoinIdentity() ) return ;
            throw new ARQNotImplemented("OpTable") ;
        }

        public void visit(OpExt opExt)
        {
//            Op op = opExt.effectiveOp() ;
//            // This does not work in all cases.
//            op.visit(this) ;
            throw new ARQNotImplemented("OpExt") ;
        }

        public void visit(OpNull opNull)
        { throw new ARQNotImplemented("OpNull") ; }

        public void visit(OpLabel opLabel)
        {
            if ( opLabel.hasSubOp() )
                opLabel.getSubOp().visit(this) ;
        }

        public void visit(OpAssign opAssign)
        {
	        /**
             * Special case: group involves and internal assignment
             * e.g.  (assign ((?.1 ?.0)) (group () ((?.0 (count)))
             * We attempt to intercept that here.
             */
            if (opAssign.getSubOp() instanceof OpGroup) {
                Map<Var, Var> subs = new HashMap<Var, Var>();
                Expr exp;
                for (Var v: opAssign.getVarExprList().getVars()) {
                    exp = opAssign.getVarExprList().getExpr(v);
                    if (exp.isVariable()) subs.put(exp.asVar(), v);
                    else throw new ARQNotImplemented("Expected simple assignment for group");
                }
                visit((OpGroup) opAssign.getSubOp(), subs);
                return;
            }

            opAssign.getSubOp().visit(this) ;
            for ( Var v : opAssign.getVarExprList().getVars() )
            {
                Element elt = new ElementAssign(v, opAssign.getVarExprList().getExpr(v)) ;
                ElementGroup g = currentGroup() ;
                g.addElement(elt) ;
            }
        }

        public void visit(OpExtend opExtend)
        { 
            /**
             * Special case: group involves and internal assignment
             * e.g.  (assign ((?.1 ?.0)) (group () ((?.0 (count)))
             * We attempt to intercept that here.
             */
            if (opExtend.getSubOp() instanceof OpGroup) {
                Map<Var, Var> subs = new HashMap<Var, Var>();
                Expr exp;
                for (Var v: opExtend.getVarExprList().getVars()) {
                    exp = opExtend.getVarExprList().getExpr(v);
                    if (exp.isVariable()) subs.put(exp.asVar(), v);
                    else throw new ARQNotImplemented("Expected simple assignment for group");
                }
                visit((OpGroup) opExtend.getSubOp(), subs);
                return;
            }

            opExtend.getSubOp().visit(this) ;
            for ( Var v : opExtend.getVarExprList().getVars() )
            {
                Element elt = new ElementAssign(v, opExtend.getVarExprList().getExpr(v)) ;
                ElementGroup g = currentGroup() ;
                g.addElement(elt) ;
            }
        }

        
        public void visit(OpList opList)
        { /* No action */ }

        public void visit(OpOrder opOrder)
        {
            List<SortCondition> x = opOrder.getConditions() ;
            for ( SortCondition sc : x )
                query.addOrderBy(sc);
            opOrder.getSubOp().visit(this) ;
        }

        public void visit(OpProject opProject)
        {
            // Defer adding result vars until the end.
            // OpGroup generates dupes otherwise
            this.projectVars = opProject.getVars();
            opProject.getSubOp().visit(this) ;
        }

        public void visit(OpReduced opReduced)
        { 
            query.setReduced(true) ;
            opReduced.getSubOp().visit(this) ;
        }

        public void visit(OpDistinct opDistinct)
        { 
            query.setDistinct(true) ;
            opDistinct.getSubOp().visit(this) ;
        }

        public void visit(OpSlice opSlice)
        {
            if ( opSlice.getStart() != Query.NOLIMIT )
                query.setOffset(opSlice.getStart()) ;
            if ( opSlice.getLength() != Query.NOLIMIT )
                query.setLimit(opSlice.getLength()) ;
            opSlice.getSubOp().visit(this) ;
        }

        @SuppressWarnings("unchecked")
        public void visit(OpGroup opGroup) {
            visit(opGroup, Collections.EMPTY_MAP);
        }

        // Specialised to cope with the preceeding assigns
        public void visit(OpGroup opGroup, Map<Var, Var> subs) {            
            List<ExprAggregator> a = opGroup.getAggregators();

            for (ExprAggregator ea : a) {
                // Substitute generated var for actual
                Var givenVar = ea.getAggVar().asVar();
                Var realVar = (subs.containsKey(givenVar))
                ? subs.get(givenVar)
                : givenVar;
                // Copy aggregator across (?)
                Expr myAggr = query.allocAggregate(ea.getAggregator());
                varExpression.put(realVar, myAggr);
                //query.addResultVar(realVar, myAggr);
            }

            VarExprList b = opGroup.getGroupVars();
            for (Var v : b.getVars()) {
                Expr e = b.getExpr(v);

                if (e != null) {
                    query.addGroupBy(v, e);

                } else {
                    query.addGroupBy(v);

                }
            }
            opGroup.getSubOp().visit(this);
        }

        public void visit(OpTopN opTop)
        { throw new ARQNotImplemented("OpTopN") ; }
        
        private Element lastElement()
        {
            ElementGroup g = currentGroup ;
            if ( g == null || g.getElements().size() == 0 )
                return null ;
            int len = g.getElements().size() ;
            return g.getElements().get(len-1) ;
        }

        private void startSubGroup()
        {
            push(currentGroup) ;
            ElementGroup g = new ElementGroup() ;
            currentGroup = g ;
        }
        
        private ElementGroup endSubGroup()
        {
            ElementGroup g = pop() ;
            ElementGroup r = currentGroup ;
            currentGroup = g ;
            return r ;
        }
        
//        private void endCurrentGroup()
//        {
//            currentGroup = null ;
//            element = null ; //??
//        }
        
        private ElementGroup currentGroup()
        {
//            if ( currentGroup == null )
//                startSubGroup() ;
            return currentGroup ;
        }
        
        private ElementGroup peek()
        {
            if ( stack.size() == 0 )
                return null ;
            return stack.peek();
        }
        private ElementGroup pop() { return stack.pop(); }
        private void push(ElementGroup el) { stack.push(el); }
    }
}

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