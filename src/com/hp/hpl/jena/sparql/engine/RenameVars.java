/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import java.util.Collection ;
import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

public class RenameVars implements NodeTransform
{
    private final Map<Var, Var> aliases = new HashMap<Var, Var>() ;
    private final Collection<Var> constants ;
    private final String varPrefix ;
    
    public RenameVars(Collection<Var> constants, String varPrefix)
    {
        this.constants = constants ;
        this.varPrefix = varPrefix ;
    }
    
    public final Node convert(Node node)
    {
        if ( ! Var.isVar(node) ) return node ;
        if ( constants.contains(node ) ) return node ;

        Var var = (Var)node ;
        Var var2 = aliases.get(var) ;
        if ( var2 != null )
            return var2 ;
        // TODO The new name is the old name with a "/" - clashes?
        // Provided the old name isn't a constant as well, this is safe 
        // if renaming is bottom up. 
        // Really safe - use the global allocator.
        //var2 = allocator.allocVar() ;
        var2 = Var.alloc(varPrefix+var.getVarName()) ;
        aliases.put(var, var2) ;
        return var2 ; 
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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