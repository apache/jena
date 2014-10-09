package com.hp.hpl.jena.sparql.function.library.leviathan;

import java.util.List;

import org.apache.jena.atlas.lib.RandomLib;

import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase;
import com.hp.hpl.jena.sparql.util.Utils;

public class rnd extends FunctionBase {

    @Override
    public NodeValue exec(List<NodeValue> args) {
        if (args.size() > 2)
            throw new ExprEvalException("Too many arguments");

        switch (args.size()) {
        case 0:
            return NodeValue.makeDouble(RandomLib.random.nextDouble());
        case 1: {
            double max = args.get(0).getDouble();
            if (max <= 0d)
                throw new ExprEvalException("Max must be > 0");
            return NodeValue.makeDouble(RandomLib.random.nextDouble() * max);
        }
        case 2: {
            double min = args.get(0).getDouble();
            double max = args.get(1).getDouble();
            if (min > max)
                throw new ExprEvalException("Min cannot be greater than the max");
            
            double range = max - min;
            double value = min + (RandomLib.random.nextDouble() * range);
            return NodeValue.makeDouble(value);
        }
        default:
            throw new ExprEvalException("Too many arguments");
        }
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if (args.size() > 2)
            throw new QueryBuildException("Function '" + Utils.className(this)
                    + "' takes between 0, 1 or 2 argument(s)");
    }

}
