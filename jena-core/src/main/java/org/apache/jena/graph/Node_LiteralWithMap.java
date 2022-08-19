package org.apache.jena.graph;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.impl.LiteralLabel;

public class Node_LiteralWithMap extends Node_Literal
{
	public static final String datatypeUriUntypedMap  = "http://www.w3.org/1999/02/22-rdf-syntax-ns#UntypedMap";
	public static final String datatypeUriTypedMap    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#TypedMap";
	public static final RDFDatatype datatypeUntypedMap  = new BaseDatatype(datatypeUriUntypedMap);
	public static final RDFDatatype datatypeTypedMap    = new BaseDatatype(datatypeUriTypedMap);

	public Node_LiteralWithMap( final LiteralLabel label ) {
		super(label);
	}

}
