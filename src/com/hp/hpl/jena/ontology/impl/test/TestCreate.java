/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            03-Apr-2003
 * Filename           $RCSfile: TestCreate.java,v $
 * Revision           $Revision: 1.12 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:27 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;

import junit.framework.*;



/**
 * <p>
 * Unit test cases for creating values in ontology models
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestCreate.java,v 1.12 2004-12-06 13:50:27 andy_seaborne Exp $
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
        new CreateTestCase( "OWL create ontology", ProfileRegistry.OWL_LANG, BASE ) {
            public OntResource doCreate( OntModel m )   { return m.createOntology( BASE ); }
            public boolean test( OntResource r )        { return r instanceof Ontology;}
        },
        new CreateTestCase( "DAML create ontology", ProfileRegistry.DAML_LANG, BASE ) {
            public OntResource doCreate( OntModel m )   { return m.createOntology( BASE ); }
            public boolean test( OntResource r )        { return r instanceof Ontology;}
        },
        
        new CreateTestCase( "OWL create class", ProfileRegistry.OWL_LANG, NS + "C" ) {
            public OntResource doCreate( OntModel m )   { return m.createClass( NS + "C" ); }
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },
        
        new CreateTestCase( "OWL create anon complement class", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createComplementClass( null, m.createClass( NS + "A" ) ); }
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof ComplementClass; }
        },
        new CreateTestCase( "OWL create anon enumeration class", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   {
                OntClass A = m.createClass( NS + "A" ); 
                Individual a0 = m.createIndividual( A );
                Individual a1 = m.createIndividual( A );
                Individual a2 = m.createIndividual( A );
                RDFList l = m.createList( new OntResource[] {a0, a1, a2} );
                return m.createEnumeratedClass( null, l ); 
            }
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof EnumeratedClass; }
        },
        new CreateTestCase( "OWL create anon union class", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   {
                OntClass A = m.createClass( NS + "A" ); 
                Individual a0 = m.createIndividual( A );
                Individual a1 = m.createIndividual( A );
                Individual a2 = m.createIndividual( A );
                RDFList l = m.createList( new OntResource[] {a0, a1, a2} );
                return m.createUnionClass( null, l ); 
            }
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof UnionClass; }
        },
        new CreateTestCase( "OWL create anon intersection class", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   {
                OntClass A = m.createClass( NS + "A" ); 
                Individual a0 = m.createIndividual( A );
                Individual a1 = m.createIndividual( A );
                Individual a2 = m.createIndividual( A );
                RDFList l = m.createList( new OntResource[] {a0, a1, a2} );
                return m.createIntersectionClass( null, l ); 
            }
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof IntersectionClass; }
        },
        
        new CreateTestCase( "OWL create class", ProfileRegistry.OWL_LANG, NS + "C" ) {
            public OntResource doCreate( OntModel m )   { return m.createClass( NS + "C" ); }
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },

        new CreateTestCase( "DAML create class", ProfileRegistry.DAML_LANG, NS + "C" ) {
            public OntResource doCreate( OntModel m )   { return m.createClass( NS + "C" ); }
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },
        new CreateTestCase( "DAML create anon class", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createClass(); }
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },
        
        new CreateTestCase( "DAML create anon complement class", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createComplementClass( null, m.createClass( NS + "A" ) ); }
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof ComplementClass; }
        },
        new CreateTestCase( "DAML create anon enumeration class", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   {
                OntClass A = m.createClass( NS + "A" ); 
                Individual a0 = m.createIndividual( A );
                Individual a1 = m.createIndividual( A );
                Individual a2 = m.createIndividual( A );
                RDFList l = m.createList( new OntResource[] {a0, a1, a2} );
                return m.createEnumeratedClass( null, l ); 
            }
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof EnumeratedClass; }
        },
        new CreateTestCase( "DAML create anon union class", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   {
                OntClass A = m.createClass( NS + "A" ); 
                Individual a0 = m.createIndividual( A );
                Individual a1 = m.createIndividual( A );
                Individual a2 = m.createIndividual( A );
                RDFList l = m.createList( new OntResource[] {a0, a1, a2} );
                return m.createUnionClass( null, l ); 
            }
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof UnionClass; }
        },
        new CreateTestCase( "DAML create anon intersection class", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   {
                OntClass A = m.createClass( NS + "A" ); 
                Individual a0 = m.createIndividual( A );
                Individual a1 = m.createIndividual( A );
                Individual a2 = m.createIndividual( A );
                RDFList l = m.createList( new OntResource[] {a0, a1, a2} );
                return m.createIntersectionClass( null, l ); 
            }
            public boolean test( OntResource r )        { return r instanceof OntClass && r instanceof IntersectionClass; }
        },

        new CreateTestCase( "OWL create individual", ProfileRegistry.OWL_LANG, NS + "a" ) {
            public OntResource doCreate( OntModel m )   { 
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( NS + "a", c ); 
            }
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },
        new CreateTestCase( "OWL create anon individual", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( c ); 
            }
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },
        new CreateTestCase( "DAML create individual", ProfileRegistry.DAML_LANG, NS + "a" ) {
            public OntResource doCreate( OntModel m )   { 
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( NS + "a", c ); 
            }
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },
        new CreateTestCase( "DAML create anon individual", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( c ); 
            }
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },
        
        // OWL property types
        new CreateTestCase( "OWL create object property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty;}
        },
        new CreateTestCase( "OWL create object property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p", false ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty  &&  !r.canAs( FunctionalProperty.class );}
        },
        new CreateTestCase( "OWL create object property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p", true ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty  &&  r.canAs( FunctionalProperty.class );}
        },
        
        new CreateTestCase( "OWL create transitive property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createTransitiveProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof TransitiveProperty;
                                                        }
        },
        new CreateTestCase( "OWL create transitive property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createTransitiveProperty( NS + "p", false ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof TransitiveProperty &&
                                                                 !r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create transitive property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createTransitiveProperty( NS + "p", true ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof TransitiveProperty &&
                                                                 r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create symmetric property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createSymmetricProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof SymmetricProperty;
                                                        }
        },
        new CreateTestCase( "OWL create symmetric property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createSymmetricProperty( NS + "p", false ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof SymmetricProperty &&
                                                                 !r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create symmetric property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createSymmetricProperty( NS + "p", true ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof SymmetricProperty &&
                                                                 r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create inverse functional property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createInverseFunctionalProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof InverseFunctionalProperty;
                                                        }
        },
        new CreateTestCase( "OWL create inverse functional property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createInverseFunctionalProperty( NS + "p", false ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof InverseFunctionalProperty &&
                                                                 !r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "OWL create inverse functional property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createInverseFunctionalProperty( NS + "p", true ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof InverseFunctionalProperty &&
                                                                 r.canAs( FunctionalProperty.class );
                                                        }
        },
        
        new CreateTestCase( "OWL create datatype property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty;}
        },
        new CreateTestCase( "OWL create datatype property non-F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p", false ); }
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty  &&  !r.canAs( FunctionalProperty.class );}
        },
        new CreateTestCase( "OWL create datatype property F", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p", true ); }
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty  &&  r.canAs( FunctionalProperty.class );}
        },
        
        new CreateTestCase( "OWL create annotation property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createAnnotationProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof AnnotationProperty;}
        },
        
        // DAML property types
        new CreateTestCase( "DAML create object property", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty;}
        },
        new CreateTestCase( "DAML create object property non-F", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p", false ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty  &&  !r.canAs( FunctionalProperty.class );}
        },
        new CreateTestCase( "DAML create object property F", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p", true ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty  &&  r.canAs( FunctionalProperty.class );}
        },
        
        new CreateTestCase( "DAML create transitive property", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createTransitiveProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof TransitiveProperty;
                                                        }
        },
        new CreateTestCase( "DAML create transitive property non-F", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createTransitiveProperty( NS + "p", false ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof TransitiveProperty &&
                                                                 !r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "DAML create transitive property F", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createTransitiveProperty( NS + "p", true ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof TransitiveProperty &&
                                                                 r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "DAML create inverse functional property", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createInverseFunctionalProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof InverseFunctionalProperty;
                                                        }
        },
        new CreateTestCase( "DAML create inverse functional property non-F", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createInverseFunctionalProperty( NS + "p", false ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof InverseFunctionalProperty &&
                                                                 !r.canAs( FunctionalProperty.class );
                                                        }
        },
        new CreateTestCase( "DAML create inverse functional property F", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createInverseFunctionalProperty( NS + "p", true ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty &&
                                                                 r instanceof InverseFunctionalProperty &&
                                                                 r.canAs( FunctionalProperty.class );
                                                        }
        },
        
        new CreateTestCase( "DAML create datatype property", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty;}
        },
        new CreateTestCase( "DAML create datatype property non-F", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p", false ); }
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty  &&  !r.canAs( FunctionalProperty.class );}
        },
        new CreateTestCase( "DAML create datatype property F", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p", true ); }
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty  &&  r.canAs( FunctionalProperty.class );}
        },
        
        new CreateTestCase( "OWL create allDifferent", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createAllDifferent(); }
            public boolean test( OntResource r )        { return r instanceof AllDifferent;}
        },
        
        // Restrictions
        
        new CreateTestCase( "OWL create restriction", ProfileRegistry.OWL_LANG, NS + "C" ) {
            public OntResource doCreate( OntModel m )   { return m.createRestriction( NS + "C", null ); }
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },
        new CreateTestCase( "OWL create anon restriction", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createRestriction( null ); }
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },
        
        new CreateTestCase( "OWL create has value restriction", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                Resource x = m.createResource( NS + "x" );
                return m.createHasValueRestriction( null, p,  x ); 
            }
            public boolean test( OntResource r )        { return r instanceof HasValueRestriction;}
        },
        new CreateTestCase( "OWL create has value restriction (literal)", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createDatatypeProperty( NS + "p" );
                Literal x = m.createTypedLiteral( new Integer( 42 ) );
                return m.createHasValueRestriction( null, p,  x ); 
            }
            public boolean test( OntResource r )        { return r instanceof HasValueRestriction;}
        },
        new CreateTestCase( "OWL create all values from restriction", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                OntClass c = m.createClass( NS + "C" );
                return m.createAllValuesFromRestriction( null, p,  c ); 
            }
            public boolean test( OntResource r )        { return r instanceof AllValuesFromRestriction;}
        },
        new CreateTestCase( "OWL create some values from restriction", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                OntClass c = m.createClass( NS + "C" );
                return m.createSomeValuesFromRestriction( null, p,  c ); 
            }
            public boolean test( OntResource r )        { return r instanceof SomeValuesFromRestriction;}
        },
        new CreateTestCase( "OWL create cardinality restriction", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                return m.createCardinalityRestriction( null, p,  17 ); 
            }
            public boolean test( OntResource r )        { return r instanceof CardinalityRestriction;}
        },
        new CreateTestCase( "OWL create min cardinality restriction", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                return m.createMinCardinalityRestriction( null, p,  1 ); 
            }
            public boolean test( OntResource r )        { return r instanceof MinCardinalityRestriction;}
        },
        new CreateTestCase( "OWL create max cardinality restriction", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                return m.createMaxCardinalityRestriction( null, p,  4 ); 
            }
            public boolean test( OntResource r )        { return r instanceof MaxCardinalityRestriction;}
        },
        
        new CreateTestCase( "DAML create restriction", ProfileRegistry.DAML_LANG, NS + "C" ) {
            public OntResource doCreate( OntModel m )   { return m.createRestriction( NS + "C", null ); }
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },
        new CreateTestCase( "DAML create anon restriction", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createRestriction( null ); }
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },
        
        new CreateTestCase( "DAML create has value restriction", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                Resource x = m.createResource( NS + "x" );
                return m.createHasValueRestriction( null, p,  x ); 
            }
            public boolean test( OntResource r )        { return r instanceof HasValueRestriction;}
        },
        new CreateTestCase( "DAML create has value restriction (literal)", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createDatatypeProperty( NS + "p" );
                Literal x = m.createTypedLiteral( new Integer( 42 ) );
                return m.createHasValueRestriction( null, p,  x ); 
            }
            public boolean test( OntResource r )        { return r instanceof HasValueRestriction;}
        },
        new CreateTestCase( "DAML create all values from restriction", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                OntClass c = m.createClass( NS + "C" );
                return m.createAllValuesFromRestriction( null, p,  c ); 
            }
            public boolean test( OntResource r )        { return r instanceof AllValuesFromRestriction;}
        },
        new CreateTestCase( "DAML create some values from restriction", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                OntClass c = m.createClass( NS + "C" );
                return m.createSomeValuesFromRestriction( null, p,  c ); 
            }
            public boolean test( OntResource r )        { return r instanceof SomeValuesFromRestriction;}
        },
        new CreateTestCase( "DAML create cardinality restriction", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                return m.createCardinalityRestriction( null, p,  17 ); 
            }
            public boolean test( OntResource r )        { return r instanceof CardinalityRestriction;}
        },
        new CreateTestCase( "DAML create min cardinality restriction", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                return m.createMinCardinalityRestriction( null, p,  1 ); 
            }
            public boolean test( OntResource r )        { return r instanceof MinCardinalityRestriction;}
        },
        new CreateTestCase( "DAML create max cardinality restriction", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                Property p = m.createObjectProperty( NS + "p" );
                return m.createMaxCardinalityRestriction( null, p,  4 ); 
            }
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


/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
