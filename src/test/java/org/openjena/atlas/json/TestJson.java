/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json;

import static org.openjena.atlas.json.LibJsonTest.read ;
import org.junit.Test ;
import org.openjena.atlas.json.JSON ;
import org.openjena.atlas.json.JsonArray ;
import org.openjena.atlas.json.JsonBoolean ;
import org.openjena.atlas.json.JsonNumber ;
import org.openjena.atlas.json.JsonObject ;
import org.openjena.atlas.json.JsonParseException ;
import org.openjena.atlas.json.JsonString ;
import org.openjena.atlas.test.BaseTest ;

public class TestJson extends BaseTest
{
    @Test public void js_value_1()
    { 
        read("\"abc\"", new JsonString("abc")) ;
    }
    
    @Test public void js_value_2()
    { 
        read("123", JsonNumber.value(123)) ;
    }
    
    @Test public void js_value_3()
    { 
        read("true", new JsonBoolean(true)) ;
    }

    @Test public void js_value_4()
    { 
        read("{}", new JsonObject()) ;
    }

    @Test public void js_value_5()
    { 
        JsonObject obj = new JsonObject() ;
        obj.put("a", JsonNumber.value(123)) ;
        read("{ \"a\": 123 }", obj) ;
    }

    
    @Test public void js_value_6()
    { 
        JsonArray array = new JsonArray() ;
        read("[ ]", array) ;
    }

    @Test public void js_value_7()
    { 
        JsonArray array = new JsonArray() ;
        array.add(JsonNumber.value(123)) ;
        read("[ 123 ]", array) ;
    }
    
    @Test public void js_value_8()
    { 
        JsonObject obj = new JsonObject() ;
        JsonArray array = new JsonArray() ;
        array.add(JsonNumber.value(123)) ;
        obj.put("a", array) ;
        read("{ \"a\" : [ 123 ] }", obj) ; 
    }

    
    @Test(expected=JsonParseException.class)
    public void js_value_9()
    {
        JSON.parse("[1 2 3]") ;
    }
    
    @Test(expected=JsonParseException.class)
    public void js_value_10()
    {
        JSON.parse("1") ;
    }

    @Test(expected=JsonParseException.class)
    public void js_value_11()
    {
        JSON.parse("\"foo\"") ;
    }
    
    @Test()
    public void js_value_12()
    {
        JSON.parse("{}") ;
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