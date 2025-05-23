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

package org.apache.jena.sparql.lang;

import java.util.*;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.*;

/** Calculate in-scope variables from the AST */
public class SyntaxVarScope {
    // @formatter:off
    /* SPARQL 1.1 "in scope" rules These define the variables from a pattern that are
     * in-scope These are not the usage rules.
     *
     * <a href="https://www.w3.org/TR/sparql11-query/#variableScope">18.2.1 Variable Scope</a>
     *
     * Syntax Form In-scope variables
     * <pre>
     * Basic Graph Pattern (BGP) v occurs in the BGP
     * Path                      v occurs in the path
     * Group { P1 P2 ... }       v is in-scope if in-scope in one or more of P1, P2, ...
     * GRAPH term { P }          v is term or v is in-scope in P
     * { P1 } UNION { P2 }       v is in-scope in P1 or in-scope in P2
     * OPTIONAL {P}              v is in-scope in P
     * SERVICE term {P}          v is term or v is in-scope in P
     * (expr AS v) for BIND, SELECT and GROUP BY   v is in-scope
     * SELECT ..v .. { P }       v is in-scope if v is mentioned as a project variable
     * SELECT * { P }            v is in-scope in P
     * VALUES var (values)       v is in-scope
     * VALUES varlist (values)   v is in-scope if v is in varlist
     * </pre>
     */
    //@formatter:on

    /**
     * Apply the SPARQL scope rules to a query.
     * Throw {@link QueryParseException} if there is a violation.
     */
    public static void check(Query query) {
        Element queryPattern = query.getQueryPattern();
        if ( queryPattern == null ) {
            // DESCRIBE may not have a pattern in which case there are no checks to perform.
            return;
        }

        checkElement(queryPattern);

        // Check scoping at this level (SELECT, GROUP BY)
        // Does not include variables in trailing VALUES.
        // A trailing VALUES is joined to the query results, including SELECT clause,
        // and so does not affect scope at this level.
        Collection<Var> vars = PatternVars.vars(query.getQueryPattern());

        // SELECT expressions
        checkExprListAssignment(vars, query.getProject());

        // Check for SELECT * GROUP BY
        // Legal in ARQ, not in SPARQL 1.1, 1.2
        if ( !Syntax.syntaxARQ.equals(query.getSyntax()) ) {
            if ( query.isQueryResultStar() && query.hasGroupBy() )
                throw new QueryParseException("SELECT * not legal with GROUP BY", -1, -1);
        }

        // Check any variable in an expression is in scope (if GROUP BY)
        checkExprVarGroupBy(query);
    }

    /**
     * @deprecated use {@link #checkElement}
     */
    @Deprecated(forRemoval = true)
    public static void check(Element queryPattern) { checkElement(queryPattern); }

    /**
     * Apply the SPARQL scope rules to a query element (part or all of a WEHERE clause).
     * Throw {@link QueryParseException} if there is a violation.
     */
    public static void checkElement(Element queryPattern) {
        checkSubQuery(queryPattern);
        checkPatternAssign(queryPattern);
    }

    // Check assignment forms that require a new variable.
    // BIND and FIND
    private static void checkPatternAssign(Element queryPattern) {
        VarScopeChecker v = new VarScopeChecker();
        ElementWalker.walk(queryPattern, v);
    }

    // Check sub-query by finding sub-queries and recursively checking.
    // Includes applying all checks to nested sub-queries.
    private static void checkSubQuery(Element el) {
        ElementVisitor v = new SubQueryScopeChecker();
        ElementWalker.walk(el, v);
    }

    private static void checkExprListAssignment(Collection<Var> vars, VarExprList exprList) {
        Set<Var> vars2 = new LinkedHashSet<>(vars);
        exprList.forEachExpr((v, e) -> {
            Set<Var> varInExpr = e.getVarsMentioned();
            // Include mentioned variables
            // These may be unused in the query (in vars) but still contribute.
            vars2.addAll(varInExpr);
            checkExpr(vars2, e, v);
            vars2.add(v);
        });
    }

    private static void checkExprVarGroupBy(Query query) {
        if ( query.hasGroupBy() ) {
            VarExprList groupKey = query.getGroupBy();

            // Copy - we need to add variables
            // SELECT (count(*) AS ?C) (?C+1 as ?D)
            List<Var> inScopeVars = new ArrayList<>(groupKey.getVars());
            VarExprList exprList = query.getProject();

            for ( Var v : exprList.getVars() ) {
                // In scope?
                Expr e = exprList.getExpr(v);
                if ( e == null ) {
                    if ( !inScopeVars.contains(v) ) {
                        throw new QueryParseException("Non-group key variable in SELECT: " + v, -1, -1);
                    }
                } else {
                    Set<Var> eVars = e.getVarsMentioned();
                    for ( Var v2 : eVars ) {
                        if ( !inScopeVars.contains(v2) ) {
                            throw new QueryParseException("Non-group key variable in SELECT: " + v2 + " in expression " + e, -1, -1);
                        }
                    }
                }
                inScopeVars.add(v);
            }
        }
    }

    private static void checkExpr(Collection<Var> scope, Expr expr, Var var) {
        // Project SELECT ?x
        if ( expr == null )
            return;
        // expr not null
        if ( scope.contains(var) )
            throw new QueryParseException("Variable used when already in-scope: "+var+" in "+fmtAssignment(expr, var), -1 , -1);

        // test for impossible variables - bound() is a bit odd.
        if ( false ) {
            Set<Var> vars = expr.getVarsMentioned();
            for ( Var v : vars ) {
                if ( !scope.contains(v) )
                    throw new QueryParseException("Variable used in expression is not in-scope: " + v + " in " + expr, -1, -1);
            }
        }
    }

//    private static String fmtExprList(VarExprList exprList) {
//        StringBuilder sb = new StringBuilder();
//        boolean first = true;
//        for ( Var v : exprList.getVars() ) {
//            Expr e = exprList.getExpr(v);
//            if ( !first ) {
//                sb.append(" ");
//            }
//            first = false;
//            sb.append("(").append(e).append(" AS ").append(v).append(")");
//        }
//        return sb.toString();
//    }

