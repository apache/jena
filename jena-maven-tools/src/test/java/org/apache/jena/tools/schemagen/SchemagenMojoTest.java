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

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.apache.jena.tools.schemagen.SchemagenMojo;

/**
 * <p>Unit tests for {@link SchemagenMojo}</p>
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
