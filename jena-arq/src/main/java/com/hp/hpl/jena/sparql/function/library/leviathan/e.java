/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class e extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        switch (XSDFuncOp.classifyNumeric("sq", v))
        {
            case OP_INTEGER:
            case OP_DECIMAL:
            case OP_FLOAT:
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.exp(v.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;
        }
    }

}
