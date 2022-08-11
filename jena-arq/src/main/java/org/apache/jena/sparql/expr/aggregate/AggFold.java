package org.apache.jena.sparql.expr.aggregate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterUnfold;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprNode;
import org.apache.jena.sparql.expr.ExprNone;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.ExprUtils;

public class AggFold extends AggregatorBase
{
	protected final Expr expr1;       // While the values of these member variables
	protected final Expr expr2;       // can also be extracted from the ExprList (see
	protected final String typeIRI1;  // createExprList below), keeping these copies
	protected final String typeIRI2;  // here makes it easier to access these values.

	public AggFold( final Expr expr1 ) {
		this(expr1, null, null, null);
	}

	public AggFold( final Expr expr1, final String typeIRI1 ) {
		this(expr1, typeIRI1, null, null);
	}

	public AggFold( final Expr expr1, final String typeIRI1, final Expr expr2 ) {
		this(expr1, typeIRI1, expr2, null);
	}

	public AggFold( final Expr expr1, final Expr expr2 ) {
		this(expr1, null, expr2, null);
	}

	public AggFold( final Expr expr1, final String typeIRI1, final Expr expr2, final String typeIRI2 ) {
		super( "FOLD", false, createExprList(expr1, typeIRI1, expr2, typeIRI2) );

		this.expr1 = expr1;
		this.expr2 = expr2;
		this.typeIRI1 = typeIRI1;
		this.typeIRI2 = typeIRI2;
	}

	protected static ExprList createExprList( final Expr expr1, final String typeIRI1,
	                                          final Expr expr2, final String typeIRI2 ) {
		final ExprList l = new ExprList(expr1);

		if ( typeIRI1 != null ) {
			l.add(dummyExprForType);
			l.add( new NodeValueString(typeIRI1) );
		}

		if ( expr2 != null ) {
			l.add(expr2);
		}

		if ( typeIRI2 != null ) {
			l.add(dummyExprForType);
			l.add( new NodeValueString(typeIRI2) );
		}

		return l;
	}

	@Override
	public Aggregator copy( final ExprList exprs ) {
		if ( exprs.size() < 1 || exprs.size() > 6 )
			throw new IllegalArgumentException();

		final Iterator<Expr> it = exprs.iterator();
		final Expr _expr1 = it.next();

		final Expr _expr2;
		final String _typeIRI1;
		final String _typeIRI2;

		Expr lookAhead = it.hasNext() ? it.next() : null;
		// _typeIRI1
		if ( lookAhead != null && lookAhead == dummyExprForType ) {
			lookAhead = it.hasNext() ? it.next() : null;
			if ( lookAhead != null && lookAhead instanceof NodeValueString )
				_typeIRI1 = ((NodeValueString) lookAhead).getString();
			else
				throw new IllegalArgumentException();

			lookAhead = it.hasNext() ? it.next() : null;
		}
		else {
			_typeIRI1 = null;
		}

		// _expr2
		if ( lookAhead != null ) {
			_expr2 = lookAhead;
			lookAhead = it.hasNext() ? it.next() : null;
		}
		else {
			_expr2 = null;
		}

		// _typeIRI2
		if ( lookAhead != null && lookAhead == dummyExprForType ) {
			lookAhead = it.hasNext() ? it.next() : null;
			if ( lookAhead != null && lookAhead instanceof NodeValueString )
				_typeIRI2 = ((NodeValueString) lookAhead).getString();
			else
				throw new IllegalArgumentException();

			lookAhead = it.hasNext() ? it.next() : null;
		}
		else {
			_typeIRI2 = null;
		}

		if ( lookAhead != null ) {
			throw new IllegalArgumentException();
		}

		return new AggFold(_expr1, _typeIRI1, _expr2, _typeIRI2);
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

		if ( typeIRI1 != null ) {
			out.append( " TYPE " + typeIRI1 );
		}

		if ( expr2 != null ) {
			out.append(", ");
			ExprUtils.fmtSPARQL(out, expr2, sCxt);
		}

		if ( typeIRI2 != null ) {
			out.append( " TYPE " + typeIRI2 );
		}

		out.append(")");
		return out.asString();
	}


	protected static Expr dummyExprForType = new ExprNode() {
		@Override public void visit(ExprVisitor visitor) { visitor.visit( (ExprNone) ExprNone.NONE ); }

		@Override public int hashCode() { return -88888; }

		@Override public boolean equals(Expr other, boolean bySyntax) { return other == this; }

		@Override public Expr copySubstitute(Binding binding) { return this; }

		@Override public Expr applyNodeTransform(NodeTransform transform) { return this; }

		@Override public NodeValue eval(Binding binding, FunctionEnv env) {
			throw new InternalErrorException("Attempt to eval DummyExprForType");
		}
	};

	protected class ListAccumulator implements Accumulator {
		final protected List<Node> list = new ArrayList<>();

		@Override
		public void accumulate( final Binding binding, final FunctionEnv functionEnv ) {
			final NodeValue nv = ExprLib.evalOrNull(expr1, binding, functionEnv);
			if ( nv != null )
				list.add( nv.asNode() );
		}

		@Override
		public NodeValue getValue() {
			final StringBuilder sb = new StringBuilder();
			if ( ! list.isEmpty() ) {
				final Iterator<Node> it = list.iterator();
				final Node firstNode = it.next();
				final String firstNodeAsString = NodeFmtLib.strTTL(firstNode);
				sb.append(firstNodeAsString);
				while ( it.hasNext() ) {
					final Node nextNode = it.next();
					final String nextNodeAsString = NodeFmtLib.strTTL(nextNode);
					sb.append(", ");
					sb.append(nextNodeAsString);
				}
			}
			final Node n = NodeFactory.createLiteral( sb.toString(), QueryIterUnfold.datatypeUntypedList );
			return NodeValue.makeNode(n);
		}
	}

	protected class MapAccumulator implements Accumulator {
		final protected List<Node> keys   = new ArrayList<>();
		final protected List<Node> values = new ArrayList<>();

		@Override
		public void accumulate( final Binding binding, final FunctionEnv functionEnv ) {
			final NodeValue key   = ExprLib.evalOrNull(expr1, binding, functionEnv);
			final NodeValue value = ExprLib.evalOrNull(expr2, binding, functionEnv);
			if ( key != null && value != null ) {
				keys.add( key.asNode() );
				values.add( value.asNode() );
			}
		}

		@Override
		public NodeValue getValue() {
			final StringBuilder sb = new StringBuilder();
			if ( ! keys.isEmpty() ) {
				final Iterator<Node> it  = keys.iterator();
				final Iterator<Node> it2 = values.iterator();
				final Node firstKey   = it.next();
				final Node firstValue = it2.next();
				final String firstKeyAsString   = NodeFmtLib.strTTL(firstKey);
				final String firstValueAsString = NodeFmtLib.strTTL(firstValue);
				sb.append(firstKeyAsString);
				sb.append(" : ");
				sb.append(firstValueAsString);
				while ( it.hasNext() ) {
					final Node nextKey   = it.next();
					final Node nextValue = it2.next();
					final String nextKeyAsString   = NodeFmtLib.strTTL(nextKey);
					final String nextValueAsString = NodeFmtLib.strTTL(nextValue);
					sb.append(", ");
					sb.append(nextKeyAsString);
					sb.append(" : ");
					sb.append(nextValueAsString);
				}
			}
			final Node n = NodeFactory.createLiteral( sb.toString(), QueryIterUnfold.datatypeUntypedMap );
			return NodeValue.makeNode(n);
		}
	}

}
