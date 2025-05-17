package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

// FIXME Can probably be removed. Listeners for life cycles should be added to QueryExec /UpdateExec.
public interface QueryExecFactory {
    /**
     * Detect appropriate requests for a particular query engine for a particular
     * graph type.
     *
     * @param query     a {@link Query} to be executed
     * @param dataset   the {@link DatasetGraph} over which the query is to be executed
     * @param context   the {@link Context} in which the query is to be executed
     * @return whether the kind of query engine produced by this factory can handle this task
     */
    public boolean accept(Query query, DatasetGraph dataset, Context context);

    /**
     * Call to create a {@link Plan} : the companion {@link #accept} will have returned {@code true}.
     * @param query
     * @param dataset
     * @param inputBinding
     * @param context
     */
    public QueryExec create(Query query, DatasetGraph dataset, Binding inputBinding, Context context);
//
//    /**
//     * Detect appropriate requests for a particular query engine for a particular
//     * graph type.
//     *
//     * @param op       an {@link Op} to be executed
//     * @param dataset  the {@link DatasetGraph} over which the operation is to be executed
//     * @param context  the {@link Context} in which the operation is to be executed
//     * @return whether the kind of query engine produced by this factory can handle this task
//     */
//    public boolean accept(Op op, DatasetGraph dataset, Context context);
//
//    /**
//     *  Call to create a {@link Plan} : the companion {@link #accept} will have returned {@code true}.
//     * @param op
//     * @param dataset
//     * @param inputBinding
//     * @param context
//     */
//    public QueryExec create(Op op, DatasetGraph dataset, Binding inputBinding, Context context);
}
