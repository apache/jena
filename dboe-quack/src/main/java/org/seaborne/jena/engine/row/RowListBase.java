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

package org.seaborne.jena.engine.row;

import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.logging.Log ;
import org.seaborne.jena.engine.Row ;
import org.seaborne.jena.engine.RowList ;

import com.hp.hpl.jena.sparql.core.Var ;

public class RowListBase<X> implements RowList<X>
{
    // Why is this not an interface?
    private final Set<Var> vars ;
    private final Iterator<Row<X>> rows ;
    private boolean yielded = false ;
    private boolean isEmpty ;

    public RowListBase(Set<Var> vars, Iterable<Row<X>> rows )
    { this(vars, rows.iterator()) ; }
    
    public RowListBase(Set<Var> vars, Iterator<Row<X>> rows) {
        this.vars = vars ;
        this.rows = rows ;
        this.isEmpty = !rows.hasNext() ;
    }   
    
    @Override
    public Set<Var> vars() { return vars ; }

    @Override
    public List<Row<X>> toList()    { return Iter.toList(rows) ; }
    @Override
    public RowListBase<X> materialize() { return new RowListBase<>(vars, toList()) ; }
    
    @Override
    public boolean isIdentity() { return vars.isEmpty() && isEmpty() ; }
    
    @Override
    public boolean isEmpty() { return isEmpty ; }

    @Override
    public Iterator<Row<X>> iterator() {
        if ( yielded )
            Log.warn(this, "Already returned the rows iterator") ;
        yielded = true ; 
        return rows ;
    }
    
    @Override
    public String toString() { return "RowList"+vars ; }
}
