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

package org.apache.jena.sparql.syntax.syntaxtransform;

import java.util.Collection;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.syntax.*;

/**
 *  Variable usage check for {@link QueryTransformOps#transform}.
 */
public class QuerySyntaxSubstituteScope {

    /**
     * Check that the query can be transformed by replacing the variables with values.
     * For example, an assigned variables ({@code AS ?var}) can not be replace by a value.
     */
    public static void scopeCheck(Query query, Collection<Var> vars) {
        checkLevel(query, vars);
        checkPattern(query.getQueryPattern(), vars);
    }

    public static void checkPattern(Element element, Collection<Var> vars) {
        ElementVisitor visitor = new SubstituteScopeVisitor(vars);
        ElementWalker.walk(element, visitor);
    }

    private static void checkLevel(Query query, Collection<Var> vars) {
        checkAssignments("Query project expression", vars, query.getProject());
        checkAssignments("GROUP BY ", vars, query.getGroupBy());
        query.getAggregators().forEach(agg->{
            checkAssignment("Aggregator", vars, agg.getVar());
        });
    }

    private static void checkAssignments(String context, Collection<Var> vars, VarExprList varExprList) {
        varExprList.forEachVarExpr((v,e)->{
            if ( e != null )
                checkAssignment(context, vars, v);
        });
    }

    private static void checkAssignment(String context, Collection<Var> vars, Var assignedVar) {
        if ( vars.contains(assignedVar) )
            reject(context, assignedVar);
    }

    private static void reject(String elementName, Var badVar) {
        throw new QueryScopeException("Can not use "+badVar+" in this query");
    }

    private static class SubstituteScopeVisitor  extends ElementVisitorBase {

        private Collection<Var> vars;

        SubstituteScopeVisitor(Collection<Var> vars) {
            this.vars = vars;
        }

        // BOUND(?x) with no ?x in scope.
//    @Override
//    public void visit(ElementFilter el)         {
//        Set<Var> mentioned = el.getExpr().getVarsMentioned();
//        // EXISTS
//    }

        @Override
        public void visit(ElementAssign el)         {
            Var assignedVar = el.getVar();
            checkAssignment("LET", vars, assignedVar);
        }

        @Override
        public void visit(ElementBind el) {
            Var assignedVar = el.getVar();
            checkAssignment("BIND", vars, assignedVar);
        }

        @Override
        public void visit(ElementData el)           {
            var assignedVars = el.getVars();
            assignedVars.forEach(v->checkAssignment("VALUES", vars, v));
        }

//    @Override
//    public void visit(ElementExists el)         { }
//
//    @Override
//    public void visit(ElementNotExists el)      { }

        @Override
        public void visit(ElementSubQuery el)       {
            // Check project
            el.getQuery().getQueryPattern().visit(this);
        }
    }
}
