package com.hp.hpl.jena.ontology.tidy;

/**
 * @author jjc
 *
 */
class Grammar {
  static int getBuiltinID(String uri) {
    return -1;
  }
  static final int Literal = 2;
  static final int LiteInteger = 3;
  static final int NonNegativeInteger = 4;
  static final int EmptyCategorySet = 0;
  static final int Failure = -1;
  // How many bits do we need for a category
  static final int NBits = 8;
  static int addTriple(int k) {
    return k;
  }
  static final int rdftype = 33;
  static final int rdfList = 34;
}
