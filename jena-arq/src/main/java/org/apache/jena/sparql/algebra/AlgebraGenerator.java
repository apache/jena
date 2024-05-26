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

package org.apache.jena.sparql.algebra;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.algebra.optimize.TransformSimplify;
import org.apache.jena.sparql.core.*;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.path.PathLib;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.sparql.util.Context;

/**
 * Class used to compile SPARQL queries into SPARQL algebra.
 * This is the SPARQL standard defined process of abstract syntax to algebra.
 */
public class AlgebraGenerator
{
    private final Context context;
    private final int subQueryDepth;

    // simplifyInAlgebraGeneration=true is the alternative reading of
    // the DAWG Algebra translation algorithm.

    // If we simplify during algebra generation, it changes the SPARQL for OPTIONAL {{ FILTER }}
    // The  {{}} results in (join unit (filter ...)) the filter is not moved
    // into the LeftJoin.

    // Fixed filter position means leave exactly where it is syntactically (illegal SPARQL)
    // Helpful only to write exactly what you mean and test the full query compiler.
    static private final boolean fixedFilterPosition                 = false;

    // False allows raw algebra to be generated (testing)
    static final private boolean applySimplification                 = true;

    // False is the correct setting.
    static final private boolean simplifyTooEarlyInAlgebraGeneration = false;

    /**
     * Create a new generator
     * @param context Context
     */
    public AlgebraGenerator(Context context) {
        this (context != null ? context : ARQ.getContext().copy(), 0);
    }

    /**
     * Create a new generator
     */
    public AlgebraGenerator() { this(null); }

    protected AlgebraGenerator(Context context, int depth) {
        this.context = context;
        this.subQueryDepth = depth;
    }

    //-- Public operations.  Do not call recursively (call compileElement).
    // These operations apply the simplification step which is done, once, at the end.

    /**
     * Compile a query
     * <p>
     * <strong>DO NOT</strong> call recursively
     * </p>
     * @param query Query to compile
     * @return Compiled algebra
     */
    public Op compile(Query query) {
        Element el = query.getQueryPattern();
        Op op = compile(el);     // Not compileElement - may need to apply simplification.
        op = compileModifiers(query, op);
        return op;
    }

    protected static Transform simplify = new TransformSimplify();

    /**
     * Compile any structural element
     * @param elt Element
     * @return Compiled algebra
     */
    public Op compile(Element elt) {
        Op op = compileElement(elt);
        Op op2 = op;
        if ( ! simplifyTooEarlyInAlgebraGeneration && applySimplification && simplify != null )
            op2 = simplify(op);
        return op2;
    }

    protected static Op simplify(Op op) {
        return Transformer.transform(simplify, op);
    }

    // This is the operation to call for recursive application.
    protected Op compileElement(Element elt) {
        if ( elt == null )
            return OpNull.create();

        if ( elt instanceof ElementGroup elt2)
            return compileElementGroup(elt2);

        if ( elt instanceof ElementUnion elt2 )
            return compileElementUnion(elt2);

        if ( elt instanceof ElementNamedGraph elt2 )
            return compileElementGraph(elt2);

        if ( elt instanceof ElementService elt2 )
            return compileElementService(elt2);

        if ( elt instanceof ElementSubQuery elt2 )
            return compileElementSubquery(elt2);

        if ( elt instanceof ElementTriplesBlock elt2 )
            return compileBasicPattern(elt2.getPattern());

        if ( elt instanceof ElementPathBlock elt2 )
            return compilePathBlock(elt2.getPattern());

        if ( elt instanceof ElementData elt2 )
            return compileElementData(elt2);

        // Special cases. Elements without being in an ElementGroup.
        if ( elt instanceof ElementFilter elt2 )
            return compileElementFilter(elt2);
        if ( elt instanceof ElementBind elt2 )
            return compileElementBind(elt2);
        if ( elt instanceof ElementAssign elt2 )
            return compileElementAssign(elt2);

        // Special cases. Element that have binary Ops with when used outside an ElementGroup.
        // Put in the "group start" state.
        if ( elt instanceof ElementOptional elt2 )
            return compileElementOptional(OpTable.unit(), elt2);
        if ( elt instanceof ElementMinus elt2 )
            return compileElementMinus(OpTable.unit(), elt2);

        return compileUnknownElement(elt, "compile(Element)/Not a structural element: "+Lib.className(elt));
    }

    /**
     * Future extension point to allow the algebra generator to be more easily extended to understand new {@link Element} implementations
     * that user defined language extensions may introduce.
     * <p>
     * This default implementation will throw an error
     * </p>
     * @param elt Element
     * @param error Error message if unable to compile the given element type
     * @return Algebra
     */
    protected Op compileUnknownElement(Element elt, String error) {
        broken(error);
        return null;
    }

