/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            02-Apr-2003
 * Filename           $RCSfile: TestListSyntaxCategories.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2009-10-06 13:04:42 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * <p>
 * Unit tests for listXXX methods on ontmodel
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: TestListSyntaxCategories.java,v 1.2 2009-10-06 13:04:42 ian_dickinson Exp $
 */
public class TestListSyntaxCategories
    extends TestCase
{
    // Constants
    //////////////////////////////////
    public static final String NS = "http://jena.hpl.hp.com/testing/ontology#";


    // Static variables
    //////////////////////////////////

    protected static DoListTest[] testCases = {
        // Ontology
        new DoListTest( "OWL list ontologies",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {"http://jena.hpl.hp.com/testing/ontology"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listOntologies();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Ontology;
            }
        },
        new DoListTest( "DAML list ontologies",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        new String[] {"http://jena.hpl.hp.com/testing/ontology"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listOntologies();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Ontology;
            }
        },
        // Properties
        new DoListTest( "OWL list properties",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  2,
                        new String[] {NS+"p",NS+"karma"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listOntProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "OWL list properties",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM,  1,
                        new String[] {NS+"p"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listOntProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "OWL list object properties",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  2,
                        new String[] {NS+"op", NS+"op1"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listObjectProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "OWL list datatype properties",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"dp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listDatatypeProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "OWL list functional properties",  "file:testing/ontology/owl/list-syntax/test-proptypes.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"fp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listFunctionalProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof FunctionalProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "OWL list transitive properties",  "file:testing/ontology/owl/list-syntax/test-proptypes.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"tp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listTransitiveProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof TransitiveProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "OWL list symmetric properties",  "file:testing/ontology/owl/list-syntax/test-proptypes.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"sp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listSymmetricProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof SymmetricProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "OWL list inverse functional properties",  "file:testing/ontology/owl/list-syntax/test-proptypes.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"ifp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listInverseFunctionalProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof InverseFunctionalProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "DAML list properties",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  5,
                        new String[] {NS+"p", NS+"rdf-p", NS+"dp", NS+"op", NS+"op1"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listOntProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "DAML list object properties",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  2,
                        new String[] {NS+"op", NS+"op1"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listObjectProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "DAML list datatype properties",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        new String[] {NS+"dp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listDatatypeProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "DAML list functional properties",  "file:testing/ontology/daml/list-syntax/test-proptypes.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        new String[] {NS+"fp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listFunctionalProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof FunctionalProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "DAML list transitive properties",  "file:testing/ontology/daml/list-syntax/test-proptypes.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        new String[] {NS+"tp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listTransitiveProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof TransitiveProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "DAML list inverse functional properties",  "file:testing/ontology/daml/list-syntax/test-proptypes.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        new String[] {NS+"ifp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listInverseFunctionalProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof InverseFunctionalProperty &&
                       r instanceof Property;
            }
        },

        // individuals
        new DoListTest( "OWL list individuals",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  8,
                        new String[] {NS+"A0", NS+"A1", NS+"C0", NS+"a0", NS+"a1", NS+"a2", NS+"z0", NS+"z1"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "OWL list typed individuals",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM,  2,
                new String[] {NS+"A0", NS+"A1"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                Model mVocab = ModelFactory.createDefaultModel();
                Resource cA = mVocab.createResource( "http://jena.hpl.hp.com/testing/ontology#A");
                return m.listIndividuals( cA );
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "OWL list individuals negative case 1",  null,  OntModelSpec.OWL_MEM,  0,
                new String[] {} )
        {
            @Override
            protected void addAxioms( OntModel m ) {
                // A0 should not an individual
                m.add( m.createResource( NS + "A0"), RDF.type, OWL.Class );
            }
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "OWL list individuals negative case 2",  null,  OntModelSpec.OWL_MEM_MICRO_RULE_INF,  0,
                new String[] {} )
        {
            @Override
            protected void addAxioms( OntModel m ) {
                // A0 should not an individual
                m.add( m.createResource( NS + "A0"), RDF.type, OWL.Class );
            }
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "OWL list individuals negative case 3",  null,  OntModelSpec.OWL_MEM,  0,
                new String[] {} )
        {
            @Override
            protected void addAxioms( OntModel m ) {
                // A0 should not an individual, even though we have materialised some of the entailment triples
                Resource a0 = m.createResource( NS + "A0");
                m.add( a0, RDF.type, OWL.Class );
                m.add( OWL.Class, RDF.type, OWL.Class );
            }
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "OWL list individuals negative case 4",  null,  OntModelSpec.OWL_MEM,  0,
                new String[] {} )
        {
            @Override
            protected void addAxioms( OntModel m ) {
                // A0 should not an individual, even though we have materialised some of the entailment triples
                Resource a0 = m.createResource( NS + "A0");
                m.add( a0, RDF.type, OWL.Class );
                m.add( OWL.Class, RDF.type, RDFS.Class );
            }
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "OWL list individuals - punning",  null,  OntModelSpec.OWL_MEM,  1,
                new String[] {NS + "A0"} )
        {
            @Override
            protected void addAxioms( OntModel m ) {
                // A0 should be an individual, since we are punning
                Resource a0 = m.createResource( NS + "A0");
                Resource a1 = m.createResource( NS + "A1");
                m.add( a0, RDF.type, OWL.Class );
                m.add( a1, RDF.type, OWL.Class );
                m.add( a0, RDF.type, a1 );
            }
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "empty OWL list individuals",  null,  OntModelSpec.OWL_MEM,  0, new String[] {} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "empty OWL+rule list individuals",  null,  OntModelSpec.OWL_MEM_RULE_INF,  0, new String[] {} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "empty OWL+RDFS rule list individuals (bug report JENA-3)",  null,  OntModelSpec.RDFS_MEM_RDFS_INF,  0, new String[] {} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "DAML list individuals",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  6,
                        new String[] {NS+"A0", NS+"A1", NS+"C0", NS+"a1", NS+"a2", NS+"a0"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "empty DAML+rule list individuals",  null,  OntModelSpec.DAML_MEM_RULE_INF,  0, new String[] {} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "OWL list individuals with inference",  "file:testing/ontology/owl/list-syntax/owlDemoSchema.xml",  OntModelSpec.OWL_LITE_MEM_RULES_INF,  6, null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },
        new DoListTest( "OWL list individuals in composite model",  null,  OntModelSpec.OWL_MEM, 1, new String[] {"http://example.com/foo#anInd"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel schema ) {
                Model data = ModelFactory.createDefaultModel();
                Resource c = schema.createResource( "http://example.com/foo#AClass" );
                Resource i = data.createResource( "http://example.com/foo#anInd" );
                schema.add( c, RDF.type, OWL.Class );
                data.add( i, RDF.type, c );

                OntModel composite = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, schema );
                composite.addSubModel( data );

                return composite.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },

        new DoListTest( "OWL list all different",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listAllDifferent();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof AllDifferent;
            }
        },
        new DoListTest( "DAML list all different",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  0,  null,
                        true /* exception expected */ )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listAllDifferent();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof AllDifferent;
            }
        },

        // classes
        new DoListTest( "OWL list classes",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  11,
                        new String[] {NS+"A", NS+"B", NS+"C", NS+"D", NS+"E", NS+"X0", NS+"X1", NS+"Y0", NS+"Y1", NS+"Z", } )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL list named classes",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  10,
                        new String[] {NS+"A", NS+"B", NS+"C", NS+"D", NS+"E", NS+"X0", NS+"X1", NS+"Y0", NS+"Y1", NS+"Z", } )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listNamedClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL list intersection classes",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"A" } )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIntersectionClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL list union classes",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"B"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listUnionClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL list complement classes",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"C"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listComplementClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL list enumerated classes",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"D"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listEnumeratedClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL list restrictions",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listRestrictions();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Restriction;
            }
        },
        new DoListTest( "DAML list classes",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  12,
                        new String[] {NS+"A", NS+"B", NS+"C", NS+"D", NS+"E", NS+"X0", NS+"X1", NS+"Y0", NS+"Y1", NS+"Z", DAML_OIL.Thing.getURI()} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "DAML list named classes",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  11,
                        new String[] {NS+"A", NS+"B", NS+"C", NS+"D", NS+"E", NS+"X0", NS+"X1", NS+"Y0", NS+"Y1", NS+"Z", DAML_OIL.Thing.getURI()} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listNamedClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "DAML list intersection classes",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        new String[] {NS+"A" } )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIntersectionClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "DAML list union classes",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        new String[] {NS+"B"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listUnionClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "DAML list complement classes",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        new String[] {NS+"C"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listComplementClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "DAML list enumerated classes",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        new String[] {NS+"D"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listEnumeratedClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "DAML list restrictions",  "file:testing/ontology/daml/list-syntax/test.rdf",  OntModelSpec.DAML_MEM_RULE_INF,  1,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listRestrictions();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Restriction;
            }
        },

        // Annotation property
        new DoListTest( "OWL list annotation properties",  "file:testing/ontology/owl/list-syntax/test.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listAnnotationProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof AnnotationProperty;
            }
        },

        // !!!!!!! Following tests use ontology that imports owl.owl !!!!!!!!!!!

        // ontologies
        new DoListTest( "OWL+import list ontologies",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  2,
                        new String[] {"http://jena.hpl.hp.com/testing/ontology", "http://www.w3.org/2002/07/owl"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listOntologies();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Ontology;
            }
        },
        // Properties
        new DoListTest( "OWL+import list properties",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  46,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listOntProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "OWL+import list object properties",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  2,
                        new String[] {NS+"op", NS+"op1"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listObjectProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },
        new DoListTest( "OWL+import list datatype properties",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"dp"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listDatatypeProperties();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntProperty &&
                       r instanceof Property;
            }
        },

        // individuals
        new DoListTest( "OWL+import list individuals",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  8,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIndividuals();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Individual;
            }
        },

        new DoListTest( "OWL+import list all different",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listAllDifferent();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof AllDifferent;
            }
        },

        // classes
        new DoListTest( "OWL+import list classes",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  14,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL+import list named classes",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  12,
                        new String[] {NS+"A", NS+"B", NS+"C", NS+"D", NS+"E", NS+"X0", NS+"X1", NS+"Y0", NS+"Y1", NS+"Z",
                                      OWL.Thing.getURI(), OWL.Nothing.getURI()} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listNamedClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL+import list intersection classes",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"A" } )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listIntersectionClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL+import list union classes",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  2,
                        new String[] {NS+"B", OWL.Thing.getURI()} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listUnionClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL+import list complement classes",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  3,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listComplementClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL+import list enumerated classes",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        new String[] {NS+"D"} )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listEnumeratedClasses();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof OntClass;
            }
        },
        new DoListTest( "OWL+import list restrictions",  "file:testing/ontology/owl/list-syntax/test-with-import.rdf",  OntModelSpec.OWL_MEM_TRANS_INF,  1,
                        null )
        {
            @Override
            public Iterator< ? extends Resource> doList( OntModel m ) {
                return m.listRestrictions();
            }
            @Override
            public boolean test( Resource r ) {
                return r instanceof Restriction;
            }
        },
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestListSyntaxCategories( String name ) {
        super( name );
    }



    // External signature methods
    //////////////////////////////////

    public static TestSuite suite() {
        TestSuite s = new TestSuite( "TestListSyntaxCategories" );

        for (int i = 0;  i < testCases.length;  i++) {
            s.addTest( testCases[i] );
        }

        return s;
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected static class DoListTest
        extends TestCase
    {
        protected String m_fileName;
        protected OntModelSpec m_spec;
        protected int m_count;
        protected String[] m_expected;
        protected boolean m_exExpected;     // exception expected during list operation

        protected DoListTest( String name, String fileName, OntModelSpec spec, int count, String[] expected ) {
            this( name, fileName, spec, count, expected, false );
        }

        protected DoListTest( String name, String fileName, OntModelSpec spec, int count, String[] expected, boolean exExpected ) {
            super( name );
            m_fileName = fileName;
            m_spec = spec;
            m_count = count;
            m_expected = expected;
            m_exExpected = exExpected;
        }

        @Override
        public void setUp() {
            // ensure the ont doc manager is in a consistent state
            OntDocumentManager.getInstance().reset( true );
        }


        @Override
        public void runTest() {
            Logger logger = LoggerFactory.getLogger( getClass() );
            OntModel m = ModelFactory.createOntologyModel( m_spec, null );
            m.getDocumentManager().setMetadataSearchPath( "file:etc/ont-policy-test.rdf", true );

            if (m_fileName != null) {
                m.read( m_fileName );
            }

            // hook to add extra axioms
            addAxioms( m );

            boolean exOccurred = false;
            Iterator<? extends Resource> i = null;
            try {
                i = doList( m );
            }
            catch (OntologyException e) {
                exOccurred = true;
            }

            assertEquals( "Ontology exception" + (m_exExpected ? " was " : " was not ") + "expected", m_exExpected, exOccurred );

            if (!exOccurred) {
                List<Resource> expected = expected( m );
                List<Resource> actual = new ArrayList<Resource>();
                int extraneous = 0;

                // now we walk the iterator
                while (i.hasNext()) {
                    Resource res = i.next();
                    assertTrue( "Should not fail node test on " + res, test( res ));

                    actual.add( res );
                    if (expected != null) {
                        if (expected.contains( res )) {
                            expected.remove( res );
                        }
                        else {
                            if (!res.isAnon()) {
                                // since we can't list expected anon resources, we ignore them in this check
                                extraneous++;
                                logger.debug( "found extraneous result: " + res );
                            }
                        }
                    }
                }

                // debugging
                if (m_count != actual.size()) {
                    logger.debug( getName() + " - expected " + m_count + " results, actual = " + actual.size() );
                    for (Iterator<Resource> j = actual.iterator(); j.hasNext(); ) {
                        logger.debug( getName() + " - saw actual: " + j.next() );
                    }
                }
                if (expected != null && !expected.isEmpty()) {
                    for (Iterator<Resource> j = expected.iterator(); j.hasNext(); ) {
                        logger.debug( getName() + " - expected but did not find: " + j.next() );
                    }
                }

                assertEquals( getName() + ": wrong number of results returned", m_count, actual.size() );
                if (expected != null) {
                    assertTrue( "Did not find all expected resources in iterator", expected.isEmpty() );
                    assertEquals( "Found extraneous results, not in expected list", 0, extraneous );
                }
            }
        }

        /* get the iterator */
        public Iterator<? extends Resource> doList( OntModel m ) {
            // should be overriden in sub-classes
            return null;
        }

        /* test the Java type of the result, and other tests */
        public boolean test( Resource r ) {
            return true;
        }

        protected List<Resource> expected( OntModel m ) {
            if (m_expected != null) {
                List<Resource> expected = new ArrayList<Resource>();

                for (int i = 0;  i < m_expected.length; i++) {
                    expected.add( m.getResource( m_expected[i] ) );
                }

                return expected;
            }
            else {
                return null;
            }
        }

        /** Add extra axioms hook */
        protected void addAxioms( OntModel m ) {
            // default is no-op
        }
    }
}


/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
