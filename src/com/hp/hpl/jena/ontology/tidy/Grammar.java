/* (c) Copyright 2003 Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Grammar.java,v 1.11 2003-04-15 19:48:18 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;
class Grammar {
    static final int orphan = 1;
    static final int notype = 2;
    static final int cyclic = 3;
    static final int cyclicRest = 4;
    static final int cyclicFirst = 5;
    static final int annotationPropID = 6;
    static final int classID = 7;
    static final int dataAnnotationPropID = 8;
    static final int dataPropID = 9;
    static final int datatypeID = 10;
    static final int dlInteger = 11;
    static final int individualID = 12;
    static final int liteInteger = 13;
    static final int literal = 14;
    static final int objectPropID = 15;
    static final int ontologyID = 16;
    static final int ontologyPropertyID = 17;
    static final int transitivePropID = 18;
    static final int unnamedIndividual = 19;
    static final int unnamedOntology = 20;
    static final int allDifferent = 21;
    static final int description77disjointWith = 22;
    static final int description77equivalentClass = 23;
    static final int description77object = 24;
    static final int description77subClassOf = 25;
    static final int description78disjointWith = 26;
    static final int description78equivalentClass = 27;
    static final int description78object = 28;
    static final int description78subClassOf = 29;
    static final int description79disjointWith = 30;
    static final int description79equivalentClass = 31;
    static final int description79object = 32;
    static final int description79subClassOf = 33;
    static final int description80disjointWith = 34;
    static final int description80equivalentClass = 35;
    static final int description80object = 36;
    static final int description80subClassOf = 37;
    static final int listOfDataLiteral = 38;
    static final int listOfDescription = 39;
    static final int listOfIndividualID = 40;
    static final int restriction52disjointWith = 41;
    static final int restriction52equivalentClass = 42;
    static final int restriction52object = 43;
    static final int restriction52subClassOf = 44;
    static final int restriction53disjointWith = 45;
    static final int restriction53equivalentClass = 46;
    static final int restriction53object = 47;
    static final int restriction53subClassOf = 48;
    static final int restriction54disjointWith = 49;
    static final int restriction54equivalentClass = 50;
    static final int restriction54object = 51;
    static final int restriction54subClassOf = 52;
    static final int restriction55disjointWith = 53;
    static final int restriction55equivalentClass = 54;
    static final int restriction55object = 55;
    static final int restriction55subClassOf = 56;
    static final int restriction56disjointWith = 57;
    static final int restriction56equivalentClass = 58;
    static final int restriction56object = 59;
    static final int restriction56subClassOf = 60;
    static final int restriction57disjointWith = 61;
    static final int restriction57equivalentClass = 62;
    static final int restriction57object = 63;
    static final int restriction57subClassOf = 64;
    static final int restriction58disjointWith = 65;
    static final int restriction58equivalentClass = 66;
    static final int restriction58object = 67;
    static final int restriction58subClassOf = 68;
    static final int restriction59disjointWith = 69;
    static final int restriction59equivalentClass = 70;
    static final int restriction59object = 71;
    static final int restriction59subClassOf = 72;
    static final int restriction60disjointWith = 73;
    static final int restriction60equivalentClass = 74;
    static final int restriction60object = 75;
    static final int restriction60subClassOf = 76;
    static final int restriction61disjointWith = 77;
    static final int restriction61equivalentClass = 78;
    static final int restriction61object = 79;
    static final int restriction61subClassOf = 80;
    static final int restriction62disjointWith = 81;
    static final int restriction62equivalentClass = 82;
    static final int restriction62object = 83;
    static final int restriction62subClassOf = 84;
    static final int restriction63disjointWith = 85;
    static final int restriction63equivalentClass = 86;
    static final int restriction63object = 87;
    static final int restriction63subClassOf = 88;
    static final int restriction64disjointWith = 89;
    static final int restriction64equivalentClass = 90;
    static final int restriction64object = 91;
    static final int restriction64subClassOf = 92;
    static final int restriction65disjointWith = 93;
    static final int restriction65equivalentClass = 94;
    static final int restriction65object = 95;
    static final int restriction65subClassOf = 96;
    static final int restriction66disjointWith = 97;
    static final int restriction66equivalentClass = 98;
    static final int restriction66object = 99;
    static final int restriction66subClassOf = 100;
    static final int unnamedDataRange = 101;
    static final int owlAllDifferent = 102;
    static final int owlAnnotationProperty = 103;
    static final int owlClass = 104;
    static final int owlDataRange = 105;
    static final int owlDatatypeProperty = 106;
    static final int owlDeprecatedClass = 107;
    static final int owlDeprecatedProperty = 108;
    static final int owlFunctionalProperty = 109;
    static final int owlInverseFunctionalProperty = 110;
    static final int owlObjectProperty = 111;
    static final int owlOntology = 112;
    static final int owlOntologyProperty = 113;
    static final int owlRestriction = 114;
    static final int owlSymmetricProperty = 115;
    static final int owlTransitiveProperty = 116;
    static final int owlallValuesFrom = 117;
    static final int owlcardinality = 118;
    static final int owlcomplementOf = 119;
    static final int owldifferentFrom = 120;
    static final int owldisjointWith = 121;
    static final int owldistinctMembers = 122;
    static final int owlequivalentClass = 123;
    static final int owlequivalentProperty = 124;
    static final int owlhasValue = 125;
    static final int owlintersectionOf = 126;
    static final int owlinverseOf = 127;
    static final int owlmaxCardinality = 128;
    static final int owlminCardinality = 129;
    static final int owlonProperty = 130;
    static final int owloneOf = 131;
    static final int owlsameIndividualAs = 132;
    static final int owlsomeValuesFrom = 133;
    static final int owlunionOf = 134;
    static final int rdfList = 135;
    static final int rdfProperty = 136;
    static final int rdffirst = 137;
    static final int rdfnil = 138;
    static final int rdfrest = 139;
    static final int rdftype = 140;
    static final int rdfsClass = 141;
    static final int rdfsDatatype = 142;
    static final int rdfsdomain = 143;
    static final int rdfsrange = 144;
    static final int rdfssubClassOf = 145;
    static final int rdfssubPropertyOf = 146;
    static final int FirstOfOne = 4;
    static final int FirstOfTwo = 8;
    static final int SecondOfTwo = 12;
    static final int ObjectAction = 2;
    static final int DL = 1;
    static final int ActionShift = 4;
    static final int CategoryShift = 9;
    static private final int W = CategoryShift;
    static final int BadXSD = 1<<W;
    static final int BadOWL = 2<<W;
    static final int BadRDF = 3<<W;
    static final int DisallowedVocab = 4<<W;
    static final int Failure = -1;
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
          return classOnly;
       } else if ( uri.equals("Seq") ) {
          return classOnly;
       } else if ( uri.equals("Alt") ) {
          return classOnly;
       } else if ( uri.equals("Statement") ) {
          return classOnly;
       } else if ( uri.equals("subject") ) {
          return propertyOnly;
       } else if ( uri.equals("predicate") ) {
          return propertyOnly;
       } else if ( uri.equals("object") ) {
          return propertyOnly;
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
static {
for (int i=0; i<=47; i++) {
if ( i != CategorySet.find(new int[]{i},true) )
      System.err.println("initialization problem");
}
};
static final int triples[] = new int[]{};
static final int userIDX[] = new int[]{
annotationPropID,
classID,
dataPropID,
datatypeID,
individualID,
notype,
objectPropID,
ontologyID,
transitivePropID,
};
static final int userID = CategorySet.find( userIDX,false);
static final int propertyOnlyX[] = new int[]{
annotationPropID,
dataPropID,
notype,
objectPropID,
transitivePropID,
};
static final int propertyOnly = CategorySet.find( propertyOnlyX,false);
static final int classOnlyX[] = new int[]{
classID,
notype,
};
static final int classOnly = CategorySet.find( classOnlyX,false);
static final int ontologyPropertyHackX[] = new int[]{
ontologyPropertyID,
orphan,
};
static final int ontologyPropertyHack = CategorySet.find( ontologyPropertyHackX,false);
static final int blankX[] = new int[]{
cyclic,
cyclicFirst,
cyclicRest,
notype,
orphan,
unnamedIndividual,
unnamedOntology,
allDifferent,
description77disjointWith,
description77equivalentClass,
description77object,
description77subClassOf,
description78disjointWith,
description78equivalentClass,
description78object,
description78subClassOf,
description79disjointWith,
description79equivalentClass,
description79object,
description79subClassOf,
description80disjointWith,
description80equivalentClass,
description80object,
description80subClassOf,
listOfDataLiteral,
listOfDescription,
listOfIndividualID,
restriction52disjointWith,
restriction52equivalentClass,
restriction52object,
restriction52subClassOf,
restriction53disjointWith,
restriction53equivalentClass,
restriction53object,
restriction53subClassOf,
restriction54disjointWith,
restriction54equivalentClass,
restriction54object,
restriction54subClassOf,
restriction55disjointWith,
restriction55equivalentClass,
restriction55object,
restriction55subClassOf,
restriction56disjointWith,
restriction56equivalentClass,
restriction56object,
restriction56subClassOf,
restriction57disjointWith,
restriction57equivalentClass,
restriction57object,
restriction57subClassOf,
restriction58disjointWith,
restriction58equivalentClass,
restriction58object,
restriction58subClassOf,
restriction59disjointWith,
restriction59equivalentClass,
restriction59object,
restriction59subClassOf,
restriction60disjointWith,
restriction60equivalentClass,
restriction60object,
restriction60subClassOf,
restriction61disjointWith,
restriction61equivalentClass,
restriction61object,
restriction61subClassOf,
restriction62disjointWith,
restriction62equivalentClass,
restriction62object,
restriction62subClassOf,
restriction63disjointWith,
restriction63equivalentClass,
restriction63object,
restriction63subClassOf,
restriction64disjointWith,
restriction64equivalentClass,
restriction64object,
restriction64subClassOf,
restriction65disjointWith,
restriction65equivalentClass,
restriction65object,
restriction65subClassOf,
restriction66disjointWith,
restriction66equivalentClass,
restriction66object,
restriction66subClassOf,
unnamedDataRange,
};
static final int blank = CategorySet.find( blankX,false);
static final int restrictionsX[] = new int[]{
restriction52disjointWith,
restriction52equivalentClass,
restriction52object,
restriction52subClassOf,
restriction53disjointWith,
restriction53equivalentClass,
restriction53object,
restriction53subClassOf,
restriction54disjointWith,
restriction54equivalentClass,
restriction54object,
restriction54subClassOf,
restriction55disjointWith,
restriction55equivalentClass,
restriction55object,
restriction55subClassOf,
restriction56disjointWith,
restriction56equivalentClass,
restriction56object,
restriction56subClassOf,
restriction57disjointWith,
restriction57equivalentClass,
restriction57object,
restriction57subClassOf,
restriction58disjointWith,
restriction58equivalentClass,
restriction58object,
restriction58subClassOf,
restriction59disjointWith,
restriction59equivalentClass,
restriction59object,
restriction59subClassOf,
restriction60disjointWith,
restriction60equivalentClass,
restriction60object,
restriction60subClassOf,
restriction61disjointWith,
restriction61equivalentClass,
restriction61object,
restriction61subClassOf,
restriction62disjointWith,
restriction62equivalentClass,
restriction62object,
restriction62subClassOf,
restriction63disjointWith,
restriction63equivalentClass,
restriction63object,
restriction63subClassOf,
restriction64disjointWith,
restriction64equivalentClass,
restriction64object,
restriction64subClassOf,
restriction65disjointWith,
restriction65equivalentClass,
restriction65object,
restriction65subClassOf,
restriction66disjointWith,
restriction66equivalentClass,
restriction66object,
restriction66subClassOf,
};
static final int restrictions = CategorySet.find( restrictionsX,false);
static final int descriptionsX[] = new int[]{
description77disjointWith,
description77equivalentClass,
description77object,
description77subClassOf,
description78disjointWith,
description78equivalentClass,
description78object,
description78subClassOf,
description79disjointWith,
description79equivalentClass,
description79object,
description79subClassOf,
description80disjointWith,
description80equivalentClass,
description80object,
description80subClassOf,
};
static final int descriptions = CategorySet.find( descriptionsX,false);
static final int listsX[] = new int[]{
listOfDataLiteral,
listOfDescription,
listOfIndividualID,
};
static final int lists = CategorySet.find( listsX,false);
static final int disjointWithX[] = new int[]{
description77disjointWith,
description78disjointWith,
description79disjointWith,
description80disjointWith,
restriction52disjointWith,
restriction53disjointWith,
restriction54disjointWith,
restriction55disjointWith,
restriction56disjointWith,
restriction57disjointWith,
restriction58disjointWith,
restriction59disjointWith,
restriction60disjointWith,
restriction61disjointWith,
restriction62disjointWith,
restriction63disjointWith,
restriction64disjointWith,
restriction65disjointWith,
restriction66disjointWith,
};
static final int disjointWith = CategorySet.find( disjointWithX,false);
}
 /*
	(c) Copyright Hewlett-Packard Company 2003
	All rights reserved.
 
	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:
 
	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.
 
	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.
 
	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.
 
	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/  
