package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.graph.*;

/**
 * The Vocabulary for OWL tidy's internal data
 * structures.
 * @author jjc
 *
 */
class Vocab {
    static final String NameSpace = "http://jena.hpl.hp.com/schemas/tidy#";
    static final Node category = Node.createURI(NameSpace+"category");
    static final Node objectOfTriple = Node.createURI(NameSpace+"objectOfTriple");
    
}
