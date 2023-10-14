package org.apache.jena.sparql.expr.aggregate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.SerializationFactory;
import org.apache.jena.atlas.data.SortedDataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.library.cdt.CDTLiteralFunctionUtils;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.writers.WriterExpr;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.ExprUtils;

public class AggFoldMap extends AggregatorBase
{
	protected final Expr expr1;  // While the values of these member variables can also be extracted from the ExprList (see
	protected final Expr expr2;  // createExprList below), keeping these copies here makes it easier to access these values.

	protected final List<SortCondition> orderBy;
	protected final ThresholdPolicy<Binding> policy;
	protected final Comparator<Binding> comparator;

	public AggFoldMap( final Expr expr1, final Expr expr2 ) {
		this(expr1, expr2, null);
	}

	public AggFoldMap( final Expr expr1, final Expr expr2, final List<SortCondition> orderBy ) {
		// We need to extract the expressions from the sort conditions
		// as well in order for them to be considered by the algebra
		// transformer that renames variables from subqueries (see
		// {@link TransformScopeRename#RenameByScope}).
		super( "FOLD", false, collectExprs(expr1, expr2, orderBy) );

		this.expr1 = expr1;
		this.expr2 = expr2;

		this.orderBy = orderBy;

		if ( orderBy != null && ! orderBy.isEmpty() ) {
			policy = ThresholdPolicyFactory.policyFromContext( ARQ.getContext() );
			comparator = new BindingComparator(orderBy);
		}
		else {
			policy = null;
			comparator = null;
		}
	}

	protected static ExprList collectExprs( final Expr expr1, final Expr expr2, final List<SortCondition> orderBy ) {
		final ExprList l = new ExprList();
		l.add(expr1);
		l.add(expr2);

		if ( orderBy != null ) {
			for ( final SortCondition c : orderBy ) {
				l.add( c.getExpression() );
			}
		}

		return l;
	}

	@Override
	public Aggregator copy( final ExprList exprs ) {
		final Expr _expr1;
		final Expr _expr2;
		final List<SortCondition> _orderBy;
		if ( orderBy == null ) {
			if ( exprs.size() != 2 ) throw new IllegalArgumentException();

			_expr1 = exprs.get(0);
			_expr2 = exprs.get(1);
			_orderBy = null;
		}
		else {
			if ( exprs.size() != orderBy.size()+2 ) throw new IllegalArgumentException();


			final Iterator<SortCondition> cit = orderBy.iterator();
			final Iterator<Expr> eit = exprs.iterator();

			_expr1 = eit.next();
			_expr2 = eit.next();

			_orderBy = new ArrayList<>( orderBy.size() );
			while ( eit.hasNext() ) {
				final SortCondition c = new SortCondition( eit.next(), cit.next().getDirection() );
				_orderBy.add(c);
			}
		}

		return new AggFoldMap(_expr1, _expr2, _orderBy);
	}

	@Override
	public boolean equals( final Aggregator other, final boolean bySyntax ) {
		if ( other == null ) return false;
		if ( this == other ) return true;
		if ( ! ( other instanceof AggFoldMap ) )
			return false;
		final AggFoldMap fold = (AggFoldMap) other;
		return exprList.equals(fold.exprList, bySyntax)
		       && Objects.equals(orderBy, fold.orderBy);
	}

	@Override
	public Accumulator createAccumulator() {
		if ( orderBy != null && ! orderBy.isEmpty() )
			return new SortingMapAccumulator();
		else
			return new BasicMapAccumulator();
	}

	@Override
	public Node getValueEmpty() {
		final Map<CDTKey,CDTValue> emptyMap = new HashMap<>();
		return CDTLiteralFunctionUtils.createNode(emptyMap);
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

		if ( orderBy != null && ! orderBy.isEmpty() ) {
			out.append(" ORDER BY ");
			final Iterator<SortCondition> it = orderBy.iterator();
			while ( it.hasNext() ) {
				it.next().output(out, sCxt);
				if ( it.hasNext() ) out.append(", ");
			}
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
		out.append(", ");
		WriterExpr.output(out, expr2, null);

		if ( orderBy != null && ! orderBy.isEmpty() ) {
			out.append(" order by ");
			out.incIndent();
			for ( final SortCondition c : orderBy ) {
				c.output(out);
			}
			out.decIndent();
		}

		out.decIndent();
		out.append(")");
		return out.asString();
	}

	protected class BasicMapAccumulator implements Accumulator {
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

	protected class SortingMapAccumulator implements Accumulator {
		protected final SortedDataBag<Binding> sbag;
		protected FunctionEnv functionEnv = null;

		public SortingMapAccumulator() {
			final SerializationFactory<Binding> sf = SerializationFactoryFinder.bindingSerializationFactory();
			sbag = BagFactory.newSortedBag(policy, sf, comparator);
		}

		@Override
		public void accumulate( final Binding binding, final FunctionEnv functionEnv ) {
			sbag.add(binding);

			if ( this.functionEnv == null )
				this.functionEnv = functionEnv;
		}

		@Override
		public NodeValue getValue() {
			final Iterator<Binding> it = sbag.iterator();
			final Accumulator acc = new BasicMapAccumulator();

			while ( it.hasNext() ) {
				acc.accumulate( it.next(), functionEnv );
			}

			sbag.close();

			return acc.getValue();
		}
	}

}
