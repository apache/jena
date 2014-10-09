package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class sec1 extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        // arcsec x = acos (1 / x) 
        return NodeValue.makeDouble(Math.acos(1 / v.getDouble()));
    }

}
