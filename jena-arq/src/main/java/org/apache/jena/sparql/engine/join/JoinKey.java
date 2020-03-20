/**
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

package org.apache.jena.sparql.engine.join;
import java.util.*;

import org.apache.jena.sparql.core.Var ;

/** JoinKey for hash joins */
public final class JoinKey implements Iterable<Var>
{
    /** Key of no variables */
    private static final JoinKey emptyKey = new JoinKey(Collections.emptyList()) ;

    /** Make a JoinKey from the intersection of two sets **/  
    public static JoinKey create(Collection<Var> vars1, Collection<Var> vars2) {
        // JoinKeys and choices for keys are generally small so short loops are best.
        List<Var> intersection = new ArrayList<>() ;
        for ( Var v : vars1 ) {
            if ( vars2.contains(v) )
                intersection.add(v) ;  
        }
        return new JoinKey(intersection) ;
    }
    
    /** Make a JoinKey of single variable from the intersection of two sets **/  
    public static JoinKey createVarKey(Collection<Var> vars1, Collection<Var> vars2) {
        for ( Var v : vars1 ) {
            if ( vars2.contains(v) )
                return create(v) ;
        }
        return emptyKey ;
    }
    
    public static JoinKey create(Var var) {
        return new JoinKey(var) ;
    }
    
    /** The builder can emit a key every time build() is caller
     * and it can be continued to be used.
     */
    public static final class Builder {
        private List<Var> keys = new ArrayList<>() ;
        
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
            JoinKey joinKey = new JoinKey(new ArrayList<>(keys)) ; 
            return joinKey ;
        }
    }
    
    // Consider using an array.
    private final List<Var> keys ;
    
    private JoinKey(List<Var> _keys) { keys = _keys ; }
    
    private JoinKey(Var var)        { keys = Collections.singletonList(var) ; }
    
    public boolean isEmpty()        { return keys.isEmpty() ; }
    
    public int length()             { return keys.size() ; }

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
