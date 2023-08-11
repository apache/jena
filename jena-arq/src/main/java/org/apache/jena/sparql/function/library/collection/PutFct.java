package org.apache.jena.sparql.function.library.collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;

public class PutFct extends FunctionBase
{
	@Override
	public void checkBuild( final String uri, final ExprList args ) {
		if ( args.size() < 2 || args.size() > 3 )
			throw new QueryBuildException("Function '"+Lib.className(this)+"' takes two or three arguments");
	}

	@Override
	public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		if ( args.size() < 2 || args.size() > 3 )
			throw new ExprException("wrong number of arguments (" + args.size() + "), must be 2 or 3");

		// check the second argument first because that's less expensive
		final NodeValue nv2 = args.get(1).eval(binding, env);
		final Node n2 = nv2.asNode();
		if ( ! n2.isURI() && ! n2.isLiteral() )
			throw new ExprEvalException("Not a valid map key: " + nv2);

		// now check the first argument
		final NodeValue nv1 = args.get(0).eval(binding, env);
		final Map<CDTKey,CDTValue> map = CDTLiteralFunctionUtils.checkAndGetMap(nv1);

		final CDTKey key = CDTFactory.createKey(n2);

		// produce a map value from the third argument (if any)
		final CDTValue newValue;
		if ( args.size() == 2 ) {
			newValue = CDTFactory.getNullValue();
		}
		else {  // in this case, we have that args.size() == 3
			NodeValue nv3 = null;
			try {
				nv3 = args.get(2).eval(binding, env);
			}
			catch ( final ExprException ex ) {
				// nothing to do here
			}

			if ( nv3 != null ) {
				newValue = CDTFactory.createValue( nv3.asNode() );
			}
			else {
				newValue = CDTFactory.getNullValue();
			}
		}

		// check if the given map already contains the exact same map entry
		// if so, simply return the given cdt:Map literal
		final CDTValue oldValue = map.get(key);
		if ( oldValue != null ) {
			if ( oldValue.isNull() && newValue.isNull() )
				return nv1;

			if ( ! oldValue.isNull() && ! newValue.isNull() ) {
				final Node on = oldValue.asNode();
				final Node nn = newValue.asNode();
				if ( on.equals(nn) )
					return nv1;
			}
		}

		final Map<CDTKey,CDTValue> newMap = new HashMap<>(map);
		newMap.put(key, newValue);

		return CDTLiteralFunctionUtils.createNodeValue(newMap);
	}

	@Override
	public NodeValue exec( final List<NodeValue> args ) {
		throw new IllegalStateException("should never end up here");
	}

}
