/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.web;

import org.openjena.atlas.logging.Log ;

/** A media range is a media type used in content negotiation.
 * It has a q parameter (others are possible but we don't need them ... yet  */

public class MediaRange extends MediaType
{
    // We pretend all MediaRanges have a q.
    private double q = 1.0 ;
    
    public MediaRange(MediaRange other)
    {  
        super(other) ;
        q = other.q ; // set_q() ;
    }
    
    public MediaRange(MediaType other)
    {  
        super(other) ;
        set_q() ;
    }

    public MediaRange(String string)
    {  
        super(string) ;  
        set_q() ;
    }
    
    public double get_q()
    { 
        return q ;
    }
    
//    public void set_q(double q)
//    { 
//        this.q = q ;
//        setParameter("q", Double.toString(q)) ;
//    }
    
    // Set from parameters.
    private void set_q()
    {
        String qStr = getParameter("q") ;
        if ( qStr == null ) return ;
        try {
            q = Double.parseDouble(qStr) ;
        } catch (NumberFormatException ex)
        {
            Log.warn(this, "Bad q seen: "+qStr) ;
        }
    }

    public boolean accepts(MediaType item)
    {
        if ( ! accept(this.getType(), item.getType()) )
            return false ;
        
        return accept(this.getSubType(), item.getSubType()) ;
    }
    
    private boolean accept(String a, String b)
    {
        // Null implies *
        if ( a == null || b == null )
            return true ;
        
        if ( a.equals("*") || b.equals("*") )
            return true ;
        
        return a.equals(b) ;
    }

    // Strictly more grounded than
    public boolean moreGroundedThan(MediaType item)
    {
        if ( isStar(item.getType()) && ! isStar(this.getType()) )
            return true ;
        if ( isStar(item.getSubType()) && ! isStar(this.getSubType()) )
            return true ;
        return false ;
    }
    
    private boolean isStar(String x)
    {
        return x == null || x.equals("*") ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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