package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.Node;

/**
 * @author jjc
 *
 */
class CBlank extends OneTwoImpl implements Blank {
	final static public Implementation factory = new Implementation() {
	public EnhNode wrap(Node n, EnhGraph eg) {
					return new CBlank(n, eg);
	}
	};
	
	/**
	 * Constructor for CBlank.
	 * @param n
	 * @param eg
	 */
	public CBlank(Node n, EnhGraph eg) {
		super(n, eg);
		if ( getCategories() == -1 )
		      setCategories(Grammar.blank,false);
	}
	

}
