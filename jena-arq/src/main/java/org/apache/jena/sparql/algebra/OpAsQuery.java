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

package org.apache.jena.sparql.algebra ;

import java.util.* ;
import java.util.function.BiConsumer ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.lib.NotImplemented ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.ARQNotImplemented ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.expr.* ;
import org.apache.jena.sparql.expr.aggregate.Aggregator ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.syntax.* ;
import org.apache.jena.sparql.util.graph.GraphList ;
import org.apache.jena.vocabulary.RDF ;

/**
 * Convert an Op expression in SPARQL syntax, that is, the reverse of algebra
 * generation
 */
public class OpAsQuery {
    
    // slice-distinct/reduce-project-order-filter[having]-extend*[AS and aggregate naming]-group-pattern
    // SELECT { ?s ?p ?o FILTER ( ?o > 5 ) ; }
    // OpTopN

    public static class /* struct */QueryLevelDetails {
        OpSlice        opSlice    = null ;
        OpDistinct     opDistinct = null ;
        OpReduced      opReduced  = null ;
        OpProject      opProject  = null ;
        OpOrder        opOrder    = null ;
        OpFilter       opHaving   = null ;
        List<OpExtend> opExtends  = new ArrayList<>() ;
        OpGroup        opGroup    = null ;
        // The pattern of the group or query if not grouped.
        Op             pattern    = null ;
        
        private QueryLevelDetails() {}
        
        public void info() {
            if ( opSlice != null )
                System.out.printf("slice: (%d, %d)\n", opSlice.getStart(), opSlice.getLength()) ;
            if ( opDistinct != null )
                System.out.printf("distinct\n") ;
            if ( opReduced != null )
                System.out.printf("reduced\n") ;
            if ( opProject != null )
                System.out.printf("project: %s\n", opProject.getVars()) ;
            if ( opOrder != null )
                System.out.printf("order: %s\n", opOrder.getConditions()) ;
            if ( opHaving != null )
                System.out.printf("having: %s\n", opHaving.getExprs()) ;
            if ( opExtends != null && !opExtends.isEmpty() ) {
                List<VarExprList> z = opExtends.stream().map(x -> x.getVarExprList()).collect(Collectors.toList()) ;
                System.out.printf("assigns: %s\n", z) ;
            }
            if ( opGroup != null ) {
                List<ExprAggregator> aggregators = opGroup.getAggregators() ;
                List<Var> aggVars = aggregators.stream().map(x -> x.getAggVar().asVar()).collect(Collectors.toList()) ;
                System.out.printf("group: %s |-| %s\n", opGroup.getGroupVars(), opGroup.getAggregators()) ;
                System.out.printf("group agg vars: %s\n", aggVars) ;
            }
        }

        static QueryLevelDetails analyse(Op operation) {
            QueryLevelDetails details = new QueryLevelDetails() ;

            Op op = operation ;
            if ( op instanceof OpSlice ) {
                details.opSlice = (OpSlice)op ;
                op = details.opSlice.getSubOp() ;
            }
            if ( op instanceof OpDistinct ) {
                details.opDistinct = (OpDistinct)op ;
                op = details.opDistinct.getSubOp() ;
            }
            if ( op instanceof OpReduced ) {
                details.opReduced = (OpReduced)op ;
                op = details.opReduced.getSubOp() ;
            }
            if ( op instanceof OpProject ) {
                details.opProject = (OpProject)op ;
                op = details.opProject.getSubOp() ;

            }
            if ( op instanceof OpOrder ) {
                details.opOrder = (OpOrder)op ;
                op = details.opOrder.getSubOp() ;
            }

            // Lookahead to see if an opGroup can be found.
            // Because inner SELECTs must have a project, the case of running
            // into an inner-SELECT-group does not occur.
            // And even if it did, either the inner-SELECT is part of a
            // group-pattern
            // (the {...} as in { { SELECT ... } ?s ?p ?o }
            // (so we'd see the othe parts of the pattern as intermediates
            // or if it's a singleton, collape the inner select into the outer
            // to give an equivalent query in a different form.

            details.opGroup = getGroup(op) ;
            if ( details.opGroup == null ) {
                op = processExtend(op, details.opExtends) ; 
                details.pattern = op ;
                return details ;
            }
            // (group) found.
            details.pattern = details.opGroup.getSubOp() ;
            if ( op instanceof OpFilter ) {
                details.opHaving = (OpFilter)op ;
                op = details.opHaving.getSubOp() ;
            }
            // Can't tell is just "aggregation" except by looking at the assignment
            // variables.
            
            // AS and aggregate renames.
            op = processExtend(op, details.opExtends) ;
            
            if ( !(op instanceof OpGroup) ) {
                System.out.println("Expected (group), got " + op.getName()) ;
            }

            return details ;
        }
    }
    
