package org.apache.jena.sparql.function.library;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.function.FunctionBase;

import java.util.List;

/** sprintf(string,string) - Java style */

public class sprintf extends FunctionBase
{
    public sprintf() { super() ; }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if ( args.size() != 2)
            throw new QueryBuildException("Function '"+ Lib.className(this)+"' takes two or three arguments") ;
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        if ( args.size() != 2 )
            throw new ExprEvalException(Lib.className(this)+": Wrong number of arguments: "+
                    args.size()+" : [wanted 2]") ;

        NodeValue v1 = args.get(0) ;
        NodeValue v2 = args.get(1) ;

        return XSDFuncOp.javaSprintf(v1, v2) ;
    }

}
