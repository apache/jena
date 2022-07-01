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

package org.apache.jena.atlas.io ;

import org.junit.Test ;
import static org.junit.Assert.* ;

public class TestIndentedWriter {
    
    @Test
    public void write01() {
        try (IndentedLineBuffer b = new IndentedLineBuffer()) {
            b.print("hell") ;
            b.print("o") ;
            assertEquals("hello", b.asString()) ;
        }
    }

    @Test
    public void write02() {
        try (IndentedLineBuffer b = new IndentedLineBuffer()) {
            b.incIndent() ;
            b.print("hell") ;
            b.print("o") ;
            b.decIndent() ;
            assertEquals("  hello", b.asString()) ;
        }
    }

    @Test
    public void write03() {
        try (IndentedLineBuffer b = new IndentedLineBuffer()) {
            b.incIndent() ;
            b.printf("0x%04X", 1) ;
            b.println() ;
            b.print("XX") ;
            b.decIndent() ;
            assertEquals("  0x0001\n  XX", b.asString()) ;
        }
    }
    
    @Test
    public void write04() {
        try (IndentedLineBuffer b = new IndentedLineBuffer()) {
            b.setLineNumbers(true);
            b.setNumberWidth(5);
            b.println("ABCD") ;
            b.print("XYZ") ;
            assertEquals("    1 ABCD\n    2 XYZ", b.asString()) ;
        }
    }
    
    @Test
    public void write05() {
        try (IndentedLineBuffer b = new IndentedLineBuffer()) {
            b.setLineNumbers(true);
            b.println("ABCD") ;
            b.println("XYZ") ;
            assertEquals("  1 ABCD\n  2 XYZ\n", b.asString()) ;
        }
    }

    
    @Test
    public void write06() {
        try (IndentedLineBuffer b = new IndentedLineBuffer()) {
            b.setLinePrefix("@.");
            b.println("ABCD") ;
            b.print("XYZ") ;
            assertEquals("@.ABCD\n@.XYZ", b.asString()) ;
        }
    }
    
    @Test
    public void write07() {
        try (IndentedLineBuffer b = new IndentedLineBuffer()) {
            b.setLinePrefix("@.");
            b.println("ABCD") ;
            assertEquals("@.ABCD\n", b.asString()) ;
        }
    }
}
