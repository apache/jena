package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * The Vocabulary for OWL tidy's internal data
 * structures.
 * @author jjc
 *
 */
class Vocab {
    static final String NameSpace = "http://jena.hpl.hp.com/schemas/tidy#";
    static final Node category = Node.createURI(NameSpace+"category");
    static final Node objectCount = Node.createURI(NameSpace+"objectCount");
    static final Node rdftype = RDF.type.asNode();
    static final Node Disjoint = Node.createURI(NameSpace+"Disjoint");
    static final Node Equivalent = Node.createURI(NameSpace+"Equivalent");
}
