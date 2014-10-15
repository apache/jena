/**
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

package org.apache.jena.tools.schemagen;


// Imports
///////////////

import static org.junit.Assert.*;

import java.util.List;

import jena.schemagen.SchemagenOptions.OPT;

import org.junit.*;

/**
 * <p>Additional unit test cases for {@link Source}, in addition
 * to parameter coverage tests in {@link SourceParameterTest}. </p>
 */
public class SourceTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

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
     * Test method for {@link Source#setInput(java.lang.String)}.
     * @throws SchemagenOptionsConfigurationException
     */
    @Test
    public void testSetInput0() throws SchemagenOptionsConfigurationException {
        SchemagenOptions so = new SchemagenOptions(null, new Source());
        List<String> values = so.getAllValues( OPT.INPUT );
        assertListMatch( new String[] {}, new String[] {}, 0, values );
    }

    @Test
    public void testSetInput1() throws SchemagenOptionsConfigurationException {
        Source s = new Source();
        s.setInput( "__file1" );
        SchemagenOptions so = new SchemagenOptions(null, s);
        List<String> values = so.getAllValues( OPT.INPUT );
        assertListMatch( new String[] {"__file1"}, new String[] {}, 1, values );
    }

    @Test
    @Ignore //jena-maven-tools doesn't support multiple inputs as of now
    public void testSetInput2() throws SchemagenOptionsConfigurationException {
        Source s = new Source();
        s.setInput( "__file1" );
        s.setInput( "__file2" );
        SchemagenOptions so = new SchemagenOptions(null, s);
        List<String> values = so.getAllValues( OPT.INPUT );
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

