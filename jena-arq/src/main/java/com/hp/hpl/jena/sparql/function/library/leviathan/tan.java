package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class tan extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        return NodeValue.makeDouble(Math.tan(v.getDouble()));
    }

}
