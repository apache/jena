package org.apache.jena.sparql.function.library.collection;

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.function.FunctionRegistry;

public class CollectionLiteralFunctions {
	public static void register( final FunctionRegistry functionRegistry ) {
		functionRegistry.put( ARQConstants.ARQFunctionLibraryURI + "size",    SizeFct.class );
	}
}
