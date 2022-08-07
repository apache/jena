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

package org.apache.jena.sparql.expr;

import java.util.Objects;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.serializer.SerializationContext;

/** Group aggregation functions calculated a value during grouping and
 *  placed in the output binding.  This class is relationship of
 *  an aggregation expression and that variable.  Evaluation returns
 *  the variable's bound value.
 */

public class ExprAggregator extends ExprNode
{
    protected Aggregator aggregator;
    protected Var var;
    protected ExprVar exprVar = null;

    public ExprAggregator(Var v, Aggregator agg) {
        _setVar(v);
        aggregator = agg;
    }

    public Var getVar() {
        return var;
    }

    public void setVar(Var v)
    {
        if (this.var != null)
            throw new ARQInternalErrorException(Lib.className(this)+ ": Attempt to set variable to " + v + " when already set as " + this.var);
        if (v == null)
            throw new ARQInternalErrorException(Lib.className(this)+ ": Attempt to set variable to null");
        _setVar(v);
    }

    private void _setVar(Var v)
    {
        this.var = v;
        this.exprVar = new ExprVar(var);
    }

    public Aggregator getAggregator()   { return aggregator; }

    @Override
    public int hashCode()
    {
        int x = aggregator.hashCode();
        if ( var != null )
            x ^= var.hashCode();
        return x;
    }

    @Override
    public boolean equals(Expr other, boolean bySyntax) {
        if ( other == null ) return false;
        if ( this == other ) return true;
        if ( ! ( other instanceof ExprAggregator ) )
            return false;
        ExprAggregator agg = (ExprAggregator)other;
        if ( ! Objects.equals(var, agg.var) )
            return false;
        return Objects.equals(aggregator, agg.aggregator);
    }

    // Ensure no confusion - in an old design, an ExprAggregator was a subclass of ExprVar.
    @Override
    public ExprVar getExprVar()
    { throw new ARQInternalErrorException(); }

    @Override
    public Var asVar()
    { throw new ARQInternalErrorException(); }

    public ExprVar getAggVar() { return exprVar; }

    // As an expression suitable for outputting the calculation.
    public String asSparqlExpr(SerializationContext sCxt)
    { return aggregator.asSparqlExpr(sCxt); }

    @Override
    public ExprAggregator copySubstitute(Binding binding)
    {
        Var v = var;
        Aggregator agg = aggregator;
        return new ExprAggregator(v, agg);
    }

    @Override
    public ExprAggregator applyNodeTransform(NodeTransform transform)
    {
        // Can't rewrite this to a non-variable.
        Node node = transform.apply(var);
        if ( ! Var.isVar(node) )
        {
            Log.warn(this, "Attempt to convert an aggregation variable to a non-variable: ignored");
            node = var;
        }

        Var v = (Var)node;
        Aggregator agg = aggregator.copyTransform(transform);
        return new ExprAggregator(Var.alloc(node), agg);
    }

    @Override
    public String toString() {
        return "(AGG "+
                (var==null?"<>":"?"+var.getVarName())+
                " "+aggregator.toString()+")";
    }

    public Expr copy(Var v)  { return new ExprAggregator(v, aggregator.copy(aggregator.getExprList())); }

    @Override
    public NodeValue eval(Binding binding, FunctionEnv env)
    {
       return ExprVar.eval(var, binding, env);
    }

    public Expr apply(ExprTransform transform)  { return transform.transform(this); }

    @Override
    public void visit(ExprVisitor visitor)
    { visitor.visit(this); }
}
