package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.rdf.model.*;
import junit.framework.*;

public class TestModelPolymorphism extends GraphTestBase
    {
    public static TestSuite suite()
        { return new TestSuite( TestModelPolymorphism.class ); }   
        
    public TestModelPolymorphism(String name)
        {
        super(name);
        }

    public void testPoly()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource( "http://www.electric-hedgehog.net/a-o-s.html" );
        assertFalse( "the Resouce should not be null", r == null );
        assertTrue( "the Resource can be a Property", r.canAs( Property.class ) );
        Property p = (Property) r.as( Property.class );
        assertFalse( "the Property should not be null", p == null );
        assertFalse( "the Resource and Property should not be identical", r == p );
        }
    }
