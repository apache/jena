package com.hp.hpl.jena.ontology.tidy;

/**
 * @author jjc
 *
 */
interface CNodeI {
    int getCategories();
    void setCategories(int c);
    void incrObjectCount(int i);

}
