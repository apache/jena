/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json.io;

import java.util.Stack ;

import org.openjena.atlas.json.JsonArray ;
import org.openjena.atlas.json.JsonBoolean ;
import org.openjena.atlas.json.JsonNull ;
import org.openjena.atlas.json.JsonNumber ;
import org.openjena.atlas.json.JsonObject ;
import org.openjena.atlas.json.JsonString ;
import org.openjena.atlas.json.JsonValue ;


/** Build a JSON structure */
public class JSONMaker implements JSONHandler
{
    public JSONMaker() {}
    
    private JsonValue value = null ;
    
    // java6: s/Stack/ArrayDeque
    private Stack<JsonArray> arrays = new Stack<JsonArray>(); 
    private Stack<JsonObject> objects = new Stack<JsonObject>();

    private Stack<String> keys = new Stack<String>();

    public JsonValue jsonValue()
    {
        return value ;
    }

    //@Override
    public void startParse()
    {}

    //@Override
    public void finishParse()
    {}

    
    //@Override
    public void startObject()
    {
        objects.push(new JsonObject()) ; 
    }

    //@Override
    public void finishObject()
    {
        value = objects.pop() ; 
    }

    //@Override
    public void startArray()
    {
        arrays.push(new JsonArray()) ;
    }

    //@Override
    public void element()
    {
        arrays.peek().add(value) ;
        value = null ;
    }

    //@Override
    public void finishArray()
    {
        value = arrays.pop() ;
    }

    //@Override
    public void startPair()
    { 
    }


    //@Override
    public void keyPair()
    {
        keys.push(value.getString().value()) ;
    }

    //@Override
    public void finishPair()
    {
        String k = keys.pop();
        objects.peek().put(k, value) ;
        value = null ;
    }

    //@Override
    public void valueBoolean(boolean b)
    { 
        value = new JsonBoolean(b) ;
    }

    //@Override
    public void valueDecimal(String image)
    {
        value = JsonNumber.valueDecimal(image) ;
    }

    //@Override
    public void valueDouble(String image)
    {
        value = JsonNumber.valueDouble(image) ;
    }

    //@Override
    public void valueInteger(String image)
    {
        value = JsonNumber.valueInteger(image) ;
    }

    //@Override
    public void valueNull()
    {
        value = JsonNull.instance ;
    }

    //@Override
    public void valueString(String image)
    {
        value = new JsonString(image) ;
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