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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.BiMap;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.service.enhancer.impl.util.VarScopeUtils;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

/**
 * Class used to map a given scoped OpService to a normalized form. Several methods abbreviate
 * normalized with normed.
 * A normalized query with a non-empty set of variables always has variables at scope level 0.
 */
public class OpServiceInfo {
    // The original opService which is assumed to make use of scoped variables
    protected OpService opService;

    // Cache of the service node / var for easier access
    protected Node serviceNode;
    protected Var serviceVar;

    protected BiMap<Var, Var> mentionedSubOpVarsScopedToNormed;

    // The restored query of opService.getSubOp() without scoping and without slice
    protected Query normedQuery;

    // Compiled algebra of rawQuery; Algebra.compile(rawQuery)
    protected Op normedQueryOp;

    // Limit and offset that effectively applies to rawQuery
    protected long limit;
    protected long offset;

    // Mapping of visible variables of rawQuery to the visible (possibly scoped) ones in opService
    protected BiMap<Var, Var> visibleSubOpVarsScopedToNorm;

    public OpServiceInfo(OpService opService) {
        this.opService = opService ;

        this.serviceNode = opService.getService();
        this.serviceVar = serviceNode.isVariable() ? (Var)serviceNode: null;

        // Get the variables used in the service clause (excluding the possible one for the service iri)
        Op baseSubOp = opService.getSubOp();

        if (baseSubOp instanceof OpSlice) {
            OpSlice slice = (OpSlice)baseSubOp;
            baseSubOp = slice.getSubOp();
            this.offset = slice.getStart();
            this.limit = slice.getLength();
        } else {
            this.limit = Query.NOLIMIT;
            this.offset = Query.NOLIMIT;
        }

        Collection<Var> mentionedSubOpVars = OpVars.mentionedVars(baseSubOp);
        // mentionedSubOpVarsScopedToNormed = VarUtils.normalizeVarScopesGlobal(mentionedSubOpVars);
        mentionedSubOpVarsScopedToNormed = VarScopeUtils.normalizeVarScopes(mentionedSubOpVars);

        normedQueryOp = NodeTransformLib.transform(new NodeTransformSubst(mentionedSubOpVarsScopedToNormed), baseSubOp);

        // Handling of a null supOp - can that happen?
        Set<Var> visibleSubOpVars = OpVars.visibleVars(baseSubOp);
        this.visibleSubOpVarsScopedToNorm = VarScopeUtils.normalizeVarScopes(visibleSubOpVars);

        this.normedQuery = OpAsQuery.asQuery(normedQueryOp);

        VarExprList vel = normedQuery.getProject();
        VarExprList newVel = new VarExprList();

        int allocId = 0;
        for (Var var : vel.getVars()) {
            Expr expr = vel.getExpr(var);
            if (Var.isAllocVar(var)) {
                Var tmp = Var.alloc("__av" + (++allocId) + "__");
                mentionedSubOpVarsScopedToNormed.put(var, tmp);
                visibleSubOpVarsScopedToNorm.put(tmp, tmp);
                // visibleSubOpVarsScopedToPlain.put(var, tmp);
                // mentionedSubOpVarsScopedToPlain.put(var, tmp);
                var = tmp;
            }
            newVel.add(var, expr);
        }
        vel.clear();
        vel.addAll(newVel);
    }

    public OpService getOpService() {
        return opService;
    }

    public Node getServiceNode() {
        return serviceNode;
    }

    public Node getSubstServiceNode(Binding binding) {
        Node result = serviceVar == null ? serviceNode : binding.get(serviceVar);
        return result;
    }

    public Var getServiceVar() {
        return serviceVar;
    }

    public Query getNormedQuery() {
        return normedQuery;
    }

    public Op getNormedQueryOp() {
        return normedQueryOp;
    }

    public long getLimit() {
        return limit;
    }

    public long getOffset() {
        return offset;
    }

    public Set<Var> getVisibleSubOpVarsScoped() {
        return visibleSubOpVarsScopedToNorm.keySet();
    }

    public Map<Var, Var> getMentionedSubOpVarsScopedToNormed() {
        return mentionedSubOpVarsScopedToNormed;
    }

    public Map<Var, Var> getVisibleSubOpVarsNormedToScoped() {
        return visibleSubOpVarsScopedToNorm.inverse();
    }
}
