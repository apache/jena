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

import java.util.ArrayList ;
import java.util.Collection ;

import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.RowBuilder ;

import com.hp.hpl.jena.sparql.core.Var ;
/** 
 * Implementation of Row based on a parent+chain of additonal bindings.
 */
public class RowVarBinding<X> implements Row<X> {
    // Companion builder?
    
    // Muttering about lack of tail recursion.
    private final Row<X> parentRow ;
    private final RowVarBinding<X> parent ;
    private final Var var ;
    private final X value ;
    private Collection<Var> vars = null ;
    
    public RowVarBinding(Row<X> parent, Var var, X value) {
        if ( parent instanceof RowVarBinding<?> ) {
            this.parent = (RowVarBinding<X>)parent ;
            this.parentRow = null ;
        } else {
            this.parent = null ;
            this.parentRow = parent ;
        }
        this.var = var ;
        this.value = value ;
    }
    
    public RowVarBinding(RowVarBinding<X> parent, Var var, X value) {
        this.parent = parent ;
        this.parentRow = null ;
        this.var = var ;
        this.value = value ;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append("(") ;
        String sep = "" ;
        RowVarBinding<X> x = this ;
        for (;;) {
            sb.append(sep).append(x.var).append("=").append(x.value) ;
            sep = ", " ;
            if ( x.parent == null )
                break ;
            x = x.parent ;
        }
        sb.append("=>") ;
        sb.append(x.parentRow) ;
        sb.append(")") ;
        return sb.toString() ;
    }
    // Avoid excession tail recursion by looping on RowVarBinding's until a
    // a proper row is encountered.
    @Override
    public X get(Var var) {
        RowVarBinding<X> here = this ;
        for(;;) {
            if ( here.var.equals(var) )
                return here.value ;
            if ( here.parent == null )
                break ;
            here = here.parent ;
        } 
        if ( here.parentRow != null ) return here.parentRow.get(var) ;
        return null ;
    }
    
    @Override
    public boolean contains(Var v) {
        return get(v) != null ;
    }
    @Override
    public Collection<Var> vars() {
        if ( vars == null ) {
            vars = new ArrayList<>() ;
            RowVarBinding<X> here = this ;
            for(;;) {
                vars.add(here.var) ;
                if ( here.parent == null )
                    break ;
                here = here.parent ;
            }
            if ( here.parentRow != null )
                vars.addAll(here.parentRow.vars()) ;
        }
        return vars ;
    }

    public Row<X> flatten(RowBuilder<X> rb) {
        RowVarBinding<X> here = this ;
        for (;;) {
            rb.add(here.var, here.value) ;
            if ( here.parent == null )
                break ;
            here = here.parent ;
        }
        if ( here.parentRow != null ) 
            rb.merge(here.parentRow) ;
        return rb.build() ;
    }
    
    @Override
    public boolean isEmpty() {
        return false ;
    }

    @Override
    public boolean isIdentity() {
        return false ;
    }
    
}