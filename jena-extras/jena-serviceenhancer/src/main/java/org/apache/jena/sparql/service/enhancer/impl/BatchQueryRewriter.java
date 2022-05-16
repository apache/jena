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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.service.enhancer.impl.util.BindingUtils;

/**
 * Rewriter for instantiating a query such that a list of initial bindings are injected.
 * In general, there are several rewriting strategies for that purpose and their applicability
 * depends on the operations used in of the query:
 *
 * <ul>
 * <li>Union/Substitution strategy: The is perhaps the most reliable (and also most verbose) strategy:
 * It creates a union query where for every input binding a union member is obtained by
 * substituting the original query with it</li>
 * <li>Join strategy: The input bindings are collected into a VALUES block and placed on the left hand size
 * of a join with an adjusted version of the original query - not yet supported</li>
 * <li>Filter strategy: Input bindings are turned into a disjunctive filter expression - not yet supported</li>
 * </ul>
 */
public class BatchQueryRewriter {
    protected OpServiceInfo serviceInfo;
    protected Var idxVar;

    /** Whether it can be assumed that union yields the bindings of the members in the
     * order those members are specified.
     * If false then ORDER BY ASC(?__idx__) is appended to the created query */
    protected boolean sequentialUnion;

    /**
     * Whether bindings returned by union members with ORDER BY remain sorted
     * If false then sort conditions are added to the outer query
     */
    protected boolean orderRetainingUnion;


    /** Whether to omit the end marker */
    protected boolean omitEndMarker;

    /** Constant to mark end of a batch (could also be dynamically set to one higher then the idx in a batch) */
    static int REMOTE_END_MARKER = 1000000000;
    static NodeValue NV_REMOTE_END_MARKER = NodeValue.makeInteger(REMOTE_END_MARKER);

    /** True if either local or remote end marker */
//    public static boolean isLocalOrRemoteEndMarker(int id) {
//        return isRemoteEndMarker(id) || isLocalEndMarker(id);
//    }

    public static boolean isRemoteEndMarker(int id) {
        return id == REMOTE_END_MARKER;
    }

    public static boolean isRemoteEndMarker(Integer id) {
        return Objects.equals(id, REMOTE_END_MARKER);
    }



    // Local end marker is not returned by the remote service
//    static int LOCAL_END_MARKER = 1000000001;
//    static NodeValue NV_LOCAL_END_MARKER = NodeValue.makeInteger(LOCAL_END_MARKER);
//
//    public static boolean isLocalEndMarker(int id) {
//        return id == LOCAL_END_MARKER;
//    }
//
//    public static boolean isLocalEndMarker(Integer id) {
//        return Objects.equals(id, LOCAL_END_MARKER);
//    }


    public BatchQueryRewriter(OpServiceInfo serviceInfo, Var idxVar,
            boolean sequentialUnion, boolean orderRetainingUnion,
            boolean omitEndMarker) {
        super();
        this.serviceInfo = serviceInfo;
        this.idxVar = idxVar;
        this.sequentialUnion = sequentialUnion;
        this.orderRetainingUnion = orderRetainingUnion;
        this.omitEndMarker = omitEndMarker;
    }

    /** The index var used by this rewriter */
    public Var getIdxVar() {
        return idxVar;
    }

    public static Set<Var> seenVars(Collection<PartitionRequest<Binding>> batchRequest) {
        Set<Var> result = new LinkedHashSet<>();
        batchRequest.forEach(br -> BindingUtils.addAll(result, br.getPartitionKey()));
        return result;
    }

    public BatchQueryRewriteResult rewrite(Batch<Integer, PartitionRequest<Binding>> batchRequest) {

        Op newOp = null;
        List<Entry<Integer, PartitionRequest<Binding>>> es = new ArrayList<>(batchRequest.getItems().entrySet());
        Collections.reverse(es);

        Query normQuery = serviceInfo.getNormedQuery();
        Op normOp = serviceInfo.getNormedQueryOp();

        // Prepare the sort conditions
        List<SortCondition> sortConditions = new ArrayList<>();
        List<SortCondition> localSortConditions =
                Optional.ofNullable(normQuery.getOrderBy()).orElse(Collections.emptyList());

        boolean noOrderNeeded =
                orderRetainingUnion || sequentialUnion && localSortConditions.isEmpty();

        boolean orderNeeded = !noOrderNeeded;

            // No ordering by index needed
        if (orderNeeded) {
            SortCondition sc = new SortCondition(new ExprVar(idxVar), Query.ORDER_ASCENDING);
            sortConditions.add(sc);
        }

        sortConditions.addAll(localSortConditions);

        for (Entry<Integer, PartitionRequest<Binding>> e : es) { // batchRequest.getItems().entrySet()) {

            PartitionRequest<Binding> req = e.getValue(); // batchRequest.get(i);
            long idx = e.getKey();
            Binding scopedBinding = req.getPartitionKey();

            Set<Var> scopedBindingVars = BindingUtils.varsMentioned(scopedBinding);

            Map<Var, Var> varMapScopedToNormed = ServiceCacheKeyFactory
                    .createJoinVarMapScopedToNormed(serviceInfo, scopedBindingVars);

            // Binding plainBinding = BindingUtils.renameKeys(scopedBinding, serviceInfo.getMentionedSubOpVarsScopedToPlain());
            Binding normedBinding = BindingUtils.renameKeys(scopedBinding, varMapScopedToNormed);

            // Op op = serviceInfo.getNormedQueryOp();
            Op op = normOp;

            // Note: QC.substitute does not remove variables being substituted from projections
            //   This may cause unbound variables to be projected

            // If the union is sequential and order retaining we can retain the order on the members
            // otherwise, we can remove any ordering on the member
            if ((sequentialUnion && orderRetainingUnion) || localSortConditions.isEmpty()) {
                // If the union is sequential and order retaining we can retain the order on the members
                // otherwise, we can remove any ordering on the member
            } else {
                // Member order may not be retained - remove it from the query

                // TODO This should be done once OpServiceInfo
                Query tmp = normQuery.cloneQuery();
                if (tmp.hasOrderBy()) {
                    tmp.getOrderBy().clear();
                }

                op = Algebra.compile(tmp);
                // TODO Something is odd with ordering here
                // Add the sort conditions
                // op = new OpOrder(op, localSortConditions);
            }

            op = QC.substitute(op, normedBinding);
            op = OpExtend.create(op, idxVar, NodeValue.makeInteger(idx));

            long o = req.hasOffset() ? req.getOffset() : Query.NOLIMIT;
            long l = req.hasLimit() ? req.getLimit() : Query.NOLIMIT;

            if (o != Query.NOLIMIT || l != Query.NOLIMIT) {
                op = new OpSlice(op, o, l);
            }

            newOp = newOp == null ? op : OpUnion.create(op, newOp);
        }

        if (!omitEndMarker) {
            Op endMarker = OpExtend.create(OpTable.unit(), idxVar, NV_REMOTE_END_MARKER);
            newOp = newOp == null ? endMarker : OpUnion.create(newOp, endMarker);
        }

        if (orderNeeded) {
            newOp = new OpOrder(newOp, sortConditions);
        }

        Query q = OpAsQuery.asQuery(newOp);

        Log.info(BatchQueryRewriter.class, "Rewritten bulk query: " + q);

        // Add a rename for idxVar so that QueryIter.map does not omit it
        Map<Var, Var> renames = new HashMap<>(serviceInfo.getVisibleSubOpVarsNormedToScoped());
        renames.put(idxVar, idxVar);
        return new BatchQueryRewriteResult(newOp, renames);
    }
}
