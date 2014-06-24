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

package org.apache.jena.atlas.json;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.json.io.JSWriter ;

public class JsonAccess
{
    public static JsonValue accessPath(JsonValue obj, String ... path)
    {
        for ( String p : path )
        {
            if ( !obj.isObject() )
            {
                throw new JsonException( "Path traverses non-object" );
            }
            obj = obj.getAsObject().get( p );
        }
        return obj ;
    }
    
    public static JsonValue access(JsonValue obj, Object ... path)
    {
        for ( Object p : path )
        {
            if ( p instanceof String )
            {
                if ( !obj.isObject() )
                {
                    throw new JsonException( "Path traverses non-object" );
                }
                obj = obj.getAsObject().get( (String) p );
            }
            if ( p instanceof Integer )
            {
                if ( !obj.isArray() )
                {
                    throw new JsonException( "Path traverses non-array" );
                }
                obj = obj.getAsArray().get( (Integer) p );
            }
        }
        return obj ;
    }
    
    public static void main(String... args)
    {
        if ( false )
        {
            JSWriter w = new JSWriter() ;
    
            w.startOutput() ;
            w.startObject() ;
            
            w.pair("key1", "value1") ;
            w.key("key2") ;
    
            w.startArray() ;
            w.arrayElement("x") ;
            w.arrayElement("y") ;
            w.finishArray() ;
            
            w.key("key3") ;
            w.startObject() ;
            w.pair("key4", "value4") ;
            w.finishObject() ;
            
            w.finishObject() ;
            w.finishOutput() ;
        
            //System.exit(0) ;
        }
        
        if ( false )
        {
                JsonValue obj = JSON.read("data.json") ;
                obj.output(IndentedWriter.stdout) ;
                System.out.println() ;
                System.out.println("OK") ;
        }
        
        if ( true )
        {
            JsonObject obj = new JsonObject() ;
            obj.put("x1", new JsonString("y")) ;
            obj.put("x2", JsonNumber.value(56)) ;
            obj.put("x2", JsonNumber.value(56)) ;
            JsonArray a = new JsonArray() ;
            a.add(JsonNumber.value(5)) ;
            a.add(new JsonBoolean(true)) ;
            a.add(new JsonBoolean(false)) ;
            obj.put("array", a) ;
            a = new JsonArray() ;
            a.add(JsonNumber.value(5)) ;
            obj.put("array2", a) ;
            obj.output(IndentedWriter.stdout) ;
            System.out.println() ;
            System.out.println(access(obj, "x1")) ;
        }
        
        //IndentedWriter.stdout.flush();
        
        
    }
}
