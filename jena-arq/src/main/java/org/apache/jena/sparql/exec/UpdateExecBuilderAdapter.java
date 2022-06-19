package org.apache.jena.sparql.exec;

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateExecutionBuilder;
import org.apache.jena.update.UpdateRequest;

/** UpdateExecBuilder view over an UpdateExecutionBuilder */
public class UpdateExecBuilderAdapter
    implements UpdateExecBuilder
{
    protected UpdateExecutionBuilder builder;

    protected UpdateExecBuilderAdapter(UpdateExecutionBuilder delegate) {
        super();
        this.builder = delegate;
    }

    /** Adapter that attempts to unwrap an UpdateExecutionBuilderAdapter's builder */
    public static UpdateExecBuilder adapt(UpdateExecutionBuilder builder) {
        Objects.requireNonNull(builder);

        UpdateExecBuilder result = builder instanceof UpdateExecutionBuilderAdapter
                ? ((UpdateExecutionBuilderAdapter)builder).getExecBuilder()
                : new UpdateExecBuilderAdapter(builder);

        return result;
    }

    public UpdateExecutionBuilder getExecBuilder() {
        return builder;
    }

    @Override
    public UpdateExecBuilder update(UpdateRequest updateRequest) {
        builder = builder.update(updateRequest);
        return this;
    }

    @Override
    public UpdateExecBuilder update(Update update) {
        builder = builder.update(update);
        return this;
    }

    @Override
    public UpdateExecBuilder update(String updateRequestString) {
        builder = builder.update(updateRequestString);
        return this;
    }

    @Override
    public UpdateExecBuilder set(Symbol symbol, Object value) {
        builder = builder.set(symbol, value);
        return this;
    }

    @Override
    public UpdateExecBuilder set(Symbol symbol, boolean value) {
        builder = builder.set(symbol, value);
        return this;
    }

    @Override
    public UpdateExecBuilder context(Context context) {
        builder = builder.context(context);
        return this;
    }

    @Override
    public UpdateExecBuilder substitution(Binding binding) {
        builder = builder.substitution(new ResultBinding(null, binding));
        return this;
    }

    @Override
    public UpdateExecBuilder substitution(String varName, Node value) {
        builder = builder.substitution(varName, ModelUtils.convertGraphNodeToRDFNode(value));
        return this;
    }

    @Override
    public UpdateExecBuilder substitution(Var var, Node value) {
        builder = builder.substitution(var.getName(), ModelUtils.convertGraphNodeToRDFNode(value));
        return this;
    }

    @Override
    public UpdateExec build() {
        UpdateExecution updateExec = builder.build();
        UpdateExec result = UpdateExecAdapter.adapt(updateExec);
        return result;
    }
}
