/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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