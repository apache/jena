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

import java.io.OutputStream ;
import java.util.Set ;
import java.util.SortedSet ;
import java.util.TreeSet ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.json.JsonArray ;
import org.openjena.atlas.json.JsonBoolean ;
import org.openjena.atlas.json.JsonNull ;
import org.openjena.atlas.json.JsonNumber ;
import org.openjena.atlas.json.JsonObject ;
import org.openjena.atlas.json.JsonString ;
import org.openjena.atlas.json.JsonValue ;
import org.openjena.atlas.json.JsonVisitor ;

public class JsonWriter implements JsonVisitor
{
    // Use JSWriter????!!!! Multiline control is tricky then.
    // Or is it?  Because we know the length of things
    
    IndentedWriter out ;
    
    public JsonWriter() { this(IndentedWriter.stdout) ; }
    @Deprecated
    public JsonWriter(OutputStream ps) { this(new IndentedWriter(ps)) ; }
    public JsonWriter(IndentedWriter ps) { out = ps ; }
    
    public void startOutput()   {  }
    public void finishOutput()  {  out.flush()  ; }
    
    private static String ArrayStart        = "[ " ;
    private static String ArrayFinish       = " ]" ;
    private static String ArraySep          = "," ; 

    private static String ObjectStart       = "{ " ;
    private static String ObjectFinish      = "}" ;
    private static String ObjectSep         = " ," ;
    private static String ObjectPairSep     = " : " ;
    
    // Make "unnecessary" space
    private static String SPC               = " " ;
    
    //@Override
    public void visit(JsonObject jsonObject)
    { 
        out.print(ObjectStart) ;
        out.incIndent() ;
        boolean first = true ; 
        boolean multiLine = false ;
        
        // Sort keys.
        Set<String> x = jsonObject.keySet() ;
        SortedSet<String> y = new TreeSet<String>(x) ;
        
        for ( String k : y )
        {
            if ( ! first )
            {
                out.print(ObjectSep) ;
                out.println() ;
                multiLine = true ; 
            }
            first =  false ;
            JSWriter.outputQuotedString(out, k) ;
            out.print(ObjectPairSep) ;
            out.incIndent() ;
            jsonObject.get(k).visit(this) ;
            out.decIndent() ;
        }
        out.decIndent() ;
        if ( multiLine )
            out.ensureStartOfLine() ;
        else
            out.print(SPC) ;
        out.print(ObjectFinish) ;
        
    }

    //@Override
    public void visit(JsonArray jsonArray)
    {
        boolean multiLine = (jsonArray.size() > 1 ) ;
        if ( multiLine )
            out.ensureStartOfLine() ;

        out.print(ArrayStart) ;
        out.incIndent() ;
        boolean first = true ; 

        for ( JsonValue elt : jsonArray )
        {
            if ( ! first )
            {
                out.print(ArraySep) ;
                out.println() ;
                multiLine = true ; 
            }
            first = false ;
            elt.visit(this) ;
        }
        out.decIndent() ;
        if ( multiLine )
            out.ensureStartOfLine() ;
//        else
//            out.print(SPC) ;
        out.print(ArrayFinish) ;
    }

    //@Override
    public void visit(JsonString jsonString)
    {
        JSWriter.outputQuotedString(out, jsonString.value()) ;
    }

    //@Override
    public void visit(JsonNumber jsonNumber)
    {
        out.print(jsonNumber.value().toString()) ;
    }

    //@Override
    public void visit(JsonBoolean jsonBoolean)
    {
        String x = jsonBoolean.value() ? "true" : "false" ; 
        out.print(x) ;
    }

    //@Override
    public void visit(JsonNull jsonNull)
    { out.print("null") ; }

}
