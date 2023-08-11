package org.apache.jena.sparql.function.library.cdt;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.cdt.CDTValue;
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
		return CDTLiteralFunctionUtils.createNodeValue(reverseList);
	}

}