    //Produce the algebra for a single group.
    //<a href="http://www.w3.org/TR/rdf-sparql-query/#sparqlQuery">Translation to the SPARQL Algebra</a>
    //
    // Step : (URI resolving and triple pattern syntax forms) was done during parsing
    // Step : Collection FILTERS [prepareGroup]
    // Step : (Paths) e.g. simple links become triple patterns. Done later in [compileOneInGroup]
    // Step : (BGPs) Merge PathBlocks - these are SPARQL 1.1's TriplesBlock   [prepareGroup]
    // Step : (BIND/LET) Associate with BGP [??]
    // Step : (Groups and unions) Was done during parsing to get ElementUnion.
    // Step : Graph Patterns [compileOneInGroup]
    // Step : Filters [here]
    // Simplification: Done later
    // If simplification is done now, it changes OPTIONAL { { ?x :p ?w . FILTER(?w>23) } } because it removes the
    //   (join Z (filter...)) that in turn stops the filter getting moved into the LeftJoin.
    //   It need a depth of 2 or more {{ }} for this to happen.

    protected Op compileElementGroup(ElementGroup groupElt) {
        Pair<List<Expr>, List<Element>> pair = prepareGroup(groupElt);
        List<Expr> filters = pair.getLeft();
        List<Element> groupElts = pair.getRight();

        // Compile the consolidated group elements.
        // "current" is the completed part only - there may be thing pushed into the accumulator.
        Op current = OpLib.unit();
        Deque<Op> acc = new ArrayDeque<>();

        for ( Element elt : groupElts ) {
            if ( elt != null ) {
                current = compileOneInGroup(elt, current, acc);
            }
        }

        // Deal with any remaining ops.
        //current = joinOpAcc(current, acc);

        if ( filters != null ) {
            // Put filters round the group.
            for ( Expr expr : filters )
                current = OpFilter.filter(expr, current);
        }
        return current;
    }

    /* Extract filters, merge adjacent BGPs, do BIND.
     * When extracting filters, BGP or PathBlocks may become adjacent
     * so merge them into one.
     * Return a list of elements with any filters at the end.
     */

    protected Pair<List<Expr>, List<Element>> prepareGroup(ElementGroup groupElt) {
        List<Element> groupElts = new ArrayList<>();

        PathBlock currentPathBlock = null;
        List<Expr> filters = null;

        for ( Element elt : groupElt.getElements() ) {
            if ( !fixedFilterPosition && elt instanceof ElementFilter ) {
                // For fixed position filters, drop through to general element
                // processing.
                // It's also illegal SPARQL - filters operate over the whole group.
                ElementFilter f = (ElementFilter)elt;
                if ( filters == null )
                    filters = new ArrayList<>();
                filters.add(f.getExpr());
                // Collect filters but do not place them yet.
                continue;
            }

            // The parser does not generate ElementTriplesBlock (SPARQL 1.1)
            // but SPARQL 1.0 does and also we cope for programmatically built
            // queries

            if ( elt instanceof ElementTriplesBlock etb) {
                if ( currentPathBlock == null ) {
                    ElementPathBlock etb2 = new ElementPathBlock();
                    currentPathBlock = etb2.getPattern();
                    groupElts.add(etb2);
                }

                for ( Triple t : etb.getPattern() )
                    currentPathBlock.add(new TriplePath(t));
                continue;
            }

            // To PathLib

            if ( elt instanceof ElementPathBlock epb ) {
                if ( currentPathBlock == null ) {
                    ElementPathBlock etb2 = new ElementPathBlock();
                    currentPathBlock = etb2.getPattern();
                    groupElts.add(etb2);
                }

                currentPathBlock.addAll(epb.getPattern());
                continue;
            }

            // else

            // Not BGP, path or filters.
            // Clear any BGP-related triple accumulators.
            currentPathBlock = null;
            // Add this element
            groupElts.add(elt);
        }
        return Pair.create(filters, groupElts);
    }

