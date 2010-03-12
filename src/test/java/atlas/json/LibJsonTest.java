/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.json;


import static org.junit.Assert.assertEquals ;

public class LibJsonTest
{
    /** Round trip string->json->string->json, compare two JOSN steps */
    public static void read(String string)
    { 
        JsonValue v = JSON.parseAny(string) ;
        writeRead(v) ;
    }

    /** Read-compare */
    public static void read(String string, JsonValue expected)
    { 
        JsonValue v = JSON.parseAny(string) ;
        assertEquals(expected, v) ;
    }
    
    /** Round trip json->string->json */
    public static void write(JsonValue v, String output, boolean whitespace)
    { 
        String str2 = v.toString();
        if ( ! whitespace )
        {
            output = output.replaceAll("[ \t\n\r]", "") ;
            str2 = str2.replaceAll("[ \t\n\r]", "") ; 
        }
        assertEquals(output, str2) ;
    }

    /** Round trip json->string->json */
    public static void writeRead(JsonValue v)
    { 
        String str2 = v.toString();
        JsonValue v2 = JSON.parseAny(str2) ;
        assertEquals(v, v2) ;
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