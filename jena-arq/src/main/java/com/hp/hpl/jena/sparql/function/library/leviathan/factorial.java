package com.hp.hpl.jena.sparql.function.library.leviathan;

import java.math.BigInteger;

import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class factorial extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        // Don't care about the return value, will just error if the thing isn't
        // a numeric
        XSDFuncOp.classifyNumeric("factorial", v);

        BigInteger i = v.getInteger();

        switch (i.compareTo(BigInteger.ZERO)) {
        case 0:
            // 0! is 1
            return NodeValue.makeInteger(BigInteger.ONE);
        case -1:
            // Negative factorial is error
            throw new ExprEvalException("Cannot evaluate a negative factorial");
        case 1:
            BigInteger res = i.add(BigInteger.ZERO);
            i = i.subtract(BigInteger.ONE);
            while (i.compareTo(BigInteger.ZERO) != 0) {
                res = res.multiply(i);
                i = i.subtract(BigInteger.ONE);
            }
            return NodeValue.makeInteger(res);
        default:
            throw new ExprEvalException("Unexpecte comparison result");
        }
    }

}
