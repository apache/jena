package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.expr.E_Exists ;
import com.hp.hpl.jena.sparql.expr.E_NotExists ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy ;

/** A copying transform that applies a Transform to the algebra operator of E_Exist and E_NoExists */
public class ExprTransformApplyTransform extends ExprTransformCopy
{
    private final Transform transform ;
    public ExprTransformApplyTransform(Transform transform)
    {
        this.transform = transform ;
    }
    
    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg)
    {
        Op opArg2 = Transformer.transform(transform, opArg) ;
        if ( opArg2 == opArg )
            return super.transform(funcOp, args, opArg) ;
        if ( funcOp instanceof E_Exists )
            return new E_Exists(opArg2) ;
        if ( funcOp instanceof E_NotExists )
            return new E_NotExists(opArg2) ;
        throw new ARQInternalErrorException("Unrecognized ExprFunctionOp: \n"+funcOp) ;
    }
}