package org.apache.jena.sparql.algebra.walker;

import org.apache.jena.sparql.expr.ExprVar ;

/** Marker, used in place of a null.
 *  This may be tested for using {@code ==} */ 
public class ExprNone extends ExprVar {
    public static ExprNone NONE = new ExprNone() ;
    private ExprNone() { super("") ; }
}
