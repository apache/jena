package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformWrapper ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprTransformer ;

/** Take transform and apply it to the algebra operator of E_Exist and E_NoExists in an OpFilter.
 * Normally, the wrapped Transform is one derived from TransformCopy. 
 */
public class TransformApplyInsideExprFunctionOp extends TransformWrapper
{
    final private ExprTransformApplyTransform exprTransform ; 
    public TransformApplyInsideExprFunctionOp(Transform transform)
    {
        super(transform) ;
        exprTransform = new ExprTransformApplyTransform(transform) ;
    }
    
    @Override
    public Op transform(OpFilter opFilter, Op x) 
    {
        ExprList ex = new ExprList() ;
        for ( Expr e : opFilter.getExprs() )
        {
            Expr e2 = ExprTransformer.transform(exprTransform, e) ;
            ex.add(e2) ;
        }
        OpFilter f = (OpFilter)OpFilter.filter(ex, x) ;
        return super.transform(f, x) ;
    }
}