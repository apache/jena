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

import java.util.* ;

import org.apache.jena.atlas.lib.NotImplemented ;

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
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.expr.* ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.syntax.* ;
import com.hp.hpl.jena.sparql.util.graph.GraphList ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** Convert an Op expression in SPARQL syntax, that is, the reverse of algebra generation */   
public class OpAsQuery
{
    public static Query asQuery(Op op)
    {        
        Converter converter = new Converter(op) ;
        return converter.convert();
    }
    
    private static Set<Var> allocProjectVars()
    {
        return new LinkedHashSet<>() ;
    }

    private static void addProjectVar(Collection<Var> vars, Var var)
    {
        // Must add uniquely.  Couple to allocProjectVars
        //if (!vars.contains(var)) 
        vars.add(var) ;
    }
    
    public static class Converter implements OpVisitor
    {
        private Query query ;
        private Op op ;
        private Element element = null ;
        private ElementGroup currentGroup = null ;
        private Deque<ElementGroup> stack = new ArrayDeque<>() ;
        private Collection<Var> projectVars = allocProjectVars();
        private Map<Var, Expr> varExpression = new HashMap<>() ;
        private int groupDepth = 0;
        private boolean inProject = false;
        private boolean hasRun = false;
        
        public Converter(Op op)
        {
            this.query = QueryFactory.create() ;
            this.op = op;
            currentGroup = new ElementGroup() ;
        }
        