    protected Op compileOneInGroup(Element elt, Op current, Deque<Op> acc) {
        // Elements that operate over their left hand size (query syntax).

        if ( elt instanceof ElementAssign assign)
            return OpAssign.assign(current, assign.getVar(), assign.getExpr());

        if ( elt instanceof ElementBind bind )
            return OpExtend.create(current, bind.getVar(), bind.getExpr());

        if ( elt instanceof ElementOptional eltOpt )
            return compileElementOptional(current, eltOpt);

        if ( elt instanceof ElementLateral eltLateral )
            return compileElementLateral(current, eltLateral);

        if ( elt instanceof ElementMinus elt2 )
            return compileElementMinus(current, elt2);

        // All elements that simply "join" into the algebra.
        if ( elt instanceof ElementGroup || elt instanceof ElementNamedGraph || elt instanceof ElementService || elt instanceof ElementUnion
             || elt instanceof ElementSubQuery || elt instanceof ElementData || elt instanceof ElementTriplesBlock
             || elt instanceof ElementPathBlock ) {
            Op op = compileElement(elt);
            return join(current, op);
        }

        if ( elt instanceof ElementExists elt2 )
            return compileElementExists(current, elt2);

        if ( elt instanceof ElementNotExists elt2 )
            return compileElementNotExists(current, elt2);

        // Filters were collected together by prepareGroup
        // This only handles filters left in place by some magic.
        if ( elt instanceof ElementFilter ef)
            return OpFilter.filter(ef.getExpr(), current);

        return compileUnknownElement(elt, "compile/Element not recognized: "+Lib.className(elt));
    }

    protected Op compileElementUnion(ElementUnion el) {
        Op current = null;

        for ( Element subElt : el.getElements() ) {
            Op op = compileElement(subElt);
            current = union(current, op);
        }
        return current;
    }

    protected Op compileElementNotExists(Op current, ElementNotExists elt2) {
        Element subElt = elt2.getElement();
        Op op = compile(subElt);    // "compile", not "compileElement" -- do simplifcation
        Expr expr = new E_NotExists(subElt, op);
        return OpFilter.filter(expr, current);
    }

    protected Op compileElementExists(Op current, ElementExists elt2) {
        Element subElt = elt2.getElement();
        Op op = compile(subElt);    // "compile", not "compileElement" -- do simplification
        Expr expr = new E_Exists(subElt, op);
        return OpFilter.filter(expr, current);
    }

    protected Op compileElementMinus(Op current, ElementMinus elt2) {
        Op op = compile(elt2.getMinusElement());
        Op opMinus = OpMinus.create(current, op);
        return opMinus;
    }

    protected Op compileElementData(ElementData elt) {
        return OpTable.create(elt.getTable());
    }

    protected Op compileElementUnion(Op current, ElementUnion elt2) {
        // Special SPARQL 1.1 case.
        Op op = compile(elt2.getElements().get(0));
        Op opUnion = OpUnion.create(current, op);
        return opUnion;
    }

    protected Op compileElementOptional(Op current, ElementOptional eltOpt) {
        Element subElt = eltOpt.getOptionalElement();
        Op op = compileElement(subElt);

        ExprList exprs = null;
        if ( op instanceof OpFilter ) {
            OpFilter f = (OpFilter)op;
            // f = OpFilter.tidy(f); // Collapse filter(filter(..))
            Op sub = f.getSubOp();
            if ( sub instanceof OpFilter )
                broken("compile/Optional/nested filters - unfinished");
            exprs = f.getExprs();
            op = sub;
        }
        current = OpLeftJoin.create(current, op, exprs);
        return current;
    }

    protected Op compileElementLateral(Op current, ElementLateral eltLateral) {
        Element subElt = eltLateral.getLateralElement();
        Op op = compileElement(subElt);
        return OpLateral.create(current, op);
    }

    protected Op compileBasicPattern(BasicPattern pattern) {
        return new OpBGP(pattern);
    }

    protected Op compilePathBlock(PathBlock pathBlock) {
        // Empty path block : the parser does not generate this case.
        if ( pathBlock.size() == 0 )
            return OpLib.unit();

        // Always turns the most basic paths to triples.
        return PathLib.pathToTriples(pathBlock);
    }

    protected Op compileElementGraph(ElementNamedGraph eltGraph) {
        Node graphNode = eltGraph.getGraphNameNode();
        Op sub = compileElement(eltGraph.getElement());
        return new OpGraph(graphNode, sub);
    }

    protected Op compileElementService(ElementService eltService) {
        Node serviceNode = eltService.getServiceNode();
        Op sub = compileElement(eltService.getElement());
        return new OpService(serviceNode, sub, eltService, eltService.getSilent());
    }

    protected Op compileElementSubquery(ElementSubQuery eltSubQuery) {
        AlgebraGenerator gen = new AlgebraGenerator(context, subQueryDepth + 1);
        return gen.compile(eltSubQuery.getQuery());
    }

    // ----
    // Elements should appear in a ElementGroup - they operate on the input from the earlier part of the group.
    // If the query was created programmatically, the app or querybuilder may
    // have omitted the ElementGroup.
    // Deal with this by creating the same Op structure as a group-of-one with ElementFilter,
    // that is unit input.

