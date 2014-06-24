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

package org.apache.jena.atlas.json.io;

import java.util.Set ;
import java.util.SortedSet ;
import java.util.TreeSet ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.json.* ;

public class JsonWriter implements JsonVisitor
{
    private final IndentedWriter out ;
    
    public JsonWriter() { this(IndentedWriter.stdout) ; }
    public JsonWriter(IndentedWriter ps) { out = ps ; }
    
    public void startOutput()   {  }
    public void finishOutput()  {  out.flush()  ; }
    
    private static String ArrayStart        = "[ " ;
    private static String ArrayFinish       = "]" ;
    private static String ArraySep          = "," ; 

    private static String ObjectStart       = "{" ;
    private static String ObjectFinish      = "}" ;
    private static String ObjectSep         = " ," ;
    private static String ObjectPairSep     = " : " ;
    private static String SPC               = " " ;
    
    // Number of key/value pairs in an object before it is always written long form.
    // It may still written long form if any of the values are consider complicated.   
    private static int maxCompactObject = 1 ;
    // Number of array elements before an array is always written long form.
    // It may still written long form if any of the elements are consider complicated.   
    private static int maxCompactArray = 1 ;
    
    @Override
    public void visit(JsonObject jsonObject)
    { 
        boolean first = true ; 
        Set<String> x = jsonObject.keySet() ;
        
        out.print(ObjectStart) ;
        if ( x.size() == 0 ) {
            out.print(SPC) ;
            out.print(ObjectFinish) ;
            return ;
        }

        out.incIndent() ;

        if ( isJsonObjectCompact(jsonObject) )
            writeObjectCompact(jsonObject, x) ;
        else
            writeObjectLong(jsonObject, x) ;
        out.decIndent() ;
        out.print(ObjectFinish) ;
    }
    
    private static boolean isJsonObjectCompact(JsonObject jsonObject) {
        Set<String> x = jsonObject.keySet() ;
        if ( x.size() == 0 ) return true ;
        if ( x.size() > maxCompactObject ) return false ;
        
        for ( String k : x ) {
            if ( ! isCompactValue(jsonObject.get(k)) )
                return false ;
        }
        return true ;
    }
    private void writeObjectCompact(JsonObject jsonObject, Set<String> x) {
        SortedSet<String> y = new TreeSet<>(x) ;
        boolean first = true ;
        for ( String k : y ) {
            if ( ! first )
                out.print(ObjectSep) ;
            first =  false ;
            JSWriter.outputQuotedString(out, k) ;
            out.print(ObjectPairSep) ;
            out.incIndent() ;
            jsonObject.get(k).visit(this) ;
            out.decIndent() ;
        }
        out.print(SPC) ;

    }
    
    private void writeObjectLong(JsonObject jsonObject, Set<String> x) {
        // Just after the opening { 
        SortedSet<String> y = new TreeSet<>(x) ;
        boolean first = true ;
        out.println() ;
        for ( String k : y ) {
            if ( ! first ) {    
                out.print(ObjectSep) ;
                out.println() ; 
            }
            first =  false ;
            JSWriter.outputQuotedString(out, k) ;
            out.print(ObjectPairSep) ;
            out.incIndent() ;
            jsonObject.get(k).visit(this) ;
            out.decIndent() ;
        }
        out.println();
    }
    
    private static boolean isCompactValue(JsonValue v) {
        if ( v.isPrimitive()) return true ;
        if ( v.isArray() ) {
            JsonArray a = v.getAsArray() ;
            if ( a.size() == 0 ) return true ;
            if ( a.size() > 1 ) return false ;
            return a.get(0).isPrimitive() ;
        }
        if ( v.isObject() ) {
            JsonObject obj = v.getAsObject() ;
            Set<String> x = obj.keySet() ; 
            if ( x.size() == 0 )
                return true ;
            if ( x.size() > 1  )
                return false ;
            String k = obj.keys().iterator().next();
            return  obj.get(k).isPrimitive() ;
        }
        return false ;
    }
    
    @Override
    public void visit(JsonArray jsonArray)
    {
        if ( jsonArray.size() == 0 ) {
            out.print(ArrayStart) ;
            out.incIndent() ;
            out.print(ArrayFinish) ;
            out.decIndent() ;
            return ;
        }

        if ( isJsonArrayCompact(jsonArray) )
            writeArrayCompact(jsonArray) ;
        else
            writeArrayLong(jsonArray) ;
    }

    private boolean isJsonArrayCompact(JsonArray jsonArray) {
        if ( jsonArray.size() > maxCompactArray ) return false ;
        for ( JsonValue aJsonArray : jsonArray )
        {
            if ( !aJsonArray.isPrimitive() )
            {
                return false;
            }
        }
        return true ;
    }
    
    private void writeArrayCompact(JsonArray jsonArray) {
        out.print(ArrayStart) ;
        out.incIndent() ;
        boolean first = true ; 

        for ( JsonValue elt : jsonArray )
        {
            if ( ! first ) {
                out.print(ArraySep) ;
                out.print(SPC) ;
            }
            first = false ;
            elt.visit(this) ;
            
        }
        out.print(SPC) ;
        out.decIndent() ;
        out.print(ArrayFinish) ;
    }

    private void writeArrayLong(JsonArray jsonArray) {
        //out.println() ;
        out.print(ArrayStart) ;
        
        out.incIndent() ;
        out.ensureStartOfLine() ;
        boolean first = true ; 

        for ( JsonValue elt : jsonArray )
        {
            if ( ! first )
            {
                out.print(SPC) ;
                out.print(ArraySep) ;
                out.println() ;
            }
            first = false ;
            elt.visit(this) ;
        }
        out.decIndent() ;
        out.ensureStartOfLine() ;
        out.print(ArrayFinish) ;
    }
    
    @Override
    public void visit(JsonString jsonString)
    {
        JSWriter.outputQuotedString(out, jsonString.value()) ;
    }

    @Override
    public void visit(JsonNumber jsonNumber)
    {
        out.print(jsonNumber.value().toString()) ;
    }

    @Override
    public void visit(JsonBoolean jsonBoolean)
    {
        String x = jsonBoolean.value() ? "true" : "false" ; 
        out.print(x) ;
    }

    @Override
    public void visit(JsonNull jsonNull)
    { out.print("null") ; }

}