    public static Op processExtend(Op op, List<OpExtend> assignments) {
        while ( op instanceof OpExtend ) {
            OpExtend opExtend = (OpExtend)op ;
            assignments.add(opExtend) ;
            op = opExtend.getSubOp() ;
        }
        return op ;
    }
    /**
     * Allows multiple filters and any number of extend - good or bad?
     */
    private static OpGroup getGroup(Op op) {
        // Unwind tail recursion to protected against extreme queries.
        for ( ; ; ) {
            if ( op instanceof OpGroup )
                return (OpGroup)op ;
            if ( op instanceof OpFilter ) {
                OpFilter opFilter = (OpFilter)op ;
                op = opFilter.getSubOp() ;
                continue ;
            }
            if ( op instanceof OpExtend ) { // AS or Aggregate naming
                OpExtend opExtend = (OpExtend)op ;
                op = opExtend.getSubOp() ;
                continue ;
            }
            return null ;
        }
    }

    public static Query asQuery(Op op) {
        Converter converter = new Converter(op) ;
        return converter.convert() ;
    }

    public static class Converter implements OpVisitor {
        private Query               query ;
        private Op                  queryOp ;
        private Element             element      = null ;
        private ElementGroup        currentGroup = null ;
        private Deque<ElementGroup> stack        = new ArrayDeque<>() ;
        private int                 groupDepth   = 0 ;
        private boolean             inProject    = false ;
        private boolean             hasRun       = false ;

        public Converter(Op op) {
            this.query = null ;
            this.queryOp = op ;
            currentGroup = new ElementGroup() ;
        }

        Query convert() {
            if ( !hasRun )
                try {
                    // Which may be broken, or null, if something went wrong.
                    query = convertInner() ;
                }
                finally {
                    this.hasRun = true ;
                }
            return query ;
        }

