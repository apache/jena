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

package org.seaborne.dboe.engine.row;

import java.util.Collection ;
import java.util.Map ;

import org.apache.jena.atlas.lib.DS ;
import org.seaborne.dboe.engine.Row ;

import org.apache.jena.sparql.core.Var ;

public class RowBase<X> implements Row<X>
{
    // Scope for smaller e.g versions that are 1 entry, 2 entries etc etc.
    private Map<Var, X> row = DS.map() ;
    
    public RowBase() {}
    
    public RowBase(Row<X> other) {
        for ( Var v : other.vars() )
            row.put(v, other.get(v)) ; 
    }
    
    /* package, the builder */ public void set(Var var, X value)   { row.put(var, value) ; } 
    /* package, the builder */ public void unset(Var var)          { row.remove(var) ; } 
    
    @Override
    public X get(Var key) {
        return row.get(key) ;
    }

    @Override
    public boolean contains(Var v) {
        return row.containsKey(v) ;
    }

    @Override
    public Collection<Var> vars() {
        // unmodifiableCollection does not support .equals.
        //return Collections.unmodifiableCollection(row.keySet()) ;
        return row.keySet() ;
    }
    
    @Override
    public boolean isEmpty() {
        return row.isEmpty() ;
    }


    @Override
    public boolean isIdentity() {
        return isEmpty() ;
    }
    
    @Override
    public String toString() {
        return row.toString();  
    }

    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((row == null) ? 0 : row.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( !(obj instanceof RowBase) )
            return false ;
        RowBase<?> other = (RowBase<?>)obj ;
        if ( row == null ) {
            if ( other.row != null )
                return false ;
        } else if ( !row.equals(other.row) )
            return false ;
        return true ;
    }
}
