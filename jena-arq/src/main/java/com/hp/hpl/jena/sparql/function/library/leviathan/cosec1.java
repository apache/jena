package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class cosec1 extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        // acosec x = asin (1 / x) 
        return NodeValue.makeDouble(Math.asin(1 / v.getDouble()));
    }

}