    protected Op compileElementFilter(ElementFilter elt) {
        return OpFilter.filter(elt.getExpr(), OpTable.unit());
    }

    protected Op compileElementBind(ElementBind elt) {
        VarExprList varExpList = new VarExprList(elt.getVar(), elt.getExpr());
        return OpExtend.extend(OpTable.unit(), varExpList);
    }

    protected Op compileElementAssign(ElementAssign elt) {
        VarExprList varExpList = new VarExprList(elt.getVar(), elt.getExpr());
        return OpAssign.assign(OpTable.unit(), varExpList);
    }

    // ----

    /** Compile query modifiers */
    protected Op compileModifiers(Query query, Op pattern) {
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
        VarExprList projectVars = query.getProject();

        VarExprList exprs = new VarExprList();     // Assignments to be done.
        List<Var> vars = new ArrayList<>();     // projection variables

        Op op = pattern;

        // ---- GROUP BY

        if ( query.hasGroupBy() ) {
            // When there is no GroupBy but there are some aggregates, it's a group
            // of no variables.
            op = OpGroup.create(op, query.getGroupBy(), query.getAggregators());
        }

        // ---- Assignments from SELECT and other places (so available to ORDER and
        // HAVING)
        // Now do assignments from expressions
        // Must be after "group by" has introduced it's variables.

        // Look for assignments in SELECT expressions.
        if ( !projectVars.isEmpty() && !query.isQueryResultStar() ) {
            // Don't project for QueryResultStar so initial bindings show
            // through in SELECT *
            if ( projectVars.size() == 0 && query.isSelectType() )
                Log.warn(this, "No project variables");
            // Separate assignments and variable projection.
            for ( Var v : query.getProject().getVars() ) {
                Expr e = query.getProject().getExpr(v);
                if ( e != null ) {
                    Expr e2 = ExprLib.replaceAggregateByVariable(e);
                    exprs.add(v, e2);
                }
                // Include in project
                vars.add(v);
            }
        }

        // ---- Assignments from SELECT and other places (so available to ORDER and
        // HAVING)
        for ( Var v : exprs.getVars() ) {
            Expr e = exprs.getExpr(v);
            op = OpExtend.create(op, v, e);
        }

        // ---- HAVING
        if ( query.hasHaving() ) {
            for ( Expr expr : query.getHavingExprs() ) {
                // HAVING expression to refer to the aggregate via the variable.
                Expr expr2 = ExprLib.replaceAggregateByVariable(expr);
                op = OpFilter.filter(expr2, op);
            }
        }
        // ---- VALUES
        if ( query.hasValues() ) {
            Table table = TableFactory.create(query.getValuesVariables());
            for ( Binding binding : query.getValuesData() )
                table.addBinding(binding);
            OpTable opTable = OpTable.create(table);
            op = OpJoin.create(op, opTable);
        }

        // ---- ToList
        if ( context.isTrue(ARQ.generateToList) )
            // Listify it.
            op = new OpList(op);

        // ---- ORDER BY
        if ( query.getOrderBy() != null ) {
            List<SortCondition> scList = new ArrayList<>();

            // Aggregates in ORDER BY
            for ( SortCondition sc : query.getOrderBy() ) {
                Expr e = sc.getExpression();
                e = ExprLib.replaceAggregateByVariable(e);
                scList.add(new SortCondition(e, sc.getDirection()));

            }
            op = new OpOrder(op, scList);
        }

        // ---- PROJECT
        // No projection => initial variables are exposed.
        // Needed for CONSTRUCT and initial bindings + SELECT *

        if ( vars.size() > 0 )
            op = new OpProject(op, vars);

        // ---- DISTINCT
        if ( query.isDistinct() )
            op = OpDistinct.create(op);

        // ---- REDUCED
        if ( query.isReduced() )
            op = OpReduced.create(op);

        // ---- LIMIT/OFFSET
        if ( query.hasLimit() || query.hasOffset() )
            op = new OpSlice(op, query.getOffset() /* start */, query.getLimit()/* length */);

        return op;
    }

    // --------

    protected static Op join(Op current, Op newOp) {
        if ( simplifyTooEarlyInAlgebraGeneration && applySimplification )
            return OpJoin.createReduce(current, newOp);

        return OpJoin.create(current, newOp);
    }

    protected Op sequence(Op current, Op newOp) {
        return OpSequence.create(current, newOp);
    }

    protected Op union(Op current, Op newOp) {
        return OpUnion.create(current, newOp);
    }

    protected final void broken(String msg) {
        throw new ARQInternalErrorException(msg);
    }
}
