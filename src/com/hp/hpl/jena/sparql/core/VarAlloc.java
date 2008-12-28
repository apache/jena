/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;

/** Allocate variables */

public class VarAlloc
{
    private String baseMarker ;
    private long counter = 0 ;

    // Globals
    // Try to avoid their use because of clashes/vry large allocated names.
    //private static VarAlloc varAnonAllocator  = new VarAlloc(ARQConstants.allocGlobalVarAnonMarker) ;
    //public static VarAlloc getVarAnonAllocator() { return bNodeAllocator ; }

    private static VarAlloc varAllocator    = new VarAlloc(ARQConstants.allocGlobalVarMarker) ;
    public static VarAlloc getVarAllocator() { return varAllocator ; }
    
    public static VarAlloc get(Context context, Symbol name)
    { 
        return (VarAlloc)context.get(name) ;
    }
    
    public VarAlloc(String baseMarker)
    {
        this.baseMarker = baseMarker ;
    }
    
    
    
    public Var allocVar()
    { return alloc(baseMarker, counter ++) ; }
    
    static private Var alloc(String base, long number)
    { return Var.alloc(base+number) ; }
    
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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