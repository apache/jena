/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json.io;


import java.io.OutputStream;
import java.util.Stack;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.lib.Ref ;


/** A low level streaming JSON writer - assumes correct sequence of calls (e.g. keys in objects).
 * Useful when writing JSON directly from some other structure 
 */

public class JSWriter
{
    private IndentedWriter out = IndentedWriter.stdout ;
    
    public JSWriter() { this(IndentedWriter.stdout) ; }
    public JSWriter(OutputStream ps) { this(new IndentedWriter(ps)) ; }
    public JSWriter(IndentedWriter ps) { out = ps ; }
    
    public void startOutput() {}
    public void finishOutput() { out.flush(); } 
    
    // Remember whether we are in the first element of a compound (object or array). 
    Stack<Ref<Boolean>> stack = new Stack<Ref<Boolean>>() ;
    
    public void startObject()
    {
        startCompound() ;
        out.print("{ ") ;
        out.incIndent() ;
    }
    
    public void finishObject()
    {
        out.decIndent() ;
        if ( isFirst() )
            out.print("}") ;
        else
        {
            out.ensureStartOfLine() ;
            out.println("}") ;
        }
        finishCompound() ;
    }
    
    public void key(String key)
    {
        if ( isFirst() )
        {
            out.println();
            setNotFirst() ;
        }
        else
            out.println(" ,") ;
        value(key) ;
        out.print(" : ") ;
        // Ready to start the pair value.
    }
    
    // "Pair" is the name used in the JSON spec. 
    public void pair(String key, String value)
    {
        key(key) ;
        value(value) ;
    }
    
    public void startArray()
    {
        startCompound() ;
        out.print("[ ") ;
        // Messy with objects out.incIndent() ;
    }
    
    public void finishArray()
    {

//        out.decIndent() ;
        out.print(" ]") ;       // Leave on same line.
        finishCompound() ;
    }

    public void arrayElement(String str)
    {
        if ( isFirst() )
            setNotFirst() ;
        else
            out.print(", ") ;
        value(str) ;
    }

    private void startCompound()    { stack.push(new Ref<Boolean>(true)) ; }
    private void finishCompound()   { stack.pop(); }
    private boolean isFirst()   { return stack.peek().getValue() ; }
    private void setNotFirst()  { stack.peek().setValue(false) ; }
    
    // Can only write a value in some context.
    private void value(String x) { out.print("\"") ; out.print(x) ; out.print("\"") ; }
    
//    void valueString(String image) {}
//    void valueInteger(String image) {}
//    void valueDouble(String image) {}
//    void valueBoolean(boolean b) {}
//    void valueNull() {}
//    void valueDecimal(String image) {}
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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