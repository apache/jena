package com.hp.hpl.jena.ontology.tidy;

/**
 * @author jjc
 *
 */
interface CNodeI {
    int getCategories();
    void setCategories(int c);
    One asOne();
    Two asTwo();
    Blank asBlank();
}
