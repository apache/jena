package com.hp.hpl.jena.ontology.tidy;

/** automatically generated. */
class Grammar {
	static final int lists[] = new int[]{};
	static final int restrictions[] = new int[]{};
	static final int descriptions[] = new int[]{};
	static final int disjointWithNodes[] = new int[]{};
    static final int CategoryShift = 8;
    // orphan must be first
	static final int orphan = 0;
    static final int annotationPropID = 1;
    static final int userID = 2;
    static final int annotationPropID_dataPropID = 3;
    static final int propertyOnly = 4;
    static final int annotationPropID_objectPropID_transitivePropID = 5;
    static final int classID = 6;
    static final int classID_datatypeID = 7;
    static final int classID_individualID = 8;
    static final int dataAnnotationPropID = 9;
    static final int dataPropID = 10;
    static final int dataPropID_objectPropID = 11;
    static final int dataPropID_objectPropID_transitivePropID = 12;
    static final int datatypeID = 13;
    static final int dlInteger = 14;
    static final int individualID = 15;
    static final int liteInteger = 16;
    static final int literal = 17;
    static final int objectPropID = 18;
    static final int objectPropID_transitivePropID = 19;
    static final int ontologyID = 20;
    static final int ontologyPropertyID = 21;
    static final int ontologyPropertyHack = 23;
    static final int orphan_unnamedIndividual = 24;
    static final int blank = 25;
    static final int orphan_allDifferent72 = 26;
    static final int orphan_description76_description77_description78_description79 = 27;
    static final int orphan_listOfDataLiteral83 = 28;
    static final int orphan_listOfDataLiteral83_listOfDescription82_listOfIndividualID84 = 29;
    static final int orphan_listOfDataLiteral83_listOfIndividualID84 = 30;
    static final int orphan_listOfDescription82 = 31;
    static final int orphan_listOfDescription82_listOfIndividualID84 = 32;
    static final int orphan_listOfIndividualID84 = 33;
    static final int orphan_restriction51_restriction52_restriction53_restriction54_restriction55_restriction56_restriction57_restriction58_restriction59_restriction60_restriction61_restriction62_restriction63_restriction64_restriction65 = 34;
    static final int orphan_unnamedDataRange80 = 35;
    static final int transitivePropID = 36;
    static final int unnamedIndividual = 37;
    static final int allDifferent72 = 38;
    static final int description76 = 39;
    static final int description76_description77_description78_description79 = 40;
    static final int description76_description77_description78_description79_restriction51_restriction52_restriction53_restriction54_restriction55_restriction56_restriction57_restriction58_restriction59_restriction60_restriction61_restriction62_restriction63_restriction64_restriction65 = 41;
    static final int description76_description77_description78_description79_restriction51_restriction52_restriction53_restriction54_restriction55_restriction56_restriction57_restriction58_restriction59_restriction60_restriction61_restriction62_restriction63_restriction64_restriction65_unnamedDataRange80 = 42;
    static final int description77 = 43;
    static final int description78 = 44;
    static final int description79 = 45;
    static final int description79_unnamedDataRange80 = 46;
    static final int listOfDataLiteral83 = 47;
    static final int listOfDataLiteral83_listOfDescription82_listOfIndividualID84 = 48;
    static final int listOfDataLiteral83_listOfIndividualID84 = 49;
    static final int listOfDescription82 = 50;
    static final int listOfDescription82_listOfIndividualID84 = 51;
    static final int listOfIndividualID84 = 52;
    static final int restriction51 = 53;
    static final int restriction51_restriction52_restriction53_restriction54_restriction55_restriction56 = 54;
    static final int restriction51_restriction52_restriction53_restriction54_restriction55_restriction56_restriction57_restriction58_restriction59_restriction60_restriction61_restriction62 = 55;
    static final int restriction51_restriction52_restriction53_restriction54_restriction55_restriction56_restriction57_restriction58_restriction59_restriction60_restriction61_restriction62_restriction63_restriction64_restriction65 = 56;
    static final int restriction51_restriction57 = 57;
    static final int restriction51_restriction57_restriction63 = 58;
    static final int restriction52 = 59;
    static final int restriction52_restriction58 = 60;
    static final int restriction52_restriction58_restriction64 = 61;
    static final int restriction53 = 62;
    static final int restriction53_restriction59 = 63;
    static final int restriction54 = 64;
    static final int restriction54_restriction60 = 65;
    static final int restriction55 = 66;
    static final int restriction55_restriction61 = 67;
    static final int restriction56 = 68;
    static final int restriction57 = 69;
    static final int restriction57_restriction58_restriction59_restriction60_restriction61_restriction62 = 70;
    static final int restriction57_restriction58_restriction59_restriction60_restriction61_restriction62_restriction63_restriction64_restriction65 = 71;
    static final int restriction57_restriction63 = 72;
    static final int restriction58 = 73;
    static final int restriction58_restriction64 = 74;
    static final int restriction59 = 75;
    static final int restriction60 = 76;
    static final int restriction61 = 77;
    static final int restriction62 = 78;
    static final int restriction62_restriction65 = 79;
    static final int restriction63 = 80;
    static final int restriction63_restriction64_restriction65 = 81;
    static final int restriction64 = 82;
    static final int restriction65 = 83;
    static final int unnamedDataRange80 = 84;
    static final int owlAllDifferent = 85;
    static final int owlAnnotationProperty = 86;
    static final int owlClass = 87;
    static final int owlDataRange = 88;
    static final int owlDatatypeProperty = 89;
    static final int owlDeprecatedClass = 90;
    static final int owlDeprecatedProperty = 91;
    static final int owlFunctionalProperty = 92;
    static final int owlInverseFunctionalProperty = 93;
    static final int owlObjectProperty = 94;
    static final int owlOntology = 95;
    static final int owlOntologyProperty = 96;
    static final int owlRestriction = 97;
    static final int owlSymmetricProperty = 98;
    static final int owlTransitiveProperty = 99;
    static final int owlallValuesFrom = 100;
    static final int owlcardinality = 101;
    static final int owlcomplementOf = 102;
    static final int owldifferentFrom = 103;
    static final int owldisjointWith = 104;
    static final int owldistinctMembers = 105;
    static final int owlequivalentClass = 106;
    static final int owlequivalentProperty = 107;
    static final int owlhasValue = 108;
    static final int owlintersectionOf = 109;
    static final int owlinverseOf = 110;
    static final int owlmaxCardinality = 111;
    static final int owlminCardinality = 112;
    static final int owlonProperty = 113;
    static final int owloneOf = 114;
    static final int owlsameIndividualAs = 115;
    static final int owlsomeValuesFrom = 116;
    static final int owlunionOf = 117;
    static final int rdfList = 118;
    static final int rdfProperty = 119;
    static final int rdffirst = 120;
    static final int rdfnil = 121;
    static final int rdfrest = 122;
    static final int rdftype = 123;
    static final int rdfsClass = 124;
    static final int rdfsDatatype = 125;
    static final int rdfsdomain = 126;
    static final int rdfsrange = 127;
    static final int rdfssubClassOf = 128;
    static final int rdfssubPropertyOf = 129;
    static final int notype = 140;
	static final int cyclic = 141;
	static final int cyclicRest = 142;
	static final int cyclicFirst = 143;
    static private final int W = CategoryShift;
    static final int NotQuiteBuiltin = 1<<W;
    static final int BadXSD = 2<<W;
    static final int BadOWL = 3<<W;
    static final int BadRDF = 5<<W;
    static final int DisallowedVocab = 4<<W;
    static final int Failure = -1;
    //  low bits are action
    static final int triples[] = new int[3000];
static int getBuiltinID(String uri) {
  if ( uri.startsWith("http://www.w3.org/") ) {
      uri = uri.substring(18);
      if (false) {}
   else if ( uri.startsWith("2002/07/owl#") ) {
       uri = uri.substring(12);
       if (false) {
       } else if ( uri.equals("Thing") ) {
          return classID;
       } else if ( uri.equals("Nothing") ) {
          return classID;
       } else if ( uri.equals("versionInfo") ) {
          return annotationPropID;
       } else if ( uri.equals("imports") ) {
          return ontologyPropertyHack;
       } else if ( uri.equals("priorVersion") ) {
          return ontologyPropertyHack;
       } else if ( uri.equals("backwardCompatibleWith") ) {
          return ontologyPropertyHack;
       } else if ( uri.equals("incompatibleWith") ) {
          return ontologyPropertyHack;
       } else if ( uri.equals("Ontology") ) {
          return owlOntology;
       } else if ( uri.equals("ObjectProperty") ) {
          return owlObjectProperty;
       } else if ( uri.equals("DataRange") ) {
          return owlDataRange;
       } else if ( uri.equals("DatatypeProperty") ) {
          return owlDatatypeProperty;
       } else if ( uri.equals("equivalentClass") ) {
          return owlequivalentClass;
       } else if ( uri.equals("sameIndividualAs") ) {
          return owlsameIndividualAs;
       } else if ( uri.equals("equivalentProperty") ) {
          return owlequivalentProperty;
       } else if ( uri.equals("Class") ) {
          return owlClass;
       } else if ( uri.equals("intersectionOf") ) {
          return owlintersectionOf;
       } else if ( uri.equals("unionOf") ) {
          return owlunionOf;
       } else if ( uri.equals("complementOf") ) {
          return owlcomplementOf;
       } else if ( uri.equals("Restriction") ) {
          return owlRestriction;
       } else if ( uri.equals("onProperty") ) {
          return owlonProperty;
       } else if ( uri.equals("allValuesFrom") ) {
          return owlallValuesFrom;
       } else if ( uri.equals("someValuesFrom") ) {
          return owlsomeValuesFrom;
       } else if ( uri.equals("cardinality") ) {
          return owlcardinality;
       } else if ( uri.equals("minCardinality") ) {
          return owlminCardinality;
       } else if ( uri.equals("maxCardinality") ) {
          return owlmaxCardinality;
       } else if ( uri.equals("hasValue") ) {
          return owlhasValue;
       } else if ( uri.equals("OntologyProperty") ) {
          return owlOntologyProperty;
       } else if ( uri.equals("AllDifferent") ) {
          return owlAllDifferent;
       } else if ( uri.equals("distinctMembers") ) {
          return owldistinctMembers;
       } else if ( uri.equals("AnnotationProperty") ) {
          return owlAnnotationProperty;
       } else if ( uri.equals("FunctionalProperty") ) {
          return owlFunctionalProperty;
       } else if ( uri.equals("InverseFunctionalProperty") ) {
          return owlInverseFunctionalProperty;
       } else if ( uri.equals("SymmetricProperty") ) {
          return owlSymmetricProperty;
       } else if ( uri.equals("TransitiveProperty") ) {
          return owlTransitiveProperty;
       } else if ( uri.equals("DeprecatedProperty") ) {
          return owlDeprecatedProperty;
       } else if ( uri.equals("DeprecatedClass") ) {
          return owlDeprecatedClass;
       } else if ( uri.equals("inverseOf") ) {
          return owlinverseOf;
       } else if ( uri.equals("oneOf") ) {
          return owloneOf;
       } else if ( uri.equals("differentFrom") ) {
          return owldifferentFrom;
       } else if ( uri.equals("disjointWith") ) {
          return owldisjointWith;
       } else if ( uri.equals("AllDifferent") ) {
          return owlAllDifferent;
       } else if ( uri.equals("distinctMembers") ) {
          return owldistinctMembers;
       } else if ( uri.equals("sameAs") ) {
           return DisallowedVocab;
       } else { return BadOWL; 
     }
   }
   else if ( uri.startsWith("1999/02/22-rdf-syntax-ns#") ) {
       uri = uri.substring(25);
       if (false) {
       } else if ( uri.equals("XMLLiteral") ) {
          return datatypeID;
       } else if ( uri.equals("Bag") ) {
          return classID| NotQuiteBuiltin;
       } else if ( uri.equals("Seq") ) {
          return classID| NotQuiteBuiltin;
       } else if ( uri.equals("Alt") ) {
          return classID| NotQuiteBuiltin;
       } else if ( uri.equals("Statement") ) {
          return classID| NotQuiteBuiltin;
       } else if ( uri.equals("subject") ) {
          return propertyOnly| NotQuiteBuiltin;
       } else if ( uri.equals("predicate") ) {
          return propertyOnly| NotQuiteBuiltin;
       } else if ( uri.equals("object") ) {
          return propertyOnly| NotQuiteBuiltin;
       } else if ( uri.equals("type") ) {
          return rdftype;
       } else if ( uri.equals("Property") ) {
          return rdfProperty;
       } else if ( uri.equals("nil") ) {
          return rdfnil;
       } else if ( uri.equals("first") ) {
          return rdffirst;
       } else if ( uri.equals("rest") ) {
          return rdfrest;
       } else if ( uri.equals("List") ) {
          return rdfList;
       } else { return BadRDF; 
     }
   }
   else if ( uri.startsWith("2001/XMLSchema#") ) {
       uri = uri.substring(15);
       if (false) {
       } else if ( uri.equals("string") ) {
          return datatypeID;
       } else if ( uri.equals("boolean") ) {
          return datatypeID;
       } else if ( uri.equals("decimal") ) {
          return datatypeID;
       } else if ( uri.equals("float") ) {
          return datatypeID;
       } else if ( uri.equals("double") ) {
          return datatypeID;
       } else if ( uri.equals("dateTime") ) {
          return datatypeID;
       } else if ( uri.equals("time") ) {
          return datatypeID;
       } else if ( uri.equals("date") ) {
          return datatypeID;
       } else if ( uri.equals("gYearMonth") ) {
          return datatypeID;
       } else if ( uri.equals("gYear") ) {
          return datatypeID;
       } else if ( uri.equals("gMonthDay") ) {
          return datatypeID;
       } else if ( uri.equals("gDay") ) {
          return datatypeID;
       } else if ( uri.equals("gMonth") ) {
          return datatypeID;
       } else if ( uri.equals("hexBinary") ) {
          return datatypeID;
       } else if ( uri.equals("base64Binary") ) {
          return datatypeID;
       } else if ( uri.equals("anyURI") ) {
          return datatypeID;
       } else if ( uri.equals("normalizedString") ) {
          return datatypeID;
       } else if ( uri.equals("token") ) {
          return datatypeID;
       } else if ( uri.equals("language") ) {
          return datatypeID;
       } else if ( uri.equals("NMTOKEN") ) {
          return datatypeID;
       } else if ( uri.equals("Name") ) {
          return datatypeID;
       } else if ( uri.equals("NCName") ) {
          return datatypeID;
       } else if ( uri.equals("integer") ) {
          return datatypeID;
       } else if ( uri.equals("nonPositiveInteger") ) {
          return datatypeID;
       } else if ( uri.equals("negativeInteger") ) {
          return datatypeID;
       } else if ( uri.equals("long") ) {
          return datatypeID;
       } else if ( uri.equals("int") ) {
          return datatypeID;
       } else if ( uri.equals("short") ) {
          return datatypeID;
       } else if ( uri.equals("byte") ) {
          return datatypeID;
       } else if ( uri.equals("nonNegativeInteger") ) {
          return datatypeID;
       } else if ( uri.equals("unsignedLong") ) {
          return datatypeID;
       } else if ( uri.equals("unsignedInt") ) {
          return datatypeID;
       } else if ( uri.equals("unsignedShort") ) {
          return datatypeID;
       } else if ( uri.equals("unsignedByte") ) {
          return datatypeID;
       } else if ( uri.equals("positiveInteger") ) {
          return datatypeID;
       } else if ( uri.equals("duration") ) {
          return datatypeID| BadXSD;
       } else if ( uri.equals("QName") ) {
          return datatypeID| BadXSD;
       } else if ( uri.equals("ENTITY") ) {
          return datatypeID| BadXSD;
       } else if ( uri.equals("ID") ) {
          return datatypeID| BadXSD;
       } else if ( uri.equals("IDREF") ) {
          return datatypeID| BadXSD;
       } else if ( uri.equals("ENTITIES") ) {
          return datatypeID| BadXSD;
       } else if ( uri.equals("IDREFS") ) {
          return datatypeID| BadXSD;
       } else if ( uri.equals("NOTATION") ) {
          return datatypeID| BadXSD;
       } else if ( uri.equals("NMTOKENS") ) {
          return datatypeID| BadXSD;
     }
   }
   else if ( uri.startsWith("2000/01/rdf-schema#") ) {
       uri = uri.substring(19);
       if (false) {
       } else if ( uri.equals("Literal") ) {
          return datatypeID;
       } else if ( uri.equals("comment") ) {
          return dataAnnotationPropID;
       } else if ( uri.equals("label") ) {
          return dataAnnotationPropID;
       } else if ( uri.equals("isDefinedBy") ) {
          return annotationPropID;
       } else if ( uri.equals("seeAlso") ) {
          return annotationPropID;
       } else if ( uri.equals("Datatype") ) {
          return rdfsDatatype;
       } else if ( uri.equals("subClassOf") ) {
          return rdfssubClassOf;
       } else if ( uri.equals("subPropertyOf") ) {
          return rdfssubPropertyOf;
       } else if ( uri.equals("domain") ) {
          return rdfsdomain;
       } else if ( uri.equals("range") ) {
          return rdfsrange;
       } else if ( uri.equals("Class") ) {
          return rdfsClass;
       } else if ( uri.equals("Resource") ) {
           return DisallowedVocab;
       } else if ( uri.equals("member") ) {
           return DisallowedVocab;
       } else if ( uri.equals("Container") ) {
           return DisallowedVocab;
       } else if ( uri.equals("ContainerMembershipProperty") ) {
           return DisallowedVocab;
       } else { return BadRDF; 
     }
   }
     }
     return Failure; 
}
    static final int FirstOfOne = 4;
    static final int FirstOfTwo = 8;
    static final int SecondOfTwo = 12;
    static final int ActionShift = 5;
    static final int ObjectAction = 2;
    static final int DL = 1;
}
