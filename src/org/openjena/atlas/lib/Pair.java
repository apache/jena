/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import static org.openjena.atlas.lib.Lib.hashCodeObject ;
import static org.openjena.atlas.lib.StrUtils.str ;

public class Pair<A, B>
{
    final A a ;
    final B b ;
    public Pair(A a, B b) { this.a = a; this.b = b ; }
    
    public A getLeft()  { return a ; }
    public B getRight() { return b ; }
    
    public A car() { return a ; }
    public B cdr() { return b ; }
    
    @Override
    public int hashCode()
    {
        return hashCodeObject(car()) ^ hashCodeObject(cdr())<<1 ; 
    }

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        // If it's a pair of a different <A,B> then .equals
        // Pair<A,B>(null,null) is equal to Pair<C,D>(null ,null)
        // Type erasure makes this hard to check otherwise.
        // Use class X extends Pair<A,B> and implement .equals to do
        // instanceof then call super.equals.
        
        if( ! ( other instanceof Pair<?,?> ) ) return false ;
        Pair<?,?> p2 = (Pair<?,?>)other ;
        return  Lib.equal(car(), p2.car()) && Lib.equal(cdr(), p2.cdr()) ;
    }
    
    @Override 
    public String toString() { return "("+str(a)+", "+str(b)+")" ; }  
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics ltd.
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