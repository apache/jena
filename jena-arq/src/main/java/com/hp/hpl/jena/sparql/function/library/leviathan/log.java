package com.hp.hpl.jena.sparql.function.library.leviathan;

import java.util.List;

import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase;
import com.hp.hpl.jena.sparql.util.Utils;

public class log extends FunctionBase {

    @Override
    public NodeValue exec(List<NodeValue> args) {
        if (args.size() < 1 || args.size() > 2)
            throw new ExprEvalException("Invalid number of arguments");

        NodeValue v = args.get(0);

        if (args.size() == 1) {
            // Log base 10
            return NodeValue.makeDouble(Math.log10(v.getDouble()));
        } else {
            // Log with arbitrary base
            // See http://en.wikipedia.org/wiki/List_of_logarithmic_identities#Changing_the_base
            NodeValue base = args.get(1);

            return NodeValue.makeDouble(Math.log10(v.getDouble()) / Math.log10(base.getDouble()));
        }
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if (args.size() < 1 || args.size() > 2)
            throw new QueryBuildException("Function '" + Utils.className(this) + "' takes one/two argument(s)");
    }

}
