/* (c) Copyright 2003 Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Grammar.java,v 1.3 2003-12-03 10:54:21 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;
class Grammar implements Constants {
    static final int orphan = 1;
    static final int notype = 2;
    static final int cyclic = 3;
    static final int cyclicRest = 4;
    static final int cyclicFirst = 5;
    static final int badRestriction = 6;
    static final int annotationPropID = 7;
    static final int classID = 8;
    static final int dataAnnotationPropID = 9;
    static final int dataPropID = 10;
    static final int dataRangeID = 11;
    static final int datatypeID = 12;
    static final int dlInteger = 13;
    static final int individualID = 14;
    static final int liteInteger = 15;
    static final int literal = 16;
    static final int objectPropID = 17;
    static final int ontologyID = 18;
    static final int ontologyPropertyID = 19;
    static final int transitivePropID = 20;
    static final int unnamedIndividual = 21;
    static final int unnamedOntology = 22;
    static final int userTypedLiteral = 23;
    static final int allDifferent = 24;
    static final int description5disjointWith = 25;
    static final int description5equivalentClass = 26;
    static final int description5object = 27;
    static final int description5subClassOf = 28;
    static final int listOfDataLiteral = 29;
    static final int listOfDescription = 30;
    static final int listOfIndividualID = 31;
    static final int restriction6disjointWith = 32;
    static final int restriction6equivalentClass = 33;
    static final int restriction6object = 34;
    static final int restriction6subClassOf = 35;
    static final int restriction7disjointWith = 36;
    static final int restriction7equivalentClass = 37;
    static final int restriction7object = 38;
    static final int restriction7subClassOf = 39;
    static final int restriction8disjointWith = 40;
    static final int restriction8equivalentClass = 41;
    static final int restriction8object = 42;
    static final int restriction8subClassOf = 43;
    static final int unnamedDataRange = 44;
    static final int owlAllDifferent = 45;
    static final int owlAnnotationProperty = 46;
    static final int owlClass = 47;
    static final int owlDataRange = 48;
    static final int owlDatatypeProperty = 49;
    static final int owlDeprecatedClass = 50;
    static final int owlDeprecatedProperty = 51;
    static final int owlFunctionalProperty = 52;
    static final int owlInverseFunctionalProperty = 53;
    static final int owlObjectProperty = 54;
    static final int owlOntology = 55;
    static final int owlOntologyProperty = 56;
    static final int owlRestriction = 57;
    static final int owlSymmetricProperty = 58;
    static final int owlTransitiveProperty = 59;
    static final int owlcomplementOf = 60;
    static final int owldifferentFrom = 61;
    static final int owldisjointWith = 62;
    static final int owldistinctMembers = 63;
    static final int owlequivalentClass = 64;
    static final int owlequivalentProperty = 65;
    static final int owlhasValue = 66;
    static final int owlintersectionOf = 67;
    static final int owlinverseOf = 68;
    static final int owlmaxCardinality = 69;
    static final int owlonProperty = 70;
    static final int owloneOf = 71;
    static final int owlsameAs = 72;
    static final int owlsomeValuesFrom = 73;
    static final int owlunionOf = 74;
    static final int rdfList = 75;
    static final int rdfProperty = 76;
    static final int rdfXMLLiteral = 77;
    static final int rdffirst = 78;
    static final int rdfnil = 79;
    static final int rdfrest = 80;
    static final int rdftype = 81;
    static final int rdfsClass = 82;
    static final int rdfsContainer = 83;
    static final int rdfsContainerMembershipProperty = 84;
    static final int rdfsDatatype = 85;
    static final int rdfsResource = 86;
    static final int rdfsdomain = 87;
    static final int rdfsmember = 88;
    static final int rdfsrange = 89;
    static final int rdfssubClassOf = 90;
    static final int rdfssubPropertyOf = 91;
 static String catNames[] = { "--not used--",
      "orphan",
      "notype",
      "cyclic",
      "cyclicRest",
      "cyclicFirst",
      "badRestriction",
      "annotationPropID",
      "classID",
      "dataAnnotationPropID",
      "dataPropID",
      "dataRangeID",
      "datatypeID",
      "dlInteger",
      "individualID",
      "liteInteger",
      "literal",
      "objectPropID",
      "ontologyID",
      "ontologyPropertyID",
      "transitivePropID",
      "unnamedIndividual",
      "unnamedOntology",
      "userTypedLiteral",
      "allDifferent",
      "description5disjointWith",
      "description5equivalentClass",
      "description5object",
      "description5subClassOf",
      "listOfDataLiteral",
      "listOfDescription",
      "listOfIndividualID",
      "restriction6disjointWith",
      "restriction6equivalentClass",
      "restriction6object",
      "restriction6subClassOf",
      "restriction7disjointWith",
      "restriction7equivalentClass",
      "restriction7object",
      "restriction7subClassOf",
      "restriction8disjointWith",
      "restriction8equivalentClass",
      "restriction8object",
      "restriction8subClassOf",
      "unnamedDataRange",
      "owlAllDifferent",
      "owlAnnotationProperty",
      "owlClass",
      "owlDataRange",
      "owlDatatypeProperty",
      "owlDeprecatedClass",
      "owlDeprecatedProperty",
      "owlFunctionalProperty",
      "owlInverseFunctionalProperty",
      "owlObjectProperty",
      "owlOntology",
      "owlOntologyProperty",
      "owlRestriction",
      "owlSymmetricProperty",
      "owlTransitiveProperty",
      "owlcomplementOf",
      "owldifferentFrom",
      "owldisjointWith",
      "owldistinctMembers",
      "owlequivalentClass",
      "owlequivalentProperty",
      "owlhasValue",
      "owlintersectionOf",
      "owlinverseOf",
      "owlmaxCardinality",
      "owlonProperty",
      "owloneOf",
      "owlsameAs",
      "owlsomeValuesFrom",
      "owlunionOf",
      "rdfList",
      "rdfProperty",
      "rdfXMLLiteral",
      "rdffirst",
      "rdfnil",
      "rdfrest",
      "rdftype",
      "rdfsClass",
      "rdfsContainer",
      "rdfsContainerMembershipProperty",
      "rdfsDatatype",
      "rdfsResource",
      "rdfsdomain",
      "rdfsmember",
      "rdfsrange",
      "rdfssubClassOf",
      "rdfssubPropertyOf",
       };
static int getBuiltinID(String uri) {
  if ( uri.startsWith("http://www.w3.org/") ) {
      uri = uri.substring(18);
      if (false) {}
   else if ( uri.startsWith("2002/07/owl#") ) {
       uri = uri.substring(12);
       if (false) {
       } else if ( uri.equals("Thing") ) {
          return classID
;
       } else if ( uri.equals("Nothing") ) {
          return classID
;
       } else if ( uri.equals("versionInfo") ) {
          return annotationPropID
;
       } else if ( uri.equals("imports") ) {
          return ontologyPropertyID
;
       } else if ( uri.equals("priorVersion") ) {
          return ontologyPropertyID
;
       } else if ( uri.equals("backwardCompatibleWith") ) {
          return ontologyPropertyID
;
       } else if ( uri.equals("incompatibleWith") ) {
          return ontologyPropertyID
;
       } else if ( uri.equals("Ontology") ) {
          return owlOntology
;
       } else if ( uri.equals("ObjectProperty") ) {
          return owlObjectProperty
;
       } else if ( uri.equals("DataRange") ) {
          return owlDataRange
;
       } else if ( uri.equals("DatatypeProperty") ) {
          return owlDatatypeProperty
;
       } else if ( uri.equals("equivalentClass") ) {
          return owlequivalentClass
;
       } else if ( uri.equals("sameAs") ) {
          return owlsameAs
;
       } else if ( uri.equals("equivalentProperty") ) {
          return owlequivalentProperty
;
       } else if ( uri.equals("Class") ) {
          return owlClass
;
       } else if ( uri.equals("intersectionOf") ) {
          return owlintersectionOf
;
       } else if ( uri.equals("unionOf") ) {
          return owlunionOf
;
       } else if ( uri.equals("complementOf") ) {
          return owlcomplementOf
;
       } else if ( uri.equals("Restriction") ) {
          return owlRestriction
;
       } else if ( uri.equals("onProperty") ) {
          return owlonProperty
;
       } else if ( uri.equals("allValuesFrom") ) {
          return owlsomeValuesFrom
;
       } else if ( uri.equals("someValuesFrom") ) {
          return owlsomeValuesFrom
;
       } else if ( uri.equals("cardinality") ) {
          return owlmaxCardinality
;
       } else if ( uri.equals("minCardinality") ) {
          return owlmaxCardinality
;
       } else if ( uri.equals("maxCardinality") ) {
          return owlmaxCardinality
;
       } else if ( uri.equals("hasValue") ) {
          return owlhasValue
;
       } else if ( uri.equals("OntologyProperty") ) {
          return owlOntologyProperty
;
       } else if ( uri.equals("AllDifferent") ) {
          return owlAllDifferent
;
       } else if ( uri.equals("distinctMembers") ) {
          return owldistinctMembers
;
       } else if ( uri.equals("AnnotationProperty") ) {
          return owlAnnotationProperty
;
       } else if ( uri.equals("FunctionalProperty") ) {
          return owlFunctionalProperty
;
       } else if ( uri.equals("InverseFunctionalProperty") ) {
          return owlInverseFunctionalProperty
;
       } else if ( uri.equals("SymmetricProperty") ) {
          return owlSymmetricProperty
;
       } else if ( uri.equals("TransitiveProperty") ) {
          return owlTransitiveProperty
;
       } else if ( uri.equals("DeprecatedProperty") ) {
          return owlDeprecatedProperty
;
       } else if ( uri.equals("DeprecatedClass") ) {
          return owlDeprecatedClass
;
       } else if ( uri.equals("inverseOf") ) {
          return owlinverseOf
;
       } else if ( uri.equals("oneOf") ) {
          return owloneOf
;
       } else if ( uri.equals("differentFrom") ) {
          return owldifferentFrom
;
       } else if ( uri.equals("disjointWith") ) {
          return owldisjointWith
;
       } else if ( uri.equals("AllDifferent") ) {
          return owlAllDifferent
;
       } else if ( uri.equals("distinctMembers") ) {
          return owldistinctMembers
;
       } else { return BadOWL; 
     }
   }
   else if ( uri.startsWith("1999/02/22-rdf-syntax-ns#") ) {
       uri = uri.substring(25);
       if (false) {
       } else if ( uri.equals("XMLLiteral") ) {
          return datatypeID
;
       } else if ( uri.equals("Bag") ) {
          return classOnly
;
       } else if ( uri.equals("Seq") ) {
          return classOnly
;
       } else if ( uri.equals("Alt") ) {
          return classOnly
;
       } else if ( uri.equals("Statement") ) {
          return classOnly
;
       } else if ( uri.equals("subject") ) {
          return propertyOnly
;
       } else if ( uri.equals("predicate") ) {
          return propertyOnly
;
       } else if ( uri.equals("object") ) {
          return propertyOnly
;
       } else if ( uri.equals("type") ) {
          return rdftype
;
       } else if ( uri.equals("Property") ) {
          return rdfProperty
;
       } else if ( uri.equals("nil") ) {
          return rdfnil
;
       } else if ( uri.equals("first") ) {
          return rdffirst
;
       } else if ( uri.equals("rest") ) {
          return rdfrest
;
       } else if ( uri.equals("List") ) {
          return rdfList
;
       } else { return BadRDF; 
     }
   }
   else if ( uri.startsWith("2001/XMLSchema#") ) {
       uri = uri.substring(15);
       if (false) {
       } else if ( uri.equals("string") ) {
          return datatypeID
;
       } else if ( uri.equals("boolean") ) {
          return datatypeID
;
       } else if ( uri.equals("decimal") ) {
          return datatypeID
;
       } else if ( uri.equals("float") ) {
          return datatypeID
;
       } else if ( uri.equals("double") ) {
          return datatypeID
;
       } else if ( uri.equals("dateTime") ) {
          return datatypeID
;
       } else if ( uri.equals("time") ) {
          return datatypeID
;
       } else if ( uri.equals("date") ) {
          return datatypeID
;
       } else if ( uri.equals("gYearMonth") ) {
          return datatypeID
;
       } else if ( uri.equals("gYear") ) {
          return datatypeID
;
       } else if ( uri.equals("gMonthDay") ) {
          return datatypeID
;
       } else if ( uri.equals("gDay") ) {
          return datatypeID
;
       } else if ( uri.equals("gMonth") ) {
          return datatypeID
;
       } else if ( uri.equals("hexBinary") ) {
          return datatypeID
;
       } else if ( uri.equals("base64Binary") ) {
          return datatypeID
;
       } else if ( uri.equals("anyURI") ) {
          return datatypeID
;
       } else if ( uri.equals("normalizedString") ) {
          return datatypeID
;
       } else if ( uri.equals("token") ) {
          return datatypeID
;
       } else if ( uri.equals("language") ) {
          return datatypeID
;
       } else if ( uri.equals("NMTOKEN") ) {
          return datatypeID
;
       } else if ( uri.equals("Name") ) {
          return datatypeID
;
       } else if ( uri.equals("NCName") ) {
          return datatypeID
;
       } else if ( uri.equals("integer") ) {
          return datatypeID
;
       } else if ( uri.equals("nonPositiveInteger") ) {
          return datatypeID
;
       } else if ( uri.equals("negativeInteger") ) {
          return datatypeID
;
       } else if ( uri.equals("long") ) {
          return datatypeID
;
       } else if ( uri.equals("int") ) {
          return datatypeID
;
       } else if ( uri.equals("short") ) {
          return datatypeID
;
       } else if ( uri.equals("byte") ) {
          return datatypeID
;
       } else if ( uri.equals("nonNegativeInteger") ) {
          return datatypeID
;
       } else if ( uri.equals("unsignedLong") ) {
          return datatypeID
;
       } else if ( uri.equals("unsignedInt") ) {
          return datatypeID
;
       } else if ( uri.equals("unsignedShort") ) {
          return datatypeID
;
       } else if ( uri.equals("unsignedByte") ) {
          return datatypeID
;
       } else if ( uri.equals("positiveInteger") ) {
          return datatypeID
;
       } else if ( uri.equals("duration") ) {
   return BadXSD;
       } else if ( uri.equals("QName") ) {
   return BadXSD;
       } else if ( uri.equals("ENTITY") ) {
   return BadXSD;
       } else if ( uri.equals("ID") ) {
   return BadXSD;
       } else if ( uri.equals("IDREF") ) {
   return BadXSD;
       } else if ( uri.equals("ENTITIES") ) {
   return BadXSD;
       } else if ( uri.equals("IDREFS") ) {
   return BadXSD;
       } else if ( uri.equals("NOTATION") ) {
   return BadXSD;
       } else if ( uri.equals("NMTOKENS") ) {
   return BadXSD;
     }
   }
   else if ( uri.startsWith("2000/01/rdf-schema#") ) {
       uri = uri.substring(19);
       if (false) {
       } else if ( uri.equals("Literal") ) {
          return dataRangeID
;
       } else if ( uri.equals("comment") ) {
          return dataAnnotationPropID
;
       } else if ( uri.equals("label") ) {
          return dataAnnotationPropID
;
       } else if ( uri.equals("isDefinedBy") ) {
          return annotationPropID
;
       } else if ( uri.equals("seeAlso") ) {
          return annotationPropID
;
       } else if ( uri.equals("Datatype") ) {
          return rdfsDatatype
;
       } else if ( uri.equals("subClassOf") ) {
          return rdfssubClassOf
;
       } else if ( uri.equals("subPropertyOf") ) {
          return rdfssubPropertyOf
;
       } else if ( uri.equals("domain") ) {
          return rdfsdomain
;
       } else if ( uri.equals("range") ) {
          return rdfsrange
;
       } else if ( uri.equals("Class") ) {
          return rdfsClass
;
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
static final int userIDX[] = new int[]{
annotationPropID,
classID,
dataPropID,
datatypeID,
individualID,
notype,
objectPropID,
ontologyID,
ontologyPropertyID,
transitivePropID,
};
static final int propertyOnlyX[] = new int[]{
annotationPropID,
dataPropID,
notype,
objectPropID,
transitivePropID,
};
static final int classOnlyX[] = new int[]{
classID,
notype,
};
static final int blankX[] = new int[]{
badRestriction,
cyclic,
cyclicFirst,
cyclicRest,
notype,
orphan,
unnamedIndividual,
unnamedOntology,
allDifferent,
description5disjointWith,
description5equivalentClass,
description5object,
description5subClassOf,
listOfDataLiteral,
listOfDescription,
listOfIndividualID,
restriction6disjointWith,
restriction6equivalentClass,
restriction6object,
restriction6subClassOf,
restriction7disjointWith,
restriction7equivalentClass,
restriction7object,
restriction7subClassOf,
restriction8disjointWith,
restriction8equivalentClass,
restriction8object,
restriction8subClassOf,
unnamedDataRange,
};
static final int restrictionsX[] = new int[]{
restriction6disjointWith,
restriction6equivalentClass,
restriction6object,
restriction6subClassOf,
restriction7disjointWith,
restriction7equivalentClass,
restriction7object,
restriction7subClassOf,
restriction8disjointWith,
restriction8equivalentClass,
restriction8object,
restriction8subClassOf,
};
static final int descriptionsX[] = new int[]{
description5disjointWith,
description5equivalentClass,
description5object,
description5subClassOf,
};
static final int listsX[] = new int[]{
listOfDataLiteral,
listOfDescription,
listOfIndividualID,
};
static final int disjointWithX[] = new int[]{
description5disjointWith,
restriction6disjointWith,
restriction7disjointWith,
restriction8disjointWith,
};
static final int MAX_SINGLETON_SET = 92 + 1;
static {
for (int i=0; i<92; i++) {
if ( i != CategorySet.find(new int[]{i},true) )
      System.err.println("initialization problem");
}
};
static final int userID = CategorySet.find( userIDX,false);
static final int propertyOnly = CategorySet.find( propertyOnlyX,false);
static final int classOnly = CategorySet.find( classOnlyX,false);
static final int blank = CategorySet.find( blankX,false);
static final int restrictions = CategorySet.find( restrictionsX,false);
static final int descriptions = CategorySet.find( descriptionsX,false);
static final int lists = CategorySet.find( listsX,false);
static final int disjointWith = CategorySet.find( disjointWithX,false);
 static boolean isPseudoCategory(int x) {
     switch ( x ) {
      case orphan:
      case notype:
      case cyclic:
      case cyclicRest:
      case cyclicFirst:
      case badRestriction:
        return true;
      default:
        return false;
     }
  }
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
