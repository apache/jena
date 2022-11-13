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
import org.apache.jena.sparql.syntax.syntaxtransform.*;
import org.apache.jena.sparql.util.graph.GraphList ;
import org.apache.jena.vocabulary.RDF ;

/**
 * Convert an Op expression in SPARQL syntax, that is, the reverse of algebra
 * generation.
 * <p>
 * The contract is to return an "equivalent query" - generates the same answers -
 * to the original query that generated the algebra.
 * That may be the same query (the code aims for this, assuming the original query
 * didn't have additional, unnecessary {}),
 * different queries with the same alegra forms,
 * or different equivalent queries - same answers, different algebra -
 * usually where extra {} are added in and not easiely cleaned out.
 * <p>
 * Some attempt is made to handle algebra expressions with operators from the optimizer.
 * <p>
 * It is possible to build algrebra expressions directly for which there is no SPARQL query
 * that generates that algebra.  This code may produce an equivalent query but that is
 * not guaranteed.
 */
public class OpAsQuery {
    // Some things that can be done further:

    // TODO Optimization formats like OpTopN (which is an optimizer additional algebra operator).
    // TODO More group flattening. This is better presentation, not chanage in algebra.
    //    OPTIONAL (LeftJoin) unbundles the LHS to avoid { { P OPTIONAL{} } OPTIONAL{} }
    //    {   { ... } BIND } -- the inner { } is not necessary
    //       This is actually a general situation.
    //   Adding onto the end of a group when the item added can not merge into the existing last element.
    // e.g. BIND, VALUES.

    public static Query asQuery(Op op) {
        Converter converter = new Converter(op) ;
        return converter.convert() ;
    }

    static class /* struct */ QueryLevelDetails {
        // The stack of processing in a query is:
        // slice-distinct/reduce-project-order-filter[having]-extend*[AS and aggregate naming]-group-pattern

        OpSlice        opSlice    = null ;
        OpDistinct     opDistinct = null ;
        OpReduced      opReduced  = null ;
        OpProject      opProject  = null ;
        OpOrder        opOrder    = null ;
        OpFilter       opHaving   = null ;
        List<OpExtend> opExtends  = new ArrayList<>() ;
        OpGroup        opGroup    = null ;
        // End of the modifiers.
        // The pattern of the group or query itself if not grouped.
        Op             pattern    = null ;

        private QueryLevelDetails() {}

        // Debugging help.
        void info() {
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
            // Walk inwards, collecting the query level information.
            // slice-distinct/reduce-project-order-filter[having]-extend*[AS and aggregate naming]-group-pattern
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
            // If no group, leave and process as in WHERE clause.
            details.opGroup = getGroup(op) ;
            if ( details.opGroup == null ) {
                // If project, deal with as AS.
                // Else as regular WHERE
                if ( details.opProject != null ) {
                    op = processExtend(op, details.opExtends) ;
                    details.pattern = op ;
                } else
                    details.pattern = op ;
                return details ;
            }
            // (group) found.
            details.pattern = details.opGroup.getSubOp() ;
            if ( op instanceof OpFilter ) {
                details.opHaving = (OpFilter)op ;
                op = details.opHaving.getSubOp() ;
            }
            // Can't tell if it's an "aggregation" except by looking at the
            // assignment variables.

            // AS and aggregate renames.
            op = processExtend(op, details.opExtends) ;

            if ( !(op instanceof OpGroup) ) {
                System.out.println("Expected (group), got " + op.getName()) ;
            }

            return details ;
        }
    }

    public static class Converter implements OpVisitor {
        private Query               query ;
        private Op                  queryOp ;
        private Element             element      = null ;
        private ElementGroup        currentGroup = null ;
        private Deque<ElementGroup> stack        = new ArrayDeque<>() ;
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
                query.resetResultVars();
                return query ;
            }

            // There is a projection.
            QueryLevelDetails level = QueryLevelDetails.analyse(queryOp) ;
            processQueryPattern(level) ;

