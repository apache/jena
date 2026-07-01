/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.tdb2.solver.index;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterDistinct;
import org.apache.jena.sparql.engine.iterator.QueryIterFilterExpr;
import org.apache.jena.sparql.engine.iterator.QueryIterGroup;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterProject;
import org.apache.jena.sparql.engine.join.JoinKey;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;
import org.apache.jena.tdb2.solver.BindingNodeId;
import org.apache.jena.tdb2.solver.BindingTDB;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.GraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.store.tupletable.TupleTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpExecutorTDB2SkipScan {
    private static final Logger logger = LoggerFactory.getLogger(OpExecutorTDB2SkipScan.class);

    /** Must only be called if hasNeededColumns returned true. */
    @SuppressWarnings("unchecked")
    public static IndexMatch computeIndexMatch(TupleMap tm, TuplePatternSpec patternSpec) {
        Node[] quad = patternSpec.tuple();
        boolean canDoDirectDistinct = true;

        // An index is only usable if covers all needed slots of the query tuple.
        // boolean[] uncoveredSlots = new boolean[queryLen];
        int uncoveredSlotsMask = patternSpec.neededSlotsMask();

        ValueFilter<Node>[] residualConditions = null;
        int numResidualConditions = 0;

        int i;
        int idxLen = tm.length();
        for (i = 0; i < idxLen; ++i) {
            int tupleSlot = tm.mapIdx(i);
            Node node = quad[tupleSlot];
            if (!node.isConcrete()) {
                break;
            }
            uncoveredSlotsMask &= ~(1 << tupleSlot);
        }
        int keyLen = i;

        // The next slots of the index must map to the projection slots
        // Any constant not matched by HEAD is considered projected and filtered on.
        int j; // additional needed value slots
        for (j = keyLen; j < idxLen && uncoveredSlotsMask != 0; ++j) {
            int tupleSlot = tm.mapIdx(j);
            Node node = quad[tupleSlot];
            if (node.isVariable()) {
                Var v = Var.alloc(node);
                uncoveredSlotsMask &= ~(1 << tupleSlot);

                // Consider: SELECT DISTINCT ?g ?p { GRAPH ?g { ?s :p ?s } }
                // PGSO and PGOS are suitable indices
                // If we pick PGOS we would evaluate the quality group for the S slot
                // So we test restriction based on the order of the original slot indices
                // S comes before O, so we test S = O instead of O = S
                // The important part is just: all members of equality groups must be covered.

                boolean isProjected = patternSpec.projection().contains(v);

                // If the variable in not projected, then we need to skip over it
                // This breaks simple distinct
                if (!isProjected) {
                    canDoDirectDistinct = false;
                }
            } else {
                if (residualConditions == null) {
                    residualConditions = new ValueFilter[idxLen - j];
                }

                // Add the constant node as a filter condition.
                residualConditions[numResidualConditions++] = new ValueFilter<>(j, node);
            }
        }
        int valLen = j - keyLen;

        if (uncoveredSlotsMask != 0) {
            return null;
        }

        return new IndexMatch(canDoDirectDistinct, residualConditions, numResidualConditions, keyLen, valLen);
    }

    public static QueryIterator tryExec(PatternQuery patternQuery, QueryIterator input, ExecutionContext execCxt) {
        QueryIterator qIter = null;
        Node[] testTuple = patternQuery.tuple();
        NodeTupleTable table;
        if (testTuple.length == 3) {
            GraphTDB graph = (GraphTDB)execCxt.getActiveGraph();
            table = graph.getNodeTupleTable();
        } else if (testTuple.length == 4) {
            DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset();
            table = ds.getQuadTable().getNodeTupleTable();
        } else {
            throw new IllegalStateException("Unexpected tuple length: " + testTuple.length);
        }

        qIter = QueryIter.flatMap(input, b -> {
            PatternQuery subst = PatternQuery.substitute(patternQuery, b);
            JoinKey proj = subst.project();
            Node[] tuple = subst.tuple();
            TuplePatternSpec lookup = TuplePatternSpec.create(tuple, proj);
            return tryExec(table, patternQuery.distinct(), lookup, execCxt);
        }, execCxt);
        return qIter;
    }

    public static QueryIterator tryExec(NodeTupleTable nodeTupleTable, boolean distinct, TuplePatternSpec lookup, ExecutionContext execCxt) {
        TupleTable tupleTable = nodeTupleTable.getTupleTable();
        NodeTable nodeTable = nodeTupleTable.getNodeTable();

        TupleIndex bestIndex = null;
        IndexMatch bestMatch = null;

        for (TupleIndex tupleIndex : tupleTable.getIndexes()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Trying index: " + tupleIndex);
            }

            // Only consider indexes backed by a BPlusTree
            if (tupleIndex instanceof TupleIndexRecord recordIdx) {
                RangeIndex rangeIndex = recordIdx.getRangeIndex();
                if (!(rangeIndex instanceof BPlusTree)) {
                    continue;
                }
            }

            TupleMap tm = tupleIndex.getMapping();
            IndexMatch match = computeIndexMatch(tm, lookup);

            if (match == null) {
                // Index is unsuitable because it could not cover all required slots.
                continue;
            }

            int d = IndexMatch.COMPARATOR.compare(match, bestMatch);
            if (d < 0) {
                bestIndex = tupleIndex;
                bestMatch = match;
            }
        }

        // In the end we must build a tuple with the constants
        if (logger.isDebugEnabled()) {
            logger.debug("Best matching index: " + bestIndex);
        }

        if (bestIndex != null && bestMatch != null) { // Redundancy: bestIndex != null actually implies bestMatch != null
            TupleMap tm = bestIndex.getMapping();
            IndexMap im = tryComputeIndexMap(lookup, bestMatch, tm, nodeTable);
            if (im == null) {
                // If null then at least one Node had no corresponding NodeId in the nodeTable.
                return new QueryIterNullIterator(execCxt);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("IndexMap Match Data: " + im);
                logger.debug("IndexMap Projection: " + Arrays.toString(im.proj()));
                logger.debug("IndexMap Conditions: " + Arrays.toString(im.residualConditions()));
                logger.debug("IndexMap Links: " + Arrays.toString(im.equalityLinks()));
                logger.debug("IndexMap Query Tuple: " + Arrays.asList(im.tuple()));
            }

            QueryIterator r = exec(bestIndex, nodeTable, lookup.projection(), im, execCxt);

            if (distinct && !bestMatch.canDoDirectDistinct()) {
                r = new QueryIterDistinct(r, null, execCxt);
            }
            return r;
        }
        return null;
    }

    /**
     * Actual execution.
     *
     * @param tupleIndex
     * @param nodeTable
     * @param projectVars
     * @param map
     * @param execCxt
     * @return
     */
    @SuppressWarnings("removal")
    public static QueryIterator exec(TupleIndex tupleIndex, NodeTable nodeTable, JoinKey projectVars, IndexMap map, ExecutionContext execCxt) {
        // Cast validity of the arguments is assumed to be ensured!
        TupleIndexRecord tupleIndexRecord = (TupleIndexRecord)tupleIndex;
        RangeIndex rangeIndex = tupleIndexRecord.getRangeIndex();
        BPlusTree bpt = (BPlusTree)rangeIndex;

        int n = map.tuple().length;
        Tuple<NodeId> pattern = TupleFactory.create(map.tuple());

        Tuple<NodeId> minPattern = NodeIdUtils.anyToMin(pattern);
        Tuple<NodeId> maxPattern = NodeIdUtils.anyToMax(pattern);

        RecordFactory rf = rangeIndex.getRecordFactory();
        Record minRecord = record(rf, minPattern);
        Record maxRecord = record(rf, maxPattern);

        // VarMap[] proj = map.proj;
        ValueFilter<NodeId>[] residualConditions = map.residualConditions();
        EqualityLink[] equalityLinks = map.equalityLinks();

        Iterator<BindingNodeId> bindingIdIt;

        VarMap[] finalProj;
        Expr filterExpr = null;
        if (equalityLinks == null) {
            finalProj = map.proj();
        } else {
            // For linked slots, such as {?s :p ?s} introduce dummy non-distinguished vars.
            // [?s :p ?s] -> {?s :p ?.foo} FILTER(?s = ?.foo)

            // The variables of the equalityLinks may not have been projected yet.
            // So we must compute a proper projection.

            VarMap[] finalProjTmp = new VarMap[map.proj().length + 2 * equalityLinks.length];
            System.arraycopy(map.proj(), 0, finalProjTmp, 0, map.proj().length);
            VarAlloc va = new VarAlloc(ARQConstants.allocVarAnonMarker + "X");
            int i = map.proj().length;
            Set<Var> seenVars = null;
            for (EqualityLink entry : equalityLinks) {
                int primaryIdx = entry.equalToIdx();
                Var primaryVar = (Var)map.nodeTuple()[primaryIdx];
                if (!projectVars.contains(primaryVar) && (seenVars == null || !seenVars.contains(primaryVar))) {
                    if (seenVars == null) {
                        seenVars = new HashSet<>();
                    }
                    seenVars.add(primaryVar);
                    finalProjTmp[i++] = new VarMap(primaryIdx, primaryVar);
                }

                int secondaryIdx = entry.idx();
                // Var v = (Var)map.nodeTuple[secondaryIdx]; -> sameAs primaryVar
                Var extraVar = va.allocVar();
                finalProjTmp[i++] = new VarMap(secondaryIdx, extraVar);

                Expr contrib = new E_Equals(new ExprVar(primaryVar), new ExprVar(extraVar));
                filterExpr = logicalAnd(filterExpr, contrib);
            }
            finalProj = Arrays.copyOf(finalProjTmp, i);
        }

        Iterator<Record> recordIt = bpt.distinctByKeyPrefix(n * NodeId.SIZE, minRecord, maxRecord);

        // Filter by recheck conditions first.
        if (residualConditions != null) {
            recordIt = Iter.filter(recordIt, record -> {
                execCxt.checkCancelSignal(); // TODO Rare case where deprecated checkCancelSignal actually makes sense?

                byte[] recordKey = record.getKey();
                for (int i = 0; i < residualConditions.length; ++i) {
                    ValueFilter<NodeId> condition = residualConditions[i];
                    int idxx = condition.idx();
                    NodeId expected = condition.value();
                    NodeId actual = extractNodeId(recordKey, idxx);
                    if (!expected.equals(actual)) {
                        return false;
                    }
                }
                return true;
            });
        }

        // Build the (initial) BindingNodeId
        bindingIdIt = Iter.map(recordIt, record -> {
            byte[] recordKey = record.getKey();
            BindingNodeId b = new BindingNodeId();
            for (VarMap varMap : finalProj) {
                NodeId nodeId = extractNodeId(recordKey, varMap.idx());
                b.put(varMap.var(), nodeId);
            }
            return b;
        });

        Iterator<Binding> bindingIt = Iter.map(bindingIdIt, bid -> new BindingTDB(bid, nodeTable));
        QueryIterator qIter = QueryIterPlainWrapper.create(bindingIt, execCxt);

        // Apply filters if needed.
        // Presence of filters also implies allocated variables that need projecting away.
        if (filterExpr != null) {
            qIter = new QueryIterFilterExpr(qIter, filterExpr, execCxt);

            // XXX QueryIterDistinguishedVars?
            qIter = QueryIterProject.create(qIter, projectVars, execCxt);
        }

        return qIter;
    }

    /**
     * Maps Nodes to NodeIds. Returns null if any constraint is unsatisfiable
     * due to lack of a corresponding node id.
     */
    public static IndexMap tryComputeIndexMap(TuplePatternSpec lookup, IndexMatch match, TupleMap tm, NodeTable nodeTable) {
        // Keep track of the highest needed slot of the index
        // int neededSlots = keyLen + valLen;
        Node[] quad = lookup.tuple();
        int neededSlots = match.numNeededSlots();

        Node[] nodeTuple = new Node[neededSlots]; // The query tuple reordered for the index.
        for (int i = 0; i <neededSlots; ++i) {
            int tupleSlot = tm.mapIdx(i);
            Node node = quad[tupleSlot];
            nodeTuple[i] = node;
        }

        NodeId[] tuple = new NodeId[neededSlots];

        int x;
        for (x = 0; x < neededSlots; ++x) {
            int tupleSlot = tm.mapIdx(x);
            Node node = quad[tupleSlot];
            if (!node.isConcrete()) {
                break;
            }
            NodeId nodeId = nodeTable.getNodeIdForNode(node);

            if (NodeId.isDoesNotExist(nodeId)) {
                // Empty result set
                return null;
            }

            tuple[x] = nodeId;
        }

        for (int y = x; y < neededSlots; ++y) {
            tuple[y] = NodeId.NodeIdAny;
        }

        ValueFilter<NodeId>[] mappedConditions = null;
        EqualityLink[] mappedLinks = null;
        // mappedLinks = mapLinks(match., numProjectSlots, tm);

        if (match.residualConditions() != null) {
            mappedConditions = mapValueConditions(match.residualConditions(), match.numResidualConditions(), tm, nodeTable);

            // Returns null if nodes could not be mapped
            if (mappedConditions == null) {
                // skip
                return null;
            }
        }

        if (lookup.equalityLinks() != null) {
            mappedLinks = mapEqualityLinks(lookup.equalityLinks(), lookup.equalityLinks().length, tm);
        }

        VarMap[] newProj = mapProjection(lookup, tm);
        return new IndexMap(nodeTuple, tuple, newProj, mappedConditions, mappedLinks);
    }

    private static Expr logicalAnd(Expr base, Expr contrib) {
        return base == null ? contrib : new E_LogicalAnd(base, contrib);
    }

    private static NodeId extractNodeId(byte[] bytes, int index) {
        NodeId nodeId = NodeIdFactory.get(bytes, index * NodeId.SIZE);
        return nodeId;
    }

    public static VarMap[] mapProjection(TuplePatternSpec lookup, TupleMap tupleMap) {
        int[] proj = lookup.projs();
        int n = proj.length;
        VarMap[] result = new VarMap[n];
        for (int i = 0; i < n; ++i) {
            int tupleIdx = proj[i];
            Node node = lookup.tuple()[tupleIdx];
            Var var = Var.alloc(node);
            int idx = tupleMap.unmapIdx(tupleIdx);
            result[i] = new VarMap(idx, var);
        }
        return result;
    }

    public static EqualityLink[] mapEqualityLinks(EqualityLink[] links, int n, TupleMap tupleMap) {
        EqualityLink[] result = new EqualityLink[n];
        for (int i = 0; i < n; ++i) {
            EqualityLink link = links[i];
            int primary = tupleMap.unmapIdx(link.idx());
            int secondary = tupleMap.unmapIdx(link.equalToIdx());
            result[i] = new EqualityLink(primary, secondary);
        }
        return result;
    }

    // returns null if any condition could not be mapped.
    public static ValueFilter<NodeId>[] mapValueConditions(ValueFilter<Node>[] conditions, int n, TupleMap tupleMap, NodeTable nodeTable) {
        @SuppressWarnings("unchecked")
        ValueFilter<NodeId>[] result = new ValueFilter[n];
        for (int i = 0; i < n; ++i) {
            ValueFilter<Node> condition = conditions[i];
            int slot = tupleMap.unmapIdx(condition.idx());
            NodeId nodeId = nodeTable.getNodeIdForNode(condition.value());
            if (nodeId == null) {
                return null;
            }
            result[i] = new ValueFilter<>(slot, nodeId);
        }
        return result;
    }

    /** Create a record directly from the tuple (without a tuple map) */
    private static Record record(RecordFactory factory, Tuple<NodeId> tuple) {
        int n = tuple.len();
        byte[] b = new byte[n * NodeId.SIZE];
        for (int i = 0; i < n ; i++) {
            NodeIdFactory.set(tuple.get(i), b, i * NodeId.SIZE);
        }
        return factory.create(b);
    }

    /** −−− Execution of OpDistinct −−− */

    public static QueryIterator tryExec(OpDistinct opDistinct, QueryIterator input, ExecutionContext execCxt) {
        PatternQuery patternQuery = PatternQuery.createOrNull(opDistinct);
        if (patternQuery != null) {
            QueryIterator qIter = OpExecutorTDB2SkipScan.tryExec(patternQuery, input, execCxt);
            if (qIter != null) {
                return qIter;
            }
        }
        return null;
    }

    /** −−− Execution of OpGroupBy −−− */

    public static QueryIterator tryExec(OpGroup opGroup, QueryIterator input, ExecutionContext execCxt) {
        if (opGroup.getGroupVars().getExprs().isEmpty() && opGroup.getAggregators().size() == 1) {
            // We come here iff there is only grouping by simple variables (no expressions)
            // and a single aggregator.
            Var v = distinctVarOrNull(opGroup.getAggregators().getFirst().getAggregator());
            if (v != null) {
                JoinKey newProj = JoinKey.newBuilder()
                    .addAll(opGroup.getGroupVars().getVars())
                    .add(v)
                    .build();
                Op newOp = new OpDistinct(new OpProject(opGroup.getSubOp(), newProj));

                PatternQuery patternQuery = PatternQuery.createOrNull(newOp);
                if (patternQuery != null) {
                    QueryIterator qIter = OpExecutorTDB2SkipScan.tryExec(patternQuery, input, execCxt);

                    if (qIter != null) {
                        // Remove redundant distinct - it is ensured by the pattern execution.
                        List<ExprAggregator> newAggs = List.of(convertToNonDistinct(opGroup.getAggregators().getFirst()));
                        qIter = new QueryIterGroup(qIter, opGroup.getGroupVars(), newAggs, execCxt);
                        return qIter;
                    }
                }
            }
        }
        return null;
    }

    private static Var distinctVarOrNull(Aggregator agg) {
        Var v = null;
        if (agg instanceof AggCountVarDistinct acvd) {
            v = varOrNull(acvd); // ExprList guaranteed to have exactly one expr.
        }
        return v;
    }

    private static ExprAggregator convertToNonDistinct(ExprAggregator eAgg) {
        Aggregator newAgg = convertToNonDistinct(eAgg.getAggregator());
        return new ExprAggregator(eAgg.getVar(), newAgg);
    }

    private static Aggregator convertToNonDistinct(Aggregator agg) {
        if (agg instanceof AggCountVarDistinct acd) {
            return AggregatorFactory.createCountExpr(false, acd.getExprList().get(0));
        }
        return agg;
    }

    /** Extract the first argument of the agg's exprList. */
    private static Var varOrNull(Aggregator agg) {
        ExprList el = agg.getExprList();
        Expr e = el.get(0);
        Var v = e.isVariable() ? e.asVar() : null;
        return v;
    }
}
