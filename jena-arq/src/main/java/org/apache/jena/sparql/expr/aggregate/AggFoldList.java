package org.apache.jena.sparql.expr.aggregate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.SerializationFactory;
import org.apache.jena.atlas.data.SortedDataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.library.cdt.CDTLiteralFunctionUtils;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.writers.WriterExpr;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.ExprUtils;

public class AggFoldList extends AggregatorBase
{
	protected final List<SortCondition> orderBy;
	protected final ThresholdPolicy<Binding> policy;
	protected final Comparator<Binding> comparator;

	public AggFoldList( final boolean isDistinct, final Expr expr1 ) {
		this(isDistinct, expr1, null);
	}

	public AggFoldList( final boolean isDistinct, final Expr expr1, final List<SortCondition> orderBy ) {
		// We need to extract the expressions from the sort conditions
		// as well in order for them to be considered by the algebra
		// transformer that renames variables from subqueries (see
		// {@link TransformScopeRename#RenameByScope}).
		super( "FOLD", isDistinct, collectExprs(expr1, orderBy) );

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

	protected static ExprList collectExprs( final Expr expr1, final List<SortCondition> orderBy ) {
		final ExprList l = new ExprList(expr1);

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
		final List<SortCondition> _orderBy;
		if ( orderBy == null ) {
			if ( exprs.size() != 1 ) throw new IllegalArgumentException();

			_expr1 = exprs.get(0);
			_orderBy = null;
		}
		else {
			if ( exprs.size() != orderBy.size()+1 ) throw new IllegalArgumentException();


			final Iterator<SortCondition> cit = orderBy.iterator();
			final Iterator<Expr> eit = exprs.iterator();

			_expr1 = eit.next();

			_orderBy = new ArrayList<>( orderBy.size() );
			while ( eit.hasNext() ) {
				final SortCondition c = new SortCondition( eit.next(), cit.next().getDirection() );
				_orderBy.add(c);
			}
		}

		return new AggFoldList(isDistinct, _expr1, _orderBy);
	}

	@Override
	public boolean equals( final Aggregator other, final boolean bySyntax ) {
		if ( other == null ) return false;
		if ( this == other ) return true;
		if ( ! ( other instanceof AggFoldList ) )
			return false;
		final AggFoldList fold = (AggFoldList) other;
		return (isDistinct == fold.isDistinct)
		       && getExprList().get(0).equals(fold.getExprList().get(0), bySyntax)
		       && Objects.equals(orderBy, fold.orderBy);
	}

	@Override
	public Accumulator createAccumulator() {
		if ( orderBy != null && ! orderBy.isEmpty() )
			return new SortingListAccumulator();
		else
			return new BasicListAccumulator( getExprList().get(0), isDistinct );
	}

	@Override
	public Node getValueEmpty() {
		final List<CDTValue> emptyList = new ArrayList<>();
		return CDTLiteralFunctionUtils.createNode(emptyList);
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

		ExprUtils.fmtSPARQL(out, getExprList().get(0), sCxt);

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

		if ( isDistinct )
			out.append(" distinct");

		WriterExpr.output(out, getExprList().get(0), null);

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

	protected static class BasicListAccumulator extends AccumulatorExpr {
		final protected List<CDTValue> list = new ArrayList<>();
		protected boolean nullValueAdded = false;

		protected BasicListAccumulator( final Expr expr, final boolean makeDistinct ) {
			super(expr, makeDistinct);
		}

		@Override
		protected void accumulate( final NodeValue nv, final Binding binding, final FunctionEnv functionEnv ) {
			final CDTValue v = CDTFactory.createValue( nv.asNode() );
			list.add(v);
		}

		@Override
		protected void accumulateError( final Binding binding, final FunctionEnv functionEnv ) {
			// By definition of FOLD, evaluation errors result in null values
			// for the created list. If FOLD is used with the keyword DISTINCT,
			// then all errors collapse to a single null value.
			if ( makeDistinct && nullValueAdded ) {
				return;
			}

			nullValueAdded = true;

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

	protected class SortingListAccumulator implements Accumulator {
		protected final SortedDataBag<Binding> sbag;
		protected FunctionEnv functionEnv = null;

		public SortingListAccumulator() {
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
			final Accumulator acc = new BasicListAccumulator( getExprList().get(0), isDistinct );

			while ( it.hasNext() ) {
				acc.accumulate( it.next(), functionEnv );
			}

			sbag.close();

			return acc.getValue();
		}
	}

}
