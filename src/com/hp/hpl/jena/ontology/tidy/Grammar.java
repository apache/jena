package com.hp.hpl.jena.ontology.tidy;

/** automatically generated. */
class Grammar {
	static final int NotQuiteBuiltin = 1;
	static final int BadXSD = 2;
	static final int Failure = -1;
	static int getBuiltinID(String uri) {
		if (uri.startsWith("http://www.w3.org/")) {
			uri = uri.substring(18);
			if (false) {
			} else if (uri.startsWith("2002/07/owl#")) {
				uri = uri.substring(12);
				if (false) {
				} else if (uri.equals("Thing")) {
					return classID;
				} else if (uri.equals("Nothing")) {
					return classID;
				} else if (uri.equals("versionInfo")) {
					return annotationPropID;
				} else if (uri.equals("imports")) {
					return ontologyPropertyID;
				} else if (uri.equals("priorVersion")) {
					return ontologyPropertyID;
				} else if (uri.equals("backwardCompatibleWith")) {
					return ontologyPropertyID;
				} else if (uri.equals("incompatibleWith")) {
					return ontologyPropertyID;
				} else if (uri.equals("Ontology")) {
					return owlOntology;
				} else if (uri.equals("ObjectProperty")) {
					return owlObjectProperty;
				} else if (uri.equals("DataRange")) {
					return owlDataRange;
				} else if (uri.equals("DatatypeProperty")) {
					return owlDatatypeProperty;
				} else if (uri.equals("equivalentClass")) {
					return owlequivalentClass;
				} else if (uri.equals("sameIndividualAs")) {
					return owlsameIndividualAs;
				} else if (uri.equals("equivalentProperty")) {
					return owlequivalentProperty;
				} else if (uri.equals("Class")) {
					return owlClass;
				} else if (uri.equals("intersectionOf")) {
					return owlintersectionOf;
				} else if (uri.equals("unionOf")) {
					return owlunionOf;
				} else if (uri.equals("complementOf")) {
					return owlcomplementOf;
				} else if (uri.equals("Restriction")) {
					return owlRestriction;
				} else if (uri.equals("onProperty")) {
					return owlonProperty;
				} else if (uri.equals("allValuesFrom")) {
					return owlallValuesFrom;
				} else if (uri.equals("someValuesFrom")) {
					return owlsomeValuesFrom;
				} else if (uri.equals("cardinality")) {
					return owlcardinality;
				} else if (uri.equals("minCardinality")) {
					return owlminCardinality;
				} else if (uri.equals("maxCardinality")) {
					return owlmaxCardinality;
				} else if (uri.equals("hasValue")) {
					return owlhasValue;
				} else if (uri.equals("OntologyProperty")) {
					return owlOntologyProperty;
				} else if (uri.equals("AllDifferent")) {
					return owlAllDifferent;
				} else if (uri.equals("distinctMembers")) {
					return owldistinctMembers;
				} else if (uri.equals("AnnotationProperty")) {
					return owlAnnotationProperty;
				} else if (uri.equals("FunctionalProperty")) {
					return owlFunctionalProperty;
				} else if (uri.equals("InverseFunctionalProperty")) {
					return owlInverseFunctionalProperty;
				} else if (uri.equals("SymmetricProperty")) {
					return owlSymmetricProperty;
				} else if (uri.equals("TransitiveProperty")) {
					return owlTransitiveProperty;
				} else if (uri.equals("DeprecatedProperty")) {
					return owlDeprecatedProperty;
				} else if (uri.equals("DeprecatedClass")) {
					return owlDeprecatedClass;
				} else if (uri.equals("inverseOf")) {
					return owlinverseOf;
				} else if (uri.equals("oneOf")) {
					return owloneOf;
				} else if (uri.equals("differentFrom")) {
					return owldifferentFrom;
				} else if (uri.equals("disjointWith")) {
					return owldisjointWith;
				} else if (uri.equals("AllDifferent")) {
					return owlAllDifferent;
				} else if (uri.equals("distinctMembers")) {
					return owldistinctMembers;
				}
			} else if (uri.startsWith("1999/02/22-rdf-syntax-ns#")) {
				uri = uri.substring(25);
				if (false) {
				} else if (uri.equals("XMLLiteral")) {
					return datatypeID;
				} else if (uri.equals("Bag")) {
					return classID | NotQuiteBuiltin;
				} else if (uri.equals("Seq")) {
					return classID | NotQuiteBuiltin;
				} else if (uri.equals("Alt")) {
					return classID | NotQuiteBuiltin;
				} else if (uri.equals("Statement")) {
					return classID | NotQuiteBuiltin;
				} else if (uri.equals("subject")) {
					return propertyOnly | NotQuiteBuiltin;
				} else if (uri.equals("predicate")) {
					return propertyOnly | NotQuiteBuiltin;
				} else if (uri.equals("object")) {
					return propertyOnly | NotQuiteBuiltin;
				} else if (uri.equals("type")) {
					return rdftype;
				} else if (uri.equals("Property")) {
					return rdfProperty;
				} else if (uri.equals("nil")) {
					return rdfnil;
				} else if (uri.equals("first")) {
					return rdffirst;
				} else if (uri.equals("rest")) {
					return rdfrest;
				} else if (uri.equals("List")) {
					return rdfList;
				}
			} else if (uri.startsWith("2001/XMLSchema#")) {
				uri = uri.substring(15);
				if (false) {
				} else if (uri.equals("string")) {
					return datatypeID;
				} else if (uri.equals("boolean")) {
					return datatypeID;
				} else if (uri.equals("decimal")) {
					return datatypeID;
				} else if (uri.equals("float")) {
					return datatypeID;
				} else if (uri.equals("double")) {
					return datatypeID;
				} else if (uri.equals("dateTime")) {
					return datatypeID;
				} else if (uri.equals("time")) {
					return datatypeID;
				} else if (uri.equals("date")) {
					return datatypeID;
				} else if (uri.equals("gYearMonth")) {
					return datatypeID;
				} else if (uri.equals("gYear")) {
					return datatypeID;
				} else if (uri.equals("gMonthDay")) {
					return datatypeID;
				} else if (uri.equals("gDay")) {
					return datatypeID;
				} else if (uri.equals("gMonth")) {
					return datatypeID;
				} else if (uri.equals("hexBinary")) {
					return datatypeID;
				} else if (uri.equals("base64Binary")) {
					return datatypeID;
				} else if (uri.equals("anyURI")) {
					return datatypeID;
				} else if (uri.equals("normalizedString")) {
					return datatypeID;
				} else if (uri.equals("token")) {
					return datatypeID;
				} else if (uri.equals("language")) {
					return datatypeID;
				} else if (uri.equals("NMTOKEN")) {
					return datatypeID;
				} else if (uri.equals("Name")) {
					return datatypeID;
				} else if (uri.equals("NCName")) {
					return datatypeID;
				} else if (uri.equals("integer")) {
					return datatypeID;
				} else if (uri.equals("nonPositiveInteger")) {
					return datatypeID;
				} else if (uri.equals("negativeInteger")) {
					return datatypeID;
				} else if (uri.equals("long")) {
					return datatypeID;
				} else if (uri.equals("int")) {
					return datatypeID;
				} else if (uri.equals("short")) {
					return datatypeID;
				} else if (uri.equals("byte")) {
					return datatypeID;
				} else if (uri.equals("nonNegativeInteger")) {
					return datatypeID;
				} else if (uri.equals("unsignedLong")) {
					return datatypeID;
				} else if (uri.equals("unsignedInt")) {
					return datatypeID;
				} else if (uri.equals("unsignedShort")) {
					return datatypeID;
				} else if (uri.equals("unsignedByte")) {
					return datatypeID;
				} else if (uri.equals("positiveInteger")) {
					return datatypeID;
				} else if (uri.equals("duration")) {
					return datatypeID | BadXSD;
				} else if (uri.equals("QName")) {
					return datatypeID | BadXSD;
				} else if (uri.equals("ENTITY")) {
					return datatypeID | BadXSD;
				} else if (uri.equals("ID")) {
					return datatypeID | BadXSD;
				} else if (uri.equals("IDREF")) {
					return datatypeID | BadXSD;
				} else if (uri.equals("ENTITIES")) {
					return datatypeID | BadXSD;
				} else if (uri.equals("IDREFS")) {
					return datatypeID | BadXSD;
				} else if (uri.equals("NOTATION")) {
					return datatypeID | BadXSD;
				} else if (uri.equals("NMTOKENS")) {
					return datatypeID | BadXSD;
				}
			} else if (uri.startsWith("2000/01/rdf-schema#")) {
				uri = uri.substring(19);
				if (false) {
				} else if (uri.equals("Literal")) {
					return datatypeID;
				} else if (uri.equals("comment")) {
					return dataAnnotationPropID;
				} else if (uri.equals("label")) {
					return dataAnnotationPropID;
				} else if (uri.equals("isDefinedBy")) {
					return annotationPropID;
				} else if (uri.equals("seeAlso")) {
					return annotationPropID;
				} else if (uri.equals("Datatype")) {
					return rdfsDatatype;
				} else if (uri.equals("subClassOf")) {
					return rdfssubClassOf;
				} else if (uri.equals("subPropertyOf")) {
					return rdfssubPropertyOf;
				} else if (uri.equals("domain")) {
					return rdfsdomain;
				} else if (uri.equals("range")) {
					return rdfsrange;
				} else if (uri.equals("Class")) {
					return rdfsClass;
				}
			}
		}
		return Failure;
	}
	static final int ActionShift = 3;
	static final int CategoryShift = 7;
	static final int allDifferent = 1;
	static final int blank = 2;
	static final int annotationPropID = 3;
	static final int userID = 4;
	static final int annotationPropID_classID_dataPropID_datatypeID_individualID_objectPropID_ontologyID_transitivePropID =
		5;
	static final int annotationPropID_dataPropID = 6;
	static final int propertyOnly = 7;
	static final int annotationPropID_objectPropID_transitivePropID = 8;
	static final int classID = 9;
	static final int classID_datatypeID = 10;
	static final int dataAnnotationPropID = 11;
	static final int dataPropID = 12;
	static final int dataPropID_objectPropID = 13;
	static final int dataPropID_objectPropID_transitivePropID = 14;
	static final int datatypeID = 15;
	static final int description = 16;
	static final int description_restriction = 17;
	static final int description_restriction_unnamedDataRange = 18;
	static final int description_unnamedDataRange = 19;
	static final int dlInteger = 20;
	static final int individualID = 21;
	static final int listOfDataLiteral = 22;
	static final int listOfDataLiteral_listOfDescription = 23;
	static final int listOfDescription = 24;
	static final int listOfIndividualID = 25;
	static final int liteInteger = 26;
	static final int literal = 27;
	static final int objectPropID = 28;
	static final int objectPropID_transitivePropID = 29;
	static final int ontologyID = 30;
	static final int ontologyPropertyID = 31;
	static final int restriction = 32;
	static final int transitivePropID = 33;
	static final int unnamedDataRange = 34;
	static final int unnamedIndividual = 35;
	static final int owlAllDifferent = 36;
	static final int owlAnnotationProperty = 37;
	static final int owlClass = 38;
	static final int owlDataRange = 39;
	static final int owlDatatypeProperty = 40;
	static final int owlDeprecatedClass = 41;
	static final int owlDeprecatedProperty = 42;
	static final int owlFunctionalProperty = 43;
	static final int owlInverseFunctionalProperty = 44;
	static final int owlObjectProperty = 45;
	static final int owlOntology = 46;
	static final int owlOntologyProperty = 47;
	static final int owlRestriction = 48;
	static final int owlSymmetricProperty = 49;
	static final int owlTransitiveProperty = 50;
	static final int owlallValuesFrom = 51;
	static final int owlcardinality = 52;
	static final int owlcomplementOf = 53;
	static final int owldifferentFrom = 54;
	static final int owldisjointWith = 55;
	static final int owldistinctMembers = 56;
	static final int owlequivalentClass = 57;
	static final int owlequivalentProperty = 58;
	static final int owlhasValue = 59;
	static final int owlintersectionOf = 60;
	static final int owlinverseOf = 61;
	static final int owlmaxCardinality = 62;
	static final int owlminCardinality = 63;
	static final int owlonProperty = 64;
	static final int owloneOf = 65;
	static final int owlsameIndividualAs = 66;
	static final int owlsomeValuesFrom = 67;
	static final int owlunionOf = 68;
	static final int rdfList = 69;
	static final int rdfProperty = 70;
	static final int rdffirst = 71;
	static final int rdfnil = 72;
	static final int rdfrest = 73;
	static final int rdftype = 74;
	static final int rdfsClass = 75;
	static final int rdfsDatatype = 76;
	static final int rdfsdomain = 77;
	static final int rdfsrange = 78;
	static final int rdfssubClassOf = 79;
	static final int rdfssubPropertyOf = 80;
	static final int DL = 1 << (3 * CategoryShift);
	static private final int W = CategoryShift;
	/** Given some knowledge about the categorization
	of a triple, return a refinement of that knowledge,
	or {@link #Failure} if no refinement exists.
	@param triple Shows the prior categorization of subject,
	predicate and object in the triple.
	@return Shows the possible legal matching categorizations of subject,
	predicate and object in the triple.
	*/
	static int addTriple(int triple) {
		switch (triple) {
			case 66052 :
				return 82949;
			case 66055 :
				return 82311;
			case 66050 :
				return 82979;
			case 66051 :
				return 82307;
			case 66057 :
				return 82313;
			case 66060 :
				return 82316;
			case 66063 :
				return 82319;
			case 66068 :
				return 82708;
			case 66069 :
				return 82965;
			case 66074 :
				return 82714;
			case 66075 :
				return 82715;
			case 66076 :
				return 82332;
			case 66078 :
				return 82334;
			case 66079 :
				return 82335;
			case 66081 :
				return 82337;
			case 66083 :
				return 82979;
			case 66436 :
				return 82949;
			case 66439 :
				return 82311;
			case 66434 :
				return 82979;
			case 66435 :
				return 82307;
			case 66441 :
				return 82313;
			case 66444 :
				return 82316;
			case 66447 :
				return 82319;
			case 66452 :
				return 82708;
			case 66453 :
				return 82965;
			case 66458 :
				return 82714;
			case 66459 :
				return 82715;
			case 66460 :
				return 82332;
			case 66462 :
				return 82334;
			case 66463 :
				return 82335;
			case 66465 :
				return 82337;
			case 66467 :
				return 82979;
			case 66437 :
				return 82949;
			case 66440 :
				return 82312;
			case 66438 :
				return 82310;
			case 65924 :
				return 82309;
			case 65927 :
				return 82311;
			case 65922 :
				return 82339;
			case 65923 :
				return 82307;
			case 65929 :
				return 82313;
			case 65932 :
				return 82316;
			case 65935 :
				return 82319;
			case 65940 :
				return 82324;
			case 65941 :
				return 82325;
			case 65946 :
				return 82330;
			case 65947 :
				return 82331;
			case 65948 :
				return 82332;
			case 65950 :
				return 82334;
			case 65951 :
				return 82335;
			case 65953 :
				return 82337;
			case 65955 :
				return 82339;
			case 65925 :
				return 82309;
			case 65928 :
				return 82312;
			case 65926 :
				return 82310;
			case 66964 :
				return 83348;
			case 66970 :
				return 83354;
			case 66971 :
				return 83355;
			case 67092 :
				return 345620;
			case 67098 :
				return 345626;
			case 67099 :
				return 345627;
			case 69124 :
				return 347669;
			case 69122 :
				return 347683;
			case 69141 :
				return 347669;
			case 69155 :
				return 347683;
			case 69125 :
				return 347669;
			case 69508 :
				return 495518;
			case 69534 :
				return 495518;
			case 69509 :
				return 495518;
			case 69764 :
				return 348309;
			case 69762 :
				return 348323;
			case 69781 :
				return 348309;
			case 69795 :
				return 348323;
			case 69765 :
				return 348309;
			case 72324 :
				return 154249 | DL;
			case 72322 :
				return 154257 | DL;
			case 72329 :
				return 154249 | DL;
			case 72336 :
				return 154256 | DL;
			case 72352 :
				return 154272 | DL;
			case 72325 :
				return 154249 | DL;
			case 72452 :
				return 350997;
			case 72469 :
				return 350997;
			case 72453 :
				return 350997;
			case 72580 :
				return 154505 | DL;
			case 72578 :
				return 154513 | DL;
			case 72585 :
				return 154505 | DL;
			case 72592 :
				return 154512 | DL;
			case 72608 :
				return 154528 | DL;
			case 72581 :
				return 154505 | DL;
			case 72593 :
				return 154513 | DL;
			case 72836 :
				return 154761;
			case 72834 :
				return 154769;
			case 72841 :
				return 154761;
			case 72848 :
				return 154768 | DL;
			case 72864 :
				return 154784;
			case 72837 :
				return 154761;
			case 72849 :
				return 154769;
			case 72964 :
				return 236814;
			case 72967 :
				return 236814;
			case 72972 :
				return 204044;
			case 72988 :
				return 466204;
			case 72993 :
				return 548129;
			case 72965 :
				return 236814;
			case 72968 :
				return 482589;
			case 72966 :
				return 204044;
			case 73218 :
				return 155160;
			case 73240 :
				return 155160;
			case 73288 :
				return 155208;
			case 73348 :
				return 482973;
			case 73351 :
				return 482973;
			case 73372 :
				return 466588;
			case 73377 :
				return 548513;
			case 73349 :
				return 482973;
			case 73352 :
				return 482973;
			case 73358 :
				return 482973;
			case 73373 :
				return 482973;
			case 73860 :
				return 155801 | DL;
			case 73881 :
				return 155801 | DL;
			case 73928 :
				return 155848 | DL;
			case 73988 :
				return 352533;
			case 74005 :
				return 352533;
			case 73989 :
				return 352533;
			case 74242 :
				return 156184 | DL;
			case 74264 :
				return 156184 | DL;
			case 74312 :
				return 156232 | DL;
			case 74628 :
				return 418709;
			case 74645 :
				return 418709;
			case 74629 :
				return 418709;
			case 74884 :
				return 418969;
			case 74905 :
				return 418969;
			case 74952 :
				return 419016;
			case 75012 :
				return 353545;
			case 75010 :
				return 353553;
			case 75017 :
				return 353545;
			case 75024 :
				return 353552 | DL;
			case 75040 :
				return 353568;
			case 75045 :
				return 58661;
			case 75046 :
				return 156966;
			case 75048 :
				return 206120;
			case 75049 :
				return 156969;
			case 75050 :
				return 238890;
			case 75051 :
				return 222507;
			case 75052 :
				return 468268;
			case 75053 :
				return 484653;
			case 75054 :
				return 501038;
			case 75057 :
				return 484657;
			case 75058 :
				return 550194;
			case 75077 :
				return 419141;
			case 75078 :
				return 124230;
			case 75083 :
				return 157003;
			case 75084 :
				return 255308;
			case 75013 :
				return 353545;
			case 75025 :
				return 353553;
			case 75396 :
				return 239241;
			case 75394 :
				return 239249 | DL;
			case 75401 :
				return 239241;
			case 75408 :
				return 239248 | DL;
			case 75424 :
				return 239264 | DL;
			case 75397 :
				return 239241;
			case 75409 :
				return 239249 | DL;
			case 75524 :
				return 239370;
			case 75522 :
				return 239378 | DL;
			case 75529 :
				return 485129;
			case 75535 :
				return 206607;
			case 75536 :
				return 485136 | DL;
			case 75552 :
				return 485152 | DL;
			case 75554 :
				return 206626 | DL;
			case 75525 :
				return 239370;
			case 75537 :
				return 485137 | DL;
			case 75652 :
				return 157577;
			case 75650 :
				return 157585;
			case 75657 :
				return 157577;
			case 75664 :
				return 157584 | DL;
			case 75680 :
				return 157600;
			case 75653 :
				return 157577;
			case 75665 :
				return 157585;
			case 75658 :
				return 157577;
			case 75666 :
				return 157585;
			case 75780 :
				return 239630;
			case 75783 :
				return 239630;
			case 75788 :
				return 206860;
			case 75804 :
				return 469020;
			case 75809 :
				return 485409;
			case 75781 :
				return 239630;
			case 75784 :
				return 485405;
			case 75782 :
				return 206860;
			case 75790 :
				return 239630;
			case 75805 :
				return 485405;
			case 75789 :
				return 223245;
			case 115204 :
				return 115077;
			case 115207 :
				return 115079;
			case 115202 :
				return 115107;
			case 115203 :
				return 115075;
			case 115209 :
				return 115081;
			case 115212 :
				return 115084;
			case 115215 :
				return 115087;
			case 115220 :
				return 115092;
			case 115221 :
				return 115093;
			case 115226 :
				return 115098;
			case 115227 :
				return 115099;
			case 115228 :
				return 115100;
			case 115230 :
				return 115102;
			case 115231 :
				return 115103;
			case 115233 :
				return 115105;
			case 115235 :
				return 115107;
			case 115205 :
				return 115077;
			case 115208 :
				return 115080;
			case 115206 :
				return 115078;
			case 115214 :
				return 115086;
			case 115229 :
				return 115101;
			case 115213 :
				return 115085;
			case 115210 :
				return 115082;
			case 115588 :
				return 115077;
			case 115591 :
				return 115079;
			case 115586 :
				return 115107;
			case 115587 :
				return 115075;
			case 115593 :
				return 115081;
			case 115596 :
				return 115084;
			case 115599 :
				return 115087;
			case 115604 :
				return 115092;
			case 115605 :
				return 115093;
			case 115610 :
				return 115098;
			case 115611 :
				return 115099;
			case 115612 :
				return 115100;
			case 115614 :
				return 115102;
			case 115615 :
				return 115103;
			case 115617 :
				return 115105;
			case 115619 :
				return 115107;
			case 115589 :
				return 115077;
			case 115592 :
				return 115080;
			case 115590 :
				return 115078;
			case 115598 :
				return 115086;
			case 115613 :
				return 115101;
			case 115597 :
				return 115085;
			case 115594 :
				return 115082;
			case 115076 :
				return 115077;
			case 115079 :
				return 115079;
			case 115074 :
				return 115107;
			case 115075 :
				return 115075;
			case 115081 :
				return 115081;
			case 115084 :
				return 115084;
			case 115087 :
				return 115087;
			case 115092 :
				return 115092;
			case 115093 :
				return 115093;
			case 115098 :
				return 115098;
			case 115099 :
				return 115099;
			case 115100 :
				return 115100;
			case 115102 :
				return 115102;
			case 115103 :
				return 115103;
			case 115105 :
				return 115105;
			case 115107 :
				return 115107;
			case 115077 :
				return 115077;
			case 115080 :
				return 115080;
			case 115078 :
				return 115078;
			case 115086 :
				return 115086;
			case 115101 :
				return 115101;
			case 115085 :
				return 115085;
			case 115082 :
				return 115082;
			case 116116 :
				return 116116;
			case 116122 :
				return 116122;
			case 116123 :
				return 116123;
			case 122116 :
				return 236814;
			case 122119 :
				return 236814;
			case 122124 :
				return 204044;
			case 122140 :
				return 466204;
			case 122145 :
				return 548129;
			case 122117 :
				return 236814;
			case 122120 :
				return 482589;
			case 122118 :
				return 204044;
			case 122126 :
				return 236814;
			case 122141 :
				return 482589;
			case 122125 :
				return 220429;
			case 122500 :
				return 482973;
			case 122503 :
				return 482973;
			case 122524 :
				return 466588;
			case 122529 :
				return 548513;
			case 122501 :
				return 482973;
			case 122504 :
				return 482973;
			case 122510 :
				return 482973;
			case 122525 :
				return 482973;
			case 122509 :
				return 466588;
			case 124197 :
				return 58661;
			case 124200 :
				return 206120;
			case 124202 :
				return 238890;
			case 124203 :
				return 222507;
			case 124204 :
				return 468268;
			case 124205 :
				return 484653;
			case 124209 :
				return 484657;
			case 124210 :
				return 550194;
			case 124230 :
				return 124230;
			case 124548 :
				return 239241;
			case 124546 :
				return 239249 | DL;
			case 124553 :
				return 239241;
			case 124560 :
				return 239248 | DL;
			case 124576 :
				return 239264 | DL;
			case 124549 :
				return 239241;
			case 124561 :
				return 239249 | DL;
			case 124554 :
				return 239241;
			case 124562 :
				return 239249 | DL;
			case 124676 :
				return 239370;
			case 124674 :
				return 239378 | DL;
			case 124681 :
				return 485129;
			case 124687 :
				return 206607;
			case 124688 :
				return 485136 | DL;
			case 124704 :
				return 485152 | DL;
			case 124706 :
				return 206626 | DL;
			case 124677 :
				return 239370;
			case 124689 :
				return 485137 | DL;
			case 124682 :
				return 239370;
			case 124690 :
				return 239378 | DL;
			case 124932 :
				return 239630;
			case 124935 :
				return 239630;
			case 124940 :
				return 206860;
			case 124956 :
				return 469020;
			case 124961 :
				return 485409;
			case 124933 :
				return 239630;
			case 124936 :
				return 485405;
			case 124934 :
				return 206860;
			case 124942 :
				return 239630;
			case 124957 :
				return 485405;
			case 124941 :
				return 223245;
			case 115332 :
				return 115077;
			case 115335 :
				return 115079;
			case 115330 :
				return 115107;
			case 115331 :
				return 115075;
			case 115337 :
				return 115081;
			case 115340 :
				return 115084;
			case 115343 :
				return 115087;
			case 115348 :
				return 115092;
			case 115349 :
				return 115093;
			case 115354 :
				return 115098;
			case 115355 :
				return 115099;
			case 115356 :
				return 115100;
			case 115358 :
				return 115102;
			case 115359 :
				return 115103;
			case 115361 :
				return 115105;
			case 115363 :
				return 115107;
			case 115333 :
				return 115077;
			case 115336 :
				return 115080;
			case 115334 :
				return 115078;
			case 115342 :
				return 115086;
			case 115357 :
				return 115101;
			case 115341 :
				return 115085;
			case 115338 :
				return 115082;
			case 115716 :
				return 115077;
			case 115719 :
				return 115079;
			case 115714 :
				return 115107;
			case 115715 :
				return 115075;
			case 115721 :
				return 115081;
			case 115724 :
				return 115084;
			case 115727 :
				return 115087;
			case 115732 :
				return 115092;
			case 115733 :
				return 115093;
			case 115738 :
				return 115098;
			case 115739 :
				return 115099;
			case 115740 :
				return 115100;
			case 115742 :
				return 115102;
			case 115743 :
				return 115103;
			case 115745 :
				return 115105;
			case 115747 :
				return 115107;
			case 115717 :
				return 115077;
			case 115720 :
				return 115080;
			case 115718 :
				return 115078;
			case 115726 :
				return 115086;
			case 115741 :
				return 115101;
			case 115725 :
				return 115085;
			case 115722 :
				return 115082;
			case 115460 :
				return 115077;
			case 115463 :
				return 115079;
			case 115458 :
				return 115107;
			case 115459 :
				return 115075;
			case 115465 :
				return 115081;
			case 115468 :
				return 115084;
			case 115471 :
				return 115087;
			case 115476 :
				return 115092;
			case 115477 :
				return 115093;
			case 115482 :
				return 115098;
			case 115483 :
				return 115099;
			case 115484 :
				return 115100;
			case 115486 :
				return 115102;
			case 115487 :
				return 115103;
			case 115489 :
				return 115105;
			case 115491 :
				return 115107;
			case 115461 :
				return 115077;
			case 115464 :
				return 115080;
			case 115462 :
				return 115078;
			case 115470 :
				return 115086;
			case 115485 :
				return 115101;
			case 115469 :
				return 115085;
			case 115466 :
				return 115082;
			case 33284 :
				return 574469;
			case 33287 :
				return 573831;
			case 33282 :
				return 574499;
			case 33283 :
				return 573827;
			case 33289 :
				return 573833;
			case 33292 :
				return 573836;
			case 33295 :
				return 573839;
			case 33300 :
				return 574228;
			case 33301 :
				return 574485;
			case 33306 :
				return 574234;
			case 33307 :
				return 574235;
			case 33308 :
				return 573852;
			case 33310 :
				return 573854;
			case 33311 :
				return 573855;
			case 33313 :
				return 573857;
			case 33315 :
				return 574499;
			case 33285 :
				return 574469;
			case 33288 :
				return 573832;
			case 33286 :
				return 573830;
			case 33294 :
				return 573838;
			case 33309 :
				return 573853;
			case 33293 :
				return 573837;
			case 33290 :
				return 573834;
			case 33668 :
				return 574469;
			case 33671 :
				return 573831;
			case 33666 :
				return 574499;
			case 33667 :
				return 573827;
			case 33673 :
				return 573833;
			case 33676 :
				return 573836;
			case 33679 :
				return 573839;
			case 33684 :
				return 574228;
			case 33685 :
				return 574485;
			case 33690 :
				return 574234;
			case 33691 :
				return 574235;
			case 33692 :
				return 573852;
			case 33694 :
				return 573854;
			case 33695 :
				return 573855;
			case 33697 :
				return 573857;
			case 33699 :
				return 574499;
			case 33669 :
				return 574469;
			case 33672 :
				return 573832;
			case 33670 :
				return 573830;
			case 33678 :
				return 573838;
			case 33693 :
				return 573853;
			case 33677 :
				return 573837;
			case 33674 :
				return 573834;
			case 33156 :
				return 573829;
			case 33159 :
				return 573831;
			case 33154 :
				return 573859;
			case 33155 :
				return 573827;
			case 33161 :
				return 573833;
			case 33164 :
				return 573836;
			case 33167 :
				return 573839;
			case 33172 :
				return 573844;
			case 33173 :
				return 573845;
			case 33178 :
				return 573850;
			case 33179 :
				return 573851;
			case 33180 :
				return 573852;
			case 33182 :
				return 573854;
			case 33183 :
				return 573855;
			case 33185 :
				return 573857;
			case 33187 :
				return 573859;
			case 33157 :
				return 573829;
			case 33160 :
				return 573832;
			case 33158 :
				return 573830;
			case 33166 :
				return 573838;
			case 33181 :
				return 573853;
			case 33165 :
				return 573837;
			case 33162 :
				return 573834;
			case 34196 :
				return 574868;
			case 34202 :
				return 574874;
			case 34203 :
				return 574875;
			case 34324 :
				return 574996;
			case 34330 :
				return 575002;
			case 34331 :
				return 575003;
			case 36356 :
				return 577045;
			case 36354 :
				return 577059;
			case 36373 :
				return 577045;
			case 36387 :
				return 577059;
			case 36357 :
				return 577045;
			case 36996 :
				return 577685;
			case 36994 :
				return 577699;
			case 37013 :
				return 577685;
			case 37027 :
				return 577699;
			case 36997 :
				return 577685;
			case 39300 :
				return 530826;
			case 39298 :
				return 530834 | DL;
			case 39305 :
				return 530825;
			case 39311 :
				return 530831;
			case 39312 :
				return 530832 | DL;
			case 39328 :
				return 530848 | DL;
			case 39330 :
				return 530850 | DL;
			case 39301 :
				return 530826;
			case 39313 :
				return 530833 | DL;
			case 39306 :
				return 530826;
			case 39314 :
				return 530834 | DL;
			case 39444 :
				return 530964 | DL;
			case 39450 :
				return 530970;
			case 39556 :
				return 268937 | DL;
			case 39554 :
				return 268945 | DL;
			case 39561 :
				return 268937 | DL;
			case 39568 :
				return 268944 | DL;
			case 39584 :
				return 268960 | DL;
			case 39557 :
				return 268937 | DL;
			case 39569 :
				return 268945 | DL;
			case 39562 :
				return 268937 | DL;
			case 39570 :
				return 268945 | DL;
			case 39812 :
				return 285577 | DL;
			case 39810 :
				return 285585 | DL;
			case 39817 :
				return 285577 | DL;
			case 39824 :
				return 285584 | DL;
			case 39840 :
				return 285600 | DL;
			case 39813 :
				return 285577 | DL;
			case 39825 :
				return 285585 | DL;
			case 39818 :
				return 285577 | DL;
			case 39826 :
				return 285585 | DL;
			case 39940 :
				return 23577;
			case 39961 :
				return 23577;
			case 40008 :
				return 23624;
			case 40068 :
				return 285833 | DL;
			case 40066 :
				return 285841 | DL;
			case 40073 :
				return 285833 | DL;
			case 40080 :
				return 285840 | DL;
			case 40096 :
				return 285856 | DL;
			case 40069 :
				return 285833 | DL;
			case 40081 :
				return 285841 | DL;
			case 40074 :
				return 285833 | DL;
			case 40082 :
				return 285841 | DL;
			case 40324 :
				return 531861 | DL;
			case 40340 :
				return 531860 | DL;
			case 40341 :
				return 531861 | DL;
			case 40346 :
				return 531866 | DL;
			case 40347 :
				return 531867 | DL;
			case 40325 :
				return 531861 | DL;
			case 40450 :
				return 269848 | DL;
			case 40472 :
				return 269848 | DL;
			case 40520 :
				return 269896 | DL;
			case 40724 :
				return 532244 | DL;
			case 40730 :
				return 532250;
			case 40852 :
				return 532372 | DL;
			case 40858 :
				return 532378;
			case 40964 :
				return 532494;
			case 40967 :
				return 532494;
			case 40972 :
				return 532492;
			case 40988 :
				return 532508;
			case 40993 :
				return 532513;
			case 40965 :
				return 532494;
			case 40968 :
				return 532509;
			case 40966 :
				return 532492;
			case 40974 :
				return 532494;
			case 40989 :
				return 532509;
			case 40973 :
				return 532493;
			case 41092 :
				return 270489 | DL;
			case 41090 :
				return 565398 | DL;
			case 41110 :
				return 565398 | DL;
			case 41113 :
				return 270489 | DL;
			case 41160 :
				return 319688 | DL;
			case 41348 :
				return 532874;
			case 41346 :
				return 532882 | DL;
			case 41353 :
				return 532873;
			case 41359 :
				return 532879;
			case 41360 :
				return 532880 | DL;
			case 41376 :
				return 532896 | DL;
			case 41378 :
				return 532898 | DL;
			case 41349 :
				return 532874;
			case 41361 :
				return 532881 | DL;
			case 41354 :
				return 532874;
			case 41362 :
				return 532882 | DL;
			case 41363 :
				return 532883 | DL;
			case 41474 :
				return 270872 | DL;
			case 41496 :
				return 270872 | DL;
			case 41544 :
				return 270920 | DL;
			case 41860 :
				return 402313;
			case 41858 :
				return 402321;
			case 41865 :
				return 402313;
			case 41872 :
				return 402320 | DL;
			case 41876 :
				return 369556 | DL;
			case 41882 :
				return 369562 | DL;
			case 41883 :
				return 369563 | DL;
			case 41888 :
				return 402336;
			case 41861 :
				return 402313;
			case 41873 :
				return 402321;
			case 41866 :
				return 402313;
			case 41874 :
				return 402321;
			case 41875 :
				return 402320 | DL;
			case 42114 :
				return 386199;
			case 42134 :
				return 369814 | DL;
			case 42136 :
				return 402584;
			case 42184 :
				return 386248;
			case 42244 :
				return 582921;
			case 42242 :
				return 582929;
			case 42249 :
				return 582921;
			case 42256 :
				return 582928 | DL;
			case 42272 :
				return 582944;
			case 42276 :
				return 25892;
			case 42278 :
				return 271654 | DL;
			case 42279 :
				return 566567 | DL;
			case 42288 :
				return 533808;
			case 42309 :
				return 386373;
			case 42315 :
				return 304459;
			case 42245 :
				return 582921;
			case 42257 :
				return 582929;
			case 42250 :
				return 582921;
			case 42258 :
				return 582929;
			case 42259 :
				return 582928 | DL;
			case 42884 :
				return 288649 | DL;
			case 42882 :
				return 288657 | DL;
			case 42889 :
				return 288649 | DL;
			case 42896 :
				return 288656 | DL;
			case 42912 :
				return 288672 | DL;
			case 42885 :
				return 288649 | DL;
			case 42897 :
				return 288657 | DL;
			case 42890 :
				return 288649 | DL;
			case 42898 :
				return 288657 | DL;
			case 42899 :
				return 288656 | DL;
			case 33412 :
				return 574469;
			case 33415 :
				return 573831;
			case 33410 :
				return 574499;
			case 33411 :
				return 573827;
			case 33417 :
				return 573833;
			case 33420 :
				return 573836;
			case 33423 :
				return 573839;
			case 33428 :
				return 574228;
			case 33429 :
				return 574485;
			case 33434 :
				return 574234;
			case 33435 :
				return 574235;
			case 33436 :
				return 573852;
			case 33438 :
				return 573854;
			case 33439 :
				return 573855;
			case 33441 :
				return 573857;
			case 33443 :
				return 574499;
			case 33413 :
				return 574469;
			case 33416 :
				return 573832;
			case 33414 :
				return 573830;
			case 33422 :
				return 573838;
			case 33437 :
				return 573853;
			case 33421 :
				return 573837;
			case 33418 :
				return 573834;
			case 33796 :
				return 574469;
			case 33799 :
				return 573831;
			case 33794 :
				return 574499;
			case 33795 :
				return 573827;
			case 33801 :
				return 573833;
			case 33804 :
				return 573836;
			case 33807 :
				return 573839;
			case 33812 :
				return 573844;
			case 33813 :
				return 574485;
			case 33818 :
				return 573850;
			case 33819 :
				return 573851;
			case 33820 :
				return 573852;
			case 33822 :
				return 573854;
			case 33823 :
				return 573855;
			case 33825 :
				return 573857;
			case 33827 :
				return 574499;
			case 33797 :
				return 574469;
			case 33800 :
				return 573832;
			case 33798 :
				return 573830;
			case 33806 :
				return 573838;
			case 33821 :
				return 573853;
			case 33805 :
				return 573837;
			case 33802 :
				return 573834;
			case 33540 :
				return 573829;
			case 33543 :
				return 573831;
			case 33538 :
				return 573859;
			case 33539 :
				return 573827;
			case 33545 :
				return 573833;
			case 33548 :
				return 573836;
			case 33551 :
				return 573839;
			case 33556 :
				return 574228;
			case 33557 :
				return 573845;
			case 33562 :
				return 574234;
			case 33563 :
				return 574235;
			case 33564 :
				return 573852;
			case 33566 :
				return 573854;
			case 33567 :
				return 573855;
			case 33569 :
				return 573857;
			case 33571 :
				return 573859;
			case 33541 :
				return 573829;
			case 33544 :
				return 573832;
			case 33542 :
				return 573830;
			case 33550 :
				return 573838;
			case 33565 :
				return 573853;
			case 33549 :
				return 573837;
			case 33546 :
				return 573834;
			case 34564 :
				return 577173;
			case 34562 :
				return 577187;
			case 34580 :
				return 574996;
			case 34581 :
				return 577173;
			case 34586 :
				return 575002;
			case 34587 :
				return 575003;
			case 34595 :
				return 577187;
			case 34565 :
				return 577173;
			case 36484 :
				return 577173;
			case 36482 :
				return 577187;
			case 36501 :
				return 577173;
			case 36515 :
				return 577187;
			case 36485 :
				return 577173;
			case 34436 :
				return 577045;
			case 34434 :
				return 577059;
			case 34452 :
				return 574996;
			case 34453 :
				return 577045;
			case 34458 :
				return 575002;
			case 34459 :
				return 575003;
			case 34467 :
				return 577059;
			case 34437 :
				return 577045;
			case 23556 :
				return 23577;
			case 23577 :
				return 23577;
			case 23624 :
				return 23624;
			case 25892 :
				return 25892;
			case 49668 :
				return 49541;
			case 49671 :
				return 49543;
			case 49666 :
				return 49571;
			case 49667 :
				return 49539;
			case 49673 :
				return 49545;
			case 49676 :
				return 49548;
			case 49679 :
				return 49551;
			case 49684 :
				return 49556;
			case 49685 :
				return 49557;
			case 49690 :
				return 49562;
			case 49691 :
				return 49563;
			case 49692 :
				return 49564;
			case 49694 :
				return 49566;
			case 49695 :
				return 49567;
			case 49697 :
				return 49569;
			case 49699 :
				return 49571;
			case 49669 :
				return 49541;
			case 49672 :
				return 49544;
			case 49670 :
				return 49542;
			case 49678 :
				return 49550;
			case 49693 :
				return 49565;
			case 49677 :
				return 49549;
			case 49674 :
				return 49546;
			case 50052 :
				return 49541;
			case 50055 :
				return 49543;
			case 50050 :
				return 49571;
			case 50051 :
				return 49539;
			case 50057 :
				return 49545;
			case 50060 :
				return 49548;
			case 50063 :
				return 49551;
			case 50068 :
				return 49556;
			case 50069 :
				return 49557;
			case 50074 :
				return 49562;
			case 50075 :
				return 49563;
			case 50076 :
				return 49564;
			case 50078 :
				return 49566;
			case 50079 :
				return 49567;
			case 50081 :
				return 49569;
			case 50083 :
				return 49571;
			case 50053 :
				return 49541;
			case 50056 :
				return 49544;
			case 50054 :
				return 49542;
			case 50062 :
				return 49550;
			case 50077 :
				return 49565;
			case 50061 :
				return 49549;
			case 50058 :
				return 49546;
			case 49540 :
				return 49541;
			case 49543 :
				return 49543;
			case 49538 :
				return 49571;
			case 49539 :
				return 49539;
			case 49545 :
				return 49545;
			case 49548 :
				return 49548;
			case 49551 :
				return 49551;
			case 49556 :
				return 49556;
			case 49557 :
				return 49557;
			case 49562 :
				return 49562;
			case 49563 :
				return 49563;
			case 49564 :
				return 49564;
			case 49566 :
				return 49566;
			case 49567 :
				return 49567;
			case 49569 :
				return 49569;
			case 49571 :
				return 49571;
			case 49541 :
				return 49541;
			case 49544 :
				return 49544;
			case 49542 :
				return 49542;
			case 49550 :
				return 49550;
			case 49565 :
				return 49565;
			case 49549 :
				return 49549;
			case 49546 :
				return 49546;
			case 50580 :
				return 50580;
			case 50586 :
				return 50586;
			case 50587 :
				return 50587;
			case 58661 :
				return 58661;
			case 58694 :
				return 58694;
			case 49796 :
				return 49541;
			case 49799 :
				return 49543;
			case 49794 :
				return 49571;
			case 49795 :
				return 49539;
			case 49801 :
				return 49545;
			case 49804 :
				return 49548;
			case 49807 :
				return 49551;
			case 49812 :
				return 49556;
			case 49813 :
				return 49557;
			case 49818 :
				return 49562;
			case 49819 :
				return 49563;
			case 49820 :
				return 49564;
			case 49822 :
				return 49566;
			case 49823 :
				return 49567;
			case 49825 :
				return 49569;
			case 49827 :
				return 49571;
			case 49797 :
				return 49541;
			case 49800 :
				return 49544;
			case 49798 :
				return 49542;
			case 49806 :
				return 49550;
			case 49821 :
				return 49565;
			case 49805 :
				return 49549;
			case 49802 :
				return 49546;
			case 50180 :
				return 49541;
			case 50183 :
				return 49543;
			case 50178 :
				return 49571;
			case 50179 :
				return 49539;
			case 50185 :
				return 49545;
			case 50188 :
				return 49548;
			case 50191 :
				return 49551;
			case 50196 :
				return 49556;
			case 50197 :
				return 49557;
			case 50202 :
				return 49562;
			case 50203 :
				return 49563;
			case 50204 :
				return 49564;
			case 50206 :
				return 49566;
			case 50207 :
				return 49567;
			case 50209 :
				return 49569;
			case 50211 :
				return 49571;
			case 50181 :
				return 49541;
			case 50184 :
				return 49544;
			case 50182 :
				return 49542;
			case 50190 :
				return 49550;
			case 50205 :
				return 49565;
			case 50189 :
				return 49549;
			case 50186 :
				return 49546;
			case 49924 :
				return 49541;
			case 49927 :
				return 49543;
			case 49922 :
				return 49571;
			case 49923 :
				return 49539;
			case 49929 :
				return 49545;
			case 49932 :
				return 49548;
			case 49935 :
				return 49551;
			case 49940 :
				return 49556;
			case 49941 :
				return 49557;
			case 49946 :
				return 49562;
			case 49947 :
				return 49563;
			case 49948 :
				return 49564;
			case 49950 :
				return 49566;
			case 49951 :
				return 49567;
			case 49953 :
				return 49569;
			case 49955 :
				return 49571;
			case 49925 :
				return 49541;
			case 49928 :
				return 49544;
			case 49926 :
				return 49542;
			case 49934 :
				return 49550;
			case 49949 :
				return 49565;
			case 49933 :
				return 49549;
			case 49930 :
				return 49546;
			case 147972 :
				return 147845;
			case 147975 :
				return 147847;
			case 147970 :
				return 147875;
			case 147971 :
				return 147843;
			case 147977 :
				return 147849;
			case 147980 :
				return 147852;
			case 147983 :
				return 147855;
			case 147988 :
				return 147860;
			case 147989 :
				return 147861;
			case 147994 :
				return 147866;
			case 147995 :
				return 147867;
			case 147996 :
				return 147868;
			case 147998 :
				return 147870;
			case 147999 :
				return 147871;
			case 148001 :
				return 147873;
			case 148003 :
				return 147875;
			case 147973 :
				return 147845;
			case 147976 :
				return 147848;
			case 147974 :
				return 147846;
			case 147982 :
				return 147854;
			case 147997 :
				return 147869;
			case 147981 :
				return 147853;
			case 147978 :
				return 147850;
			case 148356 :
				return 147845;
			case 148359 :
				return 147847;
			case 148354 :
				return 147875;
			case 148355 :
				return 147843;
			case 148361 :
				return 147849;
			case 148364 :
				return 147852;
			case 148367 :
				return 147855;
			case 148372 :
				return 147860;
			case 148373 :
				return 147861;
			case 148378 :
				return 147866;
			case 148379 :
				return 147867;
			case 148380 :
				return 147868;
			case 148382 :
				return 147870;
			case 148383 :
				return 147871;
			case 148385 :
				return 147873;
			case 148387 :
				return 147875;
			case 148357 :
				return 147845;
			case 148360 :
				return 147848;
			case 148358 :
				return 147846;
			case 148366 :
				return 147854;
			case 148381 :
				return 147869;
			case 148365 :
				return 147853;
			case 148362 :
				return 147850;
			case 147844 :
				return 147845;
			case 147847 :
				return 147847;
			case 147842 :
				return 147875;
			case 147843 :
				return 147843;
			case 147849 :
				return 147849;
			case 147852 :
				return 147852;
			case 147855 :
				return 147855;
			case 147860 :
				return 147860;
			case 147861 :
				return 147861;
			case 147866 :
				return 147866;
			case 147867 :
				return 147867;
			case 147868 :
				return 147868;
			case 147870 :
				return 147870;
			case 147871 :
				return 147871;
			case 147873 :
				return 147873;
			case 147875 :
				return 147875;
			case 147845 :
				return 147845;
			case 147848 :
				return 147848;
			case 147846 :
				return 147846;
			case 147854 :
				return 147854;
			case 147869 :
				return 147869;
			case 147853 :
				return 147853;
			case 147850 :
				return 147850;
			case 148884 :
				return 148884;
			case 148890 :
				return 148890;
			case 148891 :
				return 148891;
			case 154244 :
				return 154249 | DL;
			case 154242 :
				return 154257 | DL;
			case 154249 :
				return 154249 | DL;
			case 154256 :
				return 154256 | DL;
			case 154272 :
				return 154272 | DL;
			case 154245 :
				return 154249 | DL;
			case 154257 :
				return 154257 | DL;
			case 154250 :
				return 154249 | DL;
			case 154258 :
				return 154257 | DL;
			case 154259 :
				return 154256 | DL;
			case 154500 :
				return 154505 | DL;
			case 154498 :
				return 154513 | DL;
			case 154505 :
				return 154505 | DL;
			case 154512 :
				return 154512 | DL;
			case 154528 :
				return 154528 | DL;
			case 154501 :
				return 154505 | DL;
			case 154513 :
				return 154513 | DL;
			case 154506 :
				return 154505 | DL;
			case 154514 :
				return 154513 | DL;
			case 154515 :
				return 154512 | DL;
			case 154756 :
				return 154761;
			case 154754 :
				return 154769;
			case 154761 :
				return 154761;
			case 154768 :
				return 154768 | DL;
			case 154784 :
				return 154784;
			case 154757 :
				return 154761;
			case 154769 :
				return 154769;
			case 154762 :
				return 154761;
			case 154770 :
				return 154769;
			case 154771 :
				return 154768 | DL;
			case 155138 :
				return 155160;
			case 155160 :
				return 155160;
			case 155208 :
				return 155208;
			case 155159 :
				return 155160;
			case 155780 :
				return 155801 | DL;
			case 155801 :
				return 155801 | DL;
			case 155848 :
				return 155848 | DL;
			case 156162 :
				return 156184 | DL;
			case 156184 :
				return 156184 | DL;
			case 156232 :
				return 156232 | DL;
			case 156183 :
				return 156184 | DL;
			case 156966 :
				return 156966;
			case 156969 :
				return 156969;
			case 157003 :
				return 157003;
			case 157572 :
				return 157577;
			case 157570 :
				return 157585;
			case 157577 :
				return 157577;
			case 157584 :
				return 157584 | DL;
			case 157600 :
				return 157600;
			case 157573 :
				return 157577;
			case 157585 :
				return 157585;
			case 157578 :
				return 157577;
			case 157586 :
				return 157585;
			case 157587 :
				return 157584 | DL;
			case 148100 :
				return 147845;
			case 148103 :
				return 147847;
			case 148098 :
				return 147875;
			case 148099 :
				return 147843;
			case 148105 :
				return 147849;
			case 148108 :
				return 147852;
			case 148111 :
				return 147855;
			case 148116 :
				return 147860;
			case 148117 :
				return 147861;
			case 148122 :
				return 147866;
			case 148123 :
				return 147867;
			case 148124 :
				return 147868;
			case 148126 :
				return 147870;
			case 148127 :
				return 147871;
			case 148129 :
				return 147873;
			case 148131 :
				return 147875;
			case 148101 :
				return 147845;
			case 148104 :
				return 147848;
			case 148102 :
				return 147846;
			case 148110 :
				return 147854;
			case 148125 :
				return 147869;
			case 148109 :
				return 147853;
			case 148106 :
				return 147850;
			case 148484 :
				return 147845;
			case 148487 :
				return 147847;
			case 148482 :
				return 147875;
			case 148483 :
				return 147843;
			case 148489 :
				return 147849;
			case 148492 :
				return 147852;
			case 148495 :
				return 147855;
			case 148500 :
				return 147860;
			case 148501 :
				return 147861;
			case 148506 :
				return 147866;
			case 148507 :
				return 147867;
			case 148508 :
				return 147868;
			case 148510 :
				return 147870;
			case 148511 :
				return 147871;
			case 148513 :
				return 147873;
			case 148515 :
				return 147875;
			case 148485 :
				return 147845;
			case 148488 :
				return 147848;
			case 148486 :
				return 147846;
			case 148494 :
				return 147854;
			case 148509 :
				return 147869;
			case 148493 :
				return 147853;
			case 148490 :
				return 147850;
			case 148228 :
				return 147845;
			case 148231 :
				return 147847;
			case 148226 :
				return 147875;
			case 148227 :
				return 147843;
			case 148233 :
				return 147849;
			case 148236 :
				return 147852;
			case 148239 :
				return 147855;
			case 148244 :
				return 147860;
			case 148245 :
				return 147861;
			case 148250 :
				return 147866;
			case 148251 :
				return 147867;
			case 148252 :
				return 147868;
			case 148254 :
				return 147870;
			case 148255 :
				return 147871;
			case 148257 :
				return 147873;
			case 148259 :
				return 147875;
			case 148229 :
				return 147845;
			case 148232 :
				return 147848;
			case 148230 :
				return 147846;
			case 148238 :
				return 147854;
			case 148253 :
				return 147869;
			case 148237 :
				return 147853;
			case 148234 :
				return 147850;
			case 189733 :
				return 189733;
			case 197124 :
				return 196997;
			case 197127 :
				return 196999;
			case 197122 :
				return 197027;
			case 197123 :
				return 196995;
			case 197129 :
				return 197001;
			case 197132 :
				return 197004;
			case 197135 :
				return 197007;
			case 197140 :
				return 197012;
			case 197141 :
				return 197013;
			case 197146 :
				return 197018;
			case 197147 :
				return 197019;
			case 197148 :
				return 197020;
			case 197150 :
				return 197022;
			case 197151 :
				return 197023;
			case 197153 :
				return 197025;
			case 197155 :
				return 197027;
			case 197125 :
				return 196997;
			case 197128 :
				return 197000;
			case 197126 :
				return 196998;
			case 197134 :
				return 197006;
			case 197149 :
				return 197021;
			case 197133 :
				return 197005;
			case 197130 :
				return 197002;
			case 197508 :
				return 196997;
			case 197511 :
				return 196999;
			case 197506 :
				return 197027;
			case 197507 :
				return 196995;
			case 197513 :
				return 197001;
			case 197516 :
				return 197004;
			case 197519 :
				return 197007;
			case 197524 :
				return 197012;
			case 197525 :
				return 197013;
			case 197530 :
				return 197018;
			case 197531 :
				return 197019;
			case 197532 :
				return 197020;
			case 197534 :
				return 197022;
			case 197535 :
				return 197023;
			case 197537 :
				return 197025;
			case 197539 :
				return 197027;
			case 197509 :
				return 196997;
			case 197512 :
				return 197000;
			case 197510 :
				return 196998;
			case 197518 :
				return 197006;
			case 197533 :
				return 197021;
			case 197517 :
				return 197005;
			case 197514 :
				return 197002;
			case 196996 :
				return 196997;
			case 196999 :
				return 196999;
			case 196994 :
				return 197027;
			case 196995 :
				return 196995;
			case 197001 :
				return 197001;
			case 197004 :
				return 197004;
			case 197007 :
				return 197007;
			case 197012 :
				return 197012;
			case 197013 :
				return 197013;
			case 197018 :
				return 197018;
			case 197019 :
				return 197019;
			case 197020 :
				return 197020;
			case 197022 :
				return 197022;
			case 197023 :
				return 197023;
			case 197025 :
				return 197025;
			case 197027 :
				return 197027;
			case 196997 :
				return 196997;
			case 197000 :
				return 197000;
			case 196998 :
				return 196998;
			case 197006 :
				return 197006;
			case 197021 :
				return 197021;
			case 197005 :
				return 197005;
			case 197002 :
				return 197002;
			case 198036 :
				return 198036;
			case 198042 :
				return 198042;
			case 198043 :
				return 198043;
			case 204036 :
				return 204044;
			case 204039 :
				return 204044;
			case 204044 :
				return 204044;
			case 204037 :
				return 204044;
			case 204038 :
				return 204044;
			case 204046 :
				return 204044;
			case 204045 :
				return 204044;
			case 206120 :
				return 206120;
			case 206122 :
				return 206122;
			case 206123 :
				return 206123;
			case 206150 :
				return 206150;
			case 206468 :
				return 206473;
			case 206466 :
				return 206481 | DL;
			case 206473 :
				return 206473;
			case 206480 :
				return 206480 | DL;
			case 206496 :
				return 206496 | DL;
			case 206469 :
				return 206473;
			case 206481 :
				return 206481 | DL;
			case 206474 :
				return 206473;
			case 206482 :
				return 206481 | DL;
			case 206483 :
				return 206480 | DL;
			case 206596 :
				return 206607;
			case 206594 :
				return 206626 | DL;
			case 206607 :
				return 206607;
			case 206626 :
				return 206626 | DL;
			case 206597 :
				return 206607;
			case 206602 :
				return 206607;
			case 206610 :
				return 206626 | DL;
			case 206611 :
				return 206626 | DL;
			case 206852 :
				return 206860;
			case 206855 :
				return 206860;
			case 206860 :
				return 206860;
			case 206853 :
				return 206860;
			case 206854 :
				return 206860;
			case 206862 :
				return 206860;
			case 206861 :
				return 206860;
			case 197252 :
				return 196997;
			case 197255 :
				return 196999;
			case 197250 :
				return 197027;
			case 197251 :
				return 196995;
			case 197257 :
				return 197001;
			case 197260 :
				return 197004;
			case 197263 :
				return 197007;
			case 197268 :
				return 197012;
			case 197269 :
				return 197013;
			case 197274 :
				return 197018;
			case 197275 :
				return 197019;
			case 197276 :
				return 197020;
			case 197278 :
				return 197022;
			case 197279 :
				return 197023;
			case 197281 :
				return 197025;
			case 197283 :
				return 197027;
			case 197253 :
				return 196997;
			case 197256 :
				return 197000;
			case 197254 :
				return 196998;
			case 197262 :
				return 197006;
			case 197277 :
				return 197021;
			case 197261 :
				return 197005;
			case 197258 :
				return 197002;
			case 197636 :
				return 196997;
			case 197639 :
				return 196999;
			case 197634 :
				return 197027;
			case 197635 :
				return 196995;
			case 197641 :
				return 197001;
			case 197644 :
				return 197004;
			case 197647 :
				return 197007;
			case 197652 :
				return 197012;
			case 197653 :
				return 197013;
			case 197658 :
				return 197018;
			case 197659 :
				return 197019;
			case 197660 :
				return 197020;
			case 197662 :
				return 197022;
			case 197663 :
				return 197023;
			case 197665 :
				return 197025;
			case 197667 :
				return 197027;
			case 197637 :
				return 196997;
			case 197640 :
				return 197000;
			case 197638 :
				return 196998;
			case 197646 :
				return 197006;
			case 197661 :
				return 197021;
			case 197645 :
				return 197005;
			case 197642 :
				return 197002;
			case 197380 :
				return 196997;
			case 197383 :
				return 196999;
			case 197378 :
				return 197027;
			case 197379 :
				return 196995;
			case 197385 :
				return 197001;
			case 197388 :
				return 197004;
			case 197391 :
				return 197007;
			case 197396 :
				return 197012;
			case 197397 :
				return 197013;
			case 197402 :
				return 197018;
			case 197403 :
				return 197019;
			case 197404 :
				return 197020;
			case 197406 :
				return 197022;
			case 197407 :
				return 197023;
			case 197409 :
				return 197025;
			case 197411 :
				return 197027;
			case 197381 :
				return 196997;
			case 197384 :
				return 197000;
			case 197382 :
				return 196998;
			case 197390 :
				return 197006;
			case 197405 :
				return 197021;
			case 197389 :
				return 197005;
			case 197386 :
				return 197002;
			case 246276 :
				return 246149;
			case 246279 :
				return 246151;
			case 246274 :
				return 246179;
			case 246275 :
				return 246147;
			case 246281 :
				return 246153;
			case 246284 :
				return 246156;
			case 246287 :
				return 246159;
			case 246292 :
				return 246164;
			case 246293 :
				return 246165;
			case 246298 :
				return 246170;
			case 246299 :
				return 246171;
			case 246300 :
				return 246172;
			case 246302 :
				return 246174;
			case 246303 :
				return 246175;
			case 246305 :
				return 246177;
			case 246307 :
				return 246179;
			case 246277 :
				return 246149;
			case 246280 :
				return 246152;
			case 246278 :
				return 246150;
			case 246286 :
				return 246158;
			case 246301 :
				return 246173;
			case 246285 :
				return 246157;
			case 246282 :
				return 246154;
			case 246660 :
				return 246149;
			case 246663 :
				return 246151;
			case 246658 :
				return 246179;
			case 246659 :
				return 246147;
			case 246665 :
				return 246153;
			case 246668 :
				return 246156;
			case 246671 :
				return 246159;
			case 246676 :
				return 246164;
			case 246677 :
				return 246165;
			case 246682 :
				return 246170;
			case 246683 :
				return 246171;
			case 246684 :
				return 246172;
			case 246686 :
				return 246174;
			case 246687 :
				return 246175;
			case 246689 :
				return 246177;
			case 246691 :
				return 246179;
			case 246661 :
				return 246149;
			case 246664 :
				return 246152;
			case 246662 :
				return 246150;
			case 246670 :
				return 246158;
			case 246685 :
				return 246173;
			case 246669 :
				return 246157;
			case 246666 :
				return 246154;
			case 246148 :
				return 246149;
			case 246151 :
				return 246151;
			case 246146 :
				return 246179;
			case 246147 :
				return 246147;
			case 246153 :
				return 246153;
			case 246156 :
				return 246156;
			case 246159 :
				return 246159;
			case 246164 :
				return 246164;
			case 246165 :
				return 246165;
			case 246170 :
				return 246170;
			case 246171 :
				return 246171;
			case 246172 :
				return 246172;
			case 246174 :
				return 246174;
			case 246175 :
				return 246175;
			case 246177 :
				return 246177;
			case 246179 :
				return 246179;
			case 246149 :
				return 246149;
			case 246152 :
				return 246152;
			case 246150 :
				return 246150;
			case 246158 :
				return 246158;
			case 246173 :
				return 246173;
			case 246157 :
				return 246157;
			case 246154 :
				return 246154;
			case 247188 :
				return 247188;
			case 247194 :
				return 247194;
			case 247195 :
				return 247195;
			case 255308 :
				return 255308;
			case 246404 :
				return 246149;
			case 246407 :
				return 246151;
			case 246402 :
				return 246179;
			case 246403 :
				return 246147;
			case 246409 :
				return 246153;
			case 246412 :
				return 246156;
			case 246415 :
				return 246159;
			case 246420 :
				return 246164;
			case 246421 :
				return 246165;
			case 246426 :
				return 246170;
			case 246427 :
				return 246171;
			case 246428 :
				return 246172;
			case 246430 :
				return 246174;
			case 246431 :
				return 246175;
			case 246433 :
				return 246177;
			case 246435 :
				return 246179;
			case 246405 :
				return 246149;
			case 246408 :
				return 246152;
			case 246406 :
				return 246150;
			case 246414 :
				return 246158;
			case 246429 :
				return 246173;
			case 246413 :
				return 246157;
			case 246410 :
				return 246154;
			case 246788 :
				return 246149;
			case 246791 :
				return 246151;
			case 246786 :
				return 246179;
			case 246787 :
				return 246147;
			case 246793 :
				return 246153;
			case 246796 :
				return 246156;
			case 246799 :
				return 246159;
			case 246804 :
				return 246164;
			case 246805 :
				return 246165;
			case 246810 :
				return 246170;
			case 246811 :
				return 246171;
			case 246812 :
				return 246172;
			case 246814 :
				return 246174;
			case 246815 :
				return 246175;
			case 246817 :
				return 246177;
			case 246819 :
				return 246179;
			case 246789 :
				return 246149;
			case 246792 :
				return 246152;
			case 246790 :
				return 246150;
			case 246798 :
				return 246158;
			case 246813 :
				return 246173;
			case 246797 :
				return 246157;
			case 246794 :
				return 246154;
			case 246532 :
				return 246149;
			case 246535 :
				return 246151;
			case 246530 :
				return 246179;
			case 246531 :
				return 246147;
			case 246537 :
				return 246153;
			case 246540 :
				return 246156;
			case 246543 :
				return 246159;
			case 246548 :
				return 246164;
			case 246549 :
				return 246165;
			case 246554 :
				return 246170;
			case 246555 :
				return 246171;
			case 246556 :
				return 246172;
			case 246558 :
				return 246174;
			case 246559 :
				return 246175;
			case 246561 :
				return 246177;
			case 246563 :
				return 246179;
			case 246533 :
				return 246149;
			case 246536 :
				return 246152;
			case 246534 :
				return 246150;
			case 246542 :
				return 246158;
			case 246557 :
				return 246173;
			case 246541 :
				return 246157;
			case 246538 :
				return 246154;
			case 268932 :
				return 268937 | DL;
			case 268930 :
				return 268945 | DL;
			case 268937 :
				return 268937 | DL;
			case 268944 :
				return 268944 | DL;
			case 268960 :
				return 268960 | DL;
			case 268933 :
				return 268937 | DL;
			case 268945 :
				return 268945 | DL;
			case 268938 :
				return 268937 | DL;
			case 268946 :
				return 268945 | DL;
			case 268947 :
				return 268944 | DL;
			case 269188 :
				return 269193 | DL;
			case 269186 :
				return 269201 | DL;
			case 269193 :
				return 269193 | DL;
			case 269200 :
				return 269200 | DL;
			case 269216 :
				return 269216 | DL;
			case 269189 :
				return 269193 | DL;
			case 269201 :
				return 269201 | DL;
			case 269194 :
				return 269193 | DL;
			case 269202 :
				return 269201 | DL;
			case 269203 :
				return 269200 | DL;
			case 269444 :
				return 269449 | DL;
			case 269442 :
				return 269457 | DL;
			case 269449 :
				return 269449 | DL;
			case 269456 :
				return 269456 | DL;
			case 269472 :
				return 269472 | DL;
			case 269445 :
				return 269449 | DL;
			case 269457 :
				return 269457 | DL;
			case 269450 :
				return 269449 | DL;
			case 269458 :
				return 269457 | DL;
			case 269459 :
				return 269456 | DL;
			case 269826 :
				return 269848 | DL;
			case 269848 :
				return 269848 | DL;
			case 269896 :
				return 269896 | DL;
			case 269847 :
				return 269848 | DL;
			case 270468 :
				return 270489 | DL;
			case 270489 :
				return 270489 | DL;
			case 270536 :
				return 270536 | DL;
			case 270850 :
				return 270872 | DL;
			case 270872 :
				return 270872 | DL;
			case 270920 :
				return 270920 | DL;
			case 270871 :
				return 270872 | DL;
			case 271654 :
				return 271654 | DL;
			case 271691 :
				return 271691 | DL;
			case 272260 :
				return 272265 | DL;
			case 272258 :
				return 272273 | DL;
			case 272265 :
				return 272265 | DL;
			case 272272 :
				return 272272 | DL;
			case 272288 :
				return 272288 | DL;
			case 272261 :
				return 272265 | DL;
			case 272273 :
				return 272273 | DL;
			case 272266 :
				return 272265 | DL;
			case 272274 :
				return 272273 | DL;
			case 272275 :
				return 272272 | DL;
			case 344580 :
				return 345093;
			case 344583 :
				return 344455;
			case 344578 :
				return 345123;
			case 344579 :
				return 344451;
			case 344585 :
				return 344457;
			case 344588 :
				return 344460;
			case 344591 :
				return 344463;
			case 344596 :
				return 344852;
			case 344597 :
				return 345109;
			case 344602 :
				return 344858;
			case 344603 :
				return 344859;
			case 344604 :
				return 344476;
			case 344606 :
				return 344478;
			case 344607 :
				return 344479;
			case 344609 :
				return 344481;
			case 344611 :
				return 345123;
			case 344581 :
				return 345093;
			case 344584 :
				return 344456;
			case 344582 :
				return 344454;
			case 344590 :
				return 344462;
			case 344605 :
				return 344477;
			case 344589 :
				return 344461;
			case 344586 :
				return 344458;
			case 344964 :
				return 345093;
			case 344967 :
				return 344455;
			case 344962 :
				return 345123;
			case 344963 :
				return 344451;
			case 344969 :
				return 344457;
			case 344972 :
				return 344460;
			case 344975 :
				return 344463;
			case 344980 :
				return 344852;
			case 344981 :
				return 345109;
			case 344986 :
				return 344858;
			case 344987 :
				return 344859;
			case 344988 :
				return 344476;
			case 344990 :
				return 344478;
			case 344991 :
				return 344479;
			case 344993 :
				return 344481;
			case 344995 :
				return 345123;
			case 344965 :
				return 345093;
			case 344968 :
				return 344456;
			case 344966 :
				return 344454;
			case 344974 :
				return 344462;
			case 344989 :
				return 344477;
			case 344973 :
				return 344461;
			case 344970 :
				return 344458;
			case 344452 :
				return 344453;
			case 344455 :
				return 344455;
			case 344450 :
				return 344483;
			case 344451 :
				return 344451;
			case 344457 :
				return 344457;
			case 344460 :
				return 344460;
			case 344463 :
				return 344463;
			case 344468 :
				return 344468;
			case 344469 :
				return 344469;
			case 344474 :
				return 344474;
			case 344475 :
				return 344475;
			case 344476 :
				return 344476;
			case 344478 :
				return 344478;
			case 344479 :
				return 344479;
			case 344481 :
				return 344481;
			case 344483 :
				return 344483;
			case 344453 :
				return 344453;
			case 344456 :
				return 344456;
			case 344454 :
				return 344454;
			case 344462 :
				return 344462;
			case 344477 :
				return 344477;
			case 344461 :
				return 344461;
			case 344458 :
				return 344458;
			case 345492 :
				return 345492;
			case 345498 :
				return 345498;
			case 345499 :
				return 345499;
			case 345620 :
				return 345620;
			case 345626 :
				return 345626;
			case 345627 :
				return 345627;
			case 347652 :
				return 347669;
			case 347650 :
				return 347683;
			case 347669 :
				return 347669;
			case 347683 :
				return 347683;
			case 347653 :
				return 347669;
			case 348292 :
				return 348309;
			case 348290 :
				return 348323;
			case 348309 :
				return 348309;
			case 348323 :
				return 348323;
			case 348293 :
				return 348309;
			case 350980 :
				return 350997;
			case 350997 :
				return 350997;
			case 350981 :
				return 350997;
			case 352516 :
				return 352533;
			case 352533 :
				return 352533;
			case 352517 :
				return 352533;
			case 353540 :
				return 353545;
			case 353538 :
				return 353553;
			case 353545 :
				return 353545;
			case 353552 :
				return 353552 | DL;
			case 353568 :
				return 353568;
			case 353541 :
				return 353545;
			case 353553 :
				return 353553;
			case 353546 :
				return 353545;
			case 353554 :
				return 353553;
			case 353555 :
				return 353552 | DL;
			case 344708 :
				return 345093;
			case 344711 :
				return 344455;
			case 344706 :
				return 345123;
			case 344707 :
				return 344451;
			case 344713 :
				return 344457;
			case 344716 :
				return 344460;
			case 344719 :
				return 344463;
			case 344724 :
				return 344852;
			case 344725 :
				return 345109;
			case 344730 :
				return 344858;
			case 344731 :
				return 344859;
			case 344732 :
				return 344476;
			case 344734 :
				return 344478;
			case 344735 :
				return 344479;
			case 344737 :
				return 344481;
			case 344739 :
				return 345123;
			case 344709 :
				return 345093;
			case 344712 :
				return 344456;
			case 344710 :
				return 344454;
			case 344718 :
				return 344462;
			case 344733 :
				return 344477;
			case 344717 :
				return 344461;
			case 344714 :
				return 344458;
			case 345092 :
				return 345093;
			case 345095 :
				return 344455;
			case 345090 :
				return 345123;
			case 345091 :
				return 344451;
			case 345097 :
				return 344457;
			case 345100 :
				return 344460;
			case 345103 :
				return 344463;
			case 345108 :
				return 344468;
			case 345109 :
				return 345109;
			case 345114 :
				return 344474;
			case 345115 :
				return 344475;
			case 345116 :
				return 344476;
			case 345118 :
				return 344478;
			case 345119 :
				return 344479;
			case 345121 :
				return 344481;
			case 345123 :
				return 345123;
			case 345093 :
				return 345093;
			case 345096 :
				return 344456;
			case 345094 :
				return 344454;
			case 345102 :
				return 344462;
			case 345117 :
				return 344477;
			case 345101 :
				return 344461;
			case 345098 :
				return 344458;
			case 344836 :
				return 344453;
			case 344839 :
				return 344455;
			case 344834 :
				return 344483;
			case 344835 :
				return 344451;
			case 344841 :
				return 344457;
			case 344844 :
				return 344460;
			case 344847 :
				return 344463;
			case 344852 :
				return 344852;
			case 344853 :
				return 344469;
			case 344858 :
				return 344858;
			case 344859 :
				return 344859;
			case 344860 :
				return 344476;
			case 344862 :
				return 344478;
			case 344863 :
				return 344479;
			case 344865 :
				return 344481;
			case 344867 :
				return 344483;
			case 344837 :
				return 344453;
			case 344840 :
				return 344456;
			case 344838 :
				return 344454;
			case 344846 :
				return 344462;
			case 344861 :
				return 344477;
			case 344845 :
				return 344461;
			case 344842 :
				return 344458;
			case 345860 :
				return 347797;
			case 345858 :
				return 347811;
			case 345876 :
				return 345620;
			case 345877 :
				return 347797;
			case 345882 :
				return 345626;
			case 345883 :
				return 345627;
			case 345891 :
				return 347811;
			case 345861 :
				return 347797;
			case 347780 :
				return 347797;
			case 347778 :
				return 347811;
			case 347797 :
				return 347797;
			case 347811 :
				return 347811;
			case 347781 :
				return 347797;
			case 345732 :
				return 347669;
			case 345730 :
				return 347683;
			case 345748 :
				return 345620;
			case 345749 :
				return 347669;
			case 345754 :
				return 345626;
			case 345755 :
				return 345627;
			case 345763 :
				return 347683;
			case 345733 :
				return 347669;
			case 369556 :
				return 369556 | DL;
			case 369562 :
				return 369562 | DL;
			case 369563 :
				return 369563 | DL;
			case 369794 :
				return 369814 | DL;
			case 369814 :
				return 369814 | DL;
			case 369864 :
				return 369864 | DL;
			case 369815 :
				return 369814 | DL;
			case 369989 :
				return 369989 | DL;
			case 402308 :
				return 402313;
			case 402306 :
				return 402321;
			case 402313 :
				return 402313;
			case 402320 :
				return 402320 | DL;
			case 402336 :
				return 402336;
			case 402309 :
				return 402313;
			case 402321 :
				return 402321;
			case 402314 :
				return 402313;
			case 402322 :
				return 402321;
			case 402323 :
				return 402320 | DL;
			case 402562 :
				return 402584;
			case 402584 :
				return 402584;
			case 402632 :
				return 402632;
			case 402583 :
				return 402584;
			case 402757 :
				return 402757;
			case 418692 :
				return 418709;
			case 418709 :
				return 418709;
			case 418693 :
				return 418709;
			case 418948 :
				return 418969;
			case 418969 :
				return 418969;
			case 419016 :
				return 419016;
			case 419141 :
				return 419141;
			case 459268 :
				return 459141;
			case 459271 :
				return 459143;
			case 459266 :
				return 459171;
			case 459267 :
				return 459139;
			case 459273 :
				return 459145;
			case 459276 :
				return 459148;
			case 459279 :
				return 459151;
			case 459284 :
				return 459156;
			case 459285 :
				return 459157;
			case 459290 :
				return 459162;
			case 459291 :
				return 459163;
			case 459292 :
				return 459164;
			case 459294 :
				return 459166;
			case 459295 :
				return 459167;
			case 459297 :
				return 459169;
			case 459299 :
				return 459171;
			case 459269 :
				return 459141;
			case 459272 :
				return 459144;
			case 459270 :
				return 459142;
			case 459278 :
				return 459150;
			case 459293 :
				return 459165;
			case 459277 :
				return 459149;
			case 459274 :
				return 459146;
			case 459652 :
				return 459141;
			case 459655 :
				return 459143;
			case 459650 :
				return 459171;
			case 459651 :
				return 459139;
			case 459657 :
				return 459145;
			case 459660 :
				return 459148;
			case 459663 :
				return 459151;
			case 459668 :
				return 459156;
			case 459669 :
				return 459157;
			case 459674 :
				return 459162;
			case 459675 :
				return 459163;
			case 459676 :
				return 459164;
			case 459678 :
				return 459166;
			case 459679 :
				return 459167;
			case 459681 :
				return 459169;
			case 459683 :
				return 459171;
			case 459653 :
				return 459141;
			case 459656 :
				return 459144;
			case 459654 :
				return 459142;
			case 459662 :
				return 459150;
			case 459677 :
				return 459165;
			case 459661 :
				return 459149;
			case 459658 :
				return 459146;
			case 459140 :
				return 459141;
			case 459143 :
				return 459143;
			case 459138 :
				return 459171;
			case 459139 :
				return 459139;
			case 459145 :
				return 459145;
			case 459148 :
				return 459148;
			case 459151 :
				return 459151;
			case 459156 :
				return 459156;
			case 459157 :
				return 459157;
			case 459162 :
				return 459162;
			case 459163 :
				return 459163;
			case 459164 :
				return 459164;
			case 459166 :
				return 459166;
			case 459167 :
				return 459167;
			case 459169 :
				return 459169;
			case 459171 :
				return 459171;
			case 459141 :
				return 459141;
			case 459144 :
				return 459144;
			case 459142 :
				return 459142;
			case 459150 :
				return 459150;
			case 459165 :
				return 459165;
			case 459149 :
				return 459149;
			case 459146 :
				return 459146;
			case 460180 :
				return 460180;
			case 460186 :
				return 460186;
			case 460187 :
				return 460187;
			case 466180 :
				return 466204;
			case 466183 :
				return 466204;
			case 466204 :
				return 466204;
			case 466181 :
				return 466204;
			case 466184 :
				return 466204;
			case 466190 :
				return 466204;
			case 466205 :
				return 466204;
			case 466189 :
				return 466204;
			case 466564 :
				return 466588;
			case 466567 :
				return 466588;
			case 466588 :
				return 466588;
			case 466565 :
				return 466588;
			case 466568 :
				return 466588;
			case 466574 :
				return 466588;
			case 466589 :
				return 466588;
			case 466573 :
				return 466588;
			case 468266 :
				return 468266;
			case 468267 :
				return 468267;
			case 468268 :
				return 468268;
			case 468269 :
				return 468269;
			case 468273 :
				return 468273;
			case 468294 :
				return 468294;
			case 468612 :
				return 468617;
			case 468610 :
				return 468625 | DL;
			case 468617 :
				return 468617;
			case 468624 :
				return 468624 | DL;
			case 468640 :
				return 468640 | DL;
			case 468613 :
				return 468617;
			case 468625 :
				return 468625 | DL;
			case 468618 :
				return 468617;
			case 468626 :
				return 468625 | DL;
			case 468627 :
				return 468624 | DL;
			case 468740 :
				return 468745;
			case 468738 :
				return 468753 | DL;
			case 468745 :
				return 468745;
			case 468752 :
				return 468752 | DL;
			case 468768 :
				return 468768 | DL;
			case 468741 :
				return 468745;
			case 468753 :
				return 468753 | DL;
			case 468746 :
				return 468745;
			case 468754 :
				return 468753 | DL;
			case 468755 :
				return 468752 | DL;
			case 468996 :
				return 469021;
			case 468999 :
				return 469021;
			case 469020 :
				return 469020;
			case 469025 :
				return 469025;
			case 468997 :
				return 469021;
			case 469000 :
				return 469021;
			case 469006 :
				return 469021;
			case 469021 :
				return 469021;
			case 469005 :
				return 469020;
			case 459396 :
				return 459141;
			case 459399 :
				return 459143;
			case 459394 :
				return 459171;
			case 459395 :
				return 459139;
			case 459401 :
				return 459145;
			case 459404 :
				return 459148;
			case 459407 :
				return 459151;
			case 459412 :
				return 459156;
			case 459413 :
				return 459157;
			case 459418 :
				return 459162;
			case 459419 :
				return 459163;
			case 459420 :
				return 459164;
			case 459422 :
				return 459166;
			case 459423 :
				return 459167;
			case 459425 :
				return 459169;
			case 459427 :
				return 459171;
			case 459397 :
				return 459141;
			case 459400 :
				return 459144;
			case 459398 :
				return 459142;
			case 459406 :
				return 459150;
			case 459421 :
				return 459165;
			case 459405 :
				return 459149;
			case 459402 :
				return 459146;
			case 459780 :
				return 459141;
			case 459783 :
				return 459143;
			case 459778 :
				return 459171;
			case 459779 :
				return 459139;
			case 459785 :
				return 459145;
			case 459788 :
				return 459148;
			case 459791 :
				return 459151;
			case 459796 :
				return 459156;
			case 459797 :
				return 459157;
			case 459802 :
				return 459162;
			case 459803 :
				return 459163;
			case 459804 :
				return 459164;
			case 459806 :
				return 459166;
			case 459807 :
				return 459167;
			case 459809 :
				return 459169;
			case 459811 :
				return 459171;
			case 459781 :
				return 459141;
			case 459784 :
				return 459144;
			case 459782 :
				return 459142;
			case 459790 :
				return 459150;
			case 459805 :
				return 459165;
			case 459789 :
				return 459149;
			case 459786 :
				return 459146;
			case 459524 :
				return 459141;
			case 459527 :
				return 459143;
			case 459522 :
				return 459171;
			case 459523 :
				return 459139;
			case 459529 :
				return 459145;
			case 459532 :
				return 459148;
			case 459535 :
				return 459151;
			case 459540 :
				return 459156;
			case 459541 :
				return 459157;
			case 459546 :
				return 459162;
			case 459547 :
				return 459163;
			case 459548 :
				return 459164;
			case 459550 :
				return 459166;
			case 459551 :
				return 459167;
			case 459553 :
				return 459169;
			case 459555 :
				return 459171;
			case 459525 :
				return 459141;
			case 459528 :
				return 459144;
			case 459526 :
				return 459142;
			case 459534 :
				return 459150;
			case 459549 :
				return 459165;
			case 459533 :
				return 459149;
			case 459530 :
				return 459146;
			case 492036 :
				return 491909;
			case 492039 :
				return 491911;
			case 492034 :
				return 491939;
			case 492035 :
				return 491907;
			case 492041 :
				return 491913;
			case 492044 :
				return 491916;
			case 492047 :
				return 491919;
			case 492052 :
				return 491924;
			case 492053 :
				return 491925;
			case 492058 :
				return 491930;
			case 492059 :
				return 491931;
			case 492060 :
				return 491932;
			case 492062 :
				return 491934;
			case 492063 :
				return 491935;
			case 492065 :
				return 491937;
			case 492067 :
				return 491939;
			case 492037 :
				return 491909;
			case 492040 :
				return 491912;
			case 492038 :
				return 491910;
			case 492046 :
				return 491918;
			case 492061 :
				return 491933;
			case 492045 :
				return 491917;
			case 492042 :
				return 491914;
			case 492420 :
				return 491909;
			case 492423 :
				return 491911;
			case 492418 :
				return 491939;
			case 492419 :
				return 491907;
			case 492425 :
				return 491913;
			case 492428 :
				return 491916;
			case 492431 :
				return 491919;
			case 492436 :
				return 491924;
			case 492437 :
				return 491925;
			case 492442 :
				return 491930;
			case 492443 :
				return 491931;
			case 492444 :
				return 491932;
			case 492446 :
				return 491934;
			case 492447 :
				return 491935;
			case 492449 :
				return 491937;
			case 492451 :
				return 491939;
			case 492421 :
				return 491909;
			case 492424 :
				return 491912;
			case 492422 :
				return 491910;
			case 492430 :
				return 491918;
			case 492445 :
				return 491933;
			case 492429 :
				return 491917;
			case 492426 :
				return 491914;
			case 491908 :
				return 491909;
			case 491911 :
				return 491911;
			case 491906 :
				return 491939;
			case 491907 :
				return 491907;
			case 491913 :
				return 491913;
			case 491916 :
				return 491916;
			case 491919 :
				return 491919;
			case 491924 :
				return 491924;
			case 491925 :
				return 491925;
			case 491930 :
				return 491930;
			case 491931 :
				return 491931;
			case 491932 :
				return 491932;
			case 491934 :
				return 491934;
			case 491935 :
				return 491935;
			case 491937 :
				return 491937;
			case 491939 :
				return 491939;
			case 491909 :
				return 491909;
			case 491912 :
				return 491912;
			case 491910 :
				return 491910;
			case 491918 :
				return 491918;
			case 491933 :
				return 491933;
			case 491917 :
				return 491917;
			case 491914 :
				return 491914;
			case 492948 :
				return 492948;
			case 492954 :
				return 492954;
			case 492955 :
				return 492955;
			case 495492 :
				return 495518;
			case 495518 :
				return 495518;
			case 495493 :
				return 495518;
			case 501038 :
				return 501038;
			case 492164 :
				return 491909;
			case 492167 :
				return 491911;
			case 492162 :
				return 491939;
			case 492163 :
				return 491907;
			case 492169 :
				return 491913;
			case 492172 :
				return 491916;
			case 492175 :
				return 491919;
			case 492180 :
				return 491924;
			case 492181 :
				return 491925;
			case 492186 :
				return 491930;
			case 492187 :
				return 491931;
			case 492188 :
				return 491932;
			case 492190 :
				return 491934;
			case 492191 :
				return 491935;
			case 492193 :
				return 491937;
			case 492195 :
				return 491939;
			case 492165 :
				return 491909;
			case 492168 :
				return 491912;
			case 492166 :
				return 491910;
			case 492174 :
				return 491918;
			case 492189 :
				return 491933;
			case 492173 :
				return 491917;
			case 492170 :
				return 491914;
			case 492548 :
				return 491909;
			case 492551 :
				return 491911;
			case 492546 :
				return 491939;
			case 492547 :
				return 491907;
			case 492553 :
				return 491913;
			case 492556 :
				return 491916;
			case 492559 :
				return 491919;
			case 492564 :
				return 491924;
			case 492565 :
				return 491925;
			case 492570 :
				return 491930;
			case 492571 :
				return 491931;
			case 492572 :
				return 491932;
			case 492574 :
				return 491934;
			case 492575 :
				return 491935;
			case 492577 :
				return 491937;
			case 492579 :
				return 491939;
			case 492549 :
				return 491909;
			case 492552 :
				return 491912;
			case 492550 :
				return 491910;
			case 492558 :
				return 491918;
			case 492573 :
				return 491933;
			case 492557 :
				return 491917;
			case 492554 :
				return 491914;
			case 492292 :
				return 491909;
			case 492295 :
				return 491911;
			case 492290 :
				return 491939;
			case 492291 :
				return 491907;
			case 492297 :
				return 491913;
			case 492300 :
				return 491916;
			case 492303 :
				return 491919;
			case 492308 :
				return 491924;
			case 492309 :
				return 491925;
			case 492314 :
				return 491930;
			case 492315 :
				return 491931;
			case 492316 :
				return 491932;
			case 492318 :
				return 491934;
			case 492319 :
				return 491935;
			case 492321 :
				return 491937;
			case 492323 :
				return 491939;
			case 492293 :
				return 491909;
			case 492296 :
				return 491912;
			case 492294 :
				return 491910;
			case 492302 :
				return 491918;
			case 492317 :
				return 491933;
			case 492301 :
				return 491917;
			case 492298 :
				return 491914;
			case 517423 :
				return 517423;
			case 530820 :
				return 530826;
			case 530818 :
				return 530834 | DL;
			case 530825 :
				return 530825;
			case 530831 :
				return 530831;
			case 530832 :
				return 530832 | DL;
			case 530848 :
				return 530848 | DL;
			case 530850 :
				return 530850 | DL;
			case 530821 :
				return 530826;
			case 530833 :
				return 530833 | DL;
			case 530826 :
				return 530826;
			case 530834 :
				return 530834 | DL;
			case 530835 :
				return 530835 | DL;
			case 530964 :
				return 530964 | DL;
			case 530970 :
				return 530970;
			case 531332 :
				return 531337 | DL;
			case 531330 :
				return 531345 | DL;
			case 531337 :
				return 531337 | DL;
			case 531344 :
				return 531344 | DL;
			case 531360 :
				return 531360 | DL;
			case 531333 :
				return 531337 | DL;
			case 531345 :
				return 531345 | DL;
			case 531338 :
				return 531337 | DL;
			case 531346 :
				return 531345 | DL;
			case 531347 :
				return 531344 | DL;
			case 531588 :
				return 531593 | DL;
			case 531586 :
				return 531601 | DL;
			case 531593 :
				return 531593 | DL;
			case 531600 :
				return 531600 | DL;
			case 531616 :
				return 531616 | DL;
			case 531589 :
				return 531593 | DL;
			case 531601 :
				return 531601 | DL;
			case 531594 :
				return 531593 | DL;
			case 531602 :
				return 531601 | DL;
			case 531603 :
				return 531600 | DL;
			case 531844 :
				return 531861 | DL;
			case 531860 :
				return 531860 | DL;
			case 531861 :
				return 531861 | DL;
			case 531866 :
				return 531866 | DL;
			case 531867 :
				return 531867 | DL;
			case 531845 :
				return 531861 | DL;
			case 532244 :
				return 532244 | DL;
			case 532250 :
				return 532250;
			case 532372 :
				return 532372 | DL;
			case 532378 :
				return 532378;
			case 532484 :
				return 532494;
			case 532487 :
				return 532494;
			case 532492 :
				return 532492;
			case 532508 :
				return 532508;
			case 532513 :
				return 532513;
			case 532485 :
				return 532494;
			case 532488 :
				return 532509;
			case 532486 :
				return 532492;
			case 532494 :
				return 532494;
			case 532509 :
				return 532509;
			case 532493 :
				return 532493;
			case 532868 :
				return 532874;
			case 532866 :
				return 532882 | DL;
			case 532873 :
				return 532873;
			case 532879 :
				return 532879;
			case 532880 :
				return 532880 | DL;
			case 532896 :
				return 532896 | DL;
			case 532898 :
				return 532898 | DL;
			case 532869 :
				return 532874;
			case 532881 :
				return 532881 | DL;
			case 532874 :
				return 532874;
			case 532882 :
				return 532882 | DL;
			case 532883 :
				return 532883 | DL;
			case 533808 :
				return 533808;
			case 533835 :
				return 533835;
			case 534404 :
				return 534409 | DL;
			case 534402 :
				return 534417 | DL;
			case 534409 :
				return 534409 | DL;
			case 534416 :
				return 534416 | DL;
			case 534432 :
				return 534432 | DL;
			case 534405 :
				return 534409 | DL;
			case 534417 :
				return 534417 | DL;
			case 534410 :
				return 534409 | DL;
			case 534418 :
				return 534417 | DL;
			case 534419 :
				return 534416 | DL;
			case 541188 :
				return 541061;
			case 541191 :
				return 541063;
			case 541186 :
				return 541091;
			case 541187 :
				return 541059;
			case 541193 :
				return 541065;
			case 541196 :
				return 541068;
			case 541199 :
				return 541071;
			case 541204 :
				return 541076;
			case 541205 :
				return 541077;
			case 541210 :
				return 541082;
			case 541211 :
				return 541083;
			case 541212 :
				return 541084;
			case 541214 :
				return 541086;
			case 541215 :
				return 541087;
			case 541217 :
				return 541089;
			case 541219 :
				return 541091;
			case 541189 :
				return 541061;
			case 541192 :
				return 541064;
			case 541190 :
				return 541062;
			case 541198 :
				return 541070;
			case 541213 :
				return 541085;
			case 541197 :
				return 541069;
			case 541194 :
				return 541066;
			case 541572 :
				return 541061;
			case 541575 :
				return 541063;
			case 541570 :
				return 541091;
			case 541571 :
				return 541059;
			case 541577 :
				return 541065;
			case 541580 :
				return 541068;
			case 541583 :
				return 541071;
			case 541588 :
				return 541076;
			case 541589 :
				return 541077;
			case 541594 :
				return 541082;
			case 541595 :
				return 541083;
			case 541596 :
				return 541084;
			case 541598 :
				return 541086;
			case 541599 :
				return 541087;
			case 541601 :
				return 541089;
			case 541603 :
				return 541091;
			case 541573 :
				return 541061;
			case 541576 :
				return 541064;
			case 541574 :
				return 541062;
			case 541582 :
				return 541070;
			case 541597 :
				return 541085;
			case 541581 :
				return 541069;
			case 541578 :
				return 541066;
			case 541060 :
				return 541061;
			case 541063 :
				return 541063;
			case 541058 :
				return 541091;
			case 541059 :
				return 541059;
			case 541065 :
				return 541065;
			case 541068 :
				return 541068;
			case 541071 :
				return 541071;
			case 541076 :
				return 541076;
			case 541077 :
				return 541077;
			case 541082 :
				return 541082;
			case 541083 :
				return 541083;
			case 541084 :
				return 541084;
			case 541086 :
				return 541086;
			case 541087 :
				return 541087;
			case 541089 :
				return 541089;
			case 541091 :
				return 541091;
			case 541061 :
				return 541061;
			case 541064 :
				return 541064;
			case 541062 :
				return 541062;
			case 541070 :
				return 541070;
			case 541085 :
				return 541085;
			case 541069 :
				return 541069;
			case 541066 :
				return 541066;
			case 542100 :
				return 542100;
			case 542106 :
				return 542106;
			case 542107 :
				return 542107;
			case 548100 :
				return 548129;
			case 548103 :
				return 548129;
			case 548129 :
				return 548129;
			case 548101 :
				return 548129;
			case 548104 :
				return 548129;
			case 548110 :
				return 548129;
			case 548125 :
				return 548129;
			case 548484 :
				return 548513;
			case 548487 :
				return 548513;
			case 548513 :
				return 548513;
			case 548485 :
				return 548513;
			case 548488 :
				return 548513;
			case 548494 :
				return 548513;
			case 548509 :
				return 548513;
			case 550186 :
				return 550186;
			case 550189 :
				return 550189;
			case 550193 :
				return 550193;
			case 550194 :
				return 550194;
			case 550214 :
				return 550214;
			case 550532 :
				return 550537;
			case 550530 :
				return 550545 | DL;
			case 550537 :
				return 550537;
			case 550544 :
				return 550544 | DL;
			case 550560 :
				return 550560 | DL;
			case 550533 :
				return 550537;
			case 550545 :
				return 550545 | DL;
			case 550538 :
				return 550537;
			case 550546 :
				return 550545 | DL;
			case 550547 :
				return 550544 | DL;
			case 550660 :
				return 550665;
			case 550658 :
				return 550673 | DL;
			case 550665 :
				return 550665;
			case 550672 :
				return 550672 | DL;
			case 550688 :
				return 550688 | DL;
			case 550661 :
				return 550665;
			case 550673 :
				return 550673 | DL;
			case 550666 :
				return 550665;
			case 550674 :
				return 550673 | DL;
			case 550675 :
				return 550672 | DL;
			case 550916 :
				return 550945;
			case 550919 :
				return 550945;
			case 550945 :
				return 550945;
			case 550917 :
				return 550945;
			case 550920 :
				return 550945;
			case 550926 :
				return 550945;
			case 550941 :
				return 550945;
			case 541316 :
				return 541061;
			case 541319 :
				return 541063;
			case 541314 :
				return 541091;
			case 541315 :
				return 541059;
			case 541321 :
				return 541065;
			case 541324 :
				return 541068;
			case 541327 :
				return 541071;
			case 541332 :
				return 541076;
			case 541333 :
				return 541077;
			case 541338 :
				return 541082;
			case 541339 :
				return 541083;
			case 541340 :
				return 541084;
			case 541342 :
				return 541086;
			case 541343 :
				return 541087;
			case 541345 :
				return 541089;
			case 541347 :
				return 541091;
			case 541317 :
				return 541061;
			case 541320 :
				return 541064;
			case 541318 :
				return 541062;
			case 541326 :
				return 541070;
			case 541341 :
				return 541085;
			case 541325 :
				return 541069;
			case 541322 :
				return 541066;
			case 541700 :
				return 541061;
			case 541703 :
				return 541063;
			case 541698 :
				return 541091;
			case 541699 :
				return 541059;
			case 541705 :
				return 541065;
			case 541708 :
				return 541068;
			case 541711 :
				return 541071;
			case 541716 :
				return 541076;
			case 541717 :
				return 541077;
			case 541722 :
				return 541082;
			case 541723 :
				return 541083;
			case 541724 :
				return 541084;
			case 541726 :
				return 541086;
			case 541727 :
				return 541087;
			case 541729 :
				return 541089;
			case 541731 :
				return 541091;
			case 541701 :
				return 541061;
			case 541704 :
				return 541064;
			case 541702 :
				return 541062;
			case 541710 :
				return 541070;
			case 541725 :
				return 541085;
			case 541709 :
				return 541069;
			case 541706 :
				return 541066;
			case 541444 :
				return 541061;
			case 541447 :
				return 541063;
			case 541442 :
				return 541091;
			case 541443 :
				return 541059;
			case 541449 :
				return 541065;
			case 541452 :
				return 541068;
			case 541455 :
				return 541071;
			case 541460 :
				return 541076;
			case 541461 :
				return 541077;
			case 541466 :
				return 541082;
			case 541467 :
				return 541083;
			case 541468 :
				return 541084;
			case 541470 :
				return 541086;
			case 541471 :
				return 541087;
			case 541473 :
				return 541089;
			case 541475 :
				return 541091;
			case 541445 :
				return 541061;
			case 541448 :
				return 541064;
			case 541446 :
				return 541062;
			case 541454 :
				return 541070;
			case 541469 :
				return 541085;
			case 541453 :
				return 541069;
			case 541450 :
				return 541066;
			case 565378 :
				return 565398 | DL;
			case 565398 :
				return 565398 | DL;
			case 565448 :
				return 565448 | DL;
			case 565399 :
				return 565398 | DL;
			case 566567 :
				return 566567 | DL;
			case 566603 :
				return 566603 | DL;
			case 573956 :
				return 574469;
			case 573959 :
				return 573831;
			case 573954 :
				return 574499;
			case 573955 :
				return 573827;
			case 573961 :
				return 573833;
			case 573964 :
				return 573836;
			case 573967 :
				return 573839;
			case 573972 :
				return 574228;
			case 573973 :
				return 574485;
			case 573978 :
				return 574234;
			case 573979 :
				return 574235;
			case 573980 :
				return 573852;
			case 573982 :
				return 573854;
			case 573983 :
				return 573855;
			case 573985 :
				return 573857;
			case 573987 :
				return 574499;
			case 573957 :
				return 574469;
			case 573960 :
				return 573832;
			case 573958 :
				return 573830;
			case 573966 :
				return 573838;
			case 573981 :
				return 573853;
			case 573965 :
				return 573837;
			case 573962 :
				return 573834;
			case 574340 :
				return 574469;
			case 574343 :
				return 573831;
			case 574338 :
				return 574499;
			case 574339 :
				return 573827;
			case 574345 :
				return 573833;
			case 574348 :
				return 573836;
			case 574351 :
				return 573839;
			case 574356 :
				return 574228;
			case 574357 :
				return 574485;
			case 574362 :
				return 574234;
			case 574363 :
				return 574235;
			case 574364 :
				return 573852;
			case 574366 :
				return 573854;
			case 574367 :
				return 573855;
			case 574369 :
				return 573857;
			case 574371 :
				return 574499;
			case 574341 :
				return 574469;
			case 574344 :
				return 573832;
			case 574342 :
				return 573830;
			case 574350 :
				return 573838;
			case 574365 :
				return 573853;
			case 574349 :
				return 573837;
			case 574346 :
				return 573834;
			case 573828 :
				return 573829;
			case 573831 :
				return 573831;
			case 573826 :
				return 573859;
			case 573827 :
				return 573827;
			case 573833 :
				return 573833;
			case 573836 :
				return 573836;
			case 573839 :
				return 573839;
			case 573844 :
				return 573844;
			case 573845 :
				return 573845;
			case 573850 :
				return 573850;
			case 573851 :
				return 573851;
			case 573852 :
				return 573852;
			case 573854 :
				return 573854;
			case 573855 :
				return 573855;
			case 573857 :
				return 573857;
			case 573859 :
				return 573859;
			case 573829 :
				return 573829;
			case 573832 :
				return 573832;
			case 573830 :
				return 573830;
			case 573838 :
				return 573838;
			case 573853 :
				return 573853;
			case 573837 :
				return 573837;
			case 573834 :
				return 573834;
			case 574868 :
				return 574868;
			case 574874 :
				return 574874;
			case 574875 :
				return 574875;
			case 574996 :
				return 574996;
			case 575002 :
				return 575002;
			case 575003 :
				return 575003;
			case 577028 :
				return 577045;
			case 577026 :
				return 577059;
			case 577045 :
				return 577045;
			case 577059 :
				return 577059;
			case 577029 :
				return 577045;
			case 577668 :
				return 577685;
			case 577666 :
				return 577699;
			case 577685 :
				return 577685;
			case 577699 :
				return 577699;
			case 577669 :
				return 577685;
			case 582916 :
				return 582921;
			case 582914 :
				return 582929;
			case 582921 :
				return 582921;
			case 582928 :
				return 582928 | DL;
			case 582944 :
				return 582944;
			case 582917 :
				return 582921;
			case 582929 :
				return 582929;
			case 582922 :
				return 582921;
			case 582930 :
				return 582929;
			case 582931 :
				return 582928 | DL;
			case 574084 :
				return 574469;
			case 574087 :
				return 573831;
			case 574082 :
				return 574499;
			case 574083 :
				return 573827;
			case 574089 :
				return 573833;
			case 574092 :
				return 573836;
			case 574095 :
				return 573839;
			case 574100 :
				return 574228;
			case 574101 :
				return 574485;
			case 574106 :
				return 574234;
			case 574107 :
				return 574235;
			case 574108 :
				return 573852;
			case 574110 :
				return 573854;
			case 574111 :
				return 573855;
			case 574113 :
				return 573857;
			case 574115 :
				return 574499;
			case 574085 :
				return 574469;
			case 574088 :
				return 573832;
			case 574086 :
				return 573830;
			case 574094 :
				return 573838;
			case 574109 :
				return 573853;
			case 574093 :
				return 573837;
			case 574090 :
				return 573834;
			case 574468 :
				return 574469;
			case 574471 :
				return 573831;
			case 574466 :
				return 574499;
			case 574467 :
				return 573827;
			case 574473 :
				return 573833;
			case 574476 :
				return 573836;
			case 574479 :
				return 573839;
			case 574484 :
				return 573844;
			case 574485 :
				return 574485;
			case 574490 :
				return 573850;
			case 574491 :
				return 573851;
			case 574492 :
				return 573852;
			case 574494 :
				return 573854;
			case 574495 :
				return 573855;
			case 574497 :
				return 573857;
			case 574499 :
				return 574499;
			case 574469 :
				return 574469;
			case 574472 :
				return 573832;
			case 574470 :
				return 573830;
			case 574478 :
				return 573838;
			case 574493 :
				return 573853;
			case 574477 :
				return 573837;
			case 574474 :
				return 573834;
			case 574212 :
				return 573829;
			case 574215 :
				return 573831;
			case 574210 :
				return 573859;
			case 574211 :
				return 573827;
			case 574217 :
				return 573833;
			case 574220 :
				return 573836;
			case 574223 :
				return 573839;
			case 574228 :
				return 574228;
			case 574229 :
				return 573845;
			case 574234 :
				return 574234;
			case 574235 :
				return 574235;
			case 574236 :
				return 573852;
			case 574238 :
				return 573854;
			case 574239 :
				return 573855;
			case 574241 :
				return 573857;
			case 574243 :
				return 573859;
			case 574213 :
				return 573829;
			case 574216 :
				return 573832;
			case 574214 :
				return 573830;
			case 574222 :
				return 573838;
			case 574237 :
				return 573853;
			case 574221 :
				return 573837;
			case 574218 :
				return 573834;
			case 575236 :
				return 577173;
			case 575234 :
				return 577187;
			case 575252 :
				return 574996;
			case 575253 :
				return 577173;
			case 575258 :
				return 575002;
			case 575259 :
				return 575003;
			case 575267 :
				return 577187;
			case 575237 :
				return 577173;
			case 577156 :
				return 577173;
			case 577154 :
				return 577187;
			case 577173 :
				return 577173;
			case 577187 :
				return 577187;
			case 577157 :
				return 577173;
			case 575108 :
				return 577045;
			case 575106 :
				return 577059;
			case 575124 :
				return 574996;
			case 575125 :
				return 577045;
			case 575130 :
				return 575002;
			case 575131 :
				return 575003;
			case 575139 :
				return 577059;
			case 575109 :
				return 577045;
			case 66053 :
				return 82949;
			case 66056 :
				return 82312;
			case 66054 :
				return 82310;
			case 66062 :
				return 82318;
			case 66077 :
				return 82333;
			case 66061 :
				return 82317;
			case 66058 :
				return 82314;
			case 66446 :
				return 82318;
			case 66461 :
				return 82333;
			case 66445 :
				return 82317;
			case 66442 :
				return 82314;
			case 65934 :
				return 82318;
			case 65949 :
				return 82333;
			case 65933 :
				return 82317;
			case 65930 :
				return 82314;
			case 72337 :
				return 154257 | DL;
			case 72330 :
				return 154249 | DL;
			case 72338 :
				return 154257 | DL;
			case 72339 :
				return 154256 | DL;
			case 72586 :
				return 154505 | DL;
			case 72594 :
				return 154513 | DL;
			case 72595 :
				return 154512 | DL;
			case 72842 :
				return 154761;
			case 72850 :
				return 154769;
			case 72851 :
				return 154768 | DL;
			case 72974 :
				return 236814;
			case 72989 :
				return 482589;
			case 72973 :
				return 220429;
			case 73239 :
				return 155160;
			case 73357 :
				return 466588;
			case 74263 :
				return 156184 | DL;
			case 75018 :
				return 353545;
			case 75026 :
				return 353553;
			case 75027 :
				return 353552 | DL;
			case 75402 :
				return 239241;
			case 75410 :
				return 239249 | DL;
			case 75411 :
				return 239248 | DL;
			case 75530 :
				return 239370;
			case 75538 :
				return 239378 | DL;
			case 75539 :
				return 239379 | DL;
			case 75667 :
				return 157584 | DL;
			case 66180 :
				return 82949;
			case 66183 :
				return 82311;
			case 66178 :
				return 82979;
			case 66179 :
				return 82307;
			case 66185 :
				return 82313;
			case 66188 :
				return 82316;
			case 66191 :
				return 82319;
			case 66196 :
				return 82708;
			case 66197 :
				return 82965;
			case 66202 :
				return 82714;
			case 66203 :
				return 82715;
			case 66204 :
				return 82332;
			case 66206 :
				return 82334;
			case 66207 :
				return 82335;
			case 66209 :
				return 82337;
			case 66211 :
				return 82979;
			case 66181 :
				return 82949;
			case 66184 :
				return 82312;
			case 66182 :
				return 82310;
			case 66190 :
				return 82318;
			case 66205 :
				return 82333;
			case 66189 :
				return 82317;
			case 66186 :
				return 82314;
			case 66564 :
				return 82949;
			case 66567 :
				return 82311;
			case 66562 :
				return 82979;
			case 66563 :
				return 82307;
			case 66569 :
				return 82313;
			case 66572 :
				return 82316;
			case 66575 :
				return 82319;
			case 66580 :
				return 82324;
			case 66581 :
				return 82965;
			case 66586 :
				return 82330;
			case 66587 :
				return 82331;
			case 66588 :
				return 82332;
			case 66590 :
				return 82334;
			case 66591 :
				return 82335;
			case 66593 :
				return 82337;
			case 66595 :
				return 82979;
			case 66565 :
				return 82949;
			case 66568 :
				return 82312;
			case 66566 :
				return 82310;
			case 66574 :
				return 82318;
			case 66589 :
				return 82333;
			case 66573 :
				return 82317;
			case 66570 :
				return 82314;
			case 66308 :
				return 82309;
			case 66311 :
				return 82311;
			case 66306 :
				return 82339;
			case 66307 :
				return 82307;
			case 66313 :
				return 82313;
			case 66316 :
				return 82316;
			case 66319 :
				return 82319;
			case 66324 :
				return 82708;
			case 66325 :
				return 82325;
			case 66330 :
				return 82714;
			case 66331 :
				return 82715;
			case 66332 :
				return 82332;
			case 66334 :
				return 82334;
			case 66335 :
				return 82335;
			case 66337 :
				return 82337;
			case 66339 :
				return 82339;
			case 66309 :
				return 82309;
			case 66312 :
				return 82312;
			case 66310 :
				return 82310;
			case 66318 :
				return 82318;
			case 66333 :
				return 82333;
			case 66317 :
				return 82317;
			case 66314 :
				return 82314;
			case 67332 :
				return 347797;
			case 67330 :
				return 347811;
			case 67348 :
				return 345620;
			case 67349 :
				return 347797;
			case 67354 :
				return 345626;
			case 67355 :
				return 345627;
			case 67363 :
				return 347811;
			case 67333 :
				return 347797;
			case 69252 :
				return 347797;
			case 69250 :
				return 347811;
			case 69269 :
				return 347797;
			case 69283 :
				return 347811;
			case 69253 :
				return 347797;
			case 67204 :
				return 347669;
			case 67202 :
				return 347683;
			case 67220 :
				return 345620;
			case 67221 :
				return 347669;
			case 67226 :
				return 345626;
			case 67227 :
				return 345627;
			case 67235 :
				return 347683;
			case 67205 :
				return 347669;
			case 124563 :
				return 239248 | DL;
			case 124691 :
				return 239379 | DL;
			case 39315 :
				return 530835 | DL;
			case 39571 :
				return 268944 | DL;
			case 39827 :
				return 285584 | DL;
			case 40083 :
				return 285840 | DL;
			case 40471 :
				return 269848 | DL;
			case 41111 :
				return 565398 | DL;
			case 41495 :
				return 270872 | DL;
			case 42135 :
				return 386199;
			case 82436 :
				return 82949;
			case 82439 :
				return 82311;
			case 82434 :
				return 82979;
			case 82435 :
				return 82307;
			case 82441 :
				return 82313;
			case 82444 :
				return 82316;
			case 82447 :
				return 82319;
			case 82452 :
				return 82708;
			case 82453 :
				return 82965;
			case 82458 :
				return 82714;
			case 82459 :
				return 82715;
			case 82460 :
				return 82332;
			case 82462 :
				return 82334;
			case 82463 :
				return 82335;
			case 82465 :
				return 82337;
			case 82467 :
				return 82979;
			case 82437 :
				return 82949;
			case 82440 :
				return 82312;
			case 82438 :
				return 82310;
			case 82446 :
				return 82318;
			case 82461 :
				return 82333;
			case 82445 :
				return 82317;
			case 82442 :
				return 82314;
			case 82820 :
				return 82949;
			case 82823 :
				return 82311;
			case 82818 :
				return 82979;
			case 82819 :
				return 82307;
			case 82825 :
				return 82313;
			case 82828 :
				return 82316;
			case 82831 :
				return 82319;
			case 82836 :
				return 82708;
			case 82837 :
				return 82965;
			case 82842 :
				return 82714;
			case 82843 :
				return 82715;
			case 82844 :
				return 82332;
			case 82846 :
				return 82334;
			case 82847 :
				return 82335;
			case 82849 :
				return 82337;
			case 82851 :
				return 82979;
			case 82821 :
				return 82949;
			case 82824 :
				return 82312;
			case 82822 :
				return 82310;
			case 82830 :
				return 82318;
			case 82845 :
				return 82333;
			case 82829 :
				return 82317;
			case 82826 :
				return 82314;
			case 82308 :
				return 82309;
			case 82311 :
				return 82311;
			case 82306 :
				return 82339;
			case 82307 :
				return 82307;
			case 82313 :
				return 82313;
			case 82316 :
				return 82316;
			case 82319 :
				return 82319;
			case 82324 :
				return 82324;
			case 82325 :
				return 82325;
			case 82330 :
				return 82330;
			case 82331 :
				return 82331;
			case 82332 :
				return 82332;
			case 82334 :
				return 82334;
			case 82335 :
				return 82335;
			case 82337 :
				return 82337;
			case 82339 :
				return 82339;
			case 82309 :
				return 82309;
			case 82312 :
				return 82312;
			case 82310 :
				return 82310;
			case 82318 :
				return 82318;
			case 82333 :
				return 82333;
			case 82317 :
				return 82317;
			case 82314 :
				return 82314;
			case 83348 :
				return 83348;
			case 83354 :
				return 83354;
			case 83355 :
				return 83355;
			case 83476 :
				return 345620;
			case 83482 :
				return 345626;
			case 83483 :
				return 345627;
			case 85508 :
				return 347669;
			case 85506 :
				return 347683;
			case 85525 :
				return 347669;
			case 85539 :
				return 347683;
			case 85509 :
				return 347669;
			case 85892 :
				return 495518;
			case 85918 :
				return 495518;
			case 85893 :
				return 495518;
			case 86148 :
				return 348309;
			case 86146 :
				return 348323;
			case 86165 :
				return 348309;
			case 86179 :
				return 348323;
			case 86149 :
				return 348309;
			case 88708 :
				return 154249 | DL;
			case 88706 :
				return 154257 | DL;
			case 88713 :
				return 154249 | DL;
			case 88720 :
				return 154256 | DL;
			case 88736 :
				return 154272 | DL;
			case 88709 :
				return 154249 | DL;
			case 88721 :
				return 154257 | DL;
			case 88714 :
				return 154249 | DL;
			case 88722 :
				return 154257 | DL;
			case 88723 :
				return 154256 | DL;
			case 88836 :
				return 350997;
			case 88853 :
				return 350997;
			case 88837 :
				return 350997;
			case 88964 :
				return 154505 | DL;
			case 88962 :
				return 154513 | DL;
			case 88969 :
				return 154505 | DL;
			case 88976 :
				return 154512 | DL;
			case 88992 :
				return 154528 | DL;
			case 88965 :
				return 154505 | DL;
			case 88977 :
				return 154513 | DL;
			case 88970 :
				return 154505 | DL;
			case 88978 :
				return 154513 | DL;
			case 88979 :
				return 154512 | DL;
			case 89220 :
				return 154761;
			case 89218 :
				return 154769;
			case 89225 :
				return 154761;
			case 89232 :
				return 154768 | DL;
			case 89248 :
				return 154784;
			case 89221 :
				return 154761;
			case 89233 :
				return 154769;
			case 89226 :
				return 154761;
			case 89234 :
				return 154769;
			case 89235 :
				return 154768 | DL;
			case 89348 :
				return 236814;
			case 89351 :
				return 236814;
			case 89356 :
				return 204044;
			case 89372 :
				return 466204;
			case 89377 :
				return 548129;
			case 89349 :
				return 236814;
			case 89352 :
				return 482589;
			case 89350 :
				return 204044;
			case 89358 :
				return 236814;
			case 89373 :
				return 482589;
			case 89357 :
				return 220429;
			case 89602 :
				return 155160;
			case 89624 :
				return 155160;
			case 89672 :
				return 155208;
			case 89623 :
				return 155160;
			case 89732 :
				return 482973;
			case 89735 :
				return 482973;
			case 89756 :
				return 466588;
			case 89761 :
				return 548513;
			case 89733 :
				return 482973;
			case 89736 :
				return 482973;
			case 89742 :
				return 482973;
			case 89757 :
				return 482973;
			case 89741 :
				return 466588;
			case 90244 :
				return 155801 | DL;
			case 90265 :
				return 155801 | DL;
			case 90312 :
				return 155848 | DL;
			case 90372 :
				return 352533;
			case 90389 :
				return 352533;
			case 90373 :
				return 352533;
			case 90626 :
				return 156184 | DL;
			case 90648 :
				return 156184 | DL;
			case 90696 :
				return 156232 | DL;
			case 90647 :
				return 156184 | DL;
			case 91396 :
				return 353545;
			case 91394 :
				return 353553;
			case 91401 :
				return 353545;
			case 91408 :
				return 353552 | DL;
			case 91424 :
				return 353568;
			case 91429 :
				return 58661;
			case 91430 :
				return 156966;
			case 91432 :
				return 206120;
			case 91433 :
				return 156969;
			case 91434 :
				return 238890;
			case 91435 :
				return 222507;
			case 91436 :
				return 468268;
			case 91437 :
				return 484653;
			case 91438 :
				return 501038;
			case 91441 :
				return 484657;
			case 91442 :
				return 550194;
			case 91462 :
				return 124230;
			case 91467 :
				return 157003;
			case 91468 :
				return 255308;
			case 91397 :
				return 353545;
			case 91409 :
				return 353553;
			case 91402 :
				return 353545;
			case 91410 :
				return 353553;
			case 91411 :
				return 353552 | DL;
			case 91780 :
				return 239241;
			case 91778 :
				return 239249 | DL;
			case 91785 :
				return 239241;
			case 91792 :
				return 239248 | DL;
			case 91808 :
				return 239264 | DL;
			case 91781 :
				return 239241;
			case 91793 :
				return 239249 | DL;
			case 91786 :
				return 239241;
			case 91794 :
				return 239249 | DL;
			case 91795 :
				return 239248 | DL;
			case 91908 :
				return 239370;
			case 91906 :
				return 239378 | DL;
			case 91913 :
				return 485129;
			case 91919 :
				return 206607;
			case 91920 :
				return 485136 | DL;
			case 91936 :
				return 485152 | DL;
			case 91938 :
				return 206626 | DL;
			case 91909 :
				return 239370;
			case 91921 :
				return 485137 | DL;
			case 91914 :
				return 239370;
			case 91922 :
				return 239378 | DL;
			case 91923 :
				return 239379 | DL;
			case 92036 :
				return 157577;
			case 92034 :
				return 157585;
			case 92041 :
				return 157577;
			case 92048 :
				return 157584 | DL;
			case 92064 :
				return 157600;
			case 92037 :
				return 157577;
			case 92049 :
				return 157585;
			case 92042 :
				return 157577;
			case 92050 :
				return 157585;
			case 92051 :
				return 157584 | DL;
			case 92164 :
				return 239630;
			case 92167 :
				return 239630;
			case 92172 :
				return 206860;
			case 92188 :
				return 469020;
			case 92193 :
				return 485409;
			case 92165 :
				return 239630;
			case 92168 :
				return 485405;
			case 92166 :
				return 206860;
			case 92174 :
				return 239630;
			case 92189 :
				return 485405;
			case 92173 :
				return 223245;
			case 82564 :
				return 82949;
			case 82567 :
				return 82311;
			case 82562 :
				return 82979;
			case 82563 :
				return 82307;
			case 82569 :
				return 82313;
			case 82572 :
				return 82316;
			case 82575 :
				return 82319;
			case 82580 :
				return 82708;
			case 82581 :
				return 82965;
			case 82586 :
				return 82714;
			case 82587 :
				return 82715;
			case 82588 :
				return 82332;
			case 82590 :
				return 82334;
			case 82591 :
				return 82335;
			case 82593 :
				return 82337;
			case 82595 :
				return 82979;
			case 82565 :
				return 82949;
			case 82568 :
				return 82312;
			case 82566 :
				return 82310;
			case 82574 :
				return 82318;
			case 82589 :
				return 82333;
			case 82573 :
				return 82317;
			case 82570 :
				return 82314;
			case 82948 :
				return 82949;
			case 82951 :
				return 82311;
			case 82946 :
				return 82979;
			case 82947 :
				return 82307;
			case 82953 :
				return 82313;
			case 82956 :
				return 82316;
			case 82959 :
				return 82319;
			case 82964 :
				return 82324;
			case 82965 :
				return 82965;
			case 82970 :
				return 82330;
			case 82971 :
				return 82331;
			case 82972 :
				return 82332;
			case 82974 :
				return 82334;
			case 82975 :
				return 82335;
			case 82977 :
				return 82337;
			case 82979 :
				return 82979;
			case 82949 :
				return 82949;
			case 82952 :
				return 82312;
			case 82950 :
				return 82310;
			case 82958 :
				return 82318;
			case 82973 :
				return 82333;
			case 82957 :
				return 82317;
			case 82954 :
				return 82314;
			case 82692 :
				return 82309;
			case 82695 :
				return 82311;
			case 82690 :
				return 82339;
			case 82691 :
				return 82307;
			case 82697 :
				return 82313;
			case 82700 :
				return 82316;
			case 82703 :
				return 82319;
			case 82708 :
				return 82708;
			case 82709 :
				return 82325;
			case 82714 :
				return 82714;
			case 82715 :
				return 82715;
			case 82716 :
				return 82332;
			case 82718 :
				return 82334;
			case 82719 :
				return 82335;
			case 82721 :
				return 82337;
			case 82723 :
				return 82339;
			case 82693 :
				return 82309;
			case 82696 :
				return 82312;
			case 82694 :
				return 82310;
			case 82702 :
				return 82318;
			case 82717 :
				return 82333;
			case 82701 :
				return 82317;
			case 82698 :
				return 82314;
			case 83716 :
				return 347797;
			case 83714 :
				return 347811;
			case 83732 :
				return 345620;
			case 83733 :
				return 347797;
			case 83738 :
				return 345626;
			case 83739 :
				return 345627;
			case 83747 :
				return 347811;
			case 83717 :
				return 347797;
			case 85636 :
				return 347797;
			case 85634 :
				return 347811;
			case 85653 :
				return 347797;
			case 85667 :
				return 347811;
			case 85637 :
				return 347797;
			case 83588 :
				return 347669;
			case 83586 :
				return 347683;
			case 83604 :
				return 345620;
			case 83605 :
				return 347669;
			case 83610 :
				return 345626;
			case 83611 :
				return 345627;
			case 83619 :
				return 347683;
			case 83589 :
				return 347669;
			case 131588 :
				return 131461;
			case 131591 :
				return 131463;
			case 131586 :
				return 131491;
			case 131587 :
				return 131459;
			case 131593 :
				return 131465;
			case 131596 :
				return 131468;
			case 131599 :
				return 131471;
			case 131604 :
				return 131476;
			case 131605 :
				return 131477;
			case 131610 :
				return 131482;
			case 131611 :
				return 131483;
			case 131612 :
				return 131484;
			case 131614 :
				return 131486;
			case 131615 :
				return 131487;
			case 131617 :
				return 131489;
			case 131619 :
				return 131491;
			case 131589 :
				return 131461;
			case 131592 :
				return 131464;
			case 131590 :
				return 131462;
			case 131598 :
				return 131470;
			case 131613 :
				return 131485;
			case 131597 :
				return 131469;
			case 131594 :
				return 131466;
			case 131972 :
				return 131461;
			case 131975 :
				return 131463;
			case 131970 :
				return 131491;
			case 131971 :
				return 131459;
			case 131977 :
				return 131465;
			case 131980 :
				return 131468;
			case 131983 :
				return 131471;
			case 131988 :
				return 131476;
			case 131989 :
				return 131477;
			case 131994 :
				return 131482;
			case 131995 :
				return 131483;
			case 131996 :
				return 131484;
			case 131998 :
				return 131486;
			case 131999 :
				return 131487;
			case 132001 :
				return 131489;
			case 132003 :
				return 131491;
			case 131973 :
				return 131461;
			case 131976 :
				return 131464;
			case 131974 :
				return 131462;
			case 131982 :
				return 131470;
			case 131997 :
				return 131485;
			case 131981 :
				return 131469;
			case 131978 :
				return 131466;
			case 131460 :
				return 131461;
			case 131463 :
				return 131463;
			case 131458 :
				return 131491;
			case 131459 :
				return 131459;
			case 131465 :
				return 131465;
			case 131468 :
				return 131468;
			case 131471 :
				return 131471;
			case 131476 :
				return 131476;
			case 131477 :
				return 131477;
			case 131482 :
				return 131482;
			case 131483 :
				return 131483;
			case 131484 :
				return 131484;
			case 131486 :
				return 131486;
			case 131487 :
				return 131487;
			case 131489 :
				return 131489;
			case 131491 :
				return 131491;
			case 131461 :
				return 131461;
			case 131464 :
				return 131464;
			case 131462 :
				return 131462;
			case 131470 :
				return 131470;
			case 131485 :
				return 131485;
			case 131469 :
				return 131469;
			case 131466 :
				return 131466;
			case 132500 :
				return 132500;
			case 132506 :
				return 132506;
			case 132507 :
				return 132507;
			case 138500 :
				return 482589;
			case 138503 :
				return 482589;
			case 138524 :
				return 466204;
			case 138529 :
				return 548129;
			case 138501 :
				return 482589;
			case 138504 :
				return 482589;
			case 138510 :
				return 482589;
			case 138525 :
				return 482589;
			case 138509 :
				return 466204;
			case 138884 :
				return 482973;
			case 138887 :
				return 482973;
			case 138908 :
				return 466588;
			case 138913 :
				return 548513;
			case 138885 :
				return 482973;
			case 138888 :
				return 482973;
			case 138894 :
				return 482973;
			case 138909 :
				return 482973;
			case 138893 :
				return 466588;
			case 140581 :
				return 58661;
			case 140586 :
				return 484650;
			case 140587 :
				return 468267;
			case 140588 :
				return 468268;
			case 140589 :
				return 484653;
			case 140593 :
				return 484657;
			case 140594 :
				return 550194;
			case 140614 :
				return 140614;
			case 140932 :
				return 485001;
			case 140930 :
				return 485009 | DL;
			case 140937 :
				return 485001;
			case 140944 :
				return 485008 | DL;
			case 140960 :
				return 485024 | DL;
			case 140933 :
				return 485001;
			case 140945 :
				return 485009 | DL;
			case 140938 :
				return 485001;
			case 140946 :
				return 485009 | DL;
			case 140947 :
				return 485008 | DL;
			case 141060 :
				return 485129;
			case 141058 :
				return 485137 | DL;
			case 141065 :
				return 485129;
			case 141072 :
				return 485136 | DL;
			case 141088 :
				return 485152 | DL;
			case 141061 :
				return 485129;
			case 141073 :
				return 485137 | DL;
			case 141066 :
				return 485129;
			case 141074 :
				return 485137 | DL;
			case 141075 :
				return 485136 | DL;
			case 141316 :
				return 485405;
			case 141319 :
				return 485405;
			case 141340 :
				return 469020;
			case 141345 :
				return 485409;
			case 141317 :
				return 485405;
			case 141320 :
				return 485405;
			case 141326 :
				return 485405;
			case 141341 :
				return 485405;
			case 141325 :
				return 469020;
			case 131716 :
				return 131461;
			case 131719 :
				return 131463;
			case 131714 :
				return 131491;
			case 131715 :
				return 131459;
			case 131721 :
				return 131465;
			case 131724 :
				return 131468;
			case 131727 :
				return 131471;
			case 131732 :
				return 131476;
			case 131733 :
				return 131477;
			case 131738 :
				return 131482;
			case 131739 :
				return 131483;
			case 131740 :
				return 131484;
			case 131742 :
				return 131486;
			case 131743 :
				return 131487;
			case 131745 :
				return 131489;
			case 131747 :
				return 131491;
			case 131717 :
				return 131461;
			case 131720 :
				return 131464;
			case 131718 :
				return 131462;
			case 131726 :
				return 131470;
			case 131741 :
				return 131485;
			case 131725 :
				return 131469;
			case 131722 :
				return 131466;
			case 132100 :
				return 131461;
			case 132103 :
				return 131463;
			case 132098 :
				return 131491;
			case 132099 :
				return 131459;
			case 132105 :
				return 131465;
			case 132108 :
				return 131468;
			case 132111 :
				return 131471;
			case 132116 :
				return 131476;
			case 132117 :
				return 131477;
			case 132122 :
				return 131482;
			case 132123 :
				return 131483;
			case 132124 :
				return 131484;
			case 132126 :
				return 131486;
			case 132127 :
				return 131487;
			case 132129 :
				return 131489;
			case 132131 :
				return 131491;
			case 132101 :
				return 131461;
			case 132104 :
				return 131464;
			case 132102 :
				return 131462;
			case 132110 :
				return 131470;
			case 132125 :
				return 131485;
			case 132109 :
				return 131469;
			case 132106 :
				return 131466;
			case 131844 :
				return 131461;
			case 131847 :
				return 131463;
			case 131842 :
				return 131491;
			case 131843 :
				return 131459;
			case 131849 :
				return 131465;
			case 131852 :
				return 131468;
			case 131855 :
				return 131471;
			case 131860 :
				return 131476;
			case 131861 :
				return 131477;
			case 131866 :
				return 131482;
			case 131867 :
				return 131483;
			case 131868 :
				return 131484;
			case 131870 :
				return 131486;
			case 131871 :
				return 131487;
			case 131873 :
				return 131489;
			case 131875 :
				return 131491;
			case 131845 :
				return 131461;
			case 131848 :
				return 131464;
			case 131846 :
				return 131462;
			case 131854 :
				return 131470;
			case 131869 :
				return 131485;
			case 131853 :
				return 131469;
			case 131850 :
				return 131466;
			case 98820 :
				return 98693;
			case 98823 :
				return 98695;
			case 98818 :
				return 98723;
			case 98819 :
				return 98691;
			case 98825 :
				return 98697;
			case 98828 :
				return 98700;
			case 98831 :
				return 98703;
			case 98836 :
				return 98708;
			case 98837 :
				return 98709;
			case 98842 :
				return 98714;
			case 98843 :
				return 98715;
			case 98844 :
				return 98716;
			case 98846 :
				return 98718;
			case 98847 :
				return 98719;
			case 98849 :
				return 98721;
			case 98851 :
				return 98723;
			case 98821 :
				return 98693;
			case 98824 :
				return 98696;
			case 98822 :
				return 98694;
			case 98830 :
				return 98702;
			case 98845 :
				return 98717;
			case 98829 :
				return 98701;
			case 98826 :
				return 98698;
			case 99204 :
				return 98693;
			case 99207 :
				return 98695;
			case 99202 :
				return 98723;
			case 99203 :
				return 98691;
			case 99209 :
				return 98697;
			case 99212 :
				return 98700;
			case 99215 :
				return 98703;
			case 99220 :
				return 98708;
			case 99221 :
				return 98709;
			case 99226 :
				return 98714;
			case 99227 :
				return 98715;
			case 99228 :
				return 98716;
			case 99230 :
				return 98718;
			case 99231 :
				return 98719;
			case 99233 :
				return 98721;
			case 99235 :
				return 98723;
			case 99205 :
				return 98693;
			case 99208 :
				return 98696;
			case 99206 :
				return 98694;
			case 99214 :
				return 98702;
			case 99229 :
				return 98717;
			case 99213 :
				return 98701;
			case 99210 :
				return 98698;
			case 98692 :
				return 98693;
			case 98695 :
				return 98695;
			case 98690 :
				return 98723;
			case 98691 :
				return 98691;
			case 98697 :
				return 98697;
			case 98700 :
				return 98700;
			case 98703 :
				return 98703;
			case 98708 :
				return 98708;
			case 98709 :
				return 98709;
			case 98714 :
				return 98714;
			case 98715 :
				return 98715;
			case 98716 :
				return 98716;
			case 98718 :
				return 98718;
			case 98719 :
				return 98719;
			case 98721 :
				return 98721;
			case 98723 :
				return 98723;
			case 98693 :
				return 98693;
			case 98696 :
				return 98696;
			case 98694 :
				return 98694;
			case 98702 :
				return 98702;
			case 98717 :
				return 98717;
			case 98701 :
				return 98701;
			case 98698 :
				return 98698;
			case 99732 :
				return 99732;
			case 99738 :
				return 99738;
			case 99739 :
				return 99739;
			case 105732 :
				return 204044;
			case 105735 :
				return 204044;
			case 105740 :
				return 204044;
			case 105733 :
				return 204044;
			case 105734 :
				return 204044;
			case 105742 :
				return 204044;
			case 105741 :
				return 204044;
			case 107813 :
				return 58661;
			case 107816 :
				return 206120;
			case 107818 :
				return 206122;
			case 107819 :
				return 206123;
			case 107846 :
				return 107846;
			case 108164 :
				return 206473;
			case 108162 :
				return 206481 | DL;
			case 108169 :
				return 206473;
			case 108176 :
				return 206480 | DL;
			case 108192 :
				return 206496 | DL;
			case 108165 :
				return 206473;
			case 108177 :
				return 206481 | DL;
			case 108170 :
				return 206473;
			case 108178 :
				return 206481 | DL;
			case 108179 :
				return 206480 | DL;
			case 108292 :
				return 206607;
			case 108290 :
				return 206626 | DL;
			case 108303 :
				return 206607;
			case 108322 :
				return 206626 | DL;
			case 108293 :
				return 206607;
			case 108298 :
				return 206607;
			case 108306 :
				return 206626 | DL;
			case 108307 :
				return 206626 | DL;
			case 108548 :
				return 206860;
			case 108551 :
				return 206860;
			case 108556 :
				return 206860;
			case 108549 :
				return 206860;
			case 108550 :
				return 206860;
			case 108558 :
				return 206860;
			case 108557 :
				return 206860;
			case 98948 :
				return 98693;
			case 98951 :
				return 98695;
			case 98946 :
				return 98723;
			case 98947 :
				return 98691;
			case 98953 :
				return 98697;
			case 98956 :
				return 98700;
			case 98959 :
				return 98703;
			case 98964 :
				return 98708;
			case 98965 :
				return 98709;
			case 98970 :
				return 98714;
			case 98971 :
				return 98715;
			case 98972 :
				return 98716;
			case 98974 :
				return 98718;
			case 98975 :
				return 98719;
			case 98977 :
				return 98721;
			case 98979 :
				return 98723;
			case 98949 :
				return 98693;
			case 98952 :
				return 98696;
			case 98950 :
				return 98694;
			case 98958 :
				return 98702;
			case 98973 :
				return 98717;
			case 98957 :
				return 98701;
			case 98954 :
				return 98698;
			case 99332 :
				return 98693;
			case 99335 :
				return 98695;
			case 99330 :
				return 98723;
			case 99331 :
				return 98691;
			case 99337 :
				return 98697;
			case 99340 :
				return 98700;
			case 99343 :
				return 98703;
			case 99348 :
				return 98708;
			case 99349 :
				return 98709;
			case 99354 :
				return 98714;
			case 99355 :
				return 98715;
			case 99356 :
				return 98716;
			case 99358 :
				return 98718;
			case 99359 :
				return 98719;
			case 99361 :
				return 98721;
			case 99363 :
				return 98723;
			case 99333 :
				return 98693;
			case 99336 :
				return 98696;
			case 99334 :
				return 98694;
			case 99342 :
				return 98702;
			case 99357 :
				return 98717;
			case 99341 :
				return 98701;
			case 99338 :
				return 98698;
			case 99076 :
				return 98693;
			case 99079 :
				return 98695;
			case 99074 :
				return 98723;
			case 99075 :
				return 98691;
			case 99081 :
				return 98697;
			case 99084 :
				return 98700;
			case 99087 :
				return 98703;
			case 99092 :
				return 98708;
			case 99093 :
				return 98709;
			case 99098 :
				return 98714;
			case 99099 :
				return 98715;
			case 99100 :
				return 98716;
			case 99102 :
				return 98718;
			case 99103 :
				return 98719;
			case 99105 :
				return 98721;
			case 99107 :
				return 98723;
			case 99077 :
				return 98693;
			case 99080 :
				return 98696;
			case 99078 :
				return 98694;
			case 99086 :
				return 98702;
			case 99101 :
				return 98717;
			case 99085 :
				return 98701;
			case 99082 :
				return 98698;
			case 285060 :
				return 530826;
			case 285058 :
				return 530834 | DL;
			case 285065 :
				return 530825;
			case 285071 :
				return 530831;
			case 285072 :
				return 530832 | DL;
			case 285088 :
				return 530848 | DL;
			case 285090 :
				return 530850 | DL;
			case 285061 :
				return 530826;
			case 285073 :
				return 530833 | DL;
			case 285066 :
				return 530826;
			case 285074 :
				return 530834 | DL;
			case 285075 :
				return 530835 | DL;
			case 285204 :
				return 530964 | DL;
			case 285210 :
				return 530970;
			case 285316 :
				return 268937 | DL;
			case 285314 :
				return 268945 | DL;
			case 285321 :
				return 268937 | DL;
			case 285328 :
				return 268944 | DL;
			case 285344 :
				return 268960 | DL;
			case 285317 :
				return 268937 | DL;
			case 285329 :
				return 268945 | DL;
			case 285322 :
				return 268937 | DL;
			case 285330 :
				return 268945 | DL;
			case 285331 :
				return 268944 | DL;
			case 285572 :
				return 285577 | DL;
			case 285570 :
				return 285585 | DL;
			case 285577 :
				return 285577 | DL;
			case 285584 :
				return 285584 | DL;
			case 285600 :
				return 285600 | DL;
			case 285573 :
				return 285577 | DL;
			case 285585 :
				return 285585 | DL;
			case 285578 :
				return 285577 | DL;
			case 285586 :
				return 285585 | DL;
			case 285587 :
				return 285584 | DL;
			case 285828 :
				return 285833 | DL;
			case 285826 :
				return 285841 | DL;
			case 285833 :
				return 285833 | DL;
			case 285840 :
				return 285840 | DL;
			case 285856 :
				return 285856 | DL;
			case 285829 :
				return 285833 | DL;
			case 285841 :
				return 285841 | DL;
			case 285834 :
				return 285833 | DL;
			case 285842 :
				return 285841 | DL;
			case 285843 :
				return 285840 | DL;
			case 286084 :
				return 531861 | DL;
			case 286100 :
				return 531860 | DL;
			case 286101 :
				return 531861 | DL;
			case 286106 :
				return 531866 | DL;
			case 286107 :
				return 531867 | DL;
			case 286085 :
				return 531861 | DL;
			case 286210 :
				return 269848 | DL;
			case 286232 :
				return 269848 | DL;
			case 286280 :
				return 269896 | DL;
			case 286231 :
				return 269848 | DL;
			case 286484 :
				return 532244 | DL;
			case 286490 :
				return 532250;
			case 286612 :
				return 532372 | DL;
			case 286618 :
				return 532378;
			case 286724 :
				return 532494;
			case 286727 :
				return 532494;
			case 286732 :
				return 532492;
			case 286748 :
				return 532508;
			case 286753 :
				return 532513;
			case 286725 :
				return 532494;
			case 286728 :
				return 532509;
			case 286726 :
				return 532492;
			case 286734 :
				return 532494;
			case 286749 :
				return 532509;
			case 286733 :
				return 532493;
			case 286852 :
				return 270489 | DL;
			case 286873 :
				return 270489 | DL;
			case 286920 :
				return 270536 | DL;
			case 287108 :
				return 532874;
			case 287106 :
				return 532882 | DL;
			case 287113 :
				return 532873;
			case 287119 :
				return 532879;
			case 287120 :
				return 532880 | DL;
			case 287136 :
				return 532896 | DL;
			case 287138 :
				return 532898 | DL;
			case 287109 :
				return 532874;
			case 287121 :
				return 532881 | DL;
			case 287114 :
				return 532874;
			case 287122 :
				return 532882 | DL;
			case 287123 :
				return 532883 | DL;
			case 287234 :
				return 270872 | DL;
			case 287256 :
				return 270872 | DL;
			case 287304 :
				return 270920 | DL;
			case 287255 :
				return 270872 | DL;
			case 288038 :
				return 271654 | DL;
			case 288048 :
				return 533808;
			case 288075 :
				return 288075;
			case 288644 :
				return 288649 | DL;
			case 288642 :
				return 288657 | DL;
			case 288649 :
				return 288649 | DL;
			case 288656 :
				return 288656 | DL;
			case 288672 :
				return 288672 | DL;
			case 288645 :
				return 288649 | DL;
			case 288657 :
				return 288657 | DL;
			case 288650 :
				return 288649 | DL;
			case 288658 :
				return 288657 | DL;
			case 288659 :
				return 288656 | DL;
			case 229892 :
				return 229765;
			case 229895 :
				return 229767;
			case 229890 :
				return 229795;
			case 229891 :
				return 229763;
			case 229897 :
				return 229769;
			case 229900 :
				return 229772;
			case 229903 :
				return 229775;
			case 229908 :
				return 229780;
			case 229909 :
				return 229781;
			case 229914 :
				return 229786;
			case 229915 :
				return 229787;
			case 229916 :
				return 229788;
			case 229918 :
				return 229790;
			case 229919 :
				return 229791;
			case 229921 :
				return 229793;
			case 229923 :
				return 229795;
			case 229893 :
				return 229765;
			case 229896 :
				return 229768;
			case 229894 :
				return 229766;
			case 229902 :
				return 229774;
			case 229917 :
				return 229789;
			case 229901 :
				return 229773;
			case 229898 :
				return 229770;
			case 230276 :
				return 229765;
			case 230279 :
				return 229767;
			case 230274 :
				return 229795;
			case 230275 :
				return 229763;
			case 230281 :
				return 229769;
			case 230284 :
				return 229772;
			case 230287 :
				return 229775;
			case 230292 :
				return 229780;
			case 230293 :
				return 229781;
			case 230298 :
				return 229786;
			case 230299 :
				return 229787;
			case 230300 :
				return 229788;
			case 230302 :
				return 229790;
			case 230303 :
				return 229791;
			case 230305 :
				return 229793;
			case 230307 :
				return 229795;
			case 230277 :
				return 229765;
			case 230280 :
				return 229768;
			case 230278 :
				return 229766;
			case 230286 :
				return 229774;
			case 230301 :
				return 229789;
			case 230285 :
				return 229773;
			case 230282 :
				return 229770;
			case 229764 :
				return 229765;
			case 229767 :
				return 229767;
			case 229762 :
				return 229795;
			case 229763 :
				return 229763;
			case 229769 :
				return 229769;
			case 229772 :
				return 229772;
			case 229775 :
				return 229775;
			case 229780 :
				return 229780;
			case 229781 :
				return 229781;
			case 229786 :
				return 229786;
			case 229787 :
				return 229787;
			case 229788 :
				return 229788;
			case 229790 :
				return 229790;
			case 229791 :
				return 229791;
			case 229793 :
				return 229793;
			case 229795 :
				return 229795;
			case 229765 :
				return 229765;
			case 229768 :
				return 229768;
			case 229766 :
				return 229766;
			case 229774 :
				return 229774;
			case 229789 :
				return 229789;
			case 229773 :
				return 229773;
			case 229770 :
				return 229770;
			case 230804 :
				return 230804;
			case 230810 :
				return 230810;
			case 230811 :
				return 230811;
			case 236804 :
				return 236814;
			case 236807 :
				return 236814;
			case 236812 :
				return 204044;
			case 236828 :
				return 466204;
			case 236833 :
				return 548129;
			case 236805 :
				return 236814;
			case 236808 :
				return 482589;
			case 236806 :
				return 204044;
			case 236814 :
				return 236814;
			case 236829 :
				return 482589;
			case 236813 :
				return 220429;
			case 237188 :
				return 482973;
			case 237191 :
				return 482973;
			case 237212 :
				return 466588;
			case 237217 :
				return 548513;
			case 237189 :
				return 482973;
			case 237192 :
				return 482973;
			case 237198 :
				return 482973;
			case 237213 :
				return 482973;
			case 237197 :
				return 466588;
			case 238888 :
				return 206120;
			case 238890 :
				return 238890;
			case 238891 :
				return 222507;
			case 238892 :
				return 468268;
			case 238893 :
				return 484653;
			case 238897 :
				return 484657;
			case 238898 :
				return 550194;
			case 238918 :
				return 238918;
			case 239236 :
				return 239241;
			case 239234 :
				return 239249 | DL;
			case 239241 :
				return 239241;
			case 239248 :
				return 239248 | DL;
			case 239264 :
				return 239264 | DL;
			case 239237 :
				return 239241;
			case 239249 :
				return 239249 | DL;
			case 239242 :
				return 239241;
			case 239250 :
				return 239249 | DL;
			case 239251 :
				return 239248 | DL;
			case 239364 :
				return 239370;
			case 239362 :
				return 239378 | DL;
			case 239369 :
				return 485129;
			case 239375 :
				return 206607;
			case 239376 :
				return 485136 | DL;
			case 239392 :
				return 485152 | DL;
			case 239394 :
				return 206626 | DL;
			case 239365 :
				return 239370;
			case 239377 :
				return 485137 | DL;
			case 239370 :
				return 239370;
			case 239378 :
				return 239378 | DL;
			case 239379 :
				return 239379 | DL;
			case 239620 :
				return 239630;
			case 239623 :
				return 239630;
			case 239628 :
				return 206860;
			case 239644 :
				return 469020;
			case 239649 :
				return 485409;
			case 239621 :
				return 239630;
			case 239624 :
				return 485405;
			case 239622 :
				return 206860;
			case 239630 :
				return 239630;
			case 239645 :
				return 485405;
			case 239629 :
				return 223245;
			case 230020 :
				return 229765;
			case 230023 :
				return 229767;
			case 230018 :
				return 229795;
			case 230019 :
				return 229763;
			case 230025 :
				return 229769;
			case 230028 :
				return 229772;
			case 230031 :
				return 229775;
			case 230036 :
				return 229780;
			case 230037 :
				return 229781;
			case 230042 :
				return 229786;
			case 230043 :
				return 229787;
			case 230044 :
				return 229788;
			case 230046 :
				return 229790;
			case 230047 :
				return 229791;
			case 230049 :
				return 229793;
			case 230051 :
				return 229795;
			case 230021 :
				return 229765;
			case 230024 :
				return 229768;
			case 230022 :
				return 229766;
			case 230030 :
				return 229774;
			case 230045 :
				return 229789;
			case 230029 :
				return 229773;
			case 230026 :
				return 229770;
			case 230404 :
				return 229765;
			case 230407 :
				return 229767;
			case 230402 :
				return 229795;
			case 230403 :
				return 229763;
			case 230409 :
				return 229769;
			case 230412 :
				return 229772;
			case 230415 :
				return 229775;
			case 230420 :
				return 229780;
			case 230421 :
				return 229781;
			case 230426 :
				return 229786;
			case 230427 :
				return 229787;
			case 230428 :
				return 229788;
			case 230430 :
				return 229790;
			case 230431 :
				return 229791;
			case 230433 :
				return 229793;
			case 230435 :
				return 229795;
			case 230405 :
				return 229765;
			case 230408 :
				return 229768;
			case 230406 :
				return 229766;
			case 230414 :
				return 229774;
			case 230429 :
				return 229789;
			case 230413 :
				return 229773;
			case 230410 :
				return 229770;
			case 230148 :
				return 229765;
			case 230151 :
				return 229767;
			case 230146 :
				return 229795;
			case 230147 :
				return 229763;
			case 230153 :
				return 229769;
			case 230156 :
				return 229772;
			case 230159 :
				return 229775;
			case 230164 :
				return 229780;
			case 230165 :
				return 229781;
			case 230170 :
				return 229786;
			case 230171 :
				return 229787;
			case 230172 :
				return 229788;
			case 230174 :
				return 229790;
			case 230175 :
				return 229791;
			case 230177 :
				return 229793;
			case 230179 :
				return 229795;
			case 230149 :
				return 229765;
			case 230152 :
				return 229768;
			case 230150 :
				return 229766;
			case 230158 :
				return 229774;
			case 230173 :
				return 229789;
			case 230157 :
				return 229773;
			case 230154 :
				return 229770;
			case 475652 :
				return 475525;
			case 475655 :
				return 475527;
			case 475650 :
				return 475555;
			case 475651 :
				return 475523;
			case 475657 :
				return 475529;
			case 475660 :
				return 475532;
			case 475663 :
				return 475535;
			case 475668 :
				return 475540;
			case 475669 :
				return 475541;
			case 475674 :
				return 475546;
			case 475675 :
				return 475547;
			case 475676 :
				return 475548;
			case 475678 :
				return 475550;
			case 475679 :
				return 475551;
			case 475681 :
				return 475553;
			case 475683 :
				return 475555;
			case 475653 :
				return 475525;
			case 475656 :
				return 475528;
			case 475654 :
				return 475526;
			case 475662 :
				return 475534;
			case 475677 :
				return 475549;
			case 475661 :
				return 475533;
			case 475658 :
				return 475530;
			case 476036 :
				return 475525;
			case 476039 :
				return 475527;
			case 476034 :
				return 475555;
			case 476035 :
				return 475523;
			case 476041 :
				return 475529;
			case 476044 :
				return 475532;
			case 476047 :
				return 475535;
			case 476052 :
				return 475540;
			case 476053 :
				return 475541;
			case 476058 :
				return 475546;
			case 476059 :
				return 475547;
			case 476060 :
				return 475548;
			case 476062 :
				return 475550;
			case 476063 :
				return 475551;
			case 476065 :
				return 475553;
			case 476067 :
				return 475555;
			case 476037 :
				return 475525;
			case 476040 :
				return 475528;
			case 476038 :
				return 475526;
			case 476046 :
				return 475534;
			case 476061 :
				return 475549;
			case 476045 :
				return 475533;
			case 476042 :
				return 475530;
			case 475524 :
				return 475525;
			case 475527 :
				return 475527;
			case 475522 :
				return 475555;
			case 475523 :
				return 475523;
			case 475529 :
				return 475529;
			case 475532 :
				return 475532;
			case 475535 :
				return 475535;
			case 475540 :
				return 475540;
			case 475541 :
				return 475541;
			case 475546 :
				return 475546;
			case 475547 :
				return 475547;
			case 475548 :
				return 475548;
			case 475550 :
				return 475550;
			case 475551 :
				return 475551;
			case 475553 :
				return 475553;
			case 475555 :
				return 475555;
			case 475525 :
				return 475525;
			case 475528 :
				return 475528;
			case 475526 :
				return 475526;
			case 475534 :
				return 475534;
			case 475549 :
				return 475549;
			case 475533 :
				return 475533;
			case 475530 :
				return 475530;
			case 476564 :
				return 476564;
			case 476570 :
				return 476570;
			case 476571 :
				return 476571;
			case 482564 :
				return 482589;
			case 482567 :
				return 482589;
			case 482588 :
				return 466204;
			case 482593 :
				return 548129;
			case 482565 :
				return 482589;
			case 482568 :
				return 482589;
			case 482574 :
				return 482589;
			case 482589 :
				return 482589;
			case 482573 :
				return 466204;
			case 482948 :
				return 482973;
			case 482951 :
				return 482973;
			case 482972 :
				return 466588;
			case 482977 :
				return 548513;
			case 482949 :
				return 482973;
			case 482952 :
				return 482973;
			case 482958 :
				return 482973;
			case 482973 :
				return 482973;
			case 482957 :
				return 466588;
			case 484650 :
				return 484650;
			case 484651 :
				return 468267;
			case 484652 :
				return 468268;
			case 484653 :
				return 484653;
			case 484657 :
				return 484657;
			case 484658 :
				return 550194;
			case 484678 :
				return 484678;
			case 484996 :
				return 485001;
			case 484994 :
				return 485009 | DL;
			case 485001 :
				return 485001;
			case 485008 :
				return 485008 | DL;
			case 485024 :
				return 485024 | DL;
			case 484997 :
				return 485001;
			case 485009 :
				return 485009 | DL;
			case 485002 :
				return 485001;
			case 485010 :
				return 485009 | DL;
			case 485011 :
				return 485008 | DL;
			case 485124 :
				return 485129;
			case 485122 :
				return 485137 | DL;
			case 485129 :
				return 485129;
			case 485136 :
				return 485136 | DL;
			case 485152 :
				return 485152 | DL;
			case 485125 :
				return 485129;
			case 485137 :
				return 485137 | DL;
			case 485130 :
				return 485129;
			case 485138 :
				return 485137 | DL;
			case 485139 :
				return 485136 | DL;
			case 485380 :
				return 485405;
			case 485383 :
				return 485405;
			case 485404 :
				return 469020;
			case 485409 :
				return 485409;
			case 485381 :
				return 485405;
			case 485384 :
				return 485405;
			case 485390 :
				return 485405;
			case 485405 :
				return 485405;
			case 485389 :
				return 469020;
			case 475780 :
				return 475525;
			case 475783 :
				return 475527;
			case 475778 :
				return 475555;
			case 475779 :
				return 475523;
			case 475785 :
				return 475529;
			case 475788 :
				return 475532;
			case 475791 :
				return 475535;
			case 475796 :
				return 475540;
			case 475797 :
				return 475541;
			case 475802 :
				return 475546;
			case 475803 :
				return 475547;
			case 475804 :
				return 475548;
			case 475806 :
				return 475550;
			case 475807 :
				return 475551;
			case 475809 :
				return 475553;
			case 475811 :
				return 475555;
			case 475781 :
				return 475525;
			case 475784 :
				return 475528;
			case 475782 :
				return 475526;
			case 475790 :
				return 475534;
			case 475805 :
				return 475549;
			case 475789 :
				return 475533;
			case 475786 :
				return 475530;
			case 476164 :
				return 475525;
			case 476167 :
				return 475527;
			case 476162 :
				return 475555;
			case 476163 :
				return 475523;
			case 476169 :
				return 475529;
			case 476172 :
				return 475532;
			case 476175 :
				return 475535;
			case 476180 :
				return 475540;
			case 476181 :
				return 475541;
			case 476186 :
				return 475546;
			case 476187 :
				return 475547;
			case 476188 :
				return 475548;
			case 476190 :
				return 475550;
			case 476191 :
				return 475551;
			case 476193 :
				return 475553;
			case 476195 :
				return 475555;
			case 476165 :
				return 475525;
			case 476168 :
				return 475528;
			case 476166 :
				return 475526;
			case 476174 :
				return 475534;
			case 476189 :
				return 475549;
			case 476173 :
				return 475533;
			case 476170 :
				return 475530;
			case 475908 :
				return 475525;
			case 475911 :
				return 475527;
			case 475906 :
				return 475555;
			case 475907 :
				return 475523;
			case 475913 :
				return 475529;
			case 475916 :
				return 475532;
			case 475919 :
				return 475535;
			case 475924 :
				return 475540;
			case 475925 :
				return 475541;
			case 475930 :
				return 475546;
			case 475931 :
				return 475547;
			case 475932 :
				return 475548;
			case 475934 :
				return 475550;
			case 475935 :
				return 475551;
			case 475937 :
				return 475553;
			case 475939 :
				return 475555;
			case 475909 :
				return 475525;
			case 475912 :
				return 475528;
			case 475910 :
				return 475526;
			case 475918 :
				return 475534;
			case 475933 :
				return 475549;
			case 475917 :
				return 475533;
			case 475914 :
				return 475530;
			case 213508 :
				return 213381;
			case 213511 :
				return 213383;
			case 213506 :
				return 213411;
			case 213507 :
				return 213379;
			case 213513 :
				return 213385;
			case 213516 :
				return 213388;
			case 213519 :
				return 213391;
			case 213524 :
				return 213396;
			case 213525 :
				return 213397;
			case 213530 :
				return 213402;
			case 213531 :
				return 213403;
			case 213532 :
				return 213404;
			case 213534 :
				return 213406;
			case 213535 :
				return 213407;
			case 213537 :
				return 213409;
			case 213539 :
				return 213411;
			case 213509 :
				return 213381;
			case 213512 :
				return 213384;
			case 213510 :
				return 213382;
			case 213518 :
				return 213390;
			case 213533 :
				return 213405;
			case 213517 :
				return 213389;
			case 213514 :
				return 213386;
			case 213892 :
				return 213381;
			case 213895 :
				return 213383;
			case 213890 :
				return 213411;
			case 213891 :
				return 213379;
			case 213897 :
				return 213385;
			case 213900 :
				return 213388;
			case 213903 :
				return 213391;
			case 213908 :
				return 213396;
			case 213909 :
				return 213397;
			case 213914 :
				return 213402;
			case 213915 :
				return 213403;
			case 213916 :
				return 213404;
			case 213918 :
				return 213406;
			case 213919 :
				return 213407;
			case 213921 :
				return 213409;
			case 213923 :
				return 213411;
			case 213893 :
				return 213381;
			case 213896 :
				return 213384;
			case 213894 :
				return 213382;
			case 213902 :
				return 213390;
			case 213917 :
				return 213405;
			case 213901 :
				return 213389;
			case 213898 :
				return 213386;
			case 213380 :
				return 213381;
			case 213383 :
				return 213383;
			case 213378 :
				return 213411;
			case 213379 :
				return 213379;
			case 213385 :
				return 213385;
			case 213388 :
				return 213388;
			case 213391 :
				return 213391;
			case 213396 :
				return 213396;
			case 213397 :
				return 213397;
			case 213402 :
				return 213402;
			case 213403 :
				return 213403;
			case 213404 :
				return 213404;
			case 213406 :
				return 213406;
			case 213407 :
				return 213407;
			case 213409 :
				return 213409;
			case 213411 :
				return 213411;
			case 213381 :
				return 213381;
			case 213384 :
				return 213384;
			case 213382 :
				return 213382;
			case 213390 :
				return 213390;
			case 213405 :
				return 213405;
			case 213389 :
				return 213389;
			case 213386 :
				return 213386;
			case 214420 :
				return 214420;
			case 214426 :
				return 214426;
			case 214427 :
				return 214427;
			case 220420 :
				return 220429;
			case 220423 :
				return 220429;
			case 220428 :
				return 204044;
			case 220444 :
				return 466204;
			case 220421 :
				return 220429;
			case 220424 :
				return 466204;
			case 220422 :
				return 204044;
			case 220430 :
				return 220429;
			case 220445 :
				return 466204;
			case 220429 :
				return 220429;
			case 220804 :
				return 466588;
			case 220807 :
				return 466588;
			case 220828 :
				return 466588;
			case 220805 :
				return 466588;
			case 220808 :
				return 466588;
			case 220814 :
				return 466588;
			case 220829 :
				return 466588;
			case 220813 :
				return 466588;
			case 222504 :
				return 206120;
			case 222506 :
				return 222506;
			case 222507 :
				return 222507;
			case 222508 :
				return 468268;
			case 222509 :
				return 468269;
			case 222513 :
				return 468273;
			case 222534 :
				return 222534;
			case 222852 :
				return 222857;
			case 222850 :
				return 222865 | DL;
			case 222857 :
				return 222857;
			case 222864 :
				return 222864 | DL;
			case 222880 :
				return 222880 | DL;
			case 222853 :
				return 222857;
			case 222865 :
				return 222865 | DL;
			case 222858 :
				return 222857;
			case 222866 :
				return 222865 | DL;
			case 222867 :
				return 222864 | DL;
			case 222980 :
				return 222986;
			case 222978 :
				return 222994 | DL;
			case 222985 :
				return 468745;
			case 222991 :
				return 206607;
			case 222992 :
				return 468752 | DL;
			case 223008 :
				return 468768 | DL;
			case 223010 :
				return 206626 | DL;
			case 222981 :
				return 222986;
			case 222993 :
				return 468753 | DL;
			case 222986 :
				return 222986;
			case 222994 :
				return 222994 | DL;
			case 222995 :
				return 222995 | DL;
			case 223236 :
				return 223246;
			case 223239 :
				return 223246;
			case 223244 :
				return 206860;
			case 223260 :
				return 469020;
			case 223265 :
				return 469025;
			case 223237 :
				return 223246;
			case 223240 :
				return 469021;
			case 223238 :
				return 206860;
			case 223246 :
				return 223246;
			case 223261 :
				return 469021;
			case 223245 :
				return 223245;
			case 213636 :
				return 213381;
			case 213639 :
				return 213383;
			case 213634 :
				return 213411;
			case 213635 :
				return 213379;
			case 213641 :
				return 213385;
			case 213644 :
				return 213388;
			case 213647 :
				return 213391;
			case 213652 :
				return 213396;
			case 213653 :
				return 213397;
			case 213658 :
				return 213402;
			case 213659 :
				return 213403;
			case 213660 :
				return 213404;
			case 213662 :
				return 213406;
			case 213663 :
				return 213407;
			case 213665 :
				return 213409;
			case 213667 :
				return 213411;
			case 213637 :
				return 213381;
			case 213640 :
				return 213384;
			case 213638 :
				return 213382;
			case 213646 :
				return 213390;
			case 213661 :
				return 213405;
			case 213645 :
				return 213389;
			case 213642 :
				return 213386;
			case 214020 :
				return 213381;
			case 214023 :
				return 213383;
			case 214018 :
				return 213411;
			case 214019 :
				return 213379;
			case 214025 :
				return 213385;
			case 214028 :
				return 213388;
			case 214031 :
				return 213391;
			case 214036 :
				return 213396;
			case 214037 :
				return 213397;
			case 214042 :
				return 213402;
			case 214043 :
				return 213403;
			case 214044 :
				return 213404;
			case 214046 :
				return 213406;
			case 214047 :
				return 213407;
			case 214049 :
				return 213409;
			case 214051 :
				return 213411;
			case 214021 :
				return 213381;
			case 214024 :
				return 213384;
			case 214022 :
				return 213382;
			case 214030 :
				return 213390;
			case 214045 :
				return 213405;
			case 214029 :
				return 213389;
			case 214026 :
				return 213386;
			case 213764 :
				return 213381;
			case 213767 :
				return 213383;
			case 213762 :
				return 213411;
			case 213763 :
				return 213379;
			case 213769 :
				return 213385;
			case 213772 :
				return 213388;
			case 213775 :
				return 213391;
			case 213780 :
				return 213396;
			case 213781 :
				return 213397;
			case 213786 :
				return 213402;
			case 213787 :
				return 213403;
			case 213788 :
				return 213404;
			case 213790 :
				return 213406;
			case 213791 :
				return 213407;
			case 213793 :
				return 213409;
			case 213795 :
				return 213411;
			case 213765 :
				return 213381;
			case 213768 :
				return 213384;
			case 213766 :
				return 213382;
			case 213774 :
				return 213390;
			case 213789 :
				return 213405;
			case 213773 :
				return 213389;
			case 213770 :
				return 213386;
			case 164356 :
				return 164229;
			case 164359 :
				return 164231;
			case 164354 :
				return 164259;
			case 164355 :
				return 164227;
			case 164361 :
				return 164233;
			case 164364 :
				return 164236;
			case 164367 :
				return 164239;
			case 164372 :
				return 164244;
			case 164373 :
				return 164245;
			case 164378 :
				return 164250;
			case 164379 :
				return 164251;
			case 164380 :
				return 164252;
			case 164382 :
				return 164254;
			case 164383 :
				return 164255;
			case 164385 :
				return 164257;
			case 164387 :
				return 164259;
			case 164357 :
				return 164229;
			case 164360 :
				return 164232;
			case 164358 :
				return 164230;
			case 164366 :
				return 164238;
			case 164381 :
				return 164253;
			case 164365 :
				return 164237;
			case 164362 :
				return 164234;
			case 164740 :
				return 164229;
			case 164743 :
				return 164231;
			case 164738 :
				return 164259;
			case 164739 :
				return 164227;
			case 164745 :
				return 164233;
			case 164748 :
				return 164236;
			case 164751 :
				return 164239;
			case 164756 :
				return 164244;
			case 164757 :
				return 164245;
			case 164762 :
				return 164250;
			case 164763 :
				return 164251;
			case 164764 :
				return 164252;
			case 164766 :
				return 164254;
			case 164767 :
				return 164255;
			case 164769 :
				return 164257;
			case 164771 :
				return 164259;
			case 164741 :
				return 164229;
			case 164744 :
				return 164232;
			case 164742 :
				return 164230;
			case 164750 :
				return 164238;
			case 164765 :
				return 164253;
			case 164749 :
				return 164237;
			case 164746 :
				return 164234;
			case 164228 :
				return 164229;
			case 164231 :
				return 164231;
			case 164226 :
				return 164259;
			case 164227 :
				return 164227;
			case 164233 :
				return 164233;
			case 164236 :
				return 164236;
			case 164239 :
				return 164239;
			case 164244 :
				return 164244;
			case 164245 :
				return 164245;
			case 164250 :
				return 164250;
			case 164251 :
				return 164251;
			case 164252 :
				return 164252;
			case 164254 :
				return 164254;
			case 164255 :
				return 164255;
			case 164257 :
				return 164257;
			case 164259 :
				return 164259;
			case 164229 :
				return 164229;
			case 164232 :
				return 164232;
			case 164230 :
				return 164230;
			case 164238 :
				return 164238;
			case 164253 :
				return 164253;
			case 164237 :
				return 164237;
			case 164234 :
				return 164234;
			case 165268 :
				return 165268;
			case 165274 :
				return 165274;
			case 165275 :
				return 165275;
			case 170628 :
				return 154249 | DL;
			case 170626 :
				return 154257 | DL;
			case 170633 :
				return 154249 | DL;
			case 170640 :
				return 154256 | DL;
			case 170656 :
				return 154272 | DL;
			case 170629 :
				return 154249 | DL;
			case 170641 :
				return 154257 | DL;
			case 170634 :
				return 154249 | DL;
			case 170642 :
				return 154257 | DL;
			case 170643 :
				return 154256 | DL;
			case 170884 :
				return 154505 | DL;
			case 170882 :
				return 154513 | DL;
			case 170889 :
				return 154505 | DL;
			case 170896 :
				return 154512 | DL;
			case 170912 :
				return 154528 | DL;
			case 170885 :
				return 154505 | DL;
			case 170897 :
				return 154513 | DL;
			case 170890 :
				return 154505 | DL;
			case 170898 :
				return 154513 | DL;
			case 170899 :
				return 154512 | DL;
			case 171140 :
				return 154761;
			case 171138 :
				return 154769;
			case 171145 :
				return 154761;
			case 171152 :
				return 154768 | DL;
			case 171168 :
				return 154784;
			case 171141 :
				return 154761;
			case 171153 :
				return 154769;
			case 171146 :
				return 154761;
			case 171154 :
				return 154769;
			case 171155 :
				return 154768 | DL;
			case 171522 :
				return 155160;
			case 171544 :
				return 155160;
			case 171592 :
				return 155208;
			case 171543 :
				return 155160;
			case 172164 :
				return 155801 | DL;
			case 172185 :
				return 155801 | DL;
			case 172232 :
				return 155848 | DL;
			case 172546 :
				return 156184 | DL;
			case 172568 :
				return 156184 | DL;
			case 172616 :
				return 156232 | DL;
			case 172567 :
				return 156184 | DL;
			case 173350 :
				return 156966;
			case 173353 :
				return 156969;
			case 173387 :
				return 157003;
			case 173388 :
				return 255308;
			case 173956 :
				return 157577;
			case 173954 :
				return 157585;
			case 173961 :
				return 157577;
			case 173968 :
				return 157584 | DL;
			case 173984 :
				return 157600;
			case 173957 :
				return 157577;
			case 173969 :
				return 157585;
			case 173962 :
				return 157577;
			case 173970 :
				return 157585;
			case 173971 :
				return 157584 | DL;
			case 164484 :
				return 164229;
			case 164487 :
				return 164231;
			case 164482 :
				return 164259;
			case 164483 :
				return 164227;
			case 164489 :
				return 164233;
			case 164492 :
				return 164236;
			case 164495 :
				return 164239;
			case 164500 :
				return 164244;
			case 164501 :
				return 164245;
			case 164506 :
				return 164250;
			case 164507 :
				return 164251;
			case 164508 :
				return 164252;
			case 164510 :
				return 164254;
			case 164511 :
				return 164255;
			case 164513 :
				return 164257;
			case 164515 :
				return 164259;
			case 164485 :
				return 164229;
			case 164488 :
				return 164232;
			case 164486 :
				return 164230;
			case 164494 :
				return 164238;
			case 164509 :
				return 164253;
			case 164493 :
				return 164237;
			case 164490 :
				return 164234;
			case 164868 :
				return 164229;
			case 164871 :
				return 164231;
			case 164866 :
				return 164259;
			case 164867 :
				return 164227;
			case 164873 :
				return 164233;
			case 164876 :
				return 164236;
			case 164879 :
				return 164239;
			case 164884 :
				return 164244;
			case 164885 :
				return 164245;
			case 164890 :
				return 164250;
			case 164891 :
				return 164251;
			case 164892 :
				return 164252;
			case 164894 :
				return 164254;
			case 164895 :
				return 164255;
			case 164897 :
				return 164257;
			case 164899 :
				return 164259;
			case 164869 :
				return 164229;
			case 164872 :
				return 164232;
			case 164870 :
				return 164230;
			case 164878 :
				return 164238;
			case 164893 :
				return 164253;
			case 164877 :
				return 164237;
			case 164874 :
				return 164234;
			case 164612 :
				return 164229;
			case 164615 :
				return 164231;
			case 164610 :
				return 164259;
			case 164611 :
				return 164227;
			case 164617 :
				return 164233;
			case 164620 :
				return 164236;
			case 164623 :
				return 164239;
			case 164628 :
				return 164244;
			case 164629 :
				return 164245;
			case 164634 :
				return 164250;
			case 164635 :
				return 164251;
			case 164636 :
				return 164252;
			case 164638 :
				return 164254;
			case 164639 :
				return 164255;
			case 164641 :
				return 164257;
			case 164643 :
				return 164259;
			case 164613 :
				return 164229;
			case 164616 :
				return 164232;
			case 164614 :
				return 164230;
			case 164622 :
				return 164238;
			case 164637 :
				return 164253;
			case 164621 :
				return 164237;
			case 164618 :
				return 164234;
			case 301444 :
				return 530826;
			case 301442 :
				return 530834 | DL;
			case 301449 :
				return 530825;
			case 301455 :
				return 530831;
			case 301456 :
				return 530832 | DL;
			case 301472 :
				return 530848 | DL;
			case 301474 :
				return 530850 | DL;
			case 301445 :
				return 530826;
			case 301457 :
				return 530833 | DL;
			case 301450 :
				return 530826;
			case 301458 :
				return 530834 | DL;
			case 301459 :
				return 530835 | DL;
			case 301588 :
				return 530964 | DL;
			case 301594 :
				return 530970;
			case 301700 :
				return 268937 | DL;
			case 301698 :
				return 268945 | DL;
			case 301705 :
				return 268937 | DL;
			case 301712 :
				return 268944 | DL;
			case 301728 :
				return 268960 | DL;
			case 301701 :
				return 268937 | DL;
			case 301713 :
				return 268945 | DL;
			case 301706 :
				return 268937 | DL;
			case 301714 :
				return 268945 | DL;
			case 301715 :
				return 268944 | DL;
			case 301956 :
				return 285577 | DL;
			case 301954 :
				return 285585 | DL;
			case 301961 :
				return 285577 | DL;
			case 301968 :
				return 285584 | DL;
			case 301984 :
				return 285600 | DL;
			case 301957 :
				return 285577 | DL;
			case 301969 :
				return 285585 | DL;
			case 301962 :
				return 285577 | DL;
			case 301970 :
				return 285585 | DL;
			case 301971 :
				return 285584 | DL;
			case 302212 :
				return 285833 | DL;
			case 302210 :
				return 285841 | DL;
			case 302217 :
				return 285833 | DL;
			case 302224 :
				return 285840 | DL;
			case 302240 :
				return 285856 | DL;
			case 302213 :
				return 285833 | DL;
			case 302225 :
				return 285841 | DL;
			case 302218 :
				return 285833 | DL;
			case 302226 :
				return 285841 | DL;
			case 302227 :
				return 285840 | DL;
			case 302468 :
				return 531861 | DL;
			case 302484 :
				return 531860 | DL;
			case 302485 :
				return 531861 | DL;
			case 302490 :
				return 531866 | DL;
			case 302491 :
				return 531867 | DL;
			case 302469 :
				return 531861 | DL;
			case 302594 :
				return 269848 | DL;
			case 302616 :
				return 269848 | DL;
			case 302664 :
				return 269896 | DL;
			case 302615 :
				return 269848 | DL;
			case 302868 :
				return 532244 | DL;
			case 302874 :
				return 532250;
			case 302996 :
				return 532372 | DL;
			case 303002 :
				return 532378;
			case 303108 :
				return 532494;
			case 303111 :
				return 532494;
			case 303116 :
				return 532492;
			case 303132 :
				return 532508;
			case 303137 :
				return 532513;
			case 303109 :
				return 532494;
			case 303112 :
				return 532509;
			case 303110 :
				return 532492;
			case 303118 :
				return 532494;
			case 303133 :
				return 532509;
			case 303117 :
				return 532493;
			case 303236 :
				return 270489 | DL;
			case 303234 :
				return 565398 | DL;
			case 303254 :
				return 565398 | DL;
			case 303257 :
				return 270489 | DL;
			case 303304 :
				return 319688 | DL;
			case 303255 :
				return 565398 | DL;
			case 303492 :
				return 532874;
			case 303490 :
				return 532882 | DL;
			case 303497 :
				return 532873;
			case 303503 :
				return 532879;
			case 303504 :
				return 532880 | DL;
			case 303520 :
				return 532896 | DL;
			case 303522 :
				return 532898 | DL;
			case 303493 :
				return 532874;
			case 303505 :
				return 532881 | DL;
			case 303498 :
				return 532874;
			case 303506 :
				return 532882 | DL;
			case 303507 :
				return 532883 | DL;
			case 303618 :
				return 270872 | DL;
			case 303640 :
				return 270872 | DL;
			case 303688 :
				return 270920 | DL;
			case 303639 :
				return 270872 | DL;
			case 304422 :
				return 271654 | DL;
			case 304423 :
				return 566567 | DL;
			case 304432 :
				return 533808;
			case 304459 :
				return 304459;
			case 305028 :
				return 288649 | DL;
			case 305026 :
				return 288657 | DL;
			case 305033 :
				return 288649 | DL;
			case 305040 :
				return 288656 | DL;
			case 305056 :
				return 288672 | DL;
			case 305029 :
				return 288649 | DL;
			case 305041 :
				return 288657 | DL;
			case 305034 :
				return 288649 | DL;
			case 305042 :
				return 288657 | DL;
			case 305043 :
				return 288656 | DL;
			case 318084 :
				return 268937 | DL;
			case 318082 :
				return 268945 | DL;
			case 318089 :
				return 268937 | DL;
			case 318096 :
				return 268944 | DL;
			case 318112 :
				return 268960 | DL;
			case 318085 :
				return 268937 | DL;
			case 318097 :
				return 268945 | DL;
			case 318090 :
				return 268937 | DL;
			case 318098 :
				return 268945 | DL;
			case 318099 :
				return 268944 | DL;
			case 318340 :
				return 269193 | DL;
			case 318338 :
				return 269201 | DL;
			case 318345 :
				return 269193 | DL;
			case 318352 :
				return 269200 | DL;
			case 318368 :
				return 269216 | DL;
			case 318341 :
				return 269193 | DL;
			case 318353 :
				return 269201 | DL;
			case 318346 :
				return 269193 | DL;
			case 318354 :
				return 269201 | DL;
			case 318355 :
				return 269200 | DL;
			case 318596 :
				return 269449 | DL;
			case 318594 :
				return 269457 | DL;
			case 318601 :
				return 269449 | DL;
			case 318608 :
				return 269456 | DL;
			case 318624 :
				return 269472 | DL;
			case 318597 :
				return 269449 | DL;
			case 318609 :
				return 269457 | DL;
			case 318602 :
				return 269449 | DL;
			case 318610 :
				return 269457 | DL;
			case 318611 :
				return 269456 | DL;
			case 318978 :
				return 269848 | DL;
			case 319000 :
				return 269848 | DL;
			case 319048 :
				return 269896 | DL;
			case 318999 :
				return 269848 | DL;
			case 319620 :
				return 270489 | DL;
			case 319618 :
				return 565398 | DL;
			case 319638 :
				return 565398 | DL;
			case 319641 :
				return 270489 | DL;
			case 319688 :
				return 319688 | DL;
			case 319639 :
				return 565398 | DL;
			case 320002 :
				return 270872 | DL;
			case 320024 :
				return 270872 | DL;
			case 320072 :
				return 270920 | DL;
			case 320023 :
				return 270872 | DL;
			case 320806 :
				return 271654 | DL;
			case 320807 :
				return 566567 | DL;
			case 320843 :
				return 320843 | DL;
			case 321412 :
				return 272265 | DL;
			case 321410 :
				return 272273 | DL;
			case 321417 :
				return 272265 | DL;
			case 321424 :
				return 272272 | DL;
			case 321440 :
				return 272288 | DL;
			case 321413 :
				return 272265 | DL;
			case 321425 :
				return 272273 | DL;
			case 321418 :
				return 272265 | DL;
			case 321426 :
				return 272273 | DL;
			case 321427 :
				return 272272 | DL;
			case 385924 :
				return 402313;
			case 385922 :
				return 402321;
			case 385929 :
				return 402313;
			case 385936 :
				return 402320 | DL;
			case 385940 :
				return 369556 | DL;
			case 385946 :
				return 369562 | DL;
			case 385947 :
				return 369563 | DL;
			case 385952 :
				return 402336;
			case 385925 :
				return 402313;
			case 385937 :
				return 402321;
			case 385930 :
				return 402313;
			case 385938 :
				return 402321;
			case 385939 :
				return 402320 | DL;
			case 386178 :
				return 386199;
			case 386198 :
				return 369814 | DL;
			case 386200 :
				return 402584;
			case 386248 :
				return 386248;
			case 386199 :
				return 386199;
			case 386373 :
				return 386373;
			default :
				return Failure;
		}
	}
}
