package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class ln extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        // Don't care about the return value, will just error if the thing isn't
        // a numeric
        XSDFuncOp.classifyNumeric("ln", v);

        return NodeValue.makeDouble(Math.log(v.getDouble()));
    }

}
