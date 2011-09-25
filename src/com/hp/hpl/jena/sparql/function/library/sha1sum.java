package com.hp.hpl.jena.sparql.function.library;
// Contribution from Leigh Dodds 

import com.hp.hpl.jena.sparql.expr.E_SHA1 ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;
import com.hp.hpl.jena.sparql.function.FunctionBase1 ;

/**
 * ARQ Extension Function that will calculate the 
 * SHA1 sum of any literal. Useful for working with 
 * FOAF data.
 */
public class sha1sum extends FunctionBase1 
{
    // This exists for compatibility.
    // SPARQL 1.1 has a SHA1(expression) function.
    // This stub implements afn:sha1(expr) as an indirection to that function.
    
    private E_SHA1 sha1 = new E_SHA1(null) ;
    
    public sha1sum() {}
    
    @Override
    public NodeValue exec(NodeValue nodeValue) 
    {
        nodeValue = NodeFunctions.str(nodeValue) ;
        return sha1.eval(nodeValue) ;
    }
}
