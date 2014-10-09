package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class cot extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        // cot x = sin x / cos x
        double x = v.getDouble();
        return NodeValue.makeDouble(Math.sin(x) / Math.cos(x));
    }

}
