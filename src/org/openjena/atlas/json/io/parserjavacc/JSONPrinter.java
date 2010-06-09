/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json.io.parserjavacc;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.json.io.JSONHandler ;

/** Parser debugging */
public class JSONPrinter implements JSONHandler
{

    IndentedWriter out ;  
    
    //@Override
    public void startParse()
    {
        out = new IndentedWriter(System.out, true) ; 
        //out.setPadString("> ") ;
    }

    //@Override
    public void finishParse()
    {
        out.flush() ;
    }

    //@Override
    public void startObject()
    { 
        out.println(">>Object") ;
        out.incIndent() ;
    }

    //@Override
    public void finishObject()
    {
        out.decIndent() ;
        out.println("<<Object") ;
    
    }
    
    //@Override
    public void startPair()
    { 
        out.println(">Pair") ;
        out.incIndent() ;
    }

    //@Override
    public void keyPair()
    {}

    //@Override
    public void finishPair()
    { 
        out.decIndent() ;
        out.println("<Pair") ;
    }
    
    //@Override
    public void startArray()
    {
        out.println(">>Array") ;
        out.incIndent() ;
    }

    //@Override
    public void element()
    {}

    //@Override
    public void finishArray()
    {
        out.decIndent() ;
        out.println("<<Array") ;
    }

    //@Override
    public void valueBoolean(boolean b)
    {
        out.println("Boolean: "+b) ;
    }

    //@Override
    public void valueDecimal(String image)
    {
        out.println("Decimal: "+image) ;
    }

    //@Override
    public void valueDouble(String image)
    {
        out.println("Double: "+image) ;
    }

    //@Override
    public void valueInteger(String image)
    {
        out.println("Integer: "+image) ;
    }

    //@Override
    public void valueNull()
    {
        out.println("Null") ;
    }

    //@Override
    public void valueString(String image)
    {
        out.println("String: "+image) ;
    }

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