package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.graph.*;
/**
 * @author jjc
 *
 */
interface CNodeI {
	Node asNode();
    int getCategories();
    /**
     * @param recursive If c is not the current value then recursive processing is performed.
     * @param c
     */
    boolean setCategories(int c, boolean recursive);
    One asOne();
    Two asTwo();
    Blank asBlank();
}
