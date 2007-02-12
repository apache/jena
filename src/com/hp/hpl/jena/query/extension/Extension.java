/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.extension;

import java.util.List;

import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;

/** Interface for extension operations supported by the
 *  default, built-in query engine.
 *  
 *  Implementations may use extend ExtensionBase or ExtensionBaseUneval
 *  to process the solutions one by one.
 * 
 * @author Andy Seaborne
 * @version $Id: Extension.java,v 1.12 2007/02/02 16:23:11 andy_seaborne Exp $
 */ 

public interface Extension
{
    /** Called during query plan construction immediately after the
     * construction of the extension instance.
     * @param uri The extension URI.
     * @param args The parsed arguements
     */ 
    public void build(String uri, List args) ;


    /** Create an iterator of bindings for the given inputs 
     * @param input     QueryIterator from the previous stage
     * @param args      The argument list (unevaluated)
     * @param uri       The name of this extension 
     * @param execCxt   The execution context
     * @return          QueryIterator
     */
    public QueryIterator exec(QueryIterator input, List args, String uri, ExecutionContext execCxt) ;
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
