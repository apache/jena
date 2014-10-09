package com.hp.hpl.jena.sparql.function.library.leviathan;

import java.util.List;

import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase;
import com.hp.hpl.jena.sparql.util.Utils;

public class cartesian extends FunctionBase {

    @Override
    public NodeValue exec(List<NodeValue> args) {
        if (args.size() != 4 && args.size() != 6)
            throw new ExprEvalException("Incorrect number of arguments");

        switch (args.size()) {
        case 4: {
            double dX = args.get(0).getDouble() - args.get(2).getDouble();
            double dY = args.get(1).getDouble() - args.get(3).getDouble();

            return NodeValue.makeDouble(Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)));
        }
        case 6: {
            double dX = args.get(0).getDouble() - args.get(3).getDouble();
            double dY = args.get(1).getDouble() - args.get(4).getDouble();
            double dZ = args.get(2).getDouble() - args.get(5).getDouble();

            return NodeValue.makeDouble(Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2) + Math.pow(dZ, 2)));
        }
        default:
            throw new ExprEvalException("Incorrect number of arguments");
        }
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if (args.size() != 4 && args.size() != 6)
            throw new QueryBuildException("Function '" + Utils.className(this) + "' takes 4 or 6 argument(s)");
    }

}
