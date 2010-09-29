/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderOp ;

/** Manage extension algebra operations */
public class OpExtRegistry
{
    // Known extensions.
    static Map<String, OpExtBuilder> extensions = new HashMap<String, OpExtBuilder>() ;
    
    // Wire in (ext NAME ...) form
    static { BuilderOp.add(Tags.tagExt, new BuildExtExt()) ; }
    
    public static void register(OpExtBuilder builder)
    {
        extensions.put(builder.getTagName(), builder) ;

        if ( BuilderOp.contains(builder.getTagName()) )
            throw new ARQException("Tag '"+builder.getTagName()+"' already defined") ;
        BuilderOp.add(builder.getTagName(), new BuildExt2()) ;
    }
    
    
    public static void unregister(String subtag)
    {
        extensions.remove(subtag) ;
    }
    
    public static OpExtBuilder builder(String tag) { return extensions.get(tag) ; }

    public static Op buildExt(String tag, ItemList args)
    {
        OpExtBuilder b = builder(tag) ;
        OpExt ext = b.make(args) ;  // Arguments 2 onwards
        return ext ;
    }
    
    // (ext NAME ...) form
    static public class BuildExtExt implements BuilderOp.Build 
    { 
        public Op make(ItemList list)
        {
            // 0 is the "ext"
            String subtag = list.get(1).getSymbol() ;
            list = list.sublist(2) ;
            return buildExt(subtag, list) ; 
        }
    }
    
    // (NAME ...) form
    static public class BuildExt2 implements BuilderOp.Build 
    { 
        public Op make(ItemList list)
        {
            String subtag = list.get(0).getSymbol() ;
            list = list.sublist(1) ;
            return buildExt(subtag, list) ; 
        }
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