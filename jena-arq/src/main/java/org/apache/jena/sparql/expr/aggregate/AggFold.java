package org.apache.jena.sparql.expr.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.LiteralLabelForList;
import org.apache.jena.cdt.LiteralLabelForMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.writers.WriterExpr;
import org.apache.jena.sparql.util.ExprUtils;

public class AggFold extends AggregatorBase
{
	protected final Expr expr1;  // While the values of these member variables can also be extracted from the ExprList (see
	protected final Expr expr2;  // createExprList below), keeping these copies here makes it easier to access these values.

	public AggFold( final Expr expr1 ) {
		this(expr1, null);
	}

	public AggFold( final Expr expr1, final Expr expr2 ) {
		super( "FOLD", false, createExprList(expr1, expr2) );

		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	protected static ExprList createExprList( final Expr expr1, final Expr expr2 ) {
		final ExprList l = new ExprList(expr1);

		if ( expr2 != null ) {
			l.add(expr2);
		}

		return l;
	}

	@Override
	public Aggregator copy( final ExprList exprs ) {
		if ( exprs.size() < 1 || exprs.size() > 2 )
			throw new IllegalArgumentException();

		final Iterator<Expr> it = exprs.iterator();
		final Expr _expr1 = it.next();

		Expr lookAhead = it.hasNext() ? it.next() : null;

		final Expr _expr2 = ( lookAhead != null ) ? lookAhead : null;

		return new AggFold(_expr1, _expr2);
	}

	@Override
	public boolean equals( final Aggregator other, final boolean bySyntax ) {
		if ( other == null ) return false;
		if ( this == other ) return true;
		if ( ! ( other instanceof AggFold ) )
			return false;
		final AggFold fold = (AggFold) other;
		return exprList.equals(fold.exprList, bySyntax);
	}

	@Override
	public Accumulator createAccumulator() {
		if ( expr2 == null )
			return new ListAccumulator();
		else
			return new MapAccumulator();
	}

	@Override
	public Node getValueEmpty() {
		return null;
	}

	@Override
	public int hashCode() {
		int hc = HC_AggFold;
		for ( final Expr e : getExprList() ) {
			hc ^= e.hashCode();
		}
		return hc;
	}

	@Override
	public String asSparqlExpr( final SerializationContext sCxt ) {
		final IndentedLineBuffer out = new IndentedLineBuffer();
		out.append( getName() );
		out.append( "(" );

		ExprUtils.fmtSPARQL(out, expr1, sCxt);

		if ( expr2 != null ) {
			out.append(", ");
			ExprUtils.fmtSPARQL(out, expr2, sCxt);
		}

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

		if ( expr2 != null ) {
			out.append(", ");
			WriterExpr.output(out, expr2, null);
		}

		out.decIndent();
		out.append(")");
		return out.asString();
	}

	protected class ListAccumulator implements Accumulator {
		final protected List<CDTValue> list = new ArrayList<>();

		@Override
		public void accumulate( final Binding binding, final FunctionEnv functionEnv ) {
			final CDTValue v;
			final NodeValue nv = ExprLib.evalOrNull(expr1, binding, functionEnv);
			if ( nv == null ) {
				v = CDTFactory.getNullValue();
			}
			else {
				v = CDTFactory.createValue( nv.asNode() );
			}

			list.add(v);
		}

		@Override
		public NodeValue getValue() {
			final LiteralLabelForList lit = new LiteralLabelForList(list);
			final Node n = NodeFactory.createLiteral(lit);
			return NodeValue.makeNode(n);
		}
	}

	protected class MapAccumulator implements Accumulator {
		final protected Map<CDTKey,CDTValue> map = new HashMap<>();

		@Override
		public void accumulate( final Binding binding, final FunctionEnv functionEnv ) {
			final NodeValue nvKey = ExprLib.evalOrNull(expr1, binding, functionEnv);
			if ( nvKey == null ) {
				return; // ignore if creating the key using the given binding failed
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
			final LiteralLabelForMap lit = new LiteralLabelForMap(map);
			final Node n = NodeFactory.createLiteral(lit);
			return NodeValue.makeNode(n);
		}
	}

}
