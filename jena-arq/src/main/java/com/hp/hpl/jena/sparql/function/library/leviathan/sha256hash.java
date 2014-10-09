package com.hp.hpl.jena.sparql.function.library.leviathan;

import com.hp.hpl.jena.sparql.expr.E_SHA256;
import com.hp.hpl.jena.sparql.expr.ExprDigest;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class sha256hash extends FunctionBase1 {

    private ExprDigest digest = new E_SHA256(NodeValue.makeBoolean(true).getExpr());
    
    @Override
    public NodeValue exec(NodeValue v) {
        return digest.eval(v);
    }

}
