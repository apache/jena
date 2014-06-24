/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.vocabulary.OWL;
import junit.framework.*;



/**
 * <p>
 * Unit test cases for creating values in ontology models
 * </p>
 */
public class TestCreate
    extends TestCase
{
    // Constants
    //////////////////////////////////
    public static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    public static final String NS = BASE + "#";

    // Static variables
    //////////////////////////////////

    protected static CreateTestCase[] testCases = new CreateTestCase[] {
        new CreateTestCase( "OWL create resource - typed", ProfileRegistry.OWL_LANG, BASE + "r" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createOntResource( OntResource.class, OWL.Thing, BASE + "r" ); }
            @Override
            public boolean test( OntResource r )        { return r != null;}
        },
        new CreateTestCase( "OWL create resource - untyped", ProfileRegistry.OWL_LANG, BASE + "r" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createOntResource( OntResource.class, null, BASE + "r" ); }
            @Override
            public boolean test( OntResource r )        { return r != null;}
        },
        new CreateTestCase( "OWL create ontology", ProfileRegistry.OWL_LANG, BASE ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createOntology( BASE ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof Ontology;}
        },

        new CreateTestCase( "OWL create class", ProfileRegistry.OWL_LANG, NS + "C" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createClass( NS + "C" ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },

        new CreateTestCase( "OWL create anon complement class", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createComplementClass( null, m.createClass( NS + "A" ) ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof ComplementClass; }
        },
        new CreateTestCase( "OWL create anon enumeration class", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                OntClass A = m.createClass( NS + "A" );
                Individual a0 = m.createIndividual( A );
                Individual a1 = m.createIndividual( A );
                Individual a2 = m.createIndividual( A );
                RDFList l = m.createList( new OntResource[] {a0, a1, a2} );
                return m.createEnumeratedClass( null, l );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof EnumeratedClass; }
        },
        new CreateTestCase( "OWL create anon union class", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                OntClass A = m.createClass( NS + "A" );
                Individual a0 = m.createIndividual( A );
                Individual a1 = m.createIndividual( A );
                Individual a2 = m.createIndividual( A );
                RDFList l = m.createList( new OntResource[] {a0, a1, a2} );
                return m.createUnionClass( null, l );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof UnionClass; }
        },
        new CreateTestCase( "OWL create anon intersection class", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                OntClass A = m.createClass( NS + "A" );
                Individual a0 = m.createIndividual( A );
                Individual a1 = m.createIndividual( A );
                Individual a2 = m.createIndividual( A );
                RDFList l = m.createList( new OntResource[] {a0, a1, a2} );
                return m.createIntersectionClass( null, l );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof IntersectionClass; }
        },

        new CreateTestCase( "OWL create class", ProfileRegistry.OWL_LANG, NS + "C" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createClass( NS + "C" ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },

        new CreateTestCase( "OWL create individual", ProfileRegistry.OWL_LANG, NS + "a" ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( NS + "a", c );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },
        new CreateTestCase( "OWL create anon individual", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( c );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },

        // OWL property types
        new CreateTestCase( "OWL create object property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p" ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty;}
        },
        new CreateTestCase( "OWL create object property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p", false ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty  &&  !r.canAs( FunctionalProperty.class );}
        },
        new CreateTestCase( "OWL create object property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p", true ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty  &&  r.canAs( FunctionalProperty.class );}
        },

        new CreateTestCase( "OWL create transitive property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createTransitiveProperty( NS + "p" ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof TransitiveProperty;
                                                        }
        },
        new CreateTestCase( "OWL create transitive property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createTransitiveProperty( NS + "p", false ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof TransitiveProperty &&
                                                                 !r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create transitive property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createTransitiveProperty( NS + "p", true ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof TransitiveProperty &&
                                                                 r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create symmetric property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createSymmetricProperty( NS + "p" ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof SymmetricProperty;
                                                        }
        },
        new CreateTestCase( "OWL create symmetric property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createSymmetricProperty( NS + "p", false ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof SymmetricProperty &&
                                                                 !r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create symmetric property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createSymmetricProperty( NS + "p", true ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof SymmetricProperty &&
                                                                 r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create inverse functional property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createInverseFunctionalProperty( NS + "p" ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof InverseFunctionalProperty;
                                                        }
        },
        new CreateTestCase( "OWL create inverse functional property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createInverseFunctionalProperty( NS + "p", false ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof InverseFunctionalProperty &&
                                                                 !r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create inverse functional property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createInverseFunctionalProperty( NS + "p", true ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof InverseFunctionalProperty &&
                                                                 r.canAs( FunctionalProperty.class );
                                                        }
        },

        new CreateTestCase( "OWL create datatype property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p" ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty;}
        },
        new CreateTestCase( "OWL create datatype property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p", false ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty  &&  !r.canAs( FunctionalProperty.class );}
        },
        new CreateTestCase( "OWL create datatype property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p", true ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty  &&  r.canAs( FunctionalProperty.class );}
        },

        new CreateTestCase( "OWL create annotation property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createAnnotationProperty( NS + "p" ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof AnnotationProperty;}
        },

        new CreateTestCase( "OWL create allDifferent", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createAllDifferent(); }
            @Override
            public boolean test( OntResource r )        { return r instanceof AllDifferent;}
        },

        // Restrictions

        new CreateTestCase( "OWL create restriction", ProfileRegistry.OWL_LANG, NS + "C" ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createRestriction( NS + "C", null ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },
        new CreateTestCase( "OWL create anon restriction", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   { return m.createRestriction( null ); }
            @Override
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },

        new CreateTestCase( "OWL create has value restriction", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                Property p = m.createObjectProperty( NS + "p" );
                Resource x = m.createResource( NS + "x" );
                return m.createHasValueRestriction( null, p,  x );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof HasValueRestriction;}
        },
        new CreateTestCase( "OWL create has value restriction (literal)", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                Property p = m.createDatatypeProperty( NS + "p" );
                Literal x = m.createTypedLiteral( new Integer( 42 ) );
                return m.createHasValueRestriction( null, p,  x );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof HasValueRestriction;}
        },
        new CreateTestCase( "OWL create all values from restriction", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                Property p = m.createObjectProperty( NS + "p" );
                OntClass c = m.createClass( NS + "C" );
                return m.createAllValuesFromRestriction( null, p,  c );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof AllValuesFromRestriction;}
        },
        new CreateTestCase( "OWL create some values from restriction", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                Property p = m.createObjectProperty( NS + "p" );
                OntClass c = m.createClass( NS + "C" );
                return m.createSomeValuesFromRestriction( null, p,  c );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof SomeValuesFromRestriction;}
        },
        new CreateTestCase( "OWL create cardinality restriction", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                Property p = m.createObjectProperty( NS + "p" );
                return m.createCardinalityRestriction( null, p,  17 );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof CardinalityRestriction;}
        },
        new CreateTestCase( "OWL create min cardinality restriction", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                Property p = m.createObjectProperty( NS + "p" );
                return m.createMinCardinalityRestriction( null, p,  1 );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof MinCardinalityRestriction;}
        },
        new CreateTestCase( "OWL create max cardinality restriction", ProfileRegistry.OWL_LANG, null ) {
            @Override
            public OntResource doCreate( OntModel m )   {
                Property p = m.createObjectProperty( NS + "p" );
                return m.createMaxCardinalityRestriction( null, p,  4 );
            }
            @Override
            public boolean test( OntResource r )        { return r instanceof MaxCardinalityRestriction;}
        },
    };

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestCreate( String name ) {
        super( name );
    }


    // External signature methods
    //////////////////////////////////

    protected String getTestName() {
        return "TestCreate";
    }

    public static TestSuite suite() {
        TestSuite s = new TestSuite( "TestCreate" );

        for ( CreateTestCase testCase : testCases )
        {
            s.addTest( testCase );
        }

        return s;
    }



    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected static class CreateTestCase
        extends TestCase
    {
        protected String m_lang;
        protected String m_uri;

        public CreateTestCase( String name, String lang, String uri ) {
            super( name );
            m_lang = lang;
            m_uri = uri;
        }

        @Override
        public void runTest() {
            OntModel m = ModelFactory.createOntologyModel( m_lang );

            // do the creation step
            OntResource r = doCreate( m );
            assertNotNull( "Result of creation step should not be null", r );

            if (m_uri == null) {
                assertTrue( "Created resource should be anonymous", r.isAnon() );
            }
            else {
                assertEquals( "Created resource has wrong uri", m_uri, r.getURI() );
            }

            assertTrue( "Result test failed", test( r ));
        }

        @Override
        public void setUp() {
            // ensure the ont doc manager is in a consistent state
            OntDocumentManager.getInstance().reset( true );
        }


        /* get the resource */
        public OntResource doCreate( OntModel m ) {
            // to be overridden in sub-classes
            return null;
        }

        /* test the Java type of the result, and other tests */
        public boolean test( OntResource r ) {
            return true;
        }

    }
}
