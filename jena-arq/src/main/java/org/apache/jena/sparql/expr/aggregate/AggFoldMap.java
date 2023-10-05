package org.apache.jena.sparql.expr.aggregate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.library.cdt.CDTLiteralFunctionUtils;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.writers.WriterExpr;
import org.apache.jena.sparql.util.ExprUtils;

public class AggFoldMap extends AggregatorBase
{
	protected final Expr expr1;  // While the values of these member variables can also be extracted from the ExprList (see
	protected final Expr expr2;  // createExprList below), keeping these copies here makes it easier to access these values.

	public AggFoldMap( final Expr expr1, final Expr expr2 ) {
		super( "FOLD", false, createExprList(expr1, expr2) );

		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	protected static ExprList createExprList( final Expr expr1, final Expr expr2 ) {
		final ExprList l = new ExprList();
		l.add(expr1);
		l.add(expr2);

		return l;
	}

	@Override
	public Aggregator copy( final ExprList exprs ) {
		if ( exprs.size() != 2 )
			throw new IllegalArgumentException();

		return new AggFoldMap( exprs.get(0), exprs.get(1) );
	}

	@Override
	public boolean equals( final Aggregator other, final boolean bySyntax ) {
		if ( other == null ) return false;
		if ( this == other ) return true;
		if ( ! ( other instanceof AggFoldMap ) )
			return false;
		final AggFoldMap fold = (AggFoldMap) other;
		return exprList.equals(fold.exprList, bySyntax);
	}

	@Override
	public Accumulator createAccumulator() {
		return new MapAccumulator();
	}

	@Override
	public Node getValueEmpty() {
		return null;
	}

	@Override
	public int hashCode() {
		int hc = HC_AggFoldMap;
		hc ^= getExprList().get(0).hashCode();
		hc ^= getExprList().get(1).hashCode();

		return hc;
	}

	@Override
	public String asSparqlExpr( final SerializationContext sCxt ) {
		final IndentedLineBuffer out = new IndentedLineBuffer();
		out.append( getName() );
		out.append( "(" );

		ExprUtils.fmtSPARQL(out, expr1, sCxt);
		out.append(", ");
		ExprUtils.fmtSPARQL(out, expr2, sCxt);

		out.append(")");
		return out.asString();
	}

	@Override
	public String toPrefixString() {
		final IndentedLineBuffer out = new IndentedLineBuffer();
		out.append("(");
		out.append( getName().toLowerCase(Locale.ROOT) );
		out.incIndent();

		WriterExpr.output(out, expr1, null);
		out.append(", ");
		WriterExpr.output(out, expr2, null);

		out.decIndent();
		out.append(")");
		return out.asString();
	}

	protected class MapAccumulator implements Accumulator {
		final protected Map<CDTKey,CDTValue> map = new HashMap<>();

		@Override
		public void accumulate( final Binding binding, final FunctionEnv functionEnv ) {
			final NodeValue nvKey = ExprLib.evalOrNull(expr1, binding, functionEnv);
			if ( nvKey == null ) {
				return; // ignore if creating the key using the given binding failed
			}
			if ( nvKey.isBlank() ) {
				return; // ignore if the key would be a blank node
			}

			final CDTKey key = CDTFactory.createKey( nvKey.asNode() );

			// TODO: what do we do if the map already contains an entry with the same key?

			final CDTValue value;
			final NodeValue nvValue = ExprLib.evalOrNull(expr2, binding, functionEnv);
			if ( nvValue == null ) {
				value = CDTFactory.getNullValue();
			}
			else {
				value = CDTFactory.createValue( nvValue.asNode() );
			}

			map.put(key, value);
		}

		@Override
		public NodeValue getValue() {
			return CDTLiteralFunctionUtils.createNodeValue(map);
		}
	}

}
