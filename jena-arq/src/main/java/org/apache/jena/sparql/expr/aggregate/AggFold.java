package org.apache.jena.sparql.expr.aggregate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_LiteralWithList;
import org.apache.jena.graph.Node_LiteralWithMap;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.writers.WriterExpr;
import org.apache.jena.sparql.util.ExprUtils;

public class AggFold extends AggregatorBase
{
	protected static Expr dummyExprForTypeKeyword = new NodeValueNode( Node.ANY );// Node_Marker.xlabel("TYPE") );

	protected final Expr expr1;       // While the values of these member variables
	protected final Expr expr2;       // can also be extracted from the ExprList (see
	protected final Node typeIRI1;    // createExprList below), keeping these copies
	protected final Node typeIRI2;    // here makes it easier to access these values.

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
		this.typeIRI1 = (typeIRI1 != null) ? NodeFactory.createURI(typeIRI1) : null;
		this.typeIRI2 = (typeIRI2 != null) ? NodeFactory.createURI(typeIRI2) : null;
	}

	protected static ExprList createExprList( final Expr expr1, final String typeIRI1,
	                                          final Expr expr2, final String typeIRI2 ) {
		final ExprList l = new ExprList(expr1);

		if ( typeIRI1 != null ) {
			l.add(dummyExprForTypeKeyword);
			l.add( new NodeValueString(typeIRI1) );
		}

		if ( expr2 != null ) {
			l.add(expr2);
		}

		if ( typeIRI2 != null ) {
			l.add(dummyExprForTypeKeyword);
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
		if ( lookAhead != null && lookAhead == dummyExprForTypeKeyword ) {
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
		if ( lookAhead != null && lookAhead == dummyExprForTypeKeyword ) {
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
		final PrefixMap pmap = PrefixMapFactory.create( sCxt.getPrefixMapping() );

		final IndentedLineBuffer out = new IndentedLineBuffer();
		out.append( getName() );
		out.append( "(" );

		ExprUtils.fmtSPARQL(out, expr1, sCxt);

		if ( typeIRI1 != null ) {
			out.append( " TYPE " );
			out.append( NodeFmtLib.str(typeIRI1, pmap) );
		}

		if ( expr2 != null ) {
			out.append(", ");
			ExprUtils.fmtSPARQL(out, expr2, sCxt);
		}

		if ( typeIRI2 != null ) {
			out.append( " TYPE " );
			out.append( NodeFmtLib.str(typeIRI2, pmap) );
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

		if ( typeIRI1 != null ) {
			out.append( " TYPE " );
			out.append( typeIRI1.toString() );
		}

		if ( expr2 != null ) {
			out.append(", ");
			WriterExpr.output(out, expr2, null);
		}

		if ( typeIRI2 != null ) {
			out.append( " TYPE " );
			out.append( typeIRI2.toString() );
		}

		out.decIndent();
		out.append(")");
		return out.asString();
	}

	protected class ListAccumulator implements Accumulator {
		final protected List<Node> list = new ArrayList<>();

		@Override
		public void accumulate( final Binding binding, final FunctionEnv functionEnv ) {
			final NodeValue nv = ExprLib.evalOrNull(expr1, binding, functionEnv);
			if ( nv != null ) {
				final Node n = nv.asNode();
				if ( typeIRI1 != null ) { // check the type of the node
					final String nTypeIRI;
					if ( n.isLiteral() )
						nTypeIRI = n.getLiteralDatatypeURI();
					else
						nTypeIRI = null;
					if ( ! typeIRI1.getURI().equals(nTypeIRI) )
						return; // ignore the node if it is not of the expected type
				}
				list.add(n);
			}
		}

		@Override
		public NodeValue getValue() {
			final StringBuilder sb = new StringBuilder();
			sb.append("[");
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
			sb.append("]");

			final RDFDatatype datatype;
			if ( typeIRI1 != null ) {
				sb.append("^^");
				sb.append( NodeFmtLib.strTTL(typeIRI1) );
				datatype = Node_LiteralWithList.datatypeTypedList;
			}
			else {
				datatype = Node_LiteralWithList.datatypeUntypedList;
			}

			final Node n = NodeFactory.createLiteral( sb.toString(), datatype );
			return NodeValue.makeNode(n);
		}
	}

	protected class MapAccumulator implements Accumulator {
		final protected List<Node> keys   = new ArrayList<>();
		final protected List<Node> values = new ArrayList<>();

		@Override
		public void accumulate( final Binding binding, final FunctionEnv functionEnv ) {
			final NodeValue nvKey   = ExprLib.evalOrNull(expr1, binding, functionEnv);
			final NodeValue nvValue = ExprLib.evalOrNull(expr2, binding, functionEnv);
			if ( nvKey != null && nvValue != null ) {
				final Node key = nvKey.asNode();
				final Node value = nvValue.asNode();

				if ( typeIRI1 != null ) { // check the type of the key
					final String nTypeIRI;
					if ( key.isLiteral() )
						nTypeIRI = key.getLiteralDatatypeURI();
					else
						nTypeIRI = null;
					if ( ! typeIRI1.getURI().equals(nTypeIRI) )
						return; // ignore the key-value pair if the key is not of the expected type
				}

				if ( typeIRI2 != null ) { // check the type of the value
					final String nTypeIRI;
					if ( value.isLiteral() )
						nTypeIRI = value.getLiteralDatatypeURI();
					else
						nTypeIRI = null;
					if ( ! typeIRI2.getURI().equals(nTypeIRI) )
						return; // ignore the key-value pair if the value is not of the expected type
				}

				keys.add(key);
				values.add(value);
			}
		}

		@Override
		public NodeValue getValue() {
			final StringBuilder sb = new StringBuilder();
			sb.append("{");
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
			sb.append("}");

			final RDFDatatype datatype;
			if ( typeIRI1 != null || typeIRI2 != null ) {
				sb.append("^^");
				if ( typeIRI1 != null ) {
					sb.append( NodeFmtLib.strTTL(typeIRI1) );
				}
				if ( typeIRI2 != null ) {
					sb.append("^^");
					sb.append( NodeFmtLib.strTTL(typeIRI2) );
				}
				datatype = Node_LiteralWithMap.datatypeTypedMap;
			}
			else {
				datatype = Node_LiteralWithMap.datatypeUntypedMap;
			}

			final Node n = NodeFactory.createLiteral( sb.toString(), datatype );
			return NodeValue.makeNode(n);
		}
	}

}
