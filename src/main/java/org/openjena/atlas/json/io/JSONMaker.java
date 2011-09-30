/**
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

package org.openjena.atlas.json.io;

import java.util.Stack ;

import org.openjena.atlas.json.JsonArray ;
import org.openjena.atlas.json.JsonBoolean ;
import org.openjena.atlas.json.JsonNull ;
import org.openjena.atlas.json.JsonNumber ;
import org.openjena.atlas.json.JsonObject ;
import org.openjena.atlas.json.JsonString ;
import org.openjena.atlas.json.JsonValue ;
import org.openjena.atlas.lib.InternalErrorException ;
import org.openjena.atlas.logging.Log ;


/** Build a JSON structure */
public class JSONMaker implements JSONHandler
{
    public JSONMaker() {}
    
    private JsonValue value = null ;
    
    // java6: s/Stack/ArrayDeque
    private Stack<JsonArray> arrays = new Stack<JsonArray>(); 
    private Stack<JsonObject> objects = new Stack<JsonObject>();

    // The depth of this stack is the object depth.
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
        keys.push(value.getAsString().value()) ;
    }

    //@Override
    public void finishPair()
    {
        if ( value == null )
            throw new InternalErrorException("null for 'value' (bad finishPair() allignment)") ;
        
        String k = keys.pop();
        JsonObject obj = objects.peek() ;
        if ( obj.hasKey(k) )
            Log.warn("JSON", "Duplicate key '"+k+"' for object") ;
        obj.put(k, value) ;
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
