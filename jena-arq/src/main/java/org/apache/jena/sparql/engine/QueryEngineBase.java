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

package org.apache.jena.sparql.engine;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.irix.IRIs;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DynamicDatasets;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.mgt.Explain;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.DatasetUtils;

/** Main part of a QueryEngine - something that takes responsibility for a complete query execution */
public abstract class QueryEngineBase implements OpEval, Closeable
{
    // See also ExecutionContext.getDataset()
    protected final DatasetGraph originalDataset;
    protected DatasetGraph dataset = null;
    protected Context context;
    private Binding startBinding;

    private Query query = null;
    private Op queryOp = null;
    private Plan plan = null;

    protected QueryEngineBase(Query query, DatasetGraph dsg, Binding input, Context cxt) {
        this(dsg, input, cxt);
        this.query = query;
        query.setResultVars();
        // Unoptimized so far.
        setOp(createOp(query));
        dataset = prepareDataset(dsg, query);
    }

    private DatasetGraph prepareDataset(DatasetGraph originalDataset, Query query) {
        DatasetDescription dsDesc = DatasetDescription.create(query, context);
        DatasetGraph dsg = originalDataset;

        if ( dsg != null ) {
            if ( dsDesc != null ) {
                if ( query.hasDatasetDescription() )
                    dsg = dynamicDataset(dsDesc, dsg, false);
            }
            return dsg;
        }

        // No DatasetGraph
        if ( ! query.hasDatasetDescription() )
            throw new QueryExecException("No dataset and no dataset description for query");

        // DatasetDescription : Build it.
        String baseURI = query.getBaseURI();
        if ( baseURI == null )
            baseURI = IRIs.getSystemBase().str();

        dsg = DatasetUtils.createDatasetGraph(dsDesc, baseURI);
        return dsg;
    }

    protected QueryEngineBase(Op op, DatasetGraph dataset, Binding input, Context cxt) {
        this(dataset, input, cxt);
        // Ensure context setup - usually done in QueryExecutionBase
        // so it can be changed after initialization.
        if ( context == null )
            context = Context.setupContextForDataset(context, dataset);
        Context.setCurrentDateTime(context);
        this.query = null;
        setOp(op);
    }

    private QueryEngineBase(DatasetGraph dataset, Binding input, Context context) {
        this.context = context;
        this.originalDataset = dataset;
        this.dataset = dataset;
        if ( input == null ) {
            Log.warn(this, "Null initial input");
            input = BindingRoot.create();
        }
        this.startBinding = input;
    }

    public Plan getPlan() {
        if ( plan == null )
            plan = createPlan();
        return plan;
    }

    protected Plan createPlan() {
        // Decide the algebra to actually execute.
        Op op = queryOp;
        if ( !startBinding.isEmpty() ) {
            op = Substitute.substitute(op, startBinding);
            context.put(ARQConstants.sysCurrentAlgebra, op);
            // Don't reset the startBinding because it also is
            // needed in the output.
        }
        op = modifyOp(op);

        QueryIterator queryIterator = null;
        if ( dataset != null )
            // Null means setting up but not executing a query.
            queryIterator = evaluate(op, dataset, startBinding, context);
        else
            // Bypass management interface
            queryIterator = evaluateNoMgt(op, dataset, startBinding, context);
        // This could be an automagic iterator to catch close.
        return new PlanOp(getOp(), this, queryIterator);
    }

    protected Op modifyOp(Op op)
    { return op; }

    protected Op createOp(Query query) {
        Op op = Algebra.compile(query) ;
        return op ;
    }

    /** Calculate a dataset based on FROM and FROM NAMED in the query */
    protected DatasetGraph dynamicDataset(DatasetDescription dsDesc, DatasetGraph dataset, boolean unionDftGraph) {
        return DynamicDatasets.dynamicDataset(dsDesc, dataset, unionDftGraph);
    }

    /**
     * Return whether the dataset to execute against is the original one, or a
     * DatasetDescription modified one (including one for provided then the dataset was
     * null).
     */
    protected boolean isDynamicDataset() {
        return originalDataset != dataset;
    }

    // Record the query operation as it goes pass and call the actual worker
    @Override
    final public QueryIterator evaluate(Op op, DatasetGraph dsg, Binding binding, Context context) {
        if ( query != null )
            Explain.explain("QUERY", query, context);
        Explain.explain("ALGEBRA", op, context);
        return eval(op, dsg, binding, context);
    }

    private QueryIterator evaluateNoMgt(Op op, DatasetGraph dsg, Binding binding, Context context) {
        return eval(op, dsg, binding, context);
    }

    abstract protected QueryIterator eval(Op op, DatasetGraph dsg, Binding binding, Context context);

    /** Algebra expression (including any optimization) */
    public Op getOp() {
        return queryOp;
    }

    protected Binding getStartBinding() {
        return startBinding;
    }

    @Override
    public void close() {}

    protected void setOp(Op op) {
        queryOp = op;
        context.put(ARQConstants.sysCurrentAlgebra, op);
    }
}
