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

package com.hp.hpl.jena.sparql.path;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** Path element of the form {,N} {N,} {N,M}  but not {N} */ 
public class P_Mod extends P_Path1
{
    public static final long UNSET    = -1 ;
    public static final long INF      = -2 ;
    
    private final long min ;
    private final long max ;

    public P_Mod(Path path, long min, long max)
    {
        super(path) ;
        this.min = min ;
        this.max = max ;
    }
    
    @Override
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
        return max == min && min >= 0 ;  
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
