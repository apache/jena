package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

public class pythagoras extends FunctionBase2 {

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {
        double a = v1.getDouble();
        double b = v2.getDouble();

        return NodeValue.makeDouble(Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)));
    }

}