        Query convertInner() {
            this.query = QueryFactory.create() ;
            // Special case. SELECT * { ... BIND (  AS ...) }
            if ( queryOp instanceof OpExtend ) {
                List<OpExtend> assignments = new ArrayList<>() ;
                Op op = processExtend(queryOp, assignments) ;
                processQueryPattern(op, assignments) ;
                query.setQueryResultStar(true) ;
                query.setResultVars(); 
                return query ;
            }
            
            // There is a projection.
            QueryLevelDetails level = QueryLevelDetails.analyse(queryOp) ;
            processQueryPattern(level) ;

            // Modifiers.
            // slice-distinct/reduce-project-order-filter[having]-extend[AS]-extend[agg]-group-pattern
            // Do in reverse order because e.g. exts have effects on project.

            // Substitution mapping
            Map<ExprVar, Expr> aggVarExprMap = new HashMap<>() ;

            if ( level.opGroup != null ) {
                query.getGroupBy().addAll(level.opGroup.getGroupVars()) ;
                level.opGroup.getAggregators().forEach(eAgg -> {
                    ExprVar v = eAgg.getAggVar() ;
                    Aggregator agg = eAgg.getAggregator() ;
                    aggVarExprMap.put(v, eAgg) ;
                }) ;
                query.getAggregators().addAll(level.opGroup.getAggregators()) ;
            }
            
            ExprTransform varToExpr = new ExprTransformCopy() {
                @Override
                public Expr transform(ExprVar nv) {
                    if ( aggVarExprMap.containsKey(nv) )
                        return aggVarExprMap.get(nv) ;
                    return nv ;
                }
            } ;

            // The assignments will become part of the project. 
            Map<Var, Expr> assignments = new HashMap<>() ;
            if ( level.opExtends != null ) {
                processExtends(level.opExtends, (var,expr)->{
                    // Internal rename.
                    expr = rewrite(expr, varToExpr) ;
                    assignments.put(var, expr) ;
                }) ;
            }
            
            
            if ( level.opHaving != null ) {
                level.opHaving.getExprs().getList().forEach(expr -> {
                    expr = rewrite(expr, varToExpr) ;
                    query.getHavingExprs().add(expr) ;
                }) ;
            }

            if ( level.opOrder != null ) {
                level.opOrder.getConditions().forEach(sc -> {
                    Expr expr = sc.getExpression() ;
                    expr = rewrite(expr, varToExpr) ;
                    if ( expr == sc.getExpression() )
                        query.addOrderBy(sc) ;
                    else
                        query.addOrderBy(new SortCondition(expr, sc.getDirection())) ;
                }) ;
                // level.opOrder.getConditions().forEach(sc->query.addOrderBy(sc))
                // ;
            }
            if ( level.opProject != null ) {
                level.opProject.getVars().forEach(v -> {
                    if ( assignments.containsKey(v) ) {
                        query.addResultVar(v, assignments.get(v)) ;
                    } else
                        query.getProjectVars().add(v) ;
                    
                }) ;
            } else {
                // Insert BIND for any (extends) and no project happsn in processQueryPattern.
                query.setQueryResultStar(true) ;
            }

            if ( level.opDistinct != null )
                query.setDistinct(true) ;
            if ( level.opReduced != null )
                query.setReduced(true) ;

            if ( level.opSlice != null ) {
                query.setOffset(level.opSlice.getStart()) ;
                query.setLimit(level.opSlice.getLength()) ;
            }
            query.setResultVars() ;
            
//            // Fixup -- fixupSubQueryOfOne in processQueryPattern
//            // Simplify currentGroup if possible, primarily look for the case of a single sub-query
//            // which will mean we have an ElementGroup with a single item which is
//            ElementGroup eg = this.currentGroup ;
//            if (eg.getElements().size() == 1) {
//                Element e = eg.getElements().get(0);
//                if (e instanceof ElementSubQuery) {
//                    query.setQueryPattern(e);
//                } else {
//                    query.setQueryPattern(eg);
//                }
//            } else {
//                query.setQueryPattern(eg) ;
//            }
            return query ;
        }

        private static void processExtends(List<OpExtend> ext, BiConsumer<Var, Expr> action) {
            ext.forEach(extend->{
                extend.getVarExprList().getExprs().forEach(action) ;
            });
        }
        
        private static void processAssigns(List<OpAssign> assigns, BiConsumer<Var, Expr> action) {
            assigns.forEach(assign->{
                assign.getVarExprList().getExprs().forEach(action) ;
            });
        }

        private static Expr rewrite(Expr expr, ExprTransform transform) {
            return ExprTransformer.transform(transform, expr) ;
        }

        private void processQueryPattern(QueryLevelDetails level) {
            Op op = level.pattern ;
            op.visit(this) ;
            ElementGroup eg = this.currentGroup ;
            Element e = fixupSubQueryOfOne(eg) ;
            query.setQueryPattern(e) ;
            query.setQuerySelectType() ;
        }
        
        // Without level: This is SLEECT * { ... BIND (..) }
        // which is (extend ... 
        private void processQueryPattern(Op op, List<OpExtend> assignments) {
            op.visit(this) ;

            ElementGroup eg = this.currentGroup ;
            processExtends(assignments,(v,e)->eg.addElement(new ElementBind(v, e)) ) ;
            
            Element e = fixupSubQueryOfOne(eg) ;
            query.setQueryPattern(e) ;
            query.setQuerySelectType() ;
        }

        private Element fixupSubQueryOfOne(ElementGroup eg) {
            // Simplify currentGroup if possible, primarily look for the case of
            // a single sub-query which will mean we have an ElementGroup with a single
            // item which is ElementSubQuery.
            if ( eg.getElements().size() != 1 )
                return eg ;
            Element e = eg.getElements().get(0) ;
            if ( e instanceof ElementSubQuery )
                return e ;
            return eg ;
        }
        