    private static String fmtAssignment(Expr expr, Var var) {
        return "(" + expr + " AS " + var + ")";
    }

    /** Visitor for subqueries scope rules . */
    private static class SubQueryScopeChecker extends ElementVisitorBase {
        @Override
        public void visit(ElementSubQuery el) {
            Query query = el.getQuery();
            // Recursively check sub-queries in sub-queries.
            check(el.getQuery());
        }
    }

    // Applies scope rules at each point it matters.
    // Does some recalculation in nested structures.

    public static class VarScopeChecker extends ElementVisitorBase {
        VarScopeChecker() {}

        @Override
        public void visit(ElementGroup el) {
            // BIND scope rules
            // UNFOLD scope rules
            // (and service warning)

            for ( int i = 0 ; i < el.size() ; i++ ) {
                Element e = el.get(i);
                // Tests.
                if ( e instanceof ElementBind eltBind) {
                    Collection<Var> accScope = calcScopeAll(el.getElements(), i);
                    checkBIND(accScope, eltBind);
                }
                if ( e instanceof ElementUnfold ) {
                    Collection<Var> accScope = calcScopeAll(el.getElements(), i);
                    checkUNFOLD(accScope, (ElementUnfold)e);
                }
                if ( e instanceof ElementService eltSvc ) {
                    Collection<Var> accScope = calcScopeAll(el.getElements(), i);
                    checkSERVICE(accScope, eltSvc);
                }
                if ( e instanceof ElementLateral eltLat) {
                    Collection<Var> accScope = calcScopeAll(el.getElements(), i);
                    checkLATERAL(accScope, eltLat);
                }
            }
        }

        private static Collection<Var> calcScopeAll(List<Element> elements, int idx) {
            return calcScope(elements, 0, idx);
        }

        /** Calculate scope, working forwards */
        private static Collection<Var> calcScope(List<Element> elements, int start, int finish) {
            Collection<Var> accScope = new HashSet<>();
            for ( int i = start; i < finish; i++ ) {
                Element e = elements.get(i);
                PatternVars.vars(accScope, e);
            }
            return accScope;
        }

        private static void checkBIND(Collection<Var> scope, ElementBind el) {
            Var var = el.getVar();
            if ( scope.contains(var) )
                throw new QueryParseException("BIND: Variable used when already in-scope: " + var + " in " + el, -1, -1);
            checkExpr(scope, el.getExpr(), var);
        }

        private static void checkUNFOLD(Collection<Var> scope, ElementUnfold el) {
            Var var1 = el.getVar1();
            if ( scope.contains(var1) )
                throw new QueryParseException("UNFOLD: Variable used when already in-scope: " + var1 + " in " + el, -1, -1);

            Var var2 = el.getVar2();
            if ( var2 != null && scope.contains(var2) )
                throw new QueryParseException("UNFOLD: Variable used when already in-scope: " + var2 + " in " + el, -1, -1);

            checkExpr(scope, el.getExpr(), var1);
        }

        private static void checkSERVICE(Collection<Var> scope, ElementService el) {
            if ( ARQ.isStrictMode() && el.getServiceNode().isVariable() ) {
                Var var = Var.alloc(el.getServiceNode());
                if ( !scope.contains(var) )
                    throw new QueryParseException("SERVICE: Variable not already in-scope: " + var + " in " + el, -1, -1);
            }
        }

        private void checkLATERAL(Collection<Var> accScope, Element el) {
            // Look for BIND/VALUES/SELECT-AS inside LATERAL
            ElementVisitor checker = new ElementVisitorBase() {
                @Override
                public void visit(ElementBind eltBind) {
                    if ( accScope.contains(eltBind.getVar()) )
                        throw new QueryParseException("BIND: Variable " + eltBind.getVar() + " defined", -1, -1);
                }
                // @Override public void visit(ElementAssign eltAssign) {} -- LET -
                // always OK

                @Override
                public void visit(ElementData eltData) {
                    eltData.getVars().forEach(v -> {
                        if ( accScope.contains(v) )
                            throw new QueryParseException("VALUES: Variable " + v + " defined", -1, -1);
                    });
                }

                @Override
                public void visit(ElementSubQuery eltSubQuery) {

                    // Only called when there is an expression.
                    eltSubQuery.getQuery().getProject().forEachExpr((var, expr) -> {
                        if ( accScope.contains(var) )
                            throw new QueryParseException("SELECT: Variable " + var + " defined", -1 ,-1);
                    });
                    // Check inside query pattern
                    Query subQuery = eltSubQuery.getQuery();
                    Collection<Var> accScope2 = accScope;
                    if ( ! subQuery.isQueryResultStar() ) {
                        List<Var> projectVars = eltSubQuery.getQuery().getProject().getVars();
                        // Copy
                        accScope2 = new ArrayList<>(accScope);
                        // Calculate variables passed down : remove any that are not in the project vars
                        // Any reused name will be renamed apart later.
                        accScope2.removeIf(v->!projectVars.contains(v));
                    }
                    if ( accScope2.isEmpty() )
                        // No work to do.
                        return;
                    Element el2 = eltSubQuery.getQuery().getQueryPattern();
                    checkLATERAL(accScope2, el2);
                }
            };

            // Does not walk into subqueries but we need to change the scope for sub-queries.
            ElementWalker.walk(el, checker);
        }
    }
}
