/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.sse.builders.BuilderOp;

/** Manage extension algebra operations */
public class OpExtRegistry
{
    static Map extensions = new HashMap() ;
    // Wire in.
    static { BuilderOp.add(Tags.tagExt, new BuildExt()) ; }
    
    public static void register(ExtBuilder builder)
    {
        extensions.put(builder.getSubTab(), builder) ;
    }
    
    public static void unregister(String subtag)
    {
        extensions.remove(subtag) ;
    }
    
    public static ExtBuilder builder(String tag) { return (ExtBuilder)extensions.get(tag) ; }
    
    static public class BuildExt implements BuilderOp.Build 
    { 
        public Op make(ItemList list)
        {
            // 0 is the "ext"
            String subtag = list.get(1).getSymbol() ;
            ExtBuilder b = builder(subtag) ;
            list = list.sublist(2) ;
            OpExt ext = b.make(list) ;  // Arguments 2 onwards
            return ext ;
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