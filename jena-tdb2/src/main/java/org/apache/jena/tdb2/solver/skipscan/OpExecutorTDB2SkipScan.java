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

package org.apache.jena.tdb2.solver.skipscan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterDistinct;
import org.apache.jena.sparql.engine.iterator.QueryIterFilterExpr;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterProject;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.util.Symbol;
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
import org.apache.jena.tdb2.sys.SystemTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpExecutorTDB2SkipScan {
    private static final Logger logger = LoggerFactory.getLogger(OpExecutorTDB2SkipScan.class);

    public static final Symbol symBestIndexCache = SystemTDB.allocSymbol("bestIndexCache");

    public static QueryIterator exec(PatternQuery patternQuery, QueryIterator input, ExecutionContext execCxt, OpExecutor opExecutor) {
        // Set up a cache that maps the cacheKey(queryPattern) to the best index.
        // See PatternQuery.toCacheKey()
        Map<PatternQuery, SkipScanCandidate> indexCache = execCxt.getContext()
            .computeIfAbsent(symBestIndexCache, sym -> new ConcurrentHashMap<>());

        QueryIterator qIter = QueryIter.flatMap(input, binding -> {
            return exec(patternQuery, binding, execCxt, opExecutor, indexCache);
        }, execCxt);
        return qIter;
    }

    private static QueryIterator exec(PatternQuery patternQuery, Binding binding, ExecutionContext execCxt, OpExecutor opExecutor, Map<PatternQuery, SkipScanCandidate> indexCache) {
        PatternQuery subst = PatternQuery.substitute(patternQuery, binding);

        PatternQuery cacheKey = PatternQuery.toCacheKey(subst);
        SkipScanCandidate bestCandidate = indexCache.computeIfAbsent(cacheKey, cq -> {
            // System.err.println("NO CACHE HIT: " + canonicalQuery);
            SkipScanCandidate r = findBestCandidate(patternQuery, execCxt);
            return r;
        });

        QueryIterator innerIter = tryExec(subst, execCxt, bestCandidate);

        // Fail-over in case that no suitable index was found.
        // Should never be needed with default TDB2 configurations.
        if (innerIter == null) {
            // Execute the pattern without distinct. This avoids the infinite loop of
            // attempting to rewrite distinct as skip-scan.
            PatternQuery tmp = new PatternQuery(/*distinct=*/false, patternQuery.project(), patternQuery.tuple());
            Op op = tmp.effectiveOp();
            innerIter = opExecutor.executeOp(op, QueryIterPlainWrapper.create(Iter.singletonIter(binding)));

            if (patternQuery.distinct()) {
                innerIter = new QueryIterDistinct(innerIter, null, execCxt);
            }
        }
        return innerIter;
    }

    public static SkipScanCandidate findBestCandidate(PatternQuery patternQuery, ExecutionContext execCxt) {
        Node[] tuple = patternQuery.tuple();
        NodeTupleTable table = resolveTable(tuple.length, execCxt);
        TuplePatternSpec lookup = TuplePatternSpec.create(tuple, patternQuery.project());
        List<SkipScanCandidate> candidates = planCandidates(table, lookup);
        SkipScanCandidate best = pickBest(candidates);
        return best;
    }

//    public static SkipScanCandidate findBestCandidate(NodeTupleTable nodeTupleTable, boolean distinct, TuplePatternSpec lookup, ExecutionContext execCxt) {
//        List<SkipScanCandidate> candidates = planCandidates(nodeTupleTable, lookup);
//        SkipScanCandidate best = pickBest(candidates);
//        return best;
//    }

    public static QueryIterator tryExec(PatternQuery patternQuery, ExecutionContext execCxt, SkipScanCandidate candidate) {
        Node[] tuple = patternQuery.tuple();
        NodeTupleTable table = resolveTable(tuple.length, execCxt);
        TuplePatternSpec lookup = TuplePatternSpec.create(tuple, patternQuery.project());
        return tryExec(table, patternQuery.distinct(), lookup, execCxt, candidate);
    }

    public static QueryIterator tryExec(NodeTupleTable nodeTupleTable, boolean distinct, TuplePatternSpec lookup, ExecutionContext execCxt, SkipScanCandidate best) {
        // In the end we must build a tuple with the constants
        if (logger.isDebugEnabled()) {
            logger.debug("Best matching index: " + (best == null ? null : best.index()));
        }

        if (best == null) {
            return null;
        }
        return execCandidate(nodeTupleTable, distinct, lookup, best, execCxt);
    }

    /** Must only be called if hasNeededColumns returned true. */
    @SuppressWarnings("unchecked")
    public static IndexMatch computeIndexMatch(TupleMap tm, TuplePatternSpec patternSpec) {
        Node[] quad = patternSpec.tuple();
        boolean canDoDirectDistinct = true;

        // An index is only usable if covers all needed slots of the query tuple.
        // Start with marking all slots as not-covered.
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
            // Mark the slot as covered by setting the slot's bit to 0.
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

        // If there were residual conditions then strip trailing null values.
        if (residualConditions != null && residualConditions.length != numResidualConditions) {
            residualConditions = Arrays.copyOf(residualConditions, numResidualConditions);
        }

        return new IndexMatch(canDoDirectDistinct, residualConditions, keyLen, valLen);
    }

    /** Resolve the {@link NodeTupleTable} for a pattern of the given tuple length. */
    public static NodeTupleTable resolveTable(int tupleLength, ExecutionContext execCxt) {
        if (tupleLength == 3) {
            GraphTDB graph = (GraphTDB)execCxt.getActiveGraph();
            return graph.getNodeTupleTable();
        } else if (tupleLength == 4) {
            DatasetGraphTDB ds = (DatasetGraphTDB)execCxt.getDataset();
            return ds.getQuadTable().getNodeTupleTable();
        } else {
            throw new IllegalArgumentException("Unexpected tuple length: " + tupleLength);
        }
    }

    /**
     * Enumerate the usable indexes for the given lookup. Each returned candidate carries
     * the matched index, its {@link IndexMatch}, and the variable order the index produces.
     * This is the planning phase: it does not touch the node table or execute anything, so
     * callers may inspect candidate orders and choose a combination before executing.
     */
    public static List<SkipScanCandidate> planCandidates(NodeTupleTable nodeTupleTable, TuplePatternSpec lookup) {
        TupleTable tupleTable = nodeTupleTable.getTupleTable();
        List<SkipScanCandidate> candidates = new ArrayList<>();

        for (TupleIndex tupleIndex : tupleTable.getIndexes()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Trying index: " + tupleIndex);
            }
            SkipScanCandidate candidate = planCandidate(lookup, tupleIndex);
            if (candidate != null) {
                candidates.add(candidate);
            }
        }
        return candidates;
    }

    private static SkipScanCandidate planCandidate(TuplePatternSpec lookup, TupleIndex tupleIndex) {
        // Only consider indexes backed by a BPlusTree
        if (tupleIndex instanceof TupleIndexRecord recordIdx) {
            RangeIndex rangeIndex = recordIdx.getRangeIndex();
            if (!(rangeIndex instanceof BPlusTree)) {
                return null;
            }
        }

        TupleMap tm = tupleIndex.getMapping();
        IndexMatch match = computeIndexMatch(tm, lookup);

        if (match == null) {
            return null;
        }

        return new SkipScanCandidate(tupleIndex, match);
    }

    /** Choose the best candidate using {@link IndexMatch#COMPARATOR}, or null if none. */
    public static SkipScanCandidate pickBest(List<SkipScanCandidate> candidates) {
        SkipScanCandidate best = null;
        for (SkipScanCandidate candidate : candidates) {
            if (best == null || IndexMatch.COMPARATOR.compare(candidate.match(), best.match()) < 0) {
                best = candidate;
            }
        }
        return best;
    }

    /** Execute a previously planned candidate. */
    public static QueryIterator execCandidate(NodeTupleTable nodeTupleTable, boolean distinct, TuplePatternSpec lookup,
                                              SkipScanCandidate candidate, ExecutionContext execCxt) {
        NodeTable nodeTable = nodeTupleTable.getNodeTable();
        TupleIndex bestIndex = candidate.index();
        IndexMatch bestMatch = candidate.match();

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

    /**
     * Actual execution.
     */
    @SuppressWarnings("removal")
    public static QueryIterator exec(TupleIndex tupleIndex, NodeTable nodeTable, List<Var> projectVars, IndexMap map, ExecutionContext execCxt) {
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
            // {?s :p ?s} becomes {?s :p ?.foo} FILTER(?s = ?.foo)

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
                execCxt.checkCancelSignal();

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

        // Build the initial BindingNodeId instances.
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
        // Presence of filters also implies presence of generated variables that need projecting away.
        if (filterExpr != null) {
            qIter = new QueryIterFilterExpr(qIter, filterExpr, execCxt);
            qIter = QueryIterProject.create(qIter, projectVars, execCxt);
        }

        return qIter;
    }

    /**
     * Maps Nodes to NodeIds.
     * Returns null if any constraint is unsatisfiable due to the lack of a corresponding node id.
     */
    public static IndexMap tryComputeIndexMap(TuplePatternSpec lookup, IndexMatch match, TupleMap tm, NodeTable nodeTable) {
        Node[] quad = lookup.tuple();
        int neededSlots = match.numNeededSlots();

        // Reorder the query tuple by the order of columns in the index.
        Node[] nodeTuple = new Node[neededSlots];
        for (int i = 0; i < neededSlots; ++i) {
            int tupleSlot = tm.mapIdx(i);
            Node node = quad[tupleSlot];
            nodeTuple[i] = node;
        }

        // Build the tuple of NodeIds
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

        if (match.numResidualConditions() > 0) {
            mappedConditions = remapValueConditions(match.residualConditions(), tm, nodeTable);

            // If mappedConditions is null then some nodes could not be mapped
            if (mappedConditions == null) {
                // skip
                return null;
            }
        }

        if (lookup.equalityLinks() != null) {
            mappedLinks = remapEqualityLinks(lookup.equalityLinks(), tm);
        }

        VarMap[] newProj = remapProjection(lookup, tm);
        return new IndexMap(nodeTuple, tuple, newProj, mappedConditions, mappedLinks);
    }

    private static Expr logicalAnd(Expr base, Expr contrib) {
        return base == null ? contrib : new E_LogicalAnd(base, contrib);
    }

    private static NodeId extractNodeId(byte[] bytes, int index) {
        NodeId nodeId = NodeIdFactory.get(bytes, index * NodeId.SIZE);
        return nodeId;
    }

    /** Create varMaps from the spec's projection and remap indices w.r.t. the tupleMap. */
    public static VarMap[] remapProjection(TuplePatternSpec lookup, TupleMap tupleMap) {
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

    /**
     * Remap indices of equality links w.r.t. the tupleMap.
     */
    public static EqualityLink[] remapEqualityLinks(EqualityLink[] links, TupleMap tupleMap) {
        int n = links.length;
        EqualityLink[] result = new EqualityLink[n];
        for (int i = 0; i < n; ++i) {
            EqualityLink link = links[i];
            int primary = tupleMap.unmapIdx(link.idx());
            int secondary = tupleMap.unmapIdx(link.equalToIdx());
            result[i] = new EqualityLink(primary, secondary);
        }
        return result;
    }

    /**
     * Remap indices w.r.t. the tupleMap and map Nodes to NodeIds.
     * Returns null if any condition could not be mapped.
     */
    public static ValueFilter<NodeId>[] remapValueConditions(ValueFilter<Node>[] conditions, TupleMap tupleMap, NodeTable nodeTable) {
        int n = conditions.length;
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
}
