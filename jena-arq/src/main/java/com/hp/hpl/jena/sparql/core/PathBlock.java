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

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList ;
import java.util.List ;
import java.util.ListIterator ;

import com.hp.hpl.jena.sparql.util.Iso ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** A class whose purpose is to give a name to a collection of triple paths. */ 

public class PathBlock implements Iterable<TriplePath>
{
    private List<TriplePath> triplePaths = new ArrayList<>() ;

    public PathBlock() {}
    public PathBlock(PathBlock other) {triplePaths.addAll(other.triplePaths) ; }
    
    public void add(TriplePath tp) { triplePaths.add(tp) ; }
    public void addAll(PathBlock other) { triplePaths.addAll(other.triplePaths) ; }
    public void add(int i, TriplePath tp) { triplePaths.add(i, tp) ; }
    
    public TriplePath get(int i) { return triplePaths.get(i) ; }
    @Override
    public ListIterator<TriplePath> iterator() { return triplePaths.listIterator() ; } 
    public int size() { return triplePaths.size() ; }
    public boolean isEmpty() { return triplePaths.isEmpty() ; }
    
    public List<TriplePath> getList() { return triplePaths ; } 
    
    @Override
    public int hashCode() { return triplePaths.hashCode() ; } 
    
    @Override
    public boolean equals(Object other)
    { 
        if ( this == other ) return true ;
        if ( ! ( other instanceof PathBlock) ) 
            return false ;
        PathBlock bp = (PathBlock)other ;
        return triplePaths.equals(bp.triplePaths) ;
    }
    
    public boolean equiv(PathBlock other, NodeIsomorphismMap isoMap)
    { 
        if ( this.triplePaths.size() != other.triplePaths.size() )
            return false ;
        
        for ( int i = 0 ; i < this.triplePaths.size() ; i++ )
        {
            TriplePath tp1 = get(i) ;
            TriplePath tp2 = other.get(i) ;
            
            if ( ! Iso.triplePathIso(tp1, tp2, isoMap) )
                return false ;
        }
        return true ;
    }
    
    @Override
    public String toString()
    {
        return triplePaths.toString() ;
    }
}