        Query convert() {
            if (hasRun) {
                return this.query;
            } else {
                try {
                    op.visit(this) ;
                    
                    Collection<Var> vars = this.projectVars;
                    query.setQueryResultStar(vars.isEmpty());   // SELECT * unless we are projecting
        
                    Iterator<Var> iter = vars.iterator();
                    for (; iter.hasNext();) {
                        Var var = iter.next();           
                        if (this.varExpression.containsKey(var))
                            query.addResultVar(var, this.varExpression.get(var));
                        else
                            query.addResultVar(var);
                    }
                    
                    ElementGroup eg = this.currentGroup ;                   
                    query.setQueryPattern(eg) ;
                    query.setQuerySelectType() ;
                    query.setResultVars() ;                     // Variables from the group.
                    return query ; 
                } finally {
                    this.hasRun = true;
                }
            }
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

        @Override
        public void visit(OpBGP opBGP)
        {
            currentGroup().addElement(process(opBGP.getPattern())) ;
        }

//        public void visit(OpPropFunc opPropFunc)
//        {
//            OpBGP opBGP = opPropFunc.getBGP() ;
//            currentGroup().addElement(process(opBGP.getPattern())) ;
//        }
        
        @Override
        public void visit(OpTriple opTriple)
        { currentGroup().addElement(process(opTriple.getTriple())) ; }

        @Override
        public void visit(OpQuad opQuad)
        { throw new ARQNotImplemented("OpQuad") ; }


        @Override
        public void visit(OpProcedure opProcedure)
        {
            throw new ARQNotImplemented("OpProcedure") ;
        }
        
        @Override
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
        
        // There is one special case to consider:
        // A path expression was expaned into a OpSequence during Algenra generation.
        // The simple path expressions become an OpSequence that could be recombined
        // into on ElementPathBlock 
        
        @Override
        public void visit(OpSequence opSequence)
        {
            ElementGroup g = currentGroup() ;
            boolean nestGroup = ! g.isEmpty() ;
            if ( nestGroup )
            {
                startSubGroup() ;
                g = currentGroup() ;
            }
            
            for ( Op op : opSequence.getElements() )
            {
                Element e = asElement(op) ;
                g.addElement(e) ;
            }
            
            if ( nestGroup )
                endSubGroup() ;
            return ;
        }
        
        @Override
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
        
        @Override
        public void visit(OpQuadPattern quadPattern)
        {
            Node graphNode = quadPattern.getGraphNode();
            if (graphNode.equals(Quad.defaultGraphNodeGenerated)) {
                currentGroup().addElement(process(quadPattern.getBasicPattern())) ;
            } else {
                startSubGroup();
                Element e = asElement(new OpBGP(quadPattern.getBasicPattern())) ;
                endSubGroup();
                
                //If not element group make it one
                if (!(e instanceof ElementGroup)) {
                    ElementGroup g = new ElementGroup();
                    g.addElement(e);
                    e = g;
                }
                
                Element graphElt = new ElementNamedGraph(graphNode, e) ;
                currentGroup().addElement(graphElt) ;
            }
        }

        @Override
        public void visit(OpQuadBlock quadBlock)
        {
            // Gather into OpQuadPatterns.
            throw new NotImplemented("OpQuadBlock") ;
        }

        @Override
        public void visit(OpPath opPath)
        {
            ElementPathBlock epb = new ElementPathBlock() ;
            epb.addTriplePath(opPath.getTriplePath()) ;
            ElementGroup g = currentGroup() ;
            g.addElement(epb) ;
        }

        @Override
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
        
        @Override
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

        @Override
        public void visit(OpDiff opDiff)
        { throw new ARQNotImplemented("OpDiff") ; }

        @Override
        public void visit(OpMinus opMinus) {
            Element eLeft = asElement(opMinus.getLeft()) ;
            Element eRight = asElementGroup(opMinus.getRight()) ;
            ElementMinus elMinus = new ElementMinus(eRight) ;
            ElementGroup g = currentGroup() ;
            if ( ! emptyGroup(eLeft) )
                g.addElement(eLeft) ;
            g.addElement(elMinus) ;
        }

        @Override
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

        @Override
        public void visit(OpConditional opCondition)
        { throw new ARQNotImplemented("OpCondition") ; }

        @Override
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

        @Override
        public void visit(OpGraph opGraph)
        {
            startSubGroup() ;
            Element e = asElement(opGraph.getSubOp()) ;
            endSubGroup() ;
            
            //If not element group make it one
            if (!(e instanceof ElementGroup)) {
                ElementGroup g = new ElementGroup();
                g.addElement(e);
                e = g;
            }
            
            Element graphElt = new ElementNamedGraph(opGraph.getNode(), e) ;
            currentGroup().addElement(graphElt) ;
        }

        @Override
        public void visit(OpService opService)
        { 
            // Hmm - if the subnode has been optimized, we may fail.
            Op op = opService.getSubOp() ;
            Element x = asElement(opService.getSubOp()) ; 
            Element elt = new ElementService(opService.getService(), x, opService.getSilent()) ;
            currentGroup().addElement(elt) ;
        }
        
        @Override
        public void visit(OpDatasetNames dsNames)
        { throw new ARQNotImplemented("OpDatasetNames") ; }

        @Override
        public void visit(OpTable opTable)
        { 
            // This will go in a group so simply forget it. 
            if ( opTable.isJoinIdentity() ) return ;
            
            // Put in a VALUES
            // This may be related to the grpup of the overall query.
            
            ElementData el = new ElementData() ; 
            el.getVars().addAll(opTable.getTable().getVars()) ;
            QueryIterator qIter = opTable.getTable().iterator(null) ;
            while(qIter.hasNext())
                el.getRows().add(qIter.next()) ;
            qIter.close() ;
            currentGroup().addElement(el) ;
        }

        @Override
        public void visit(OpExt opExt)
        {
//            Op op = opExt.effectiveOp() ;
//            // This does not work in all cases.
//            op.visit(this) ;
            throw new ARQNotImplemented("OpExt") ;
        }

        @Override
        public void visit(OpNull opNull)
        { throw new ARQNotImplemented("OpNull") ; }

        @Override
        public void visit(OpLabel opLabel)
        {
            if ( opLabel.hasSubOp() )
                opLabel.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpAssign opAssign)
        {
            opAssign.getSubOp().visit(this) ;
            
            // Go through each var and get the assigned expression
            for ( Var v : opAssign.getVarExprList().getVars() )
            {
                Expr e = opAssign.getVarExprList().getExpr(v);
                
                // Substitute group aggregate expressions in for generated vars
                SubExprForVar sefr = new SubExprForVar(varExpression);
                Expr tr = ExprTransformer.transform(sefr, e);
                
                // If in top level we defer assignment to SELECT section
                // This also covers the GROUP recombine
                if (inTopLevel()) {
                    if (!inGroupRecombine(opAssign)) {
                        // If not wrapped over a Group then we need to ensure we add the variable
                        // to the list or otherwise the BIND will not round trip
                        // Note - This does mean top level BIND will manifest as a project expression
                        //        rather than a BIND but this is semantically equivalent so is not an issue
                        addProjectVar(projectVars, v) ;
                    }
                    varExpression.put(v, tr);
                } else {
                    Element elt = new ElementAssign(v, e) ;
                    ElementGroup g = currentGroup() ;
                    g.addElement(elt) ;
                }
            }
        }

        @Override
        public void visit(OpExtend opExtend)
        { 
            opExtend.getSubOp().visit(this) ;
            
            // Go through each var and get the assigned expression
            for ( Var v : opExtend.getVarExprList().getVars() )
            {
                Expr e = opExtend.getVarExprList().getExpr(v);
                
                // Substitute group aggregate expressions in for generated vars
                Expr tr = ExprTransformer.transform(new SubExprForVar(varExpression), e);
                
                // If in top level we defer assignment to SELECT section
                // This also covers the GROUP recombine
                if (inTopLevel()) {
                    if (groupDepth == 0 || inGroupRecombine(opExtend)) {
                        if (!inGroupRecombine(opExtend)) {
                            // If not wrapped over a Group then we need to ensure we add the variable
                            // to the list or otherwise the BIND will not round trip
                            // Note - This does mean top level BIND will manifest as a project expression
                            //        rather than a BIND but this is semantically equivalent so is not an issue
                            addProjectVar(projectVars, v) ;
                        }
                        varExpression.put(v, tr);
                    } else {
                        Element elt = new ElementBind(v, tr) ;
                        ElementGroup g = currentGroup() ;
                        g.addElement(elt);
                    }
                } else {
                    Element elt = new ElementBind(v, tr) ;
                    ElementGroup g = currentGroup() ;
                    g.addElement(elt) ;
                }
            }
        }

        
        @Override
        public void visit(OpList opList)
        { /* No action */ }

        @Override
        public void visit(OpOrder opOrder)
        {
            List<SortCondition> x = opOrder.getConditions() ;
            for ( SortCondition sc : x )
                query.addOrderBy(sc);
            opOrder.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpProject opProject)
        {
            if (inProject) {
                // If we've already inside a project then we are reconstructing a sub-query
                // Create a new converter and call on the sub-op to get the sub-query
                Converter subConverter = new Converter(opProject);
                ElementSubQuery subQuery = new ElementSubQuery(subConverter.convert());
                ElementGroup g = currentGroup();
                g.addElement(subQuery);
            } else {
                // Defer adding result vars until the end.
                // OpGroup generates dupes otherwise
                this.projectVars = allocProjectVars() ;
                this.projectVars.addAll(opProject.getVars());
                inProject = true;
                opProject.getSubOp().visit(this) ;
                inProject = false;
            }
        }

        @Override
        public void visit(OpReduced opReduced)
        { 
            query.setReduced(true) ;
            opReduced.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpDistinct opDistinct)
        { 
            query.setDistinct(true) ;
            opDistinct.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpSlice opSlice)
        {
            if ( opSlice.getStart() != Query.NOLIMIT )
                query.setOffset(opSlice.getStart()) ;
            if ( opSlice.getLength() != Query.NOLIMIT )
                query.setLimit(opSlice.getLength()) ;
            opSlice.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpGroup opGroup) {               
            List<ExprAggregator> a = opGroup.getAggregators();
            
            // Aggregators are broken up in the algebra, split between a
            // group and an assignment (extend or assign) using a generated var.
            // We record them here and insert later.
            for (ExprAggregator ea : a) {
                // Substitute generated var for actual
                Var givenVar = ea.getAggVar().asVar();
                // Copy aggregator across (?)
                Expr myAggr = query.allocAggregate(ea.getAggregator());
                varExpression.put(givenVar, myAggr);
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
            groupDepth++;
            opGroup.getSubOp().visit(this);
            groupDepth--;
        }

        @Override
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
        private boolean inTopLevel() { return stack.size() == 0; }
        
        private boolean inGroupRecombine(OpExtend op) {
            Op subOp = op.getSubOp();
            if (subOp instanceof OpExtend) {
                return inGroupRecombine((OpExtend)subOp);
            } else if (subOp instanceof OpAssign) {
                return inGroupRecombine((OpAssign)subOp);
            } else if (subOp instanceof OpGroup) {
                return true;
            } else {
                return false;
            }
        }
        
        private boolean inGroupRecombine(OpAssign op) {
            Op subOp = op.getSubOp();
            if (subOp instanceof OpExtend) {
                return inGroupRecombine((OpExtend)subOp);
            } else if (subOp instanceof OpAssign) {
                return inGroupRecombine((OpAssign)subOp);
            } else if (subOp instanceof OpGroup) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * This class is used to take substitute an expressions for variables
     * in another expression. It is used to stick grouping expressions back
     * together.
     */
    public static class SubExprForVar extends ExprTransformCopy {
        private final Map<Var, Expr> varExpr;
        private boolean subOccurred = false;
        public SubExprForVar(Map<Var, Expr> varExpr) {
            this.varExpr = varExpr;
        }
        
        public boolean didChange() { return subOccurred; }
        
        @Override
        public Expr transform(ExprVar var) {
            if (varExpr.containsKey(var.asVar())) {
                subOccurred = true;
                return varExpr.get(var.asVar()).deepCopy();
            }
            else return var.deepCopy();
        }
    }
}
