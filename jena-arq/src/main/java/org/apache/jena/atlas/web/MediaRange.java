/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.web;

import org.apache.jena.atlas.logging.Log ;

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
        super(MediaType.parse(string)) ;  
        set_q() ;
    }
    
    public MediaRange(String type, String subType, String charset)
    {
        super(type, subType, charset) ;
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

    public boolean grounded(MediaType item) {
        return ! isStar(item.getType()) && ! isStar(item.getSubType()) ;
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
    
    // Waeighting for "exactness"
    // 3 for grounded, 2 for foo/*, 1 for */foo and 0 for */*  
    int subweight() {
        int x = 0 ;
        if ( !isStar(getType()))
            x += 2 ;
        if ( !isStar(getSubType()))
            x += 1 ;
        return x ; 
    }
    
    
    private boolean isStar(String x)
    {
        return x == null || x.equals("*") ;
    }
}
