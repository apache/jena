/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.lib;

import org.junit.Test;
import atlas.test.BaseTest;

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
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */