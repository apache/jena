package org.apache.jena.sparql.function.library.collection;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.LiteralLabelForList;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.NodeValue;

public class ReverseFct extends FunctionBase1List
{
	@Override
	protected NodeValue _exec( final List<CDTValue> list, final NodeValue nvList ) {
		if ( list.size() < 2 )
			return nvList;

		final CDTValue[] reverseArray = new CDTValue[ list.size() ];
		int i = list.size() - 1;
		for ( final CDTValue v : list ) {
			reverseArray[i] = v;
			i--;
		}

		final List<CDTValue> reverseList = Arrays.asList(reverseArray);
		final LiteralLabel lit = new LiteralLabelForList(reverseList);
		final Node n = NodeFactory.createLiteral(lit);
		return NodeValue.makeNode(n);
	}

}
