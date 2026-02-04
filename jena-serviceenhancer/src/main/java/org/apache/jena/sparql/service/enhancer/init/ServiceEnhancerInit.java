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

package org.apache.jena.sparql.service.enhancer.init;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.algebra.optimize.RewriteFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DynamicDatasets.DynamicDatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterProject;
import org.apache.jena.sparql.engine.iterator.QueryIteratorMapped;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.bulk.ChainingServiceExecutorBulk;
import org.apache.jena.sparql.service.enhancer.algebra.TransformSE_EffectiveOptions;
import org.apache.jena.sparql.service.enhancer.algebra.TransformSE_JoinStrategy;
import org.apache.jena.sparql.service.enhancer.assembler.DatasetAssemblerServiceEnhancer;
import org.apache.jena.sparql.service.enhancer.assembler.ServiceEnhancerVocab;
import org.apache.jena.sparql.service.enhancer.function.cacheRm;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkConcurrent;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;
import org.apache.jena.sparql.service.enhancer.impl.ServiceOpts;
import org.apache.jena.sparql.service.enhancer.impl.ServiceOptsSE;
import org.apache.jena.sparql.service.enhancer.impl.ServiceResponseCache;
import org.apache.jena.sparql.service.enhancer.impl.ServiceResultSizeCache;
import org.apache.jena.sparql.service.enhancer.impl.util.DynamicDatasetUtils;
import org.apache.jena.sparql.service.enhancer.impl.util.Lazy;
import org.apache.jena.sparql.service.enhancer.impl.util.VarScopeUtils;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.QueryIteratorMaterialize;
import org.apache.jena.sparql.service.enhancer.pfunction.cacheLs;
import org.apache.jena.sparql.service.single.ChainingServiceExecutor;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.MappingRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceEnhancerInit
    implements JenaSubsystemLifecycle
{
    private static final Logger logger = LoggerFactory.getLogger(ServiceEnhancerInit.class);

    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    public static void initMappingRegistry() {
        // Register the "se" prefix for use with cli options, such as ./arq --set 'se:serviceCacheMaxEntryCount=1000'
        MappingRegistry.addPrefixMapping("se", ServiceEnhancerVocab.getURI());
    }

    /**
     * Initialize the SERVICE &lt;collect:&gt; { }.
     * This collects all bindings of the graph pattern into a list and serves them
     * from the list.
     */
    public static void initFeatureCollect() {
        String collectOptName = "collect";
        ServiceExecutorRegistry.get().addBulkLink((opService, input, execCxt, chain) -> {
            QueryIterator r;
            ServiceOpts opts = ServiceOpts.getEffectiveService(opService, ServiceEnhancerConstants.SELF.getURI(), key -> key.equals("collect"));
            if (opts.containsKey(collectOptName)) {
                opts.removeKey(collectOptName);
                OpService newOp = opts.toService();
                QueryIterator tmp  = chain.createExecution(newOp, input, execCxt);
                r = new QueryIteratorMaterialize(tmp, execCxt);
            } else {
                r = chain.createExecution(opService, input, execCxt);
            }
            return r;
        });
    }

    public static void initFeatureConcurrent() {
        ServiceExecutorRegistry.get().addBulkLink(new ChainingServiceExecutorBulkConcurrent("concurrent"));
    }


    public static void init() {
        initMappingRegistry();

        Context cxt = ARQ.getContext();

        initFeatureConcurrent();
        initFeatureCollect();

        // Creation of the cache is deferred until first use.
        // This allows for the cache creation to read settings from the context.
        Lazy<ServiceResponseCache> cache = Lazy.of(() -> {
            ServiceResponseCache.SimpleConfig conf = ServiceResponseCache.buildConfig(cxt);
            ServiceResponseCache r = new ServiceResponseCache(conf);
            if (logger.isInfoEnabled()) {
                logger.info("Initialized Service Enhancer Cache with config {}", conf);
            }
            return r;
        });
        cxt.put(ServiceEnhancerConstants.serviceCache, cache);

        ServiceResultSizeCache resultSizeCache = new ServiceResultSizeCache();
        ServiceResultSizeCache.set(cxt, resultSizeCache);

        ServiceExecutorRegistry.get().addBulkLink(new ChainingServiceExecutorBulkServiceEnhancer());

        // Register SELF extension
        registerServiceExecutorBulkSelf(ServiceExecutorRegistry.get());
        registerServiceExecutorSelf(ServiceExecutorRegistry.get());

        registerWith(Assembler.general());

        // Important: This registers the (property) functions but
        // without setting enableMgmt to true in the context some of them
        // will refuse to work
        registerFunctions(FunctionRegistry.get());
        registerPFunctions(PropertyFunctionRegistry.get());
    }

    public static void registerFunctions(FunctionRegistry reg) {
        reg.put(cacheRm.DEFAULT_IRI, cacheRm.class);
    }

    public static void registerPFunctions(PropertyFunctionRegistry reg) {
        reg.put(cacheLs.DEFAULT_IRI, cacheLs.class);
    }

    public static void registerServiceExecutorBulkSelf(ServiceExecutorRegistry registry) {
        ChainingServiceExecutorBulk selfExec = (opExec, rawInput, execCxt, chain) -> {
            QueryIterator r;
            ServiceOpts so = ServiceOptsSE.getEffectiveService(opExec);
            OpService target = so.getTargetService();

            // Remove path variables from the input binding
            QueryIterator input = new QueryIteratorWrapper(rawInput) {
                @Override
                public Binding moveToNextBinding() {
                    Binding rawBinding = super.moveToNextBinding();
                    Iterator<Var> it = rawBinding.vars();
                    boolean hasPathVar = false;
                    while (it.hasNext()) {
                        Var v = it.next();
                        if (v.getName().startsWith(ARQConstants.allocPathVariables)) {
                            hasPathVar = true;
                            break;
                        }
                    }

                    Binding rr;
                    if (hasPathVar) {
                        BindingBuilder bb = BindingFactory.builder();
                        rawBinding.forEach((v, n) -> {
                            if (!v.getName().startsWith(ARQConstants.allocPathVariables)) {
                                bb.add(v, n);
                            }
                        });
                        rr = bb.build();
                    } else {
                        rr = rawBinding;
                    }
                    return rr;
                }
            };

            // Issue: Because of the service clause, the optimizer has not yet been run.
            // - in order to have property functions recognized properly.
            // - However: We must not touch scoping or we will break a prior SERVICE <loop:>.
            // - Also: TransformPathFlatten may introduce new variables that clash resulting in incompatible bindings.
            //     Current workaround: remove those variables from the input binding.

            if (ServiceEnhancerConstants.SELF_BULK.equals(target.getService())) {
                String optimizerMode = so.getFirstValue(ServiceOptsSE.SO_OPTIMIZE, "on", "on");
                Op op = opExec.getSubOp();
                Op tmpOp = op;
                Op finalOp = tmpOp;

                boolean useQc = true;
                if (useQc) {

                    // Run the optimizer unless disabled

                    if (!"off".equals(optimizerMode)) {
                    // if (false) {

                        // Here we try to protect loop variables from getting renamed
                        // by the optimizer.
                        // This code first saves the scope levels of variables,
                        // then runs the optimizer, and then restores the scope levels again.
                        Collection<Var> opVars = OpVars.mentionedVars(op);
                        Map<String, NavigableSet<Integer>> opVarLevels = VarScopeUtils.getScopeLevels(opVars);

                        Map<String, Integer> opVarMinLevels = opVarLevels.entrySet().stream()
                            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().first()));

                        // Variables that only appear on the same level may be loop vars
                        Set<String> candidateLoopVars = opVarLevels.entrySet().stream()
                            .filter(e -> e.getValue().size() == 1)
                            .map(Entry::getKey)
                            .collect(Collectors.toSet());

                        // Run the optimizer.
                        // TransformPathFlatten may introduce variables that did not exist before.
                        Context cxt = execCxt.getContext();
                        RewriteFactory rf = decideOptimizer(cxt);
                        Rewrite rw = rf.create(cxt);
                        tmpOp = rw.rewrite(op);

                        Collection<Var> newOpVars = OpVars.mentionedVars(tmpOp);
                        Map<String, Integer> newOpVarMinLevels = VarScopeUtils.getMinimumScopeLevels(newOpVars);

                        NodeTransform nf = node -> {
                            Node rNode;
                            if (Var.isVar(node)) {
                                Var v = Var.alloc(node);
                                String vn = v.getName();
                                String plainName = VarScopeUtils.getPlainName(vn);
                                Integer oldMinLevel = opVarMinLevels.get(plainName);

                                if (oldMinLevel != null) {
                                    if (candidateLoopVars.contains(plainName)) {
                                        // If the variable appeared only at exactly 1 level
                                        // then restore that level.
                                        rNode = VarScopeUtils.allocScoped(plainName, oldMinLevel);
                                    } else {
                                        // Variable appeared at different scopes - subtract the delta.
                                        Integer newMinLevel = newOpVarMinLevels.get(plainName);
                                        if (newMinLevel != null) {
                                            int newLevel = VarScopeUtils.getScopeLevel(vn);
                                            int delta = newMinLevel - oldMinLevel;
                                            int finalLevel = newLevel - delta;
                                            rNode = VarScopeUtils.allocScoped(plainName, finalLevel);
                                        } else {
                                            // Variable name disappeared after rewrite - should not happen.
                                            // But retain node as is.
                                            rNode = node;
                                        }
                                    }
                                } else {
                                    rNode = node;
                                }
                            } else {
                                rNode = node;
                            }
                            return rNode;
                        };

                        finalOp = NodeTransformLib.transform(nf, tmpOp);
                    }
                    // Using QC with e.g. TDB2 breaks unionDefaultGraph mode.
                    //   Issue seems to be mitigated going through QueryEngineRegistry.

                    // The issue now is that PathCompile may introduce clashing variables.
                    // We could apply path transformation now and rename those vars.
                     r = QC.execute(finalOp, input, execCxt);
                } else {
                    // A context copy is needed in order to isolate changes from further executions;
                    //   without a copy query engines may e.g. overwrite the context value for the NOW() function.
                    // Context cxtCopy = execCxt.getContext().copy();
                    // r = execute(op, dataset, input, cxtCopy);
                    throw new RuntimeException("Cannot go through query engine factory for bulk requests.");
                }
            } else {
                r = chain.createExecution(opExec, input, execCxt);
            }
            return r;
        };
        // registry.addBulkLink(selfExec);
        registry.getBulkChain().add(selfExec);
    }

    public static void registerServiceExecutorSelf(ServiceExecutorRegistry registry) {
        ChainingServiceExecutor selfExec = (opExec, opOrig, binding, execCxt, chain) -> {
            QueryIterator r;
            ServiceOpts so = ServiceOptsSE.getEffectiveService(opExec);
            OpService target = so.getTargetService();
            DatasetGraph dataset = execCxt.getDataset();

            // It seems that we always need to run the optimizer here
            // in order to have property functions recognized properly
            if (ServiceEnhancerConstants.SELF.equals(target.getService())) {
                String optimizerMode = so.getFirstValue(ServiceOptsSE.SO_OPTIMIZE, "on", "on");
                Op op = opExec.getSubOp();

                boolean useQc = false;
                if (useQc) {
                    // Run the optimizer unless disabled
                    if (!"off".equals(optimizerMode)) {
                        Context cxt = execCxt.getContext();
                        RewriteFactory rf = decideOptimizer(cxt);
                        Rewrite rw = rf.create(cxt);
                        op = rw.rewrite(op);
                    }
                    // Using QC with e.g. TDB2 breaks unionDefaultGraph mode.
                    //   Issue seems to be mitigated going through QueryEngineRegistry.
                    r = QC.execute(op, binding, execCxt);
                } else {
                    // A context copy is needed in order to isolate changes from further executions;
                    //   without a copy query engines may e.g. overwrite the context value for the NOW() function.
                    Context cxtCopy = execCxt.getContext().copy();
                    r = execute(op, dataset, binding, cxtCopy, execCxt);
                }
            } else {
                r = chain.createExecution(opExec, opOrig, binding, execCxt);
            }
            return r;
        };
        registry.addSingleLink(selfExec);
    }

    /**
     * Special processing that unwraps dynamic datasets.
     * Returns a QueryIterCommonParent with the given input binding. If the tracker is non-null then the returned iterator will be tracked in it.
     *
     * Note, that the execCxt of the sub-execution must generally remain isolated from the execCxt of the parent-execution (the tracker).
     * For example, sub-executions will create their own QueryIteratorCheck which upon close will check for any non-closed iterators tracked in their
     * execCxt. This means, that if QueryIteratorCheck is not a top-level QueryIter but somehow nested, it will complain if there exists an ancestor
     * iterator that has not yet been closed. Non-closed ancestors can happen as a result of concurrent cancel: A child iterator may close itself in
     * reaction to the cancel signal, whereas its parent iterator may not have it seen yet.
     */
    private static QueryIterator execute(Op op, DatasetGraph dataset, Binding binding, Context cxt, ExecutionContext tracker) {
        QueryIterator innerIter = null;
        QueryIterator outerIter = null;

        DynamicDatasetGraph ddg = DynamicDatasetUtils.asUnwrappableDynamicDatasetOrNull(dataset);
        if (ddg != null) {
            // We are about to create a query from the op which loses scope information
            // Set up the map that allows for mapping the query's result set variables's
            // to the appropriately scoped ones
            Set<Var> visibleVars = OpVars.visibleVars(op);
            Set<String> visiblePlainNames = VarScopeUtils.getPlainNames(visibleVars);
            Map<Var, Var> normedToScoped = VarScopeUtils.normalizeVarScopes(visibleVars, visiblePlainNames).inverse();

            Op opRestored = Rename.reverseVarRename(op, true);
            Query baseQuery = OpAsQuery.asQuery(opRestored);
            Pair<Query, DatasetGraph> pair = DynamicDatasetUtils.unwrap(baseQuery, ddg);
            Query effQuery = pair.getLeft();
            DatasetGraph effDataset = pair.getRight();

            QueryEngineFactory qef = QueryEngineRegistry.findFactory(effQuery, effDataset, cxt);
            // The scoping of the binding does not match with that of the query.
            // Therefore pass on an empty binding and rename the result set variables
            // back into their proper scope
            Plan plan = qef.create(effQuery, effDataset, BindingFactory.empty(), cxt);
            innerIter = plan.iterator();
            outerIter = new QueryIteratorMapped(innerIter, normedToScoped);
        }

        if (innerIter == null) {
            QueryEngineFactory qef = QueryEngineRegistry.findFactory(op, dataset, cxt);
            Plan plan = qef.create(op, dataset, BindingFactory.empty(), cxt);

            // Note: The default plan implementation returns an iterator
            // that closes the plan when closing the iterator.
            innerIter = plan.iterator();

            // If the op contains an OpPath then variables such as ??P0
            // may get introduced and projected - which we must filter out.
            List<Var> visibleVars = new ArrayList<>(OpVars.visibleVars(op));
            innerIter = QueryIterProject.create(innerIter, visibleVars, tracker);

            outerIter = innerIter;
        }

        // execCxt = innerIter instanceof QueryIter ? ((QueryIter)innerIter).getExecContext() : null;
        QueryIterator result = new QueryIterCommonParent(outerIter, binding, tracker);
        return result;
    }

    static void registerWith(AssemblerGroup g) {
        AssemblerUtils.register(g, ServiceEnhancerVocab.DatasetServiceEnhancer, new DatasetAssemblerServiceEnhancer(), DatasetAssembler.getGeneralType());

        // Note: We can't install the plugin on graphs because they don't have a context
    }

    /** If there is an optimizer in tgt that wrap it. Otherwise put a fresh optimizer into tgt
     * that lazily wraps the optimizer from src */
    public static void wrapOptimizer(Context tgt, Context src) {
        if (tgt == src) {
            throw new IllegalArgumentException("Target and source contexts for optimizer must differ to avoid infinite loop during lookup");
        }

        RewriteFactory baseFactory = tgt.get(ARQConstants.sysOptimizerFactory);
        if (baseFactory == null) {
            // Wrap the already present optimizer
            wrapOptimizer(tgt);
        } else {
            // Lazily delegate to the optimizer in src
            RewriteFactory factory = cxt -> op -> {
                RewriteFactory f = decideOptimizer(src);
                f = enhance(f);
                Context mergedCxt = Context.mergeCopy(src, cxt);
                Rewrite r = f.create(mergedCxt);
                return r.rewrite(op);
            };
            tgt.set(ARQConstants.sysOptimizerFactory, factory);
        }
    }

    public static RewriteFactory decideOptimizer(Context context) {
        RewriteFactory result = context.get(ARQConstants.sysOptimizerFactory);
        if (result == null) {
            result = Optimize.getFactory();

            if (result == null) {
                result = Optimize.stdOptimizationFactory;
            }
        }
        return result;
    }

    /** Register the algebra transformer that enables forcing linear joins via {@code SERVICE <loop:>}*/
    public static void wrapOptimizer(Context cxt) {
        RewriteFactory baseFactory = decideOptimizer(cxt);
        RewriteFactory enhancedFactory = enhance(baseFactory);
        cxt.set(ARQConstants.sysOptimizerFactory, enhancedFactory);
    }

    public static RewriteFactory enhance(RewriteFactory baseFactory) {
        RewriteFactory enhancedFactory = cxt -> {
            Rewrite baseRewrite = baseFactory.create(cxt);
            Rewrite[] rw = { null };
            rw[0] = op -> {
                Op a = Transformer.transform(new TransformSE_EffectiveOptions(), op);
                Op b = Transformer.transform(new TransformSE_JoinStrategy(), a);
                Op r = baseRewrite.rewrite(b);
                Op q = Transformer.transform(new TransformSE_JoinStrategy(), r);
                return q;
            };
            return rw[0];
        };
        return enhancedFactory;
    }

    public static Node resolveServiceNode(Node node, ExecutionContext execCxt) {
        Node result = ServiceEnhancerConstants.SELF.equals(node)
                ? resolveSelfId(execCxt)
                : node;

        return result;
    }

    /**
     * Return the value for {@link ServiceEnhancerConstants#datasetId} in the context.
     * If it is null then instead return an IRI that includes the involved dataset's
     * system identity hash code.
     */
    public static Node resolveSelfId(ExecutionContext execCxt) {
        Context context = execCxt.getContext();
        Node id = context.get(ServiceEnhancerConstants.datasetId);
        if (id == null) {
            DatasetGraph dg = execCxt.getDataset();
            int hashCode = System.identityHashCode(dg);
            id = NodeFactory.createLiteralString(ServiceEnhancerConstants.SELF.getURI() + "@dataset" + hashCode);
        }
        return id;
    }
}
