/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.jena.engine;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.lib.DS ;

import com.hp.hpl.jena.sparql.core.Var ;

public final class JoinKey implements Iterable<Var>
{
    // Common way to make a JoinKey
    /** Make a JoinKey from the intersection of two sets **/  
    
    public static JoinKey create(Collection<Var> vars1, Collection<Var> vars2) {
        //JoinKeys are generally small so short loops are best.
        // vars2 may be smallest e.g. from triple and running accumulator (vars1) 
        List<Var> intersection = DS.list() ;
        for ( Var v : vars1 ) {
            if ( vars2.contains(v) )
                intersection.add(v) ;
        }
        return new JoinKey(intersection) ;
    }
    
    public static JoinKey create(Var var) {
        return new JoinKey(var) ;
    }
    
    /** The builder can emit a key every time build() is caller
     * and it can be continued to be used.
     */
    public static final class Builder {
        private List<Var> keys = DS.list() ;
        
        public Builder() { }
        
        public boolean contains(Var var) {
            return keys.contains(var) ;
        }
        
        public Builder add(Var var) {
            // We expect the keys list to be short - a Set is overkill(??)
            if ( ! contains(var) )
                keys.add(var) ;
            return this ;
        }
        
        public Builder remove(Var var) {
            keys.remove(var) ;
            return this ;
        }

        public Builder clear()      { keys.clear() ; return this ; }

        public JoinKey build() {
            JoinKey joinKey = new JoinKey(DS.list(keys)) ; 
            return joinKey ;
        }
    }
    
    // Consider using an array.
    private final List<Var> keys ;
    
    private JoinKey(List<Var> _keys) { keys = _keys ; }
    
    private JoinKey(Var var)     { keys = DS.listOfOne(var) ; }
    
    public boolean isEmpty()    { return keys.isEmpty() ; }

    /** Get a single variable for this key. 
     *  For any one key, it always returns the same var */ 
    public Var getVarKey() { 
        if ( keys.isEmpty() )
            return null ;
        return keys.get(0) ;
    }
    
    @Override
    public Iterator<Var> iterator() { return keys.iterator() ; }
    
    @Override
    public String toString() {
        return keys.toString() ;
    }
}


