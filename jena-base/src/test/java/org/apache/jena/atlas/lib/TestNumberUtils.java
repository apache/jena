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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.NumberUtils ;
import org.junit.Test ;

public class TestNumberUtils extends BaseTest
{
    @Test public void int_format_01() { testInt(1, 1, "1") ; } 

    @Test public void int_format_02() { testInt(1, 2, "01") ; } 
    
    @Test public void int_format_03() { testInt(0, 1, "0") ; }
    
    @Test public void int_format_04() { testInt(0, 2, "00") ; }

    @Test public void int_format_05() { testInt(-1, 2, "-1") ; }
   
    @Test public void int_format_06() { testInt(-1, 3, "-01") ; }


    @Test public void int_format_11() { testSigned(1, 2, "+1") ; } 
    
    @Test public void int_format_12() { testSigned(1, 3, "+01") ; } 

    @Test public void int_format_13() { testSigned(0, 2, "+0") ; }
    
    @Test public void int_format_14() { testSigned(0, 3, "+00") ; }

    @Test public void int_format_15() { testSigned(-1, 2, "-1") ; }
   
    @Test public void int_format_16() { testSigned(-1, 3, "-01") ; }
    
    @Test public void int_format_21() { testInt(1, "1") ; } 

    @Test public void int_format_22() { testInt(0, "0") ; } 
    
    @Test public void int_format_23() { testInt(-1, "-1") ; }
    
    @Test public void int_format_24() { testInt(10, "10") ; }

    @Test public void int_format_25() { testInt(100, "100") ; }

    @Test public void int_format_26() { testInt(-10, "-10") ; }

    @Test public void int_format_27() { testInt(-100, "-100") ; }


    @Test public void int_format_31() { testUnsigned(1, 2, "01") ; } 
    
    @Test public void int_format_32() { testUnsigned(1, 1, "1") ; } 

    @Test public void int_format_33() { testUnsigned(0, 1, "0") ; }

    
    private static void testInt(int value, String expected)
    {
        StringBuilder sb = new StringBuilder() ;
        NumberUtils.formatInt(sb, value) ;
        String result = sb.toString();
        assertEquals(expected, result) ;
    }

    private static void testInt(int value, int width, String expected)
    {
        StringBuilder sb = new StringBuilder() ;
        NumberUtils.formatInt(sb, value, width) ;
        String result = sb.toString();
        assertEquals(expected, result) ;
    }

    private static void testSigned(int value, int width, String expected)
    {
        StringBuilder sb = new StringBuilder() ;
        NumberUtils.formatSignedInt(sb, value, width) ;
        String result = sb.toString();
        assertEquals(expected, result) ;
    }
    
    private static void testUnsigned(int value, int width, String expected)
    {
        StringBuilder sb = new StringBuilder() ;
        NumberUtils.formatUnsignedInt(sb, value, width) ;
        String result = sb.toString();
        assertEquals(expected, result) ;
    }

}
