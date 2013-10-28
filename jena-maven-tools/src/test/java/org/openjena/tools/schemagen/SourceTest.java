/*****************************************************************************
 * File:    SourceTest.java
 * Project: schemagen
 * Created: 18 May 2010
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

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Additional unit test cases for {@link Source}, in addition
 * to parameter coverage tests in {@link SourceParameterTest}. </p>
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class SourceTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( SourceTest.class );

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
     * Test method for {@link org.openjena.tools.schemagen.Source#setInput(java.lang.String)}.
     */
    @Test
    public void testSetInput0() {
        Source s = new Source();
        List<String> values = s.getAllValues( OPT.INPUT );
        assertListMatch( new String[] {}, new String[] {}, 0, values );
    }

    @Test
    public void testSetInput1() {
        Source s = new Source();
        s.setInput( "__file1" );
        List<String> values = s.getAllValues( OPT.INPUT );
        assertListMatch( new String[] {"__file1"}, new String[] {}, 1, values );
    }

    @Test
    public void testSetInput2() {
        Source s = new Source();
        s.setInput( "__file1" );
        s.setInput( "__file2" );
        List<String> values = s.getAllValues( OPT.INPUT );
        assertListMatch( new String[] {"__file1", "__file2"}, new String[] {}, 2, values );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    protected void assertListMatch( String[] positives, String[] negatives, int expectedLen, List<String> values ) {
        assertEquals( expectedLen, values.size() );

        for (String match: positives) {
            assertTrue( "Should contain " + match, values.contains( match ) );
        }

        for (String match: negatives) {
            assertFalse( "Should not contain " + match, values.contains( match ) );
        }
    }


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

