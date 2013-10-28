/*****************************************************************************
 * File:    SchemagenMojoTest.java
 * Project: schemagen
 * Created: 22 Mar 2010
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

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.openjena.tools.schemagen.SchemagenMojo;

/**
 * <p>Unit tests for {@link SchemagenMojo}</p>
 *
 * @author ian
 */
public class SchemagenMojoTest {

    @Test
    public void testMatchFileNames0() {
        SchemagenMojo sm = new SchemagenMojo();

        List<String> s = sm.matchFileNames();
        assertNotNull(s);
        assertTrue( s.isEmpty() );
    }

    @Test
    public void testMatchFileNames1() {
        SchemagenMojo sm = new SchemagenMojo();
        String f = "src/test/resources/test1/test1.ttl";
        sm.addIncludes( f );
        List<String> s = sm.matchFileNames();
        assertNotNull(s);
        assertEquals( 1, s.size() );
        assertEquals( new File(f), new File(s.get(0)) );
    }

    @Test
    public void testMatchFileNames2() {
        SchemagenMojo sm = new SchemagenMojo();
        String f = "src/test/resources/test1/*.ttl";
        sm.addIncludes( f );
        List<String> s = sm.matchFileNames();
        assertNotNull(s);
        assertEquals( 2, s.size() );
        assertTrue( s.get(0).endsWith( "test1.ttl" ));
        assertTrue( s.get(1).endsWith( "test2.ttl" ));
    }

    @Test
    public void testMatchFileNames3() {
        SchemagenMojo sm = new SchemagenMojo();
        String f = "src/test/resources/test1/*.ttl";
        sm.addIncludes( f );
        sm.addExcludes( "src/test/resources/test1/test1.ttl" );

        List<String> s = sm.matchFileNames();
        assertNotNull(s);
        assertEquals( 1, s.size() );
        assertTrue( s.get(0).endsWith( "test2.ttl" ));
    }


}
