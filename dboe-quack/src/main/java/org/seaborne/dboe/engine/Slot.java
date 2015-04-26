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

package org.seaborne.dboe.engine;

import org.apache.jena.atlas.lib.Lib ;

import org.apache.jena.sparql.core.Var ;

/** Abstraction of either a variable or a term, never both. */
public final class Slot<X> {
    // Exactly one of these is null and the other non-null.
    public final X term ;
    public final Var var ;
    
    public static <X> Slot<X> createVarSlot(Var var) { return new Slot<X>(var, null) ; }
    public static <X> Slot<X> createTermSlot(X term) { return new Slot<X>(null, term) ; }
    
    private Slot(Var var, X id)
    {
        this.term = id ;
        this.var = var ;
    }
    
    public boolean isVar()  { return var != null ; }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder() ;
        //builder.append("(") ;
        if ( var == null )
        {
            builder.append("id=") ;
            builder.append(term) ;
        }else {
            builder.append("var=") ;
            builder.append(var) ;
        }
        //builder.append(")") ;
        return builder.toString() ;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((term == null) ? 0 : term.hashCode()) ;
        result = prime * result + ((var == null) ? 0 : var.hashCode()) ;
        return result ;
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( getClass() != obj.getClass() )
            return false ;
        @SuppressWarnings("unchecked")
        Slot<X> other = (Slot<X>)obj ;
        return Lib.equal(term, other.term) && Lib.equal(var, other.var) ;  
    }
}
