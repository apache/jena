package org.apache.jena.graph;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.impl.LiteralLabel;

public class Node_LiteralWithList extends Node_Literal
{
	public static final String datatypeUriUntypedList = "http://www.w3.org/1999/02/22-rdf-syntax-ns#UntypedList";
	public static final String datatypeUriTypedList   = "http://www.w3.org/1999/02/22-rdf-syntax-ns#TypedList";
	public static final RDFDatatype datatypeUntypedList = new BaseDatatype(datatypeUriUntypedList);
	public static final RDFDatatype datatypeTypedList   = new BaseDatatype(datatypeUriTypedList);

	public Node_LiteralWithList( final LiteralLabel label ) {
		super(label);
	}
	

}
