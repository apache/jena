/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class P_Mod extends P_Path1
{
    public static long INF = -2 ;
    public static long UNSET = -1 ;
    
    private long min ;
    private long max ;

    public P_Mod(Path path, long val)
    {
        this(path, val, val) ;
    }
    
    public P_Mod(Path path, long min, long max)
    {
        super(path) ;
        this.min = min ;
        this.max = max ;
    }
    
    //@Override
    public void visit(PathVisitor visitor)
    { visitor.visit(this) ; }

    public long getMin()
    {
        return min ;
    }

    public long getMax()
    {
        return max ;
    }

    @Override
    public int hashCode()
    {
        return hashMod ^ (int)min ^ (int)max ^ getSubPath().hashCode() ;
    }

    @Override
    public boolean equalTo(Path path2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( path2 instanceof P_Mod ) ) return false ;
        P_Mod other = (P_Mod)path2 ;
        return other.min == min && other.max == max && getSubPath().equalTo(other.getSubPath(), isoMap)  ;
    }

    
    public boolean isFixedLength()
    {
        return min == max && min > 0 ; 
    }
    
    public long getFixedLength()
    {
        if ( ! isFixedLength() ) return -1 ;
        return min ;
    }
    
    public boolean isZeroOrMore()
    {
        return min == 0 && max < 0 ;
    }

    public boolean isOneOrMore()
    {
        return min == 1 && max < 0 ;
    }
    
    public boolean isZeroOrOne()
    {
        return min == 0 && max == 1 ;
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