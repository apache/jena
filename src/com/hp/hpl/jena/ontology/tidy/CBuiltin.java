package com.hp.hpl.jena.ontology.tidy;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/**
 * @author jjc
 *
 */
class CBuiltin extends CNode {
    final int category;
    CBuiltin(Node n, EnhGraph eg, int i) {
        super(n, eg);
        category = i;
    }
    public int getCategories() {
        return category;
    }
    public boolean setCategories(int c, boolean rec) {
        if (c!=category)
          throw new SyntaxException("Internal error in syntax checker.");
        return true;
    }
    
}
