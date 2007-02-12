/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.main;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.core.ARQConstants;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.query.util.Symbol;

/** Configuration properties for the default query engine.
 *  These are the more internal properties - the more user-centric ones
 *  are in class ARQ.
 */

public class EngineConfig
{
    
    // ---- Engine specific parameters
    
    /**
     * Apply a DISTINCT to all SELECT queries, whether they explicitly
     * state that or not. 
     */
    
    // Move to ARQ?
    public static final Symbol autoDistinct = ARQConstants.allocSymbol("autoDistinct") ;
    
    /** The property function registry */
    public static final Symbol registryPropertyFunctions =
        ARQConstants.allocSymbol("registryPropertyFunctions") ;
    
    /** The describe handler registry */
    public static final Symbol registryDescribeHandlers =
        ARQConstants.allocSymbol("registryDescribeHandlers") ;

    /** The function library registry */
    public static final Symbol registryFunctions =
        ARQConstants.allocSymbol("registryFunctions") ;
    
    /** The extension library registry */
    public static final Symbol registryExtensions =
        ARQConstants.allocSymbol("registryExtensions") ;
    
    /** The extension library registry */
    public static final Symbol registryMagicProperties =
        ARQConstants.allocSymbol("registryMagicProperties") ;
    
    static {
        // getContext().setTrue(autoDistinct) ;
//        getContext().setIfUndef(niceOptionals,           "true") ;

        // These are self initializing
        //getContext().setIfUndef(registryPropertyFunctions,  PropertyFunctionRegistry.get()) ;
        //getContext().setIfUndef(registryDescribeHandlers,   DescribeHandlerRegistry.get()) ;
        //getContext().setIfUndef(registryFunctions,          FunctionRegistry.get()) ;
        //getContext().setIfUndef(registryExtensions,         ExtensionRegistry.get()) ;
    }
    
    // --------
    
    public static Context getContext()
    {
        // Must also ensure all system initialization has been done.  Calling ARQ.getContext() does that. 
        return ARQ.getContext() ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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