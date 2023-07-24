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

package org.apache.jena.sparql.service.enhancer.init;

import java.util.Map;
import java.util.Set;

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
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIteratorMapped;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.enhancer.algebra.TransformSE_EffectiveOptions;
import org.apache.jena.sparql.service.enhancer.algebra.TransformSE_JoinStrategy;
import org.apache.jena.sparql.service.enhancer.assembler.DatasetAssemblerServiceEnhancer;
import org.apache.jena.sparql.service.enhancer.assembler.ServiceEnhancerVocab;
import org.apache.jena.sparql.service.enhancer.function.cacheRm;
import org.apache.jena.sparql.service.enhancer.impl.ChainingServiceExecutorBulkServiceEnhancer;
import org.apache.jena.sparql.service.enhancer.impl.ServiceOpts;
import org.apache.jena.sparql.service.enhancer.impl.ServiceResponseCache;
import org.apache.jena.sparql.service.enhancer.impl.ServiceResultSizeCache;
import org.apache.jena.sparql.service.enhancer.impl.util.DynamicDatasetUtils;
import org.apache.jena.sparql.service.enhancer.impl.util.VarScopeUtils;
import org.apache.jena.sparql.service.enhancer.pfunction.cacheLs;
import org.apache.jena.sparql.service.single.ChainingServiceExecutor;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class ServiceEnhancerInit
    implements JenaSubsystemLifecycle
{
    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    public static void init() {
        ServiceResponseCache cache = new ServiceResponseCache();
        ARQ.getContext().put(ServiceEnhancerConstants.serviceCache, cache);

        ServiceResultSizeCache resultSizeCache = new ServiceResultSizeCache();
        ServiceResultSizeCache.set(ARQ.getContext(), resultSizeCache);

        ServiceExecutorRegistry.get().addBulkLink(new ChainingServiceExecutorBulkServiceEnhancer());

        // Register SELF extension
        registerServiceExecutorSelf(ServiceExecutorRegistry.get());

        registerWith(Assembler.general);

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

    public static void registerServiceExecutorSelf(ServiceExecutorRegistry registry) {
        ChainingServiceExecutor selfExec = (opExec, opOrig, binding, execCxt, chain) -> {
            QueryIterator r;
            ServiceOpts so = ServiceOpts.getEffectiveService(opExec);
            OpService target = so.getTargetService();
            DatasetGraph dataset = execCxt.getDataset();

            // It seems that we always need to run the optimizer here
            // in order to have property functions recognized properly
            if (ServiceEnhancerConstants.SELF.equals(target.getService())) {
                String optimizerMode = so.getFirstValue(ServiceOpts.SO_OPTIMIZE, "on", "on");
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
                    r = execute(op, dataset, binding, cxtCopy);
                }
            } else {
                r = chain.createExecution(opExec, opOrig, binding, execCxt);
            }
            return r;
        };
        registry.addSingleLink(selfExec);
    }

    /** Special processing that unwraps dynamic datasets */
    private static QueryIterator execute(Op op, DatasetGraph dataset, Binding binding, Context cxt) {
        QueryIterator innerIter = null;
        QueryIterator outerIter = null;
        ExecutionContext execCxt = null;

        DynamicDatasetGraph ddg = DynamicDatasetUtils.asUnwrappableDynamicDatasetOrNull(dataset);
        if (ddg != null) {
            // We are about to create a query from the op which loses scope information
            // Set up the map that allows for mapping the query's result set variables's
            // to the appropriately scoped ones
            Set<Var> visibleVars = OpVars.visibleVars(op);
            Map<Var, Var> normedToScoped = VarScopeUtils.normalizeVarScopes(visibleVars).inverse();

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
            innerIter = plan.iterator();
            outerIter = innerIter;
        }

        execCxt = innerIter instanceof QueryIter ? ((QueryIter)innerIter).getExecContext() : null;
        QueryIterator result = new QueryIterCommonParent(outerIter, binding, execCxt);
        return result;
    }

    static void registerWith(AssemblerGroup g)
    {
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

    public static Node resolveSelfId(ExecutionContext execCxt) {
        Context context = execCxt.getContext();

        Node id = context.get(ServiceEnhancerConstants.datasetId);
        if (id == null) {
            DatasetGraph dg = execCxt.getDataset();
            int hashCode = System.identityHashCode(dg);
            id = NodeFactory.createLiteral(ServiceEnhancerConstants.SELF.getURI() + "@dataset" + hashCode);
        }

        return id;
    }
}
