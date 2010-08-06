/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.json.io.JSWriter ;

public class JsonAccess
{
    public static JsonValue accessPath(JsonValue obj, String ... path)
    {
        for ( int i = 0 ; i < path.length ; i++ )
        {
            String p = path[i] ;
            if ( ! obj.isObject() )
                throw new JsonException("Path traverses non-object") ;
            obj = obj.getAsObject().get(p) ;
        }
        return obj ;
    }
    
    public static JsonValue access(JsonValue obj, Object ... path)
    {
        for ( int i = 0 ; i < path.length ; i++ )
        {
            Object p = path[i] ;
            if ( p instanceof String )
            {
                if ( ! obj.isObject() )
                    throw new JsonException("Path traverses non-object") ;
                obj = obj.getAsObject().get((String)p) ;
            }
            if ( p instanceof Integer )
            {
                if ( ! obj.isArray() )
                    throw new JsonException("Path traverses non-array") ;
                obj = obj.getAsArray().get((Integer)p) ;
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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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