        Element asElement(Op op) {
            ElementGroup g = asElementGroup(op) ;
            if ( g.getElements().size() == 1 )
                return g.getElements().get(0) ;
            return g ;
        }

        ElementGroup asElementGroup(Op op) {
            startSubGroup() ;
            op.visit(this) ;
            return endSubGroup() ;
        }

        @Override
        public void visit(OpBGP opBGP) {
            currentGroup().addElement(process(opBGP.getPattern())) ;
        }

        // public void visit(OpPropFunc opPropFunc)
        // {
        // OpBGP opBGP = opPropFunc.getBGP() ;
        // currentGroup().addElement(process(opBGP.getPattern())) ;
        // }

        @Override
        public void visit(OpTriple opTriple) {
            currentGroup().addElement(process(opTriple.getTriple())) ;
        }

        @Override
        public void visit(OpQuad opQuad) {
            throw new ARQNotImplemented("OpQuad") ;
        }

        @Override
        public void visit(OpProcedure opProcedure) {
            throw new ARQNotImplemented("OpProcedure") ;
        }

        @Override
        public void visit(OpPropFunc opPropFunc) {
            Node s = processPropFuncArg(opPropFunc.getSubjectArgs()) ;
            Node o = processPropFuncArg(opPropFunc.getObjectArgs()) ;
            Triple t = new Triple(s, opPropFunc.getProperty(), o) ;
            currentGroup().addElement(process(t)) ;
        }

