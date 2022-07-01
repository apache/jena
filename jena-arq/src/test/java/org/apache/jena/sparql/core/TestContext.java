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

package org.apache.jena.sparql.core ;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol ;
import org.junit.Test ;

public class TestContext {
    static Symbol p1 = Symbol.create("p1") ;
    static Symbol p2 = Symbol.create("p2") ;

    @Test
    public void testCxt1() {
        Context cxt = new Context() ;
    }

    @Test
    public void testCxt2() {
        Context cxt = new Context() ;
        assertTrue("Defined in empty context", !cxt.isDefined(p1)) ;
        cxt.set(p1, "v") ;
        assertTrue("Not defined after .set", cxt.isDefined(p1)) ;
        Object v = cxt.get(p1) ;
        assertSame("Not the same", "v", v) ;
    }

    @Test
    public void testCxt3() {
        Context cxt = new Context() ;
        cxt.set(p1, "v") ;
        cxt.setIfUndef(p1, "w") ;
        Object v = cxt.get(p1) ;
        assertSame("Not as first set", "v", v) ;
    }

    @Test
    public void testCxt4() {
        Context cxt = new Context() ;
        cxt.set(p1, "true") ;
        assertTrue("Not true", cxt.isTrue(p1)) ;
        assertTrue("Not true or undef", cxt.isTrueOrUndef(p1)) ;

        assertTrue("Not false or undef", cxt.isFalseOrUndef(p2)) ;
        assertTrue("False when undef", !cxt.isFalse(p2)) ;
    }

    @Test
    public void testCxt5() {
        Context cxt = new Context() ;
        cxt.set(p1, "false") ;
        assertTrue("Not false", cxt.isFalse(p1)) ;
        assertTrue("Not false or undef", cxt.isFalseOrUndef(p1)) ;
    }

    @Test
    public void testCxt6() {
        Context cxt = new Context() ;
        cxt.setTrue(p1) ;
        assertTrue("Not true", cxt.isTrue(p1)) ;
        String x = cxt.getAsString(p1) ;
        assertEquals("Not string 'true'", "true", x) ;
    }

    @Test
    public void testCxt7() {
        Context cxt = new Context() ;
        assertEquals(-1, cxt.getInt(p1, -1)) ;
        cxt.set(p1, 1) ;
        int x = cxt.getInt(p1, -1) ;
        assertEquals(1, x) ;
    }
    
    @Test
    public void testCxt8() {
        Context cxt = new Context() ;
        assertEquals(-1L, cxt.getLong(p1, -1L)) ;
        cxt.set(p1, 1L) ;
        long x = cxt.getLong(p1, -2L) ;
        assertEquals(1L, x) ;
    }
    
    @Test(expected=ARQException.class)
    public void testCxt9() {
        Context cxt = new Context() ;
        assertEquals(-1L, cxt.getLong(p1, -1L)) ;
        cxt.set(p1, 1L) ;
        // Bad. Long for Integer.
        cxt.getInt(p1, -2) ;
    }
}
