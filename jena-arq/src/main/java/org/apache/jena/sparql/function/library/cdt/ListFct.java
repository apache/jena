package org.apache.jena.sparql.function.library.cdt;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;

public class ListFct extends FunctionBase
{
	@Override
	public void checkBuild( final String uri, final ExprList args ) {
		// nothing to do here
	}

	@Override
	public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		final List<CDTValue> list = new ArrayList<>( args.size() );

		for ( final Expr e : args ) {
			CDTValue v;
			try {
				final NodeValue nv = e.eval(binding, env);
				v = CDTFactory.createValue( nv.asNode() );
			} catch ( final ExprException ex ) {
				v = CDTFactory.getNullValue();
			}

			list.add(v);
		}

		return CDTLiteralFunctionUtils.createNodeValue(list);
	}

	@Override
	public NodeValue exec( final List<NodeValue> args ) {
		throw new IllegalStateException("should never end up here");
	}

}
