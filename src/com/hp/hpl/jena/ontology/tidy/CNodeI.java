package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.graph.*;
/**
 * @author jjc
 *
 */
interface CNodeI {
	Node asNode();
    int getCategories();
    void setCategories(int c);
    One asOne();
    Two asTwo();
    Blank asBlank();
}
