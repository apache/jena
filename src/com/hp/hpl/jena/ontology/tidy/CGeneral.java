package com.hp.hpl.jena.ontology.tidy;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/**
 * @author jjc
 *
 */
class CGeneral extends CNode {

    CGeneral(Node n, EnhGraph eg) {
        super(n, eg);
    }
    /**
     * @see CNodeI#getCategories()
     */
    public int getCategories() {
        return 0;
    }
    /**
     * @see CNodeI#setCategories(int)
     */
    public void setCategories(int c) {
    }
}
