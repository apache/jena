/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.function.library.leviathan;

import java.math.BigInteger;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class cube extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        switch (XSDFuncOp.classifyNumeric("sq", v))
        {
            case OP_INTEGER:
                BigInteger i = v.getInteger();
                return NodeValue.makeInteger( i.pow(3) );
            case OP_DECIMAL:
                double dec = v.getDecimal().doubleValue() ;
                return NodeValue.makeDecimal( Math.pow(dec, 3d)) ;
            case OP_FLOAT:
                // TODO Should squaring a float keep it a float?
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.pow(v.getDouble(), 3d) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;
        }
    }

}
