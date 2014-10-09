package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class cot1 extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        // acot x = atan (1 / x)
        return NodeValue.makeDouble(Math.atan(1 / v.getDouble()));
    }

}
