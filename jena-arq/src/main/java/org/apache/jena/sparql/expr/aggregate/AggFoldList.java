package org.apache.jena.sparql.expr.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.library.cdt.CDTLiteralFunctionUtils;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.writers.WriterExpr;
import org.apache.jena.sparql.util.ExprUtils;

public class AggFoldList extends AggregatorBase
{
	public AggFoldList( final boolean isDistinct, final Expr expr1 ) {
		super( "FOLD", isDistinct, expr1 );
	}

	@Override
	public Aggregator copy( final ExprList exprs ) {
		if ( exprs.size() != 1 )
			throw new IllegalArgumentException();

		final Expr _expr1 = exprs.get(0);

		return new AggFoldList(isDistinct, _expr1);
	}

	@Override
	public boolean equals( final Aggregator other, final boolean bySyntax ) {
		if ( other == null ) return false;
		if ( this == other ) return true;
		if ( ! ( other instanceof AggFoldList ) )
			return false;
		final AggFoldList fold = (AggFoldList) other;
		return getExpr().equals(fold.getExpr(), bySyntax) && (isDistinct == fold.isDistinct);
	}

	@Override
	public Accumulator createAccumulator() {
		return new ListAccumulator( getExpr(), isDistinct );
	}

	@Override
	public Node getValueEmpty() {
		return null;
	}

	@Override
	public int hashCode() {
		return HC_AggFoldList ^ getExpr().hashCode();
	}

	@Override
	public String asSparqlExpr( final SerializationContext sCxt ) {
		final IndentedLineBuffer out = new IndentedLineBuffer();
		out.append( getName() );
		out.append( "(" );

		if ( isDistinct )
			out.append("DISTINCT ");

		ExprUtils.fmtSPARQL(out, getExpr(), sCxt);

		out.append(")");
		return out.asString();
	}

	@Override
	public String toPrefixString() {
		final IndentedLineBuffer out = new IndentedLineBuffer();
		out.append("(");
		out.append( getName().toLowerCase(Locale.ROOT) );
		out.incIndent();

		if ( isDistinct )
			out.append(" distinct");

		WriterExpr.output(out, getExpr(), null);

		out.decIndent();
		out.append(")");
		return out.asString();
	}

	protected class ListAccumulator extends AccumulatorExpr {
		final protected List<CDTValue> list = new ArrayList<>();

		protected ListAccumulator( final Expr expr, final boolean makeDistinct ) {
			super(expr, makeDistinct);
		}

		@Override
		protected void accumulate( final NodeValue nv, final Binding binding, final FunctionEnv functionEnv ) {
			final CDTValue v = CDTFactory.createValue( nv.asNode() );
			list.add(v);
		}

		@Override
		protected void accumulateError( final Binding binding, final FunctionEnv functionEnv ) {
			final CDTValue v = CDTFactory.getNullValue();
			list.add(v);
		}

		@Override
		protected NodeValue getAccValue() {
			return CDTLiteralFunctionUtils.createNodeValue(list);
		}

		// Overriding this function because the base class would otherwise not
		// call getAccValue() in cases in which there was an error during the
		// evaluation of the 'expr' for any of the solution mappings. For FOLD,
		// however, errors do not cause an aggregation error but just produce
		// null values in the created list.
		@Override
		public NodeValue getValue() {
			return getAccValue();
		}
	}

}
