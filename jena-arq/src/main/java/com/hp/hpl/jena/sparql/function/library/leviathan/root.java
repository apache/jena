package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

public class root extends FunctionBase2 {

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {
        double value = v1.getDouble();
        double root = v2.getDouble();

        return NodeValue.makeDouble(Math.pow(value, 1d / root));
    }

}