            // Modifier stack.
            // slice-distinct/reduce-project-order-filter[having]-extend[AS]-extend[agg]-group-pattern
            // Do as executed (the reverse order) because e.g. extends have effects on project.

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
            // Using VarExprList to preserve order; https://github.com/apache/jena/issues/1369
            VarExprList assignments = new VarExprList();
            if ( level.opExtends != null ) {
                processExtends(level.opExtends, (var,expr)->{
                    // Internal rename.
                    expr = rewrite(expr, varToExpr) ;
                    assignments.add(var, expr) ;
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
            }

            if ( level.opProject == null ) {
                query.setQueryResultStar(true) ;
                // No project, Make BINDs
                //processQueryPattern(op, assignments) ;

            } else {
                // Where assignments and projections align the assignments will become part of the projection
                // otherwise the assignments will become BINDs; https://github.com/apache/jena/issues/1369
                List<Var> projectVars = level.opProject.getVars();

                List<Var> assignVars = assignments.getVars();
                int assignVarsSize = assignVars.size();
                int projectOffset = assignVarsSize; // Start at end and search backwards
                int idxThreshold = Integer.MAX_VALUE; // Prevent adding later mentioned expressions earlier to the projection list

                // Find an offset in the assignments from which on
                // *all remaining* variables appears in the same order as in the projection
                while (projectOffset-- > 0) {
                    Var assignVar = assignVars.get(projectOffset);

                    // Ensure that the projection does not shuffle the given order of expressions
                    // A later assignment must also appear later in the built projection
                    int idx = projectVars.indexOf(assignVar);
                    if (idx < 0 || idx > idxThreshold) {
                        break;
                    }
                    idxThreshold = idx;
                }

                // Assignments with index <= projectOffset become BINDs
                // Note that a projectOffset of -1 means there won't be any BINDs
                if (projectOffset >= 0) {
                    Element activeElement = query.getQueryPattern();

                    ElementGroup activeGroup;
                    if (activeElement instanceof ElementGroup) {
                        activeGroup = (ElementGroup)activeElement;
                    } else {
                        // Not sure whether it's possible here for BINDs to exist with the
                        // activeElement NOT being a group pattern - but better safe than sorry
                        activeGroup = new ElementGroup();
                        activeGroup.addElement(activeElement);
                        query.setQueryPattern(activeGroup);
                    }

                    for (int i = 0; i <= projectOffset; ++i) {
                        Var v = assignVars.get(i);
                        Expr e = assignments.getExpr(v);
                        activeGroup.addElement(new ElementBind(v, e));
                    }
                }

                // For each projected variable determine whether a possible expression was already
                // added as a BIND or whether it needs to be projected
                for (Var v : projectVars) {
                    Expr e = assignments.getExpr(v);

                    int offset = assignVars.indexOf(v);
                    if (offset > projectOffset) {
                        // Note that 'query.addResultVar' handles the case where e is null
                        query.addResultVar(v, e) ;
                    }
                    else {
                        // Either the variable did not map to an expression or
                        // the expression was added as BIND - in any case just project the variable
                        query.addResultVar(v, null) ;
                    }
                }
            }

            if ( level.opDistinct != null )
                query.setDistinct(true) ;
            if ( level.opReduced != null )
                query.setReduced(true) ;

            if ( level.opSlice != null ) {
                query.setOffset(level.opSlice.getStart()) ;
                query.setLimit(level.opSlice.getLength()) ;
            }
            query.resetResultVars() ;
            return query ;
        }

        /**
         * Collect the OpExtend in a stack of OpExtend into a list for later
         * processing. (Processing only uses opExtend in the list, not inner one
         * which will also be in the list.)
         */
        private static Op processExtend(Op op, List<OpExtend> assignments) {
            while ( op instanceof OpExtend ) {
                OpExtend opExtend = (OpExtend)op ;
                // JENA-1843
                assignments.add(0, opExtend) ;
                op = opExtend.getSubOp() ;
            }
            return op ;
        }

        private static void processExtends(List<OpExtend> ext, BiConsumer<Var, Expr> action) {
            ext.forEach(extend->{
                extend.getVarExprList().forEachVarExpr(action) ;
            });
        }

        private static void processAssigns(List<OpAssign> assigns, BiConsumer<Var, Expr> action) {
            assigns.forEach(assign->{
                assign.getVarExprList().forEachExpr(action) ;
            });
        }

        private static Expr rewrite(Expr expr, ExprTransform transform) {
            return ExprTransformer.transform(transform, expr) ;
        }

        /**
         * Process for a single pattern below the modifiers.
         * Cleans up the ElementGroup produced.
         */
        private void processQueryPattern(QueryLevelDetails level) {
            Op op = level.pattern ;
            op.visit(this) ;
            ElementGroup eg = this.currentGroup ;
            Element e = fixupGroupsOfOne(eg) ;
            query.setQueryPattern(e) ;
            query.setQuerySelectType() ;
        }

        // Can't distinguish
        //    SELECT * { ... BIND ( ?v AS ...) }
        // from
        //    SELECT ( ?v AS ...) { ... }.
        // They have the same algebra.
        // This code chooses to use the second form.
        private void processQueryPattern(Op op, List<OpExtend> assignments) {
            op.visit(this) ;
            ElementGroup eg = this.currentGroup ;
            processExtends(assignments,(v,e)->eg.addElement(new ElementBind(v, e)) ) ;
            Element e = cleanupGroup(eg) ;
            query.setQueryPattern(e) ;
            query.setQuerySelectType() ;
        }

        private Element cleanupGroup(ElementGroup eg) {
            Element el = fixupGroupsOfOne(eg);
            // Other cleanups.
            return el;
        }

        private Element fixupGroupsOfOne(ElementGroup eg) {
            ElementTransform transform = new ElementTransformCleanGroupsOfOne() ;
            ExprTransform exprTransform = new ExprTransformApplyElementTransform(transform) ;
            Element el2 = ElementTransformer.transform(eg, transform, exprTransform) ;
            // Top level is always a group or a subquery.
            if ( ! ( el2 instanceof ElementGroup ) && ! ( el2 instanceof ElementSubQuery ) ) {
                ElementGroup eg2 = new ElementGroup() ;
                eg2.addElement(el2);
                el2 = eg2 ;
            }
            return el2 ;
        }

        private Element asElement(Op op) {
            ElementGroup g = asElementGroup(op) ;
            if ( g.size() == 1 )
                return g.get(0) ;
            return g ;
        }

        private ElementGroup asElementGroup(Op op) {
            startSubGroup() ;
            op.visit(this) ;
            return endSubGroup() ;
        }

        @Override
        public void visit(OpBGP opBGP) {
            currentGroup().addElement(process(opBGP.getPattern())) ;
        }

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
            Triple t = Triple.create(s, opPropFunc.getProperty(), o) ;
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
        // A path expression was expanded into a OpSequence during Algebra
        // generation. The simple path expressions become an OpSequence that could be
        // recombined into an ElementPathBlock.

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
                insertIntoGroup(g, e) ;
            }

