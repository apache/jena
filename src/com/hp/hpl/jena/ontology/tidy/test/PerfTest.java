/*
  (c) Copyright 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PerfTest.java,v 1.4 2004-08-31 09:49:51 andy_seaborne Exp $
*/
package com.hp.hpl.jena.ontology.tidy.test;
import org.xml.sax.InputSource;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.tidy.*;

import org.apache.xerces.parsers.*;
import com.hp.hpl.jena.rdf.arp.*;
import java.net.URL;

/**
 * Computes the cost of OWL Syntax checking
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class PerfTest {
	/* The test data. */
	int cnt[] = {
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 1, 1, 1,
	 1, 1, 2, 2, 2,
	 2, 2, 2, 2, 2,
	 2, 2, 2, 2, 2,
	 2, 2, 2, 2, 2,
	 2, 2, 2, 2, 2,
	 2, 2, 2, 2, 2,
	 2, 2, 2, 2, 2,
	 2, 3, 3, 3, 3,
	 3, 3, 3, 3, 3,
	 3, 3, 3, 3, 3,
	 3, 3, 3, 3, 3,
	 3, 3, 3, 3, 3,
	 3, 3, 3, 3, 3,
	 3, 3, 3, 3, 3,
	 3, 3, 3, 3, 3,
	 3, 3, 3, 3, 3,
	 3, 3, 3, 3, 3,
	 3, 4, 4, 4, 4,
	 4, 4, 4, 4, 4,
	 4, 4, 4, 4, 4,
	 4, 4, 4, 4, 4,
	 4, 4, 4, 4, 4,
	 4, 4, 4, 4, 4,
	 4, 5, 5, 5, 5,
	 5, 5, 5, 5, 5,
	 5, 5, 5, 5, 5,
	 6, 6, 6, 6, 6,
	 6, 6, 6, 6, 6,
	 6, 6, 6, 6, 6,
	 6, 6, 6, 6, 6,
	 6, 6, 6, 6, 6,
	 7, 7, 7, 7, 7,
	 7, 7, 7, 7, 7,
	 7, 7, 7, 7, 7,
	 7, 8, 8, 8, 8,
	 8, 8, 8, 8, 8,
	 8, 8, 8, 9, 9,
	 9, 9, 9, 9, 9,
	 9, 9, 9, 10, 10,
	 10, 10, 10, 10, 11,
	 11, 11, 11, 11, 11,
	 11, 11, 11, 11, 11,
	 11, 11, 11, 12, 13,
	 13, 13, 13, 14, 14,
	 14, 14, 14, 14, 14,
	 14, 15, 15, 15, 15,
	 15, 15, 15, 15, 16,
	 16, 17, 18, 18, 18,
	 18, 18, 18, 18, 18,
	 19, 19, 19, 19, 21,
	 21, 21, 21, 21, 23,
	 23, 24, 24, 25, 25,
	 26, 26, 28, 29, 30,
	 30, 32, 32, 33, 33,
	 33, 34, 35, 35, 35,
	 35, 36, 36, 36, 36,
	 37, 37, 37, 37, 37,
	 37, 39, 39, 41, 42,
	 42, 43, 44, 44, 44,
	 45, 45, 45, 46, 46,
	 46, 46, 46, 46, 46,
	 47, 47, 48, 48, 48,
	 49, 49, 50, 50, 50,
	 50, 50, 50, 50, 50,
	 50, 51, 52, 52, 53,
	 54, 54, 54, 56, 58,
	 58, 58, 59, 61, 61,
	 62, 62, 62, 64, 65,
	 66, 70, 70, 71, 74,
	 74, 78, 78, 81, 81,
	 81, 83, 86, 88, 96,
	 103, 108, 114, 121, 121,
	 125, 127, 137, 138, 140,
	 142, 146, 159, 474, 477,
	 477, 478, 478, 500, 592,
	 695, 782, 799, 928, 1074,
	 1103, 1103, 1236, 1386, 2912,
	 2912, 2912, 2912, 2912, 2912,
	 2915, 2915,
	  };
	int threshold[] = { 40, 80, 120, 200,400,600,1000,1500,2900,3000 };
	String name[] = {
	 "file:testing/wg/AllDifferent/conclusions001.rdf", "file:testing/wg/Class/conclusions002.rdf", "file:testing/wg/Class/conclusions003.rdf", "file:testing/wg/Class/premises002.rdf", "file:testing/wg/Class/premises003.rdf",
	 "file:testing/wg/Class/premises005.rdf", "file:testing/wg/FunctionalProperty/conclusions001.rdf", "file:testing/wg/FunctionalProperty/conclusions002.rdf", "file:testing/wg/FunctionalProperty/conclusions003.rdf", "file:testing/wg/FunctionalProperty/conclusions004.rdf",
	 "file:testing/wg/I3.4/bad001.rdf", "file:testing/wg/I4.1/bad001.rdf", "file:testing/wg/I5.26/premises009.rdf", "file:testing/wg/I5.26/premises010.rdf", "file:testing/wg/I5.3/conclusions014.rdf",
	 "file:testing/wg/I5.3/consistent005.rdf", "file:testing/wg/I5.3/consistent007.rdf", "file:testing/wg/I5.5/conclusions001.rdf", "file:testing/wg/I5.5/conclusions002.rdf", "file:testing/wg/I5.5/inconsistent003.rdf",
	 "file:testing/wg/I5.5/inconsistent004.rdf", "file:testing/wg/I5.5/premises005.rdf", "file:testing/wg/I5.5/premises006.rdf", "file:testing/wg/I5.5/premises007.rdf", "file:testing/wg/I5.8/conclusions017.rdf",
	 "file:testing/wg/InverseFunctionalProperty/conclusions001.rdf", "file:testing/wg/InverseFunctionalProperty/conclusions002.rdf", "file:testing/wg/InverseFunctionalProperty/conclusions003.rdf", "file:testing/wg/InverseFunctionalProperty/conclusions004.rdf", "file:testing/wg/Nothing/inconsistent001.rdf",
	 "file:testing/wg/SymmetricProperty/conclusions001.rdf", "file:testing/wg/TransitiveProperty/conclusions001.rdf", "file:testing/wg/complementOf/conclusions001.rdf", "file:testing/wg/complementOf/premises001.rdf", "file:testing/wg/differentFrom/conclusions001.rdf",
	 "file:testing/wg/differentFrom/conclusions002.rdf", "file:testing/wg/differentFrom/premises001.rdf", "file:testing/wg/disjointWith/conclusions002.rdf", "file:testing/wg/distinctMembers/conclusions001.rdf", "file:testing/wg/equivalentProperty/conclusions005.rdf",
	 "file:testing/wg/equivalentProperty/conclusions006.rdf", "file:testing/wg/extra-credit/conclusions002.rdf", "file:testing/wg/imports/conclusions001.rdf", "file:testing/wg/imports/conclusions003.rdf", "file:testing/wg/imports/imports004.rdf",
	 "file:testing/wg/imports/imports013.rdf", "file:testing/wg/imports/imports014.rdf", "file:testing/wg/imports/nonconclusions002.rdf", "file:testing/wg/imports/premises002.rdf", "file:testing/wg/intersectionOf/conclusions001.rdf",
	 "file:testing/wg/inverseOf/conclusions001.rdf", "file:testing/wg/miscellaneous/consistent303.rdf", "file:testing/wg/oneOf/conclusions002.rdf", "file:testing/wg/oneOf/conclusions003.rdf", "file:testing/wg/someValuesFrom/nonconclusions002.rdf",
	 "file:testing/wg/unionOf/conclusions001.rdf", "file:testing/wg/unionOf/conclusions002.rdf", "file:testing/wg/AnnotationProperty/consistent004.rdf", "file:testing/wg/Class/premises006.rdf", "file:testing/wg/FunctionalProperty/premises003.rdf",
	 "file:testing/wg/FunctionalProperty/premises005.rdf", "file:testing/wg/I4.5/conclusions001.rdf", "file:testing/wg/I4.6/nonconclusions005.rdf", "file:testing/wg/I5.1/conclusions001.rdf", "file:testing/wg/I5.3/conclusions015.rdf",
	 "file:testing/wg/I5.3/premises014.rdf", "file:testing/wg/I5.8/conclusions006.rdf", "file:testing/wg/I5.8/conclusions008.rdf", "file:testing/wg/I5.8/conclusions009.rdf", "file:testing/wg/I5.8/conclusions011.rdf",
	 "file:testing/wg/I5.8/consistent014.rdf", "file:testing/wg/I5.8/nonconclusions007.rdf", "file:testing/wg/I5.8/premises006.rdf", "file:testing/wg/I5.8/premises007.rdf", "file:testing/wg/I5.8/premises017.rdf",
	 "file:testing/wg/InverseFunctionalProperty/premises003.rdf", "file:testing/wg/Restriction/premises006.rdf", "file:testing/wg/SymmetricProperty/premises001.rdf", "file:testing/wg/Thing/inconsistent003.rdf", "file:testing/wg/backwardCompatibleWith/consistent002.rdf",
	 "file:testing/wg/equivalentClass/premises006.rdf", "file:testing/wg/equivalentClass/premises007.rdf", "file:testing/wg/equivalentProperty/premises006.rdf", "file:testing/wg/imports/conclusions011.rdf", "file:testing/wg/imports/imports006.rdf",
	 "file:testing/wg/imports/imports007.rdf", "file:testing/wg/imports/imports008.rdf", "file:testing/wg/inverseOf/premises001.rdf", "file:testing/wg/miscellaneous/nonconclusions301.rdf", "file:testing/wg/miscellaneous/premises301.rdf",
	 "file:testing/wg/someValuesFrom/conclusions001.rdf", "file:testing/wg/AnnotationProperty/consistent003.rdf", "file:testing/wg/Class/conclusions001.rdf", "file:testing/wg/FunctionalProperty/premises001.rdf", "file:testing/wg/I4.6/bad006.rdf",
	 "file:testing/wg/I4.6/bad007.rdf", "file:testing/wg/I4.6/bad008.rdf", "file:testing/wg/I4.6/conclusions003.rdf", "file:testing/wg/I4.6/nonconclusions004.rdf", "file:testing/wg/I4.6/premises003.rdf",
	 "file:testing/wg/I4.6/premises004.rdf", "file:testing/wg/I5.2/conclusions002.rdf", "file:testing/wg/I5.2/conclusions004.rdf", "file:testing/wg/I5.24/conclusions001.rdf", "file:testing/wg/I5.24/conclusions004.rdf",
	 "file:testing/wg/I5.24/premises002.rdf", "file:testing/wg/I5.24/premises003.rdf", "file:testing/wg/I5.3/consistent008.rdf", "file:testing/wg/I5.8/conclusions004.rdf", "file:testing/wg/I5.8/conclusions010.rdf",
	 "file:testing/wg/I5.8/consistent016.rdf", "file:testing/wg/I5.8/nonconclusions005.rdf", "file:testing/wg/I5.8/premises008.rdf", "file:testing/wg/I5.8/premises009.rdf", "file:testing/wg/I6.1/consistent001.rdf",
	 "file:testing/wg/InverseFunctionalProperty/premises001.rdf", "file:testing/wg/Nothing/conclusions002.rdf", "file:testing/wg/Thing/inconsistent005.rdf", "file:testing/wg/TransitiveProperty/premises001.rdf", "file:testing/wg/allValuesFrom/conclusions001.rdf",
	 "file:testing/wg/backwardCompatibleWith/consistent001.rdf", "file:testing/wg/description-logic/nonconclusions209.rdf", "file:testing/wg/disjointWith/conclusions001.rdf", "file:testing/wg/disjointWith/premises002.rdf", "file:testing/wg/equivalentClass/conclusions003.rdf",
	 "file:testing/wg/equivalentClass/conclusions004.rdf", "file:testing/wg/equivalentClass/nonconclusions005.rdf", "file:testing/wg/equivalentClass/nonconclusions008.rdf", "file:testing/wg/equivalentClass/premises002.rdf", "file:testing/wg/equivalentProperty/conclusions003.rdf",
	 "file:testing/wg/equivalentProperty/conclusions004.rdf", "file:testing/wg/equivalentProperty/premises002.rdf", "file:testing/wg/imports/conclusions010.rdf", "file:testing/wg/imports/consistent012.rdf", "file:testing/wg/imports/main004.rdf",
	 "file:testing/wg/imports/main013.rdf", "file:testing/wg/imports/main014.rdf", "file:testing/wg/miscellaneous/nonconclusions302.rdf", "file:testing/wg/miscellaneous/premises302.rdf", "file:testing/wg/oneOf/conclusions004.rdf",
	 "file:testing/wg/sameAs/conclusions001.rdf", "file:testing/wg/AnnotationProperty/conclusions002.rdf", "file:testing/wg/AnnotationProperty/nonconclusions001.rdf", "file:testing/wg/AnnotationProperty/premises001.rdf", "file:testing/wg/AnnotationProperty/premises002.rdf",
	 "file:testing/wg/Class/nonconclusions004.rdf", "file:testing/wg/Class/premises004.rdf", "file:testing/wg/FunctionalProperty/premises002.rdf", "file:testing/wg/I3.2/bad001.rdf", "file:testing/wg/I3.2/bad002.rdf",
	 "file:testing/wg/I3.2/bad003.rdf", "file:testing/wg/I4.6/premises005.rdf", "file:testing/wg/I5.26/conclusions010.rdf", "file:testing/wg/I5.3/consistent006.rdf", "file:testing/wg/I5.3/consistent010.rdf",
	 "file:testing/wg/I5.3/consistent011.rdf", "file:testing/wg/I5.3/premises015.rdf", "file:testing/wg/I5.5/nonconclusions006.rdf", "file:testing/wg/I5.8/consistent013.rdf", "file:testing/wg/InverseFunctionalProperty/premises002.rdf",
	 "file:testing/wg/SymmetricProperty/conclusions003.rdf", "file:testing/wg/SymmetricProperty/premises003.rdf", "file:testing/wg/equivalentClass/conclusions002.rdf", "file:testing/wg/equivalentClass/premises003.rdf", "file:testing/wg/equivalentProperty/conclusions001.rdf",
	 "file:testing/wg/equivalentProperty/conclusions002.rdf", "file:testing/wg/equivalentProperty/premises003.rdf", "file:testing/wg/imports/support001-A.rdf", "file:testing/wg/imports/support002-A.rdf", "file:testing/wg/imports/support003-B.rdf",
	 "file:testing/wg/imports/support011-A.rdf", "file:testing/wg/DatatypeProperty/consistent001.rdf", "file:testing/wg/I5.1/premises001.rdf", "file:testing/wg/I5.24/premises001.rdf", "file:testing/wg/I5.26/conclusions009.rdf",
	 "file:testing/wg/I5.3/consistent009.rdf", "file:testing/wg/I5.8/consistent015.rdf", "file:testing/wg/Thing/consistent004.rdf", "file:testing/wg/equivalentClass/premises008.rdf", "file:testing/wg/imports/main006.rdf",
	 "file:testing/wg/miscellaneous/consistent202.rdf", "file:testing/wg/miscellaneous/consistent205.rdf", "file:testing/wg/miscellaneous/inconsistent203.rdf", "file:testing/wg/miscellaneous/inconsistent204.rdf", "file:testing/wg/sameAs/premises001.rdf",
	 "file:testing/wg/FunctionalProperty/conclusions005.rdf", "file:testing/wg/FunctionalProperty/premises004.rdf", "file:testing/wg/I5.2/consistent010.rdf", "file:testing/wg/I5.2/consistent011.rdf", "file:testing/wg/I5.5/conclusions005.rdf",
	 "file:testing/wg/I5.8/consistent002.rdf", "file:testing/wg/I5.8/inconsistent001.rdf", "file:testing/wg/I5.8/premises010.rdf", "file:testing/wg/InverseFunctionalProperty/premises004.rdf", "file:testing/wg/Ontology/conclusions004.rdf",
	 "file:testing/wg/Restriction/inconsistent002.rdf", "file:testing/wg/TransitiveProperty/conclusions002.rdf", "file:testing/wg/allValuesFrom/nonconclusions002.rdf", "file:testing/wg/cardinality/conclusions002.rdf", "file:testing/wg/cardinality/conclusions006.rdf",
	 "file:testing/wg/cardinality/premises001.rdf", "file:testing/wg/cardinality/premises003.rdf", "file:testing/wg/description-logic/conclusions207.rdf", "file:testing/wg/description-logic/conclusions667.rdf", "file:testing/wg/equivalentClass/conclusions001.rdf",
	 "file:testing/wg/equivalentProperty/premises001.rdf", "file:testing/wg/imports/imports005.rdf", "file:testing/wg/imports/premises001.rdf", "file:testing/wg/someValuesFrom/conclusions003.rdf", "file:testing/wg/unionOf/premises001.rdf",
	 "file:testing/wg/I5.24/conclusions003.rdf", "file:testing/wg/I5.24/premises004.rdf", "file:testing/wg/I5.8/consistent012.rdf", "file:testing/wg/I5.8/inconsistent003.rdf", "file:testing/wg/I5.8/premises004.rdf",
	 "file:testing/wg/I5.8/premises005.rdf", "file:testing/wg/Ontology/conclusions001.rdf", "file:testing/wg/Ontology/nonconclusions003.rdf", "file:testing/wg/Ontology/premises003.rdf", "file:testing/wg/disjointWith/premises001.rdf",
	 "file:testing/wg/equivalentClass/premises001.rdf", "file:testing/wg/imports/main007.rdf", "file:testing/wg/imports/main008.rdf", "file:testing/wg/imports/premises011.rdf", "file:testing/wg/oneOf/premises002.rdf",
	 "file:testing/wg/someValuesFrom/premises003.rdf", "file:testing/wg/I5.2/conclusions006.rdf", "file:testing/wg/I5.21/bad001.rdf", "file:testing/wg/I5.24/conclusions002.rdf", "file:testing/wg/I5.26/consistent002.rdf",
	 "file:testing/wg/Ontology/premises001.rdf", "file:testing/wg/Restriction/premises005.rdf", "file:testing/wg/disjointWith/inconsistent010.rdf", "file:testing/wg/equivalentProperty/premises005.rdf", "file:testing/wg/imports/main005.rdf",
	 "file:testing/wg/someValuesFrom/premises001.rdf", "file:testing/wg/unionOf/conclusions003.rdf", "file:testing/wg/unionOf/conclusions004.rdf", "file:testing/wg/I5.26/consistent004.rdf", "file:testing/wg/I5.26/consistent005.rdf",
	 "file:testing/wg/Ontology/premises004.rdf", "file:testing/wg/Restriction/inconsistent001.rdf", "file:testing/wg/Restriction/nonconclusions005.rdf", "file:testing/wg/allValuesFrom/premises002.rdf", "file:testing/wg/description-logic/conclusions203.rdf",
	 "file:testing/wg/description-logic/conclusions663.rdf", "file:testing/wg/imports/support003-A.rdf", "file:testing/wg/someValuesFrom/premises002.rdf", "file:testing/wg/Class/conclusions006.rdf", "file:testing/wg/cardinality/conclusions001.rdf",
	 "file:testing/wg/cardinality/conclusions003.rdf", "file:testing/wg/cardinality/conclusions004.rdf", "file:testing/wg/cardinality/premises002.rdf", "file:testing/wg/miscellaneous/conclusions011.rdf", "file:testing/wg/Class/nonconclusions005.rdf",
	 "file:testing/wg/I5.2/consistent001.rdf", "file:testing/wg/I5.2/premises002.rdf", "file:testing/wg/I5.5/nonconclusions007.rdf", "file:testing/wg/allValuesFrom/premises001.rdf", "file:testing/wg/cardinality/premises006.rdf",
	 "file:testing/wg/description-logic/premises901.rdf", "file:testing/wg/description-logic/premises902.rdf", "file:testing/wg/description-logic/premises903.rdf", "file:testing/wg/description-logic/premises904.rdf", "file:testing/wg/equivalentClass/premises005.rdf",
	 "file:testing/wg/imports/premises003.rdf", "file:testing/wg/intersectionOf/premises001.rdf", "file:testing/wg/maxCardinality/inconsistent001.rdf", "file:testing/wg/maxCardinality/inconsistent002.rdf", "file:testing/wg/I5.26/consistent001.rdf",
	 "file:testing/wg/extra-credit/conclusions003.rdf", "file:testing/wg/extra-credit/conclusions004.rdf", "file:testing/wg/unionOf/premises002.rdf", "file:testing/wg/AllDifferent/premises001.rdf", "file:testing/wg/I5.26/consistent003.rdf",
	 "file:testing/wg/I5.26/consistent006.rdf", "file:testing/wg/TransitiveProperty/premises002.rdf", "file:testing/wg/differentFrom/premises002.rdf", "file:testing/wg/distinctMembers/premises001.rdf", "file:testing/wg/equivalentClass/conclusions007.rdf",
	 "file:testing/wg/miscellaneous/consistent201.rdf", "file:testing/wg/I5.26/consistent007.rdf", "file:testing/wg/Restriction/conclusions006.rdf", "file:testing/wg/SymmetricProperty/conclusions002.rdf", "file:testing/wg/SymmetricProperty/premises002.rdf",
	 "file:testing/wg/cardinality/premises004.rdf", "file:testing/wg/equivalentClass/premises004.rdf", "file:testing/wg/oneOf/consistent001.rdf", "file:testing/wg/oneOf/premises003.rdf", "file:testing/wg/disjointWith/consistent008.rdf",
	 "file:testing/wg/equivalentProperty/premises004.rdf", "file:testing/wg/disjointWith/consistent009.rdf", "file:testing/wg/I5.1/consistent010.rdf", "file:testing/wg/Restriction/consistent003.rdf", "file:testing/wg/description-logic/conclusions205.rdf",
	 "file:testing/wg/description-logic/conclusions665.rdf", "file:testing/wg/miscellaneous/consistent102.rdf", "file:testing/wg/miscellaneous/consistent103.rdf", "file:testing/wg/unionOf/premises003.rdf", "file:testing/wg/unionOf/premises004.rdf",
	 "file:testing/wg/description-logic/conclusions901.rdf", "file:testing/wg/description-logic/conclusions903.rdf", "file:testing/wg/description-logic/nonconclusions902.rdf", "file:testing/wg/description-logic/nonconclusions904.rdf", "file:testing/wg/Restriction/consistent004.rdf",
	 "file:testing/wg/description-logic/conclusions204.rdf", "file:testing/wg/description-logic/conclusions664.rdf", "file:testing/wg/equivalentClass/conclusions006.rdf", "file:testing/wg/equivalentClass/consistent009.rdf", "file:testing/wg/description-logic/inconsistent002.rdf",
	 "file:testing/wg/description-logic/inconsistent105.rdf", "file:testing/wg/description-logic/conclusions201.rdf", "file:testing/wg/description-logic/conclusions661.rdf", "file:testing/wg/I5.2/consistent003.rdf", "file:testing/wg/I5.2/premises004.rdf",
	 "file:testing/wg/description-logic/inconsistent035.rdf", "file:testing/wg/description-logic/inconsistent104.rdf", "file:testing/wg/disjointWith/consistent007.rdf", "file:testing/wg/description-logic/inconsistent602.rdf", "file:testing/wg/description-logic/inconsistent106.rdf",
	 "file:testing/wg/disjointWith/consistent005.rdf", "file:testing/wg/description-logic/inconsistent033.rdf", "file:testing/wg/disjointWith/consistent006.rdf", "file:testing/wg/description-logic/inconsistent101.rdf", "file:testing/wg/description-logic/inconsistent103.rdf",
	 "file:testing/wg/oneOf/premises004.rdf", "file:testing/wg/I4.5/premises001.rdf", "file:testing/wg/description-logic/consistent009.rdf", "file:testing/wg/description-logic/inconsistent030.rdf", "file:testing/wg/description-logic/inconsistent646.rdf",
	 "file:testing/wg/disjointWith/consistent004.rdf", "file:testing/wg/description-logic/conclusions202.rdf", "file:testing/wg/description-logic/conclusions662.rdf", "file:testing/wg/description-logic/consistent025.rdf", "file:testing/wg/description-logic/inconsistent003.rdf",
	 "file:testing/wg/I4.5/inconsistent002.rdf", "file:testing/wg/description-logic/consistent016.rdf", "file:testing/wg/description-logic/consistent034.rdf", "file:testing/wg/description-logic/inconsistent014.rdf", "file:testing/wg/description-logic/inconsistent644.rdf",
	 "file:testing/wg/disjointWith/consistent003.rdf", "file:testing/wg/description-logic/consistent024.rdf", "file:testing/wg/description-logic/inconsistent032.rdf", "file:testing/wg/description-logic/inconsistent012.rdf", "file:testing/wg/description-logic/consistent616.rdf",
	 "file:testing/wg/description-logic/inconsistent029.rdf", "file:testing/wg/description-logic/inconsistent017.rdf", "file:testing/wg/description-logic/consistent609.rdf", "file:testing/wg/description-logic/inconsistent109.rdf", "file:testing/wg/description-logic/inconsistent110.rdf",
	 "file:testing/wg/description-logic/inconsistent011.rdf", "file:testing/wg/description-logic/inconsistent603.rdf", "file:testing/wg/description-logic/inconsistent633.rdf", "file:testing/wg/description-logic/consistent908.rdf", "file:testing/wg/description-logic/inconsistent102.rdf",
	 "file:testing/wg/description-logic/inconsistent630.rdf", "file:testing/wg/description-logic/inconsistent641.rdf", "file:testing/wg/description-logic/inconsistent643.rdf", "file:testing/wg/extra-credit/premises003.rdf", "file:testing/wg/extra-credit/premises004.rdf",
	 "file:testing/wg/description-logic/inconsistent614.rdf", "file:testing/wg/extra-credit/premises002.rdf", "file:testing/wg/description-logic/inconsistent010.rdf", "file:testing/wg/description-logic/inconsistent013.rdf", "file:testing/wg/description-logic/inconsistent111.rdf",
	 "file:testing/wg/description-logic/consistent031.rdf", "file:testing/wg/description-logic/consistent625.rdf", "file:testing/wg/description-logic/consistent905.rdf", "file:testing/wg/description-logic/consistent906.rdf", "file:testing/wg/description-logic/consistent907.rdf",
	 "file:testing/wg/description-logic/inconsistent015.rdf", "file:testing/wg/description-logic/inconsistent612.rdf", "file:testing/wg/description-logic/inconsistent617.rdf", "file:testing/wg/description-logic/inconsistent632.rdf", "file:testing/wg/description-logic/inconsistent910.rdf",
	 "file:testing/wg/miscellaneous/conclusions010.rdf", "file:testing/wg/description-logic/inconsistent027.rdf", "file:testing/wg/description-logic/consistent028.rdf", "file:testing/wg/description-logic/consistent624.rdf", "file:testing/wg/description-logic/inconsistent629.rdf",
	 "file:testing/wg/description-logic/inconsistent001.rdf", "file:testing/wg/description-logic/inconsistent026.rdf", "file:testing/wg/description-logic/inconsistent611.rdf", "file:testing/wg/description-logic/consistent634.rdf", "file:testing/wg/description-logic/consistent005.rdf",
	 "file:testing/wg/description-logic/inconsistent004.rdf", "file:testing/wg/description-logic/inconsistent627.rdf", "file:testing/wg/description-logic/inconsistent642.rdf", "file:testing/wg/I5.2/consistent005.rdf", "file:testing/wg/I5.2/premises006.rdf",
	 "file:testing/wg/description-logic/inconsistent610.rdf", "file:testing/wg/description-logic/inconsistent613.rdf", "file:testing/wg/description-logic/inconsistent626.rdf", "file:testing/wg/description-logic/inconsistent650.rdf", "file:testing/wg/description-logic/inconsistent909.rdf",
	 "file:testing/wg/description-logic/inconsistent107.rdf", "file:testing/wg/description-logic/consistent605.rdf", "file:testing/wg/description-logic/inconsistent615.rdf", "file:testing/wg/description-logic/inconsistent604.rdf", "file:testing/wg/description-logic/consistent006.rdf",
	 "file:testing/wg/description-logic/premises207.rdf", "file:testing/wg/I5.21/conclusions002.rdf", "file:testing/wg/I5.21/premises002.rdf", "file:testing/wg/description-logic/inconsistent008.rdf", "file:testing/wg/description-logic/inconsistent023.rdf",
	 "file:testing/wg/description-logic/premises667.rdf", "file:testing/wg/description-logic/inconsistent007.rdf", "file:testing/wg/description-logic/consistent631.rdf", "file:testing/wg/description-logic/consistent628.rdf", "file:testing/wg/description-logic/conclusions208.rdf",
	 "file:testing/wg/description-logic/inconsistent108.rdf", "file:testing/wg/description-logic/inconsistent623.rdf", "file:testing/wg/description-logic/premises665.rdf", "file:testing/wg/description-logic/consistent018.rdf", "file:testing/wg/description-logic/inconsistent601.rdf",
	 "file:testing/wg/description-logic/premises205.rdf", "file:testing/wg/description-logic/inconsistent019.rdf", "file:testing/wg/description-logic/consistent606.rdf", "file:testing/wg/description-logic/conclusions206.rdf", "file:testing/wg/description-logic/consistent020.rdf",
	 "file:testing/wg/description-logic/inconsistent608.rdf", "file:testing/wg/description-logic/inconsistent022.rdf", "file:testing/wg/description-logic/consistent021.rdf", "file:testing/wg/description-logic/inconsistent040.rdf", "file:testing/wg/description-logic/consistent501.rdf",
	 "file:testing/wg/description-logic/inconsistent502.rdf", "file:testing/wg/description-logic/consistent503.rdf", "file:testing/wg/description-logic/inconsistent504.rdf", "file:testing/wg/description-logic/premises202.rdf", "file:testing/wg/description-logic/premises662.rdf",
	 "file:testing/wg/description-logic/premises203.rdf", "file:testing/wg/description-logic/premises663.rdf", "file:testing/wg/description-logic/premises201.rdf", "file:testing/wg/description-logic/premises661.rdf", "file:testing/wg/description-logic/premises204.rdf",
	 "file:testing/wg/description-logic/premises208.rdf", "file:testing/wg/description-logic/premises209.rdf", "file:testing/wg/description-logic/premises664.rdf", "file:testing/wg/description-logic/premises206.rdf", "file:testing/wg/miscellaneous/consistent001.rdf",
	 "file:testing/wg/miscellaneous/consistent001.rdf", "file:testing/wg/miscellaneous/consistent001.rdf", "file:testing/wg/miscellaneous/consistent002.rdf", "file:testing/wg/miscellaneous/consistent002.rdf", "file:testing/wg/miscellaneous/consistent002.rdf",
	 "file:testing/wg/miscellaneous/premises010.rdf", "file:testing/wg/miscellaneous/premises011.rdf",
	  };
	String names[][] = {
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { "file:testing/wg/imports/imports004.rdf", },
	 { "file:testing/wg/imports/imports013.rdf", }, { "file:testing/wg/imports/imports014.rdf", }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { "file:testing/wg/imports/imports006.rdf", },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { "file:testing/wg/imports/support001-A.rdf", }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { "file:testing/wg/imports/imports007.rdf", }, { "file:testing/wg/imports/imports008.rdf", }, { "file:testing/wg/imports/support011-A.rdf", }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { "file:testing/wg/imports/imports005.rdf", },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { "file:testing/wg/imports/support003-B.rdf", }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { "file:testing/wg/imports/support003-B.rdf", "file:testing/wg/imports/support003-A.rdf", }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { },
	 { }, { }, { }, { }, { "file:testing/wg/miscellaneous/consistent002.rdf", },
	 { "file:testing/wg/miscellaneous/consistent002.rdf", }, { "file:testing/wg/miscellaneous/consistent002.rdf", }, { "file:testing/wg/miscellaneous/consistent001.rdf", }, { "file:testing/wg/miscellaneous/consistent001.rdf", }, { "file:testing/wg/miscellaneous/consistent001.rdf", },
	 { "file:testing/wg/miscellaneous/consistent002.rdf", "file:testing/wg/miscellaneous/consistent001.rdf", }, { "file:testing/wg/miscellaneous/consistent002.rdf", "file:testing/wg/miscellaneous/consistent001.rdf", },
	  };
	int time[] = new int[cnt.length];
	int mem[] = new int[cnt.length];
	int tripleTide[] = new int[cnt.length];
	
	void test(String nm, String nms[]) {
		switch (type) {
			case 1:
			  testXerces(nm,nms);
			  break;
		  case 2:
		    testARP(nm,nms);
		    break;
		  case 3:
		    testJena(nm,nms);
		    break;
		  case 4:
		    testOWL(nm,nms);
		    break;
		  case 5:
		    testHP(nm,nms);
		    break;
		  case 6:
		    testHPNoOps(nm,nms);
		    break;
		  case 7:
		    testWW(nm,nms);
		    break;
		  case 8:
		    testPellet(nm,nms);
		    break;
		}
	}
	  
	/**
	 * @param nm
	 * @param nms
	 */
	private void testXerces(String nm, String[] nms) {
		 SAXParser sax = new SAXParser();
		 try {
			InputSource in = new InputSource(new URL(nm).openStream());
			sax.parse(in);
			for (int i=0;i<nms.length;i++) {
				in = new InputSource(new URL(nms[i]).openStream());
				sax.parse(in);
			}
			currentMem();
		  sax.equals("");
      
		 }
		 catch (Exception e) {
		 }
		
	}

	/**
	 * @param nm
	 * @param nms
	 */
	private void testWW(String nm, String[] nms) {
//		uk.ac.man.cs.img.owl.validation.SpeciesValidator.main(new String[]{"-q","-d",nm});
 //  runtime.gc();
 //   System.err.println(runtime.totalMemory()-runtime.freeMemory());
	}
