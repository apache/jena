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

import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public abstract class PathBase implements Path
{
    protected static final int hashAlt          = 0x190 ;
    protected static final int hashSeq          = 0x191 ;
    protected static final int hashMod          = 0x192 ;
    protected static final int hashInverse      = 0x193 ;
    protected static final int hashNegPropClass = 0x194 ;
    protected static final int hashLink         = 0x195 ;
    protected static final int hashRevLink      = 0x196 ;
    
    protected static final int hashZeroOrMore1  = 0x197 ;
    protected static final int hashOneOrMore1   = 0x198 ;
    protected static final int hashZeroOrMoreN  = 0x199 ;
    protected static final int hashOneOrMoreN   = 0x200 ;
    
    protected static final int hashZeroOrOne    = 0x201 ;
    protected static final int hashFixedLength  = 0x202 ;
    protected static final int hashDistinct     = 0x203 ;
    protected static final int hashMulti        = 0x204 ;
    protected static final int hashShortest     = 0x205 ;

    
    @Override
    public abstract int hashCode() ;
    
    // If the labeMap is null, do .equals() on nodes, else map from
    // bNode varables in one to bNodes variables in the other 
    @Override
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
    
    @Override
    public String toString(Prologue prologue)
    {
        return PathWriter.asString(this, prologue) ;
    }
}
