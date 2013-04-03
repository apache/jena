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

package com.hp.hpl.jena.sparql.util;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.riot.RIOT;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.query.ARQ;

/**
 * Tests for the {@link Version} utility class
 *
 */
public class TestVersion {

    @Test
    public void test_version_print_01() {
        Version ver = new Version();
        ver.addClass(ARQ.class);
        
        IndentedLineBuffer buffer = new IndentedLineBuffer();
        ver.print(buffer);
        
        String info = buffer.asString();
        Assert.assertNotNull(info);
        Assert.assertTrue(info.contains("ARQ"));
    }
    
    @Test
    public void test_version_string_01() {
        Version ver = new Version();
        ver.addClass(ARQ.class);
        ver.addClass(RIOT.class);
        
        String info = ver.toString();
        Assert.assertNotNull(info);
        Assert.assertTrue(info.contains("ARQ"));
        Assert.assertTrue(info.contains("\n"));
    }
    
    @Test
    public void test_version_string_02() {
        Version ver = new Version();
        ver.addClass(ARQ.class);
        ver.addClass(RIOT.class);
        
        String info = ver.toString(false);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.contains("ARQ"));
        Assert.assertTrue(info.contains("\n"));
    }
    
    @Test
    public void test_version_string_03() {
        Version ver = new Version();
        ver.addClass(ARQ.class);
        
        String info = ver.toString(true);
        Assert.assertNotNull(info);
        Assert.assertTrue(info.contains("ARQ"));
        Assert.assertFalse(info.contains("\n"));
    }
}
