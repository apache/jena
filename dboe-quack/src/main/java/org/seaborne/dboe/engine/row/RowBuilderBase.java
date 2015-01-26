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

package org.seaborne.dboe.engine.row ;

import com.hp.hpl.jena.sparql.core.Var ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Lib ;
import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.RowBuilder ;

/** RowBuilder */

public class RowBuilderBase<X> implements RowBuilder<X>
{
    private static final boolean CHECK = true ;
    private RowBase<X> row = new RowBase<X>() ;

    public RowBuilderBase() {}
    
    @Override
    public RowBuilder<X> add(Var var, X value) { 
        if ( row == null )
            throw new IllegalStateException("Row has been built - reset needed") ;
        if ( row.contains(var) )
            // Could test "compatibility" (same value) here but better done elsewhere. 
            throw new InternalErrorException("Variable "+var+ " already present ("+row.get(var)+","+value+")") ;
        row.set(var, value); 
        return this ;
    }

//    @Override
//    public RowBuilder<X> delete(Var var) {
//        if ( row == null )
//            throw new IllegalStateException("Row has been built - reset needed") ;
//        row.unset(var);
//        return this ;
//    }

//    @Override
//    public RowBuilder<X> replace(Var key, X newValue) {
//        if ( row == null )
//            throw new IllegalStateException("Row has been built - reset needed") ;
//        // No check. 
//        row.set(key, newValue);
//        return this ;
//    }

    @Override
    public boolean contains(Var key)    { return row.contains(key) ; }
    
    @Override
    public X get(Var key)               { return row.get(key) ; }
    
    @Override
    public RowBuilder<X> merge(Row<X> other) {
        if ( row == null )
            throw new InternalErrorException("Build already called") ;
        for ( Var var : other.vars() ) {
            boolean b  = row.contains(var) ;
            if ( b ) {
                if ( CHECK ) {
                    X val = row.get(var) ;
                    X newVal = other.get(var) ;
                    if ( ! Lib.equal(newVal, val))
                        throw new InternalErrorException("Incompatible: "+var+ " already present ("+val+","+newVal+")") ;
                }  
                 // No check - silently discard.
            } else
                row.set(var, other.get(var));
        }
        return this ;
    }

    @Override
    public Row<X> build() {
        if ( row == null )
            throw new InternalErrorException("Build already called") ;
        Row<X> r = row ;
        row = null ;
        // Copy?
        return r ;
    }

    @Override
    public RowBuilder<X> reset()
    { 
        row = new RowBase<X>() ;
        return this ;
    }

    @Override
    public RowBuilder<X> duplicate() {
        RowBuilderBase<X> dup = new RowBuilderBase<>() ;
        dup.merge(row) ;
        return dup ;
    }

}