//	OWLSpeciesValidator owlTest = new OWLSpeciesValidator();
		/**
		 * @param nm
		 * @param nms
		 */
		private void testPellet(String nm, String[] nms) {
	//		owlTest.getFileLevel(nm);
		
		}

	/**
	 * @param nm
	 * @param nms
	 */
	private void testOWL(String nm, String[] nms) {
		Model m = ModelFactory.createDefaultModel();
		m.read(nm);
		for (int i=0;i<nms.length;i++) {
			   m.read(nms[i]);
		}
		Checker chk = new Checker(false);
	//	chk.noremove();
		chk.addRaw(m.getGraph());
		chk.getSubLanguage();
		currentMem();
		chk.equals("");
		hpHighTide = chk.getHighTide();
		
	}

	/**
	 * @param nm
	 * @param nms
	 */
	private void testJena(String nm, String[] nms) {
		Model m = ModelFactory.createDefaultModel();
		m.read(nm);
		for (int i=0;i<nms.length;i++) {
			   m.read(nms[i]);
		}
		currentMem();
	  m.equals("");
    
	}

	/**
	 * @param nm
	 * @param nms
	 */
	private void testHPNoOps(String nm, String[] nms) {
		StreamingChecker hp = new StreamingChecker(false);
		hp.getRedirect().add(
		"http://www.w3.org/2002/03owlt",
		"file:testing/wg");
		hp.noremove();
		hp.load(nm);
		hp.getSubLanguage();
		  currentMem();
		  hp.equals("");
		hpHighTide = hp.getHighTide();
	}
  int hpHighTide;
	/**
	 * @param nm
	 * @param nms
	 */
	private void testHP(String nm, String[] nms) {
    StreamingChecker hp = new StreamingChecker(false);
    hp.getRedirect().add(
	"http://www.w3.org/2002/03owlt",
	"file:testing/wg");
    hp.load(nm);
    hp.getSubLanguage();
	  currentMem();
	  hp.equals("");
	  hpHighTide = hp.getHighTide();
    
  }

	/**
	 * @param nm
	 * @param nms
	 */
	private void testARP(String nm, String[] nms) {
		ARP a = new ARP();
		try {
			   a.load(new URL(nm).openStream());
			   for (int i=0;i<nms.length;i++) {
				   a.load(new URL(nms[i]).openStream());
				   }
			   currentMem();
				 a.equals("");
      
			}
			catch (Exception e) {
			}
	
	}
	int type;
	boolean warmup;
	PerfTest(int t) {
		type = t;
	}
	void go() {
		
		if ( DoMem )
		new Thread() {
			public void run() {
				while (true) {
					try {
					Thread.sleep(10);
					}
					catch (Exception e) {}
					currentMem();
				}
			}
		}.start();
		
		
		warmup();
		testAll();
	}
	
	void warmup() {
		warmup = true;
		testAll(1);
	}
	
	void testAll() {
		warmup = false;
		testAll(DoMem?1:10);
	}
	
	void testAll(int rep) {
		for (int i=0;i<cnt.length;i++) {
			if (i%20==0)
			  System.err.println(i+"/"+cnt.length);
			long totalTime = 0;
			int totalMem = 0;
			for (int j=0;j<rep;j++) {
				int startMem = restartMem();
			//	if (!warmup)
			//					  System.err.println(startMem);
				
				long startTime = System.currentTimeMillis();
				testNumber(i);
				currentMem();
				totalTime += System.currentTimeMillis() - startTime;
				totalMem += highTide - startMem;
				  
			}
			time[i] = (int)totalTime/rep;
			mem[i] = totalMem/rep;
			if ( type == 4 || type == 5 || type == 6) {
				tripleTide[i] = hpHighTide;
				if (hpHighTide>3000) {
					System.err.println(name[i]+" "+hpHighTide);
				}
				
			}
		}
	}
	int highTide;
	int restartMem() {
		if (!DoMem)
		return 0;
		else {
		runtime.gc();
		runtime.gc();
		runtime.gc();
		runtime.gc();
		highTide = currentMem();
		return highTide;
		}
	}
	static boolean DoMem = false;
	Runtime runtime = Runtime.getRuntime(); 
	int currentMem() {
		if (DoMem) {
		runtime.gc();
		long f = runtime.freeMemory();
		int rslt = (int)(runtime.totalMemory()-f);
		if ( rslt > highTide)
		  highTide = rslt;
		return rslt;
		} else {
			return 0;
		}
	}   
	
	void testNumber(int i) {
		test(name[i],names[i]); 
	}
	
	public void printResults() {
		int low = 0;
		int high;
		int j = 0;
		for (int i=0;i<threshold.length;i++) {
			high = threshold[i];
			int n = 0;
			int t = 0;
			int m = 0;
			int tri = 0;
			int h = 0;
			while (j < cnt.length && cnt[j]<high) {
				n++;
				t+=time[j];
				m+=mem[j];
				tri += cnt[j];
				h += tripleTide[j];
				j++;
			}
			if (n!=0) {
				System.out.print("["+low+","+high+")" + n + " sz: " + (float)tri/n 
				     + " time: " + (float)t/n + " mem: " + (float)m/n);
				if (type == 4 || type == 5 || type == 6)
				  System.out.print(" high: "+(float)h/n);
				System.out.println();
			}
			low = high;
		}
	}
	
	 
	public static void main(String[] args) {
		if (args.length>1) DoMem = true;
		PerfTest p = new PerfTest(Integer.parseInt(args[0]));
		p.go();
		p.printResults();
		System.exit(0);
	}
}

/*
  (c) Copyright 2004 Hewlett-Packard Development Company, LP
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

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
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