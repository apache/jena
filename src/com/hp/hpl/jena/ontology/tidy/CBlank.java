package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;

/**
 * @author jjc
 *
 */
class CBlank extends OneTwoImpl implements Blank {
	public void addObjectTriple(Triple t) {
		check(2, t);
	}
	final static public Implementation factory = new Implementation() {
	public EnhNode wrap(Node n, EnhGraph eg) {
					return new CBlank(n, eg);
	}
    public boolean canWrap( Node n, EnhGraph eg )
        { return true; }
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