            if ( nestGroup )
                endSubGroup() ;
            return ;
        }

        @Override
        public void visit(OpDisjunction opDisjunction) {
            ElementUnion elUnion = new ElementUnion();
            for ( Op op : opDisjunction.getElements() ) {
                Element el = asElement(op) ;
                elUnion.addElement(el);
            }
            currentGroup().addElement(elUnion) ;
        }

        private Element process(BasicPattern pattern) {
            // The different SPARQL versions (1.0, 1.1) use different internal
            // structures for BGPs.
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
            // reparsing into a combined element of a group, strip the group-of-one.
            // See also ElementTransformCleanGroupsOfOne
            if ( eRightGroup.size() == 1 ) {
                // This always was a {} around it but it's unnecessary in a group of one.
                if ( eRightGroup.get(0) instanceof ElementSubQuery )
                    eRight = eRightGroup.get(0) ;
            }

            ElementGroup g = currentGroup() ;
            insertIntoGroup(g, eLeft) ;
            insertIntoGroup(g, eRight) ;
            return ;
        }

        @Override
        public void visit(OpLeftJoin opLeftJoin) {
            convertLeftJoin(opLeftJoin.getLeft(), opLeftJoin.getRight(), opLeftJoin.getExprs());
        }

        private void convertLeftJoin(Op opLeft, Op opRight, ExprList exprs) {
            Element eLeft = asElement(opLeft) ;
            ElementGroup eRight = asElementGroup(opRight) ;

            // If the RHS is (filter) we need to protect it from becoming
            // part of the expr for the LeftJoin.
            // OPTIONAL {{ ?s ?p ?o FILTER (?o>34) }} is not the same as
            // OPTIONAL { ?s ?p ?o FILTER (?o>34) }

            boolean mustProtect = eRight.getElements().stream().anyMatch(el -> el instanceof ElementFilter ) ;

            if ( mustProtect ) {
                ElementGroup eRight2 = new ElementGroup() ;
                eRight2.addElement(eRight);
                eRight = eRight2 ;
            }

            if ( exprs != null ) {
                for ( Expr expr : exprs ) {
                    ElementFilter f = new ElementFilter(expr) ;
                    eRight.addElement(f) ;
                }
            }
            ElementGroup g = currentGroup() ;
            if ( !emptyGroup(eLeft) ) {
                if ( eLeft instanceof ElementGroup )
                    g.getElements().addAll(((ElementGroup)eLeft).getElements()) ;
                else
                    g.addElement(eLeft) ;
            }

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
            // Multiple unions.
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
            // Possibly imperfect because there might be filters outside the OpConditional.
            convertLeftJoin(opCondition.getLeft(), opCondition.getRight(), null);
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

        @Override
        public void visit(OpAssign opAssign) {
            Element e = asElement(opAssign.getSubOp()) ;
            // If (assign ... (table unit)), and first in group, don't add the empty group.
            insertIntoGroup(currentGroup(), e) ;
            processAssigns(Arrays.asList(opAssign), (var,expr)->{
                currentGroup().addElement(new ElementAssign(var,expr)) ;
            }) ;
        }

        @Override
        public void visit(OpExtend opExtend) {
            Element e = asElement(opExtend.getSubOp()) ;
            // If (extend ... (table unit)), and first in group, don't add the empty group.
            insertIntoGroup(currentGroup(), e) ;
            processExtends(Arrays.asList(opExtend), (var,expr)->{
                currentGroup().addElement(new ElementBind(var,expr)) ;
            }) ;
        }

        @Override
        public void visit(OpList opList) {
            opList.getSubOp().visit(this) ;
        }

        // When some modifers (e.g. OpDistinct) are met in a pattern, they signal
        // a new query level (new inner SELECT), where we switch back to
        // looking for the level start in

        private void newLevel(Op op) {
            convertAsSubQuery(op) ;
        }

        private void convertAsSubQuery(Op op) {
            Converter subConverter = new Converter(op) ;
            ElementSubQuery subQuery = new ElementSubQuery(subConverter.convert()) ;
            ElementGroup g = currentGroup() ;
            g.addElement(subQuery) ;
        }

        @Override
        public void visit(OpOrder opOrder) {
            newLevel(opOrder) ;
        }

        @Override
        public void visit(OpProject opProject) {
            newLevel(opProject) ;
        }

        @Override
        public void visit(OpReduced opReduced) {
            newLevel(opReduced) ;
        }

        @Override
        public void visit(OpDistinct opDistinct) {
            newLevel(opDistinct) ;
        }

        @Override
        public void visit(OpSlice opSlice) {
            newLevel(opSlice) ;
        }

        @Override
        public void visit(OpGroup opGroup) {
            newLevel(opGroup) ;
        }

        @Override
        public void visit(OpTopN opTop) {
            throw new ARQNotImplemented("OpTopN") ;
        }

        private static boolean emptyGroup(Element element) {
            if ( !(element instanceof ElementGroup) )
                return false ;
            ElementGroup eg = (ElementGroup)element ;
            return eg.isEmpty() ;
        }

        private static boolean groupOfOne(Element element) {
            if ( !(element instanceof ElementGroup) )
                return false ;
            ElementGroup eg = (ElementGroup)element ;
            return eg.size() == 1 ;
        }

        /** Insert into a group, skip initial empty subgroups; recombining ElementPathBlock */
        private static void insertIntoGroup(ElementGroup eg, Element e) {
            // Skip initial empty subgroup.
            if ( emptyGroup(e) && eg.isEmpty() )
                return ;

            // Empty group.
            if ( eg.isEmpty() ) {
                eg.addElement(e);
                return ;
            }

            Element eltTop = eg.getLast() ;
            if ( ! ( eltTop instanceof ElementPathBlock ) ) {
                // Not working on a ElementPathBlock - no need to group-of-one
                // when inserting ElementPathBlock.
                e = unwrapGroupOfOnePathBlock(e) ;
                eg.addElement(e);
                return ;
            }
            if ( ! ( e  instanceof ElementPathBlock ) ) {
                eg.addElement(e);
                return ;
            }
            // Combine.
            ElementPathBlock currentPathBlock = (ElementPathBlock)eltTop ;
            ElementPathBlock newPathBlock = (ElementPathBlock)e ;
            currentPathBlock.getPattern().addAll(newPathBlock.getPattern());
        }

        private static Element unwrapGroupOfOnePathBlock(Element e) {
            Element e2 = getElementOfGroupOfOne(e) ;
            if ( e2 != null )
                return e2 ;
            return e ;
        }

        private static Element getElementOfGroupOfOne(Element e) {
            if ( groupOfOne(e) ) {
                ElementGroup eg = (ElementGroup)e ;
                return eg.get(0) ;
            }
            return null ;
        }

        private Element lastElement() {
            ElementGroup g = currentGroup ;
            return g.getLast() ;
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
    }


    /**
     * Allows multiple filters and any number of extend
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

    private static Op processExtend(Op op, List<OpExtend> assignments) {
        while (op instanceof OpExtend) {
            OpExtend opExtend = (OpExtend)op;
            assignments.add(opExtend);
            op = opExtend.getSubOp();
        }
        // JENA-1843
        if ( assignments.size() > 1 )
            Collections.reverse(assignments);

        return op;
    }
}
