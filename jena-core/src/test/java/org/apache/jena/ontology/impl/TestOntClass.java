/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

// Package
///////////////
package org.apache.jena.ontology.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

// Imports
///////////////

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.test.JenaTestLib;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * <p>
 * Misc. tests for OntClass, over and above those in
 * {@link TestClassExpression}
 * </p>
 */
@SuppressWarnings("removal")
public class TestOntClass
{

    static { JenaTestLib.setup(); }

    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    private static final String NS = "http://example.com/test#";

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    @Test
    public void testSuperClassNE() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntClass a = m.createClass( NS + "A" );

        assertNull( a.getSuperClass() );
        assertFalse( a.hasSuperClass() );
    }

    @Test
    public void testSubClassNE() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntClass a = m.createClass( NS + "A" );

        assertNull( a.getSubClass() );
        assertFalse( a.hasSubClass() );
    }

    @Test
    public void testCreateIndividual() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntClass a = m.createClass( NS + "A" );
        Individual i = a.createIndividual( NS + "i" );
        assertTrue( i.hasRDFType(a) );

        Individual j = a.createIndividual();
        assertTrue( j.hasRDFType(a) );
    }

    @Test
    public void testIsHierarchyRoot0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        a.addSubClass( b );
        assertTrue( a.isHierarchyRoot() );
        assertFalse( b.isHierarchyRoot() );
    }

    @Test
    public void testIsHierarchyRoot1() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RULE_INF );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        a.addSubClass( b );
        assertTrue( a.isHierarchyRoot() );
        assertFalse( b.isHierarchyRoot() );
    }

    @Test
    public void testIsHierarchyRoot2() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        a.addSubClass( b );
        assertTrue( a.isHierarchyRoot() );
        assertFalse( b.isHierarchyRoot() );
    }

    @Test
    public void testIsHierarchyRoot3() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_TRANS_INF );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        a.addSubClass( b );
        assertTrue( a.isHierarchyRoot() );
        assertFalse( b.isHierarchyRoot() );
    }

    @Test
    public void testIsHierarchyRoot4() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        a.addSubClass( b );
        assertTrue( a.isHierarchyRoot() );
        assertFalse( b.isHierarchyRoot() );
    }

    @Test
    public void testIsHierarchyRoot5() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        a.addSubClass( b );
        assertTrue( a.isHierarchyRoot() );
        assertFalse( b.isHierarchyRoot() );
    }

    @Test
    public void testIsHierarchyRoot8() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.RDFS_MEM );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        a.addSubClass( b );
        assertTrue( a.isHierarchyRoot() );
        assertFalse( b.isHierarchyRoot() );
    }

    @Test
    public void testIsHierarchyRoot9() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.RDFS_MEM_RDFS_INF );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        a.addSubClass( b );
        assertTrue( a.isHierarchyRoot() );
        assertFalse( b.isHierarchyRoot() );
    }

    @Test
    public void testListSubClasses0() {
        // no inference
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM );
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );
        OntClass c = m.getOntClass( NS + "C" );
        OntClass d = m.getOntClass( NS + "D" );
        OntClass e = m.getOntClass( NS + "E" );

        OntTestUtil.assertIteratorValues(a.listSubClasses(), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(a.listSubClasses( false ), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(a.listSubClasses( true ), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(b.listSubClasses( true ), new Object[] {d,e} );
    }

    @Test
    public void testListSubClasses1() {
        // rule inference
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM_RULE_INF );
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );
        OntClass c = m.getOntClass( NS + "C" );
        OntClass d = m.getOntClass( NS + "D" );
        OntClass e = m.getOntClass( NS + "E" );
        OntClass f = m.getOntClass( NS + "F" );

        OntTestUtil.assertIteratorValues(a.listSubClasses(), new Object[] {b,c,d,e,f} );
        OntTestUtil.assertIteratorValues(a.listSubClasses( false ), new Object[] {b,c,d,e,f} );
        OntTestUtil.assertIteratorValues(a.listSubClasses( true ), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(b.listSubClasses( true ), new Object[] {d,e} );
    }

    @Test
    public void testListSubClasses2() {
        // micro rule inference
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF );
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );
        OntClass c = m.getOntClass( NS + "C" );
        OntClass d = m.getOntClass( NS + "D" );
        OntClass e = m.getOntClass( NS + "E" );
        OntClass f = m.getOntClass( NS + "F" );

        OntTestUtil.assertIteratorValues(a.listSubClasses(), new Object[] {b,c,d,e,f, OWL.Nothing} );
        OntTestUtil.assertIteratorValues(a.listSubClasses( false ), new Object[] {b,c,d,e,f, OWL.Nothing} );
        OntTestUtil.assertIteratorValues(a.listSubClasses( true ), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(b.listSubClasses( true ), new Object[] {d,e} );
    }

    @Test
    public void testListSuperClasses0() {
        // no inference
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM );
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );
        OntClass c = m.getOntClass( NS + "C" );
        OntClass e = m.getOntClass( NS + "E" );

        OntTestUtil.assertIteratorValues(e.listSuperClasses(), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(e.listSuperClasses( false ), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(e.listSuperClasses( true ), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(b.listSuperClasses( true ), new Object[] {a} );
    }

    @Test
    public void testListSuperClasses1() {
        // rule inference
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM_RULE_INF );
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );
        OntClass c = m.getOntClass( NS + "C" );
        OntClass e = m.getOntClass( NS + "E" );

        OntTestUtil.assertIteratorValues(e.listSuperClasses(), new Object[] {b,c,a,RDFS.Resource, OWL.Thing} );
        OntTestUtil.assertIteratorValues(e.listSuperClasses( false ), new Object[] {b,c,a,RDFS.Resource, OWL.Thing} );
        OntTestUtil.assertIteratorValues(e.listSuperClasses( true ), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(b.listSuperClasses( true ), new Object[] {a} );
    }

    @Test
    public void testListSuperClasses2() {
        // micro rule inference
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );
        OntClass c = m.getOntClass( NS + "C" );
        OntClass e = m.getOntClass( NS + "E" );

        OntTestUtil.assertIteratorValues(e.listSuperClasses(), new Object[] {b,c,a, OWL.Thing} );
        OntTestUtil.assertIteratorValues(e.listSuperClasses( false ), new Object[] {b,c,a, OWL.Thing} );
        OntTestUtil.assertIteratorValues(e.listSuperClasses( true ), new Object[] {b,c} );
        OntTestUtil.assertIteratorValues(b.listSuperClasses( true ), new Object[] {a} );
    }

    @Test
    public void testListSuperClasses3() {
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntClass A = m.createClass( NS +"A");
        OntClass B = m.createClass( NS +"B");
        OntClass C = m.createClass( NS +"C");
        A.addSuperClass(B);
        A.addSuperClass(C);
        B.addSuperClass(C);
        C.addSuperClass(B);

        OntTestUtil.assertIteratorValues(A.listSuperClasses( true ), new Object[] {B,C} );
    }

    @Test
    public void testListInstances0() {
        // no inference
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM );
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );

        Individual ia = a.createIndividual();
        Individual ib = b.createIndividual();

        OntTestUtil.assertIteratorValues(a.listInstances(), new Object[] {ia} );
        OntTestUtil.assertIteratorValues(b.listInstances(), new Object[] {ib} );

        OntTestUtil.assertIteratorValues(a.listInstances(true), new Object[] {ia} );
        OntTestUtil.assertIteratorValues(b.listInstances(true), new Object[] {ib} );
    }

    @Test
    public void testListInstances1() {
        // no inference
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM_RULE_INF );
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );
        OntClass c = m.getOntClass( NS + "C" );
        OntClass d = m.getOntClass( NS + "D" );
        OntClass e = m.getOntClass( NS + "E" );

        Individual ia = a.createIndividual(NS + "iA");
        Individual ib = b.createIndividual(NS + "iB");
        Individual ic = c.createIndividual(NS + "iC");
        Individual id = d.createIndividual(NS + "iD");
        Individual ie = e.createIndividual(NS + "iE");

        OntTestUtil.assertIteratorValues(a.listInstances(), new Object[] {ia,ib,ic,id,ie} );
        OntTestUtil.assertIteratorValues(b.listInstances(), new Object[] {ib,id,ie} );

        OntTestUtil.assertIteratorValues(a.listInstances(true), new Object[] {ia} );
        OntTestUtil.assertIteratorValues(b.listInstances(true), new Object[] {ib} );
    }

    @Test
    public void testListInstances2() {
        // no inference
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF );
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );
        OntClass c = m.getOntClass( NS + "C" );
        OntClass d = m.getOntClass( NS + "D" );
        OntClass e = m.getOntClass( NS + "E" );

        Individual ia = a.createIndividual(NS + "iA");
        Individual ib = b.createIndividual(NS + "iB");
        Individual ic = c.createIndividual(NS + "iC");
        Individual id = d.createIndividual(NS + "iD");
        Individual ie = e.createIndividual(NS + "iE");

        OntTestUtil.assertIteratorValues(a.listInstances(), new Object[] {ia,ib,ic,id,ie} );
        OntTestUtil.assertIteratorValues(b.listInstances(), new Object[] {ib,id,ie} );

        OntTestUtil.assertIteratorValues(a.listInstances(true), new Object[] {ia} );
        OntTestUtil.assertIteratorValues(b.listInstances(true), new Object[] {ib} );
    }

    @Test
    public void testDropIndividual() {
        OntModel m = createABCDEFModel( OntModelSpec.OWL_MEM );
        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );
        Individual ia = a.createIndividual(NS + "iA");
        ia.addOntClass( b );

        assertTrue( ia.hasOntClass( a ) );
        assertTrue( ia.hasOntClass( b ) );

        // drop ia from the extension of A
        a.dropIndividual( ia );

        assertFalse( ia.hasOntClass( a ) );
        assertTrue( ia.hasOntClass( b ) );

        // do it again - should be a no-op
        a.dropIndividual( ia );

        assertFalse( ia.hasOntClass( a ) );
        assertTrue( ia.hasOntClass( b ) );

        // drop ia from the extension of b
        b.dropIndividual( ia );

        assertFalse( ia.hasOntClass( a ) );
        assertFalse( ia.hasOntClass( b ) );
    }

    @Test
    public void testDatatypeIsClassOwlFull() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        Resource c = m.createResource();
        c.addProperty( RDF.type, RDFS.Datatype );
        assertTrue( c.canAs( OntClass.class ));
    }

    @Test
    public void testDatatypeIsClassOwlDL() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
        Resource c = m.createResource();
        c.addProperty( RDF.type, RDFS.Datatype );
        assertTrue( c.canAs( OntClass.class ));
    }

    @Test
    public void testDatatypeIsClassOwlLite() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM );
        Resource c = m.createResource();
        c.addProperty( RDF.type, RDFS.Datatype );
        assertTrue( c.canAs( OntClass.class ));
    }

    @Test
    public void testDatatypeIsClassOwlRDFS() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.RDFS_MEM );
        Resource c = m.createResource();
        c.addProperty( RDF.type, RDFS.Datatype );
        assertTrue( c.canAs( OntClass.class ));
    }

    @Test
    public void testOwlThingNothingClass() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );

        Resource r = OWL.Thing.inModel( m );
        OntClass thingClass = r.as( OntClass.class );
        assertNotNull( thingClass );

        r = OWL.Nothing.inModel( m );
        OntClass nothingClass = r.as( OntClass.class );
        assertNotNull( nothingClass );

        OntClass c = m.getOntClass( OWL.Thing.getURI() );
        assertNotNull( c );
        assertEquals( c, OWL.Thing );

        c = m.getOntClass( OWL.Nothing.getURI() );
        assertNotNull( c );
        assertEquals( c, OWL.Nothing );
    }

    // Internal implementation methods
    //////////////////////////////////

    protected OntModel createABCDEFModel( OntModelSpec spec ) {
        OntModel m = ModelFactory.createOntologyModel( spec );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        OntClass c = m.createClass( NS + "C" );
        OntClass d = m.createClass( NS + "D" );
        OntClass e = m.createClass( NS + "E" );
        OntClass f = m.createClass( NS + "F" );

        //      A
        //     / \
        //    B   C
        //   / \ / \
        //  D   E   F

        a.addSubClass( b );
        a.addSubClass( c );
        b.addSubClass( d );
        b.addSubClass( e );
        c.addSubClass( e );
        c.addSubClass( f );
        return m;
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