        private Node processPropFuncArg(PropFuncArg args) {
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
        // A path expression was expaned into a OpSequence during Algenra
        // generation.
        // The simple path expressions become an OpSequence that could be
        // recombined
        // into on ElementPathBlock

        @Override
        public void visit(OpSequence opSequence) {
            ElementGroup g = currentGroup() ;
            boolean nestGroup = !g.isEmpty() ;
            if ( nestGroup ) {
                startSubGroup() ;
                g = currentGroup() ;
            }

            for ( Op op : opSequence.getElements() ) {
                Element e = asElement(op) ;
                g.addElement(e) ;
            }

            if ( nestGroup )
                endSubGroup() ;
            return ;
        }

        @Override
        public void visit(OpDisjunction opDisjunction) {
            throw new ARQNotImplemented("OpDisjunction") ;
        }

        private Element process(BasicPattern pattern) {
            // The different SPARQL versions use different internal structures
            // for BGPs.
            if ( query.getSyntax() == Syntax.syntaxSPARQL_10 ) {
                ElementTriplesBlock e = new ElementTriplesBlock() ;
                for ( Triple t : pattern )
                    // Leave bNode variables as they are
                    // Query serialization will deal with them.
                    e.addTriple(t) ;
                return e ;
            }

            if ( query.getSyntax() == Syntax.syntaxSPARQL_11 || query.getSyntax() == Syntax.syntaxARQ ) {
                ElementPathBlock e = new ElementPathBlock() ;
                for ( Triple t : pattern )
                    // Leave bNode variables as they are
                    // Query serialization will deal with them.
                    e.addTriple(t) ;
                return e ;
            }

            throw new ARQInternalErrorException("Unrecognized syntax: " + query.getSyntax()) ;

        }

        private ElementTriplesBlock process(Triple triple) {
            // Unsubtle
            ElementTriplesBlock e = new ElementTriplesBlock() ;
            e.addTriple(triple) ;
            return e ;
        }

        @Override
        public void visit(OpQuadPattern quadPattern) {
            Node graphNode = quadPattern.getGraphNode() ;
            if ( graphNode.equals(Quad.defaultGraphNodeGenerated) ) {
                currentGroup().addElement(process(quadPattern.getBasicPattern())) ;
            } else {
                startSubGroup() ;
                Element e = asElement(new OpBGP(quadPattern.getBasicPattern())) ;
                endSubGroup() ;

                // If not element group make it one
                if ( !(e instanceof ElementGroup) ) {
                    ElementGroup g = new ElementGroup() ;
                    g.addElement(e) ;
                    e = g ;
                }

                Element graphElt = new ElementNamedGraph(graphNode, e) ;
                currentGroup().addElement(graphElt) ;
            }
        }

        @Override
        public void visit(OpQuadBlock quadBlock) {
            // Gather into OpQuadPatterns.
            throw new NotImplemented("OpQuadBlock") ;
        }

        @Override
        public void visit(OpPath opPath) {
            ElementPathBlock epb = new ElementPathBlock() ;
            epb.addTriplePath(opPath.getTriplePath()) ;
            ElementGroup g = currentGroup() ;
            g.addElement(epb) ;
        }

        @Override
        public void visit(OpJoin opJoin) {
            Element eLeft = asElement(opJoin.getLeft()) ;
            ElementGroup eRightGroup = asElementGroup(opJoin.getRight()) ;
            Element eRight = eRightGroup ;
            // Very special case. If the RHS is not something that risks
            // reparsing into a copmbined element of a group, strip the group-of-one. 
            if ( eRightGroup.getElements().size() == 1 ) {
                // This always was a {} around it but it's unnecessary in a group of one. 
                if ( eRightGroup.getElements().get(0) instanceof ElementSubQuery )
                    eRight = eRightGroup.getElements().get(0) ;
            }

            ElementGroup g = currentGroup() ;
            g.addElement(eLeft) ;
            g.addElement(eRight) ;
            return ;
        }

        private static boolean emptyGroup(Element element) {
            if ( !(element instanceof ElementGroup) )
                return false ;
            ElementGroup eg = (ElementGroup)element ;
            return eg.isEmpty() ;
        }

        @Override
        public void visit(OpLeftJoin opLeftJoin) {
            Element eLeft = asElement(opLeftJoin.getLeft()) ;
            ElementGroup eRight = asElementGroup(opLeftJoin.getRight()) ;
            
            // If the RHS is (filter) we need to protect it from becoming
            // part of the expr for the LeftJoin.
            // OPTIONAL {{ ?s ?p ?o FILTER (?o>34) }} is not the same as
            // OPTIONAL { ?s ?p ?o FILTER (?o>34) }
            
            boolean mustProtect = false ;
            for ( Element el : eRight.getElements() ) {
                if ( el instanceof ElementFilter ) {
                    mustProtect = true ;
                    break ;
                }
            }
                
            if ( mustProtect ) {
                ElementGroup eRight2 = new ElementGroup() ;
                eRight2.addElement(eRight);
                eRight = eRight2 ;
            }

            if ( opLeftJoin.getExprs() != null ) {
                for ( Expr expr : opLeftJoin.getExprs() ) {
                    ElementFilter f = new ElementFilter(expr) ;
                    eRight.addElement(f) ;
                }
            }
            ElementGroup g = currentGroup() ;
            if ( !emptyGroup(eLeft) )
                g.addElement(eLeft) ;
            ElementOptional opt = new ElementOptional(eRight) ;
            g.addElement(opt) ;
        }

        @Override
        public void visit(OpDiff opDiff) {
            throw new ARQNotImplemented("OpDiff") ;
        }

        @Override
        public void visit(OpMinus opMinus) {
            Element eLeft = asElement(opMinus.getLeft()) ;
            Element eRight = asElementGroup(opMinus.getRight()) ;
            ElementMinus elMinus = new ElementMinus(eRight) ;
            ElementGroup g = currentGroup() ;
            if ( !emptyGroup(eLeft) )
                g.addElement(eLeft) ;
            g.addElement(elMinus) ;
        }

        @Override
        public void visit(OpUnion opUnion) {
            Element eLeft = asElementGroup(opUnion.getLeft()) ;
            Element eRight = asElementGroup(opUnion.getRight()) ;
            if ( eLeft instanceof ElementUnion ) {
                ElementUnion elUnion = (ElementUnion)eLeft ;
                elUnion.addElement(eRight) ;
                return ;
            }

            // if ( eRight instanceof ElementUnion )
            // {
            // ElementUnion elUnion = (ElementUnion)eRight ;
            // elUnion.getElements().add(0, eLeft) ;
            // return ;
            // }

            ElementUnion elUnion = new ElementUnion() ;
            elUnion.addElement(eLeft) ;
            elUnion.addElement(eRight) ;
            currentGroup().addElement(elUnion) ;
        }

        @Override
        public void visit(OpConditional opCondition) {
            throw new ARQNotImplemented("OpCondition") ;
        }

        @Override
        public void visit(OpFilter opFilter) {
            // (filter .. (filter ( ... )) (non-canonicalizing OpFilters)
            // Inner gets Grouped unnecessarily.
            Element e = asElement(opFilter.getSubOp()) ;
            if ( currentGroup() != e )
                currentGroup().addElement(e) ;
            element = currentGroup() ; // Was cleared by asElement.

            ExprList exprs = opFilter.getExprs() ;
            for ( Expr expr : exprs ) {
                ElementFilter f = new ElementFilter(expr) ;
                currentGroup().addElement(f) ;
            }
        }

        @Override
        public void visit(OpGraph opGraph) {
            startSubGroup() ;
            Element e = asElement(opGraph.getSubOp()) ;
            endSubGroup() ;

            // If not element group make it one
            if ( !(e instanceof ElementGroup) ) {
                ElementGroup g = new ElementGroup() ;
                g.addElement(e) ;
                e = g ;
            }

            Element graphElt = new ElementNamedGraph(opGraph.getNode(), e) ;
            currentGroup().addElement(graphElt) ;
        }

        @Override
        public void visit(OpService opService) {
            // Hmm - if the subnode has been optimized, we may fail.
            Op op = opService.getSubOp() ;
            Element x = asElement(opService.getSubOp()) ;
            Element elt = new ElementService(opService.getService(), x, opService.getSilent()) ;
            currentGroup().addElement(elt) ;
        }

        @Override
        public void visit(OpDatasetNames dsNames) {
            throw new ARQNotImplemented("OpDatasetNames") ;
        }

        @Override
        public void visit(OpTable opTable) {
            // This will go in a group so simply forget it.
            if ( opTable.isJoinIdentity() )
                return ;

            // Put in a VALUES
            // This may be related to the grpup of the overall query.

            ElementData el = new ElementData() ;
            el.getVars().addAll(opTable.getTable().getVars()) ;
            QueryIterator qIter = opTable.getTable().iterator(null) ;
            while (qIter.hasNext())
                el.getRows().add(qIter.next()) ;
            qIter.close() ;
            currentGroup().addElement(el) ;
        }

        @Override
        public void visit(OpExt opExt) {
            // Op op = opExt.effectiveOp() ;
            // // This does not work in all cases.
            // op.visit(this) ;
            throw new ARQNotImplemented("OpExt") ;
        }

        @Override
        public void visit(OpNull opNull) {
            throw new ARQNotImplemented("OpNull") ;
        }

        @Override
        public void visit(OpLabel opLabel) {
            if ( opLabel.hasSubOp() )
                opLabel.getSubOp().visit(this) ;
        }

        private void newLevel(Op op) {
            convertAsSubQuery(op) ;
        }

        @Override
        public void visit(OpAssign opAssign) {
            Element e = asElement(opAssign.getSubOp()) ;
            if ( currentGroup() != e )
                currentGroup().addElement(e) ;
            processAssigns(Arrays.asList(opAssign), (var,expr)->{
                currentGroup().addElement(new ElementAssign(var,expr)) ;
            }) ;
        }

        @Override
        public void visit(OpExtend opExtend) {
            Element e = asElement(opExtend.getSubOp()) ;
            if ( currentGroup() != e )
                currentGroup().addElement(e) ;
            processExtends(Arrays.asList(opExtend), (var,expr)->{
                currentGroup().addElement(new ElementBind(var,expr)) ;
            }) ;
        }

        @Override
        public void visit(OpList opList) {
            opList.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpOrder opOrder) {
            newLevel(opOrder) ;
            // List<SortCondition> x = opOrder.getConditions() ;
            // for ( SortCondition sc : x )
            // query.addOrderBy(sc);
            // opOrder.getSubOp().visit(this) ;
        }

        @Override
        public void visit(OpProject opProject) {
            newLevel(opProject) ;
            // if (inProject) {
            // // If we've already inside a project then we are reconstructing a
            // sub-query
            // // Create a new converter and call on the sub-op to get the
            // sub-query
            // convertAsSubQuery(opProject);
            // } else {
            // // Defer adding result vars until the end.
            // // OpGroup generates dupes otherwise
            // this.projectVars = allocProjectVars() ;
            // this.projectVars.addAll(opProject.getVars());
            // inProject = true;
            // opProject.getSubOp().visit(this) ;
            // inProject = false;
            // }
        }

        private void convertAsSubQuery(Op op) {
            Converter subConverter = new Converter(op) ;
            ElementSubQuery subQuery = new ElementSubQuery(subConverter.convert()) ;
            ElementGroup g = currentGroup() ;
            g.addElement(subQuery) ;
        }

        @Override
        public void visit(OpReduced opReduced) {
            newLevel(opReduced) ;
            // if (inProject) {
            // convertAsSubQuery(opReduced);
            // } else {
            // query.setReduced(true) ;
            // opReduced.getSubOp().visit(this) ;
            // }
        }

        @Override
        public void visit(OpDistinct opDistinct) {
            newLevel(opDistinct) ;
            // if (inProject) {
            // convertAsSubQuery(opDistinct);
            // } else {
            // query.setDistinct(true) ;
            // opDistinct.getSubOp().visit(this) ;
            // }
        }

        @Override
        public void visit(OpSlice opSlice) {
            newLevel(opSlice) ;
            // if (inProject) {
            // convertAsSubQuery(opSlice);
            // } else {
            // if ( opSlice.getStart() != Query.NOLIMIT )
            // query.setOffset(opSlice.getStart()) ;
            // if ( opSlice.getLength() != Query.NOLIMIT )
            // query.setLimit(opSlice.getLength()) ;
            // opSlice.getSubOp().visit(this) ;
            // }
        }

        @Override
        public void visit(OpGroup opGroup) {
            newLevel(opGroup) ;
            // List<ExprAggregator> a = opGroup.getAggregators();
            //
            // // Aggregators are broken up in the algebra, split between a
            // // group and an assignment (extend or assign) using a generated
            // var.
            // // We record them here and insert later.
            // for (ExprAggregator ea : a) {
            // // Substitute generated var for actual
            // Var givenVar = ea.getAggVar().asVar();
            // // Copy aggregator across (?)
            // Expr myAggr = query.allocAggregate(ea.getAggregator());
            // varExpression.put(givenVar, myAggr);
            // }
            //
            // VarExprList b = opGroup.getGroupVars();
            // for (Var v : b.getVars()) {
            // Expr e = b.getExpr(v);
            //
            // if (e != null) {
            // query.addGroupBy(v, e);
            //
            // } else {
            // query.addGroupBy(v);
            //
            // }
            // }
            // groupDepth++;
            // opGroup.getSubOp().visit(this);
            // groupDepth--;
        }

        @Override
        public void visit(OpTopN opTop) {
            throw new ARQNotImplemented("OpTopN") ;
        }

        private Element lastElement() {
            ElementGroup g = currentGroup ;
            if ( g == null || g.getElements().size() == 0 )
                return null ;
            int len = g.getElements().size() ;
            return g.getElements().get(len - 1) ;
        }

        private void startSubGroup() {
            push(currentGroup) ;
            ElementGroup g = new ElementGroup() ;
            currentGroup = g ;
        }

        private ElementGroup endSubGroup() {
            ElementGroup g = pop() ;
            ElementGroup r = currentGroup ;
            currentGroup = g ;
            return r ;
        }

        private ElementGroup currentGroup() {
            // if ( currentGroup == null )
            // startSubGroup() ;
            return currentGroup ;
        }

        private ElementGroup peek() {
            if ( stack.size() == 0 )
                return null ;
            return stack.peek() ;
        }

        private ElementGroup pop() {
            return stack.pop() ;
        }

        private void push(ElementGroup el) {
            stack.push(el) ;
        }

        private boolean inTopLevel() {
            return stack.size() == 0 ;
        }
    }
}
