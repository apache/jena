package com.hp.hpl.jena.ontology.tidy;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/**
 * @author jjc
 *
 */
class CGeneral extends CNode {
// local cache
    int c = -1;
    CGeneral(Node n, EnhGraph eg) {
        super(n, eg);
    }
    /**
     * @see CNodeI#getCategories()
     */
    public int getCategories() {
    	if ( c == -1 ) {
    		c = getIntAttribute(Vocab.category,-1);
    	}
        return c;
    }
    /**
     * @see CNodeI#setCategories(int)
     */
    public boolean setCategories(int cat, boolean rec) {
    	int old = rec?getCategories():-1;
    	setIntAttribute(Vocab.category,cat);
    	c = cat;
    	if ( rec && old != cat ) {
    		return getChecker().recursivelyUpdate(asNode());
    	}
    	return true;
    	
    }
}
