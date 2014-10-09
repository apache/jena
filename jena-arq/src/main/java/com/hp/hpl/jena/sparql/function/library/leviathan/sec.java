package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class sec extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        // sec x = 1 / cos x 
        return NodeValue.makeDouble(1 / Math.cos(v.getDouble()));
    }

}
