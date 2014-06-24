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

package com.hp.hpl.jena.sparql.engine.optimizer.reorder;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;

import org.apache.jena.atlas.logging.Log ;

public class ReorderProcIndexes implements ReorderProc
{
    private int[] indexes ;

    public ReorderProcIndexes(int[] indexes)
    {
        this.indexes = indexes ;   
    }
    
    /** Return a new basic pattern with the same triples as the input,
     *  but ordered as per the index list of this reorder processor. 
     */ 
    @Override
    public BasicPattern reorder(BasicPattern bgp)
    {
        if ( indexes.length != bgp.size() )
        {
            String str = String.format("Expected size = %d : actual basic pattern size = %d", indexes.length, bgp.size()) ;
            Log.fatal(this, str) ;
            throw new ARQException(str) ; 
        }        
        
        BasicPattern bgp2 = new BasicPattern() ;
        for ( int idx : indexes )
        {
            Triple t = bgp.get( idx );
            bgp2.add( t );
        }
        return bgp2 ;
    }
    
    @Override
    public String toString()
    {
        String x = "" ;
        String sep = "" ;
        for ( int idx : indexes )
        {
            x = x + sep + idx ;
            sep = ", " ;
        }
        return x;
    }
}
