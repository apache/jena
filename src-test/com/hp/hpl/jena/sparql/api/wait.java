/*
 * (c) Copyright Apache Software Foundation - Apache Software License 2.0
 */

package com.hp.hpl.jena.sparql.api;

import org.openjena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionBase1 ;

public class wait extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue nv)
    {
        if ( ! nv.isInteger() )
            throw new ExprEvalException("Not an integer") ;
        int x = nv.getInteger().intValue() ;
        Lib.sleep(x) ;
        return nv ;
    }
}