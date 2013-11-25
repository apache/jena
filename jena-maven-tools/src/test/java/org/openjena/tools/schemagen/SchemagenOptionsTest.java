/*****************************************************************************
 * File:    SchemagenOptionsTest.java
 * Project: schemagen
 * Created: 2 May 2010
 * By:      ian
 *
 * Copyright (c) 2010-11 Epimorphics Ltd. See LICENSE file for license terms.
 *****************************************************************************/

// Package
///////////////

package org.openjena.tools.schemagen;


// Imports
///////////////

import static org.junit.Assert.*;

import java.util.List;

import jena.schemagen.SchemagenOptions.OPT;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * <p>Unit tests for {@link SchemagenOptions}</p>
 *
 * @author ian
 */
public class SchemagenOptionsTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( SchemagenOptionsTest.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        //
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#getParent()}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testGetParent() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so = new SchemagenOptions();
        assertNull( so.getParent() );
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#setParent(org.openjena.tools.schemagen.SchemagenOptions)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testSetParent() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        assertSame( so1, so0.getParent() );
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#hasParent()}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testHasParent() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        assertTrue( so0.hasParent() );
        assertFalse( so1.hasParent() );
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#getOption(jena.schemagen.SchemagenOptions.OPT)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testGetOption0() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        assertNull( so0.getOption( OPT.CLASS_SECTION ));
    }

    @Test
    public void testGetOption1() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        assertNull( so0.getOption( OPT.CLASS_SECTION ));
    }

    @Test
    public void testGetOption2() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        so0.setOption( OPT.CLASS_SECTION, "test123" );
        assertEquals( "test123", so0.getOption( OPT.CLASS_SECTION ).asLiteral().getString() );
    }

    @Test
    public void testGetOption3() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        so0.setOption( OPT.CLASS_SECTION, "test123" );
        assertEquals( "test123", so0.getOption( OPT.CLASS_SECTION ).asLiteral().getString() );
    }

    @Test
    public void testGetOption4() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        so1.setOption( OPT.CLASS_SECTION, "test123" );
        assertEquals( "test123", so0.getOption( OPT.CLASS_SECTION ).asLiteral().getString());
    }

    @Test
    public void testGetOption5() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        so0.setOption( OPT.CLASS_SECTION, "test.child" );
        so1.setOption( OPT.CLASS_SECTION, "test.parent" );
        assertEquals( "test.child", so0.getOption( OPT.CLASS_SECTION ).asLiteral().getString());
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#setOption(java.lang.String, java.lang.String)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testSetOptionStringString() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        so0.setOption( "CLASS_SECTION", "test123" );
        assertEquals( "test123", so0.getOption( OPT.CLASS_SECTION ).asLiteral().getString() );
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#asOption(java.lang.String)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testAsOption() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        assertSame( OPT.DOS, so0.asOption( "DOS" ));
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#isTrue(jena.schemagen.SchemagenOptions.OPT)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testIsTrueOPT0() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        assertFalse( so0.isTrue( OPT.DOS ));
    }

    @Test
    public void testIsTrueOPT1() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        so0.setOption( OPT.DOS, true );
        assertTrue( so0.isTrue( OPT.DOS ));
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#hasValue(jena.schemagen.SchemagenOptions.OPT)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testHasValueOPT0() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        assertFalse( so0.hasValue( OPT.CLASS_SECTION ));
    }

    @Test
    public void testHasValueOPT1() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        so0.setOption( OPT.CLASS_SECTION, "foo" );
        assertTrue( so0.hasValue( OPT.CLASS_SECTION ));
    }

    @Test
    public void testHasValueOPT2() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        so1.setOption( OPT.CLASS_SECTION, "foo" );
        assertTrue( so0.hasValue( OPT.CLASS_SECTION ));
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#getValue(jena.schemagen.SchemagenOptions.OPT)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testGetValueOPT0() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        assertNull( so0.getValue( OPT.CLASS_SECTION ));
    }

    @Test
    public void testGetValueOPT1() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        so0.setOption( OPT.CLASS_SECTION, "foo" );
        assertEquals( "foo", so0.getValue( OPT.CLASS_SECTION ).asLiteral().getString() );
    }

    @Test
    public void testGetValueOPT2() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        so1.setOption( OPT.CLASS_SECTION, "foo" );
        assertEquals( "foo", so0.getValue( OPT.CLASS_SECTION ).asLiteral().getString() );
    }

    @Test
    public void testGetValueOPT3() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        so1.setOption( OPT.CLASS_SECTION, "foo" );
        so0.setOption( OPT.CLASS_SECTION, "bar" );
        assertEquals( "bar", so0.getValue( OPT.CLASS_SECTION ).asLiteral().getString() );
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#hasResourceValue(jena.schemagen.SchemagenOptions.OPT)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testHasResourceValueOPT0() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        assertFalse( so0.hasResourceValue( OPT.ROOT ));
    }

    @Test
    public void testHasResourceValueOPT1() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        Resource r = ResourceFactory.createResource( "http://example.org/foo" );
        so0.setOption( OPT.ROOT, r );
        assertTrue( so0.hasResourceValue( OPT.ROOT ));
    }

    @Test
    public void testHasResourceValueOPT2() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        Resource r = ResourceFactory.createResource( "http://example.org/foo" );
        so1.setOption( OPT.ROOT, r );
        assertTrue( so0.hasResourceValue( OPT.ROOT ));
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#getResource(jena.schemagen.SchemagenOptions.OPT)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testGetResourceOPT0() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        assertNull( so0.getResource( OPT.ROOT ));
    }

    @Test
    public void testGetResourceValueOPT1() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        Resource r = ResourceFactory.createResource( "http://example.org/foo" );
        so0.setOption( OPT.ROOT, r );
        assertEquals( r, so0.getResource( OPT.ROOT ));
    }

    @Test
    public void testGetResourceValueOPT2() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        Resource r = ResourceFactory.createResource( "http://example.org/foo" );
        so1.setOption( OPT.ROOT, r );
        assertEquals( r, so0.getResource( OPT.ROOT ));
    }

    @Test
    public void testGetResourceValueOPT3() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        Resource r0 = ResourceFactory.createResource( "http://example.org/foo" );
        Resource r1 = ResourceFactory.createResource( "http://example.org/bar" );
        so0.setOption( OPT.ROOT, r0 );
        so1.setOption( OPT.ROOT, r1 );
        assertEquals( r0, so0.getResource( OPT.ROOT ));
    }

    /**
     * Test method for {@link org.openjena.tools.schemagen.SchemagenOptions#getAllValues(jena.schemagen.SchemagenOptions.OPT)}.
     * @throws SchemagenOptionsConfigurationException 
     */
    @Test
    public void testGetAllValuesOPT0() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        List<String> l = so0.getAllValues( OPT.INCLUDE );
        assertNotNull( l );
        assertTrue( l.isEmpty() );
    }

    @Test
    public void testGetAllValuesOPT1() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        so0.setOption( OPT.INCLUDE, "foo" );
        List<String> l = so0.getAllValues( OPT.INCLUDE );
        assertNotNull( l );
        assertEquals( 1, l.size() );
        assertTrue( l.contains( "foo" ));
    }

    @Test
    public void testGetAllValuesOPT2() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        so0.setOption( OPT.INCLUDE, "foo" );
        so0.setOption( OPT.INCLUDE, "bar" );
        List<String> l = so0.getAllValues( OPT.INCLUDE );
        assertNotNull( l );
        assertEquals( 2, l.size() );
        assertTrue( l.contains( "foo" ));
        assertTrue( l.contains( "bar" ));
    }

    @Test
    public void testGetAllValuesOPT3() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so0 = new SchemagenOptions();
        SchemagenOptions so1 = new SchemagenOptions();
        so0.setParent( so1 );
        so1.setOption( OPT.INCLUDE, "foo" );
        so1.setOption( OPT.INCLUDE, "bar" );
        List<String> l = so0.getAllValues( OPT.INCLUDE );
        assertNotNull( l );
        assertEquals( 2, l.size() );
        assertTrue( l.contains( "foo" ));
        assertTrue( l.contains( "bar" ));
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

