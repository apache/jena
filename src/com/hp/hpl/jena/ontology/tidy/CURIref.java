package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;

/**
 * @author jjc
 *
 */
class CURIref extends CGeneral {

	/**
	 * Constructor for CURIref.
	 * @param n
	 * @param eg
	 */
	public CURIref(Node n, EnhGraph eg) {
		this(n,eg,Grammar.userID);
	}
	public CURIref(Node n, EnhGraph eg, int c) {
		super(n, eg);
		if ( getCategories() == -1 )
			  setCategories(c,false);
	}

}
