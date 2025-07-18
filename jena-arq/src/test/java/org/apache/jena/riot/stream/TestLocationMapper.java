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

package org.apache.jena.riot.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.adapters.TestFileManager;
import org.apache.jena.util.LocationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class TestLocationMapper
{
    static Logger log = LoggerFactory.getLogger(TestLocationMapper.class);
    static final String testingDir = TestFileManager.testingDir;
    static final String filename1 = "file:test";
    static final String notFilename = "zzzz";
    static final String filename2 = "file:"+testingDir+"/location-mapping-test-file";
    public static final String mapping = "location-mapping-test.ttl;"+testingDir+"/location-mapping-test.ttl";

    public TestLocationMapper() { }

    @Test public void testLocationMapper()
    {
        LocationMapper locMap = new LocationMapper(mapping);
        String alt = locMap.altMapping(filename1);
        assertNotNull(alt);
        assertEquals(filename2, alt);
    }

    @Test public void testLocationMapperMiss()
    {
        LocationMapper locMap = new LocationMapper(mapping);
        String alt = locMap.altMapping(notFilename);
        assertNotNull(alt);
        assertEquals(notFilename, alt);
    }

    @Test public void testLocationMapperURLtoFile()
    {
        LocationMapper locMap = new LocationMapper(mapping);
        String alt = locMap.altMapping("http://example.org/file");
        assertNotNull(alt);
        assertEquals("file:"+testingDir+"/location-mapping-test-file", alt);
    }

    @Test public void testLocationMapperFromModel()
    {
        Model model = RDFDataMgr.loadModel(testingDir+"/location-mapping-test.ttl");
        LocationMapper loc = new LocationMapper(model);

        // Light test that the two location mappers are "the same"
        LocationMapper locMap = new LocationMapper(mapping);
        for ( Iterator<String> iter = loc.listAltEntries(); iter.hasNext(); )
        {
            String e = iter.next();
            String v1 = locMap.getAltEntry(e);
            String v2 = loc.getAltEntry(e);
            assertEquals(v1, v2, ()->"Different entries");
        }
        for ( Iterator<String> iter = loc.listAltPrefixes(); iter.hasNext(); )
        {
            String e = iter.next();
            String v1 = locMap.getAltPrefix(e);
            String v2 = loc.getAltPrefix(e);
            assertEquals(v1, v2, ()->"Different entries");
        }
    }

    @Test public void testLocationMapperClone1()
    {
        LocationMapper locMap1 = new LocationMapper(mapping);
        // See testLocationMapperURLtoFile
//        String alt = locMap.altMapping("http://example.org/file");
//        assertNotNull(alt);
//        assertEquals(alt, "file:"+testingDir+"/location-mapping-test-file");

        LocationMapper locMap2 = new LocationMapper(locMap1);
        // Remove from original
        locMap1.removeAltEntry("http://example.org/file");
        String alt = locMap2.altMapping("http://example.org/file");
        assertNotNull(alt);
        assertEquals("file:"+testingDir+"/location-mapping-test-file", alt);
    }

    @Test public void testLocationMapperClone2()
    {
        LocationMapper locMap1 = new LocationMapper(mapping);
        // See testLocationMapperURLtoFile
//        String alt = locMap.altMapping("http://example.org/file");
//        assertNotNull(alt);
//        assertEquals(alt, "file:"+testingDir+"/location-mapping-test-file");

        LocationMapper locMap2 = new LocationMapper(locMap1);

        // Change this one
        locMap2.addAltPrefix("http://example.org/OTHER", "file:OTHER");
        {
            String alt = locMap2.altMapping("http://example.org/OTHER/f");
            assertNotNull(alt);
            assertEquals("file:OTHER/f", alt);
        }
        // Not the other
        {
            String alt = locMap1.altMapping("http://example.org/OTHER/f");
            assertNotNull(alt);
            // Did not change
            assertEquals("http://example.org/OTHER/f", alt);
        }
    }

    @Test public void testLocationMapperEquals1()
    {
        LocationMapper locMap1 = new LocationMapper(mapping);
        LocationMapper locMap2 = new LocationMapper(mapping);
        assertEquals(locMap1, locMap2);
        assertEquals(locMap1.hashCode(), locMap2.hashCode());
    }

    @Test public void testLocationMapperEquals2()
    {
        LocationMapper locMap1 = new LocationMapper(mapping);
        LocationMapper locMap2 = new LocationMapper(mapping);
        locMap2.addAltEntry("file:nowhere", "file:somewhere");
        assertFalse(locMap1.equals(locMap2));
        assertFalse(locMap2.equals(locMap1));
    }

    @Test public void testLocationMapperToModel1()
    {
        LocationMapper locMap1 = new LocationMapper(mapping);
        LocationMapper locMap2 = new LocationMapper(locMap1.toModel());
        assertEquals(locMap1, locMap2);
        assertEquals(locMap1.hashCode(), locMap2.hashCode());
    }

    @Test public void testLocationMapperToModel2()
    {
        LocationMapper locMap1 = new LocationMapper(mapping);
        LocationMapper locMap2 = new LocationMapper(mapping);
        locMap1 = new LocationMapper(locMap1.toModel());
        locMap2.addAltEntry("file:nowhere", "file:somewhere");
        assertFalse(locMap1.equals(locMap2));
        assertFalse(locMap2.equals(locMap1));
    }
}
