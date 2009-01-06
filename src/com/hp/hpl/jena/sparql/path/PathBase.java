/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public abstract class PathBase implements Path
{
    protected static final int hashAlt      = 0x190 ;
    protected static final int hashSeq      = 0x191 ;
    protected static final int hashMod      = 0x193 ;
    protected static final int hashReverse  = 0x193 ;
    
    @Override
    public abstract int hashCode() ;
    
    // If the labeMap is null, do .equals() on nodes, else map from
    // bNode varables in one to bNodes variables in the other 
    public abstract boolean equalTo(Path path2, NodeIsomorphismMap isoMap) ;
    
    @Override
    final public boolean equals(Object path2)
    { 
        if ( this == path2 ) return true ;

        if ( ! ( path2 instanceof Path ) )
            return false ;
        return equalTo((Path)path2, null) ;
    }
    
    @Override
    public String toString()
    {
        return PathWriter.asString(this) ;
    }
    
    public String toString(Prologue prologue)
    {
        return PathWriter.asString(this, prologue) ;
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