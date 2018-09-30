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

package org.apache.jena.atlas.io;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestFilenameExtensions {

    @Test public void ext_1() { 
        String fn1 = "file.txt";
        String fn2 = IO.filenameNoCompression(fn1);
        assertEquals(fn1, fn2);
    }
    
    @Test public void ext_2() { 
        String fn1 = "a/b/file.gz";
        String fn2 = IO.filenameNoCompression(fn1);
        assertEquals("a/b/file", fn2);
    }

    @Test public void ext_3() { 
        String fn1 = "file.ttl.bz2";
        String fn2 = IO.filenameNoCompression(fn1);
        assertEquals("file.ttl", fn2);
    }
    
    @Test public void ext_4() { 
        String fn1 = "file.txt.gz";
        String fn2 = IO.filenameNoCompression(fn1);
        assertEquals("file.txt", fn2);
    }

    @Test public void ext_5() { 
        String fn1 = "a/b/file.ttl.bz2";
        String fn2 = IO.filenameNoCompression(fn1);
        assertEquals("a/b/file.ttl", fn2);
    }
}
