package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class cosec extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        // cosec x = 1 / sin x 
        return NodeValue.makeDouble(1 / Math.sin(v.getDouble()));
    }

}
