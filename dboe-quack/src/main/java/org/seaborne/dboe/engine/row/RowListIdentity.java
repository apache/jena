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

import java.util.* ;

import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.RowLib ;
import org.seaborne.dboe.engine.RowList ;

import com.hp.hpl.jena.sparql.core.Var ;

/** The join identity RowList : one row, no variables */ 
public class RowListIdentity<X> implements RowList<X>
{
    /* package*/ 
    @SuppressWarnings("rawtypes")
    private static RowListIdentity instance = new RowListIdentity<>() ; 
    
    @SuppressWarnings({"unchecked", "cast"})
    public static <Z> RowList<Z> identityRowList() { return (RowList<Z>)instance ; }
    
    private static Set<Var> vars = Collections.emptySet() ;
    
    @Override
    public String toString() { return "RowList"+vars ; }

    @Override
    public Set<Var> vars() { return Collections.emptySet() ; }

    @Override
    public List<Row<X>> toList() { return Collections.<Row<X>>singletonList(RowLib.<X>identityRow()) ; }

    @Override
    public RowList<X> materialize() {
        return this ;
    }

    @Override
    public boolean isIdentity() {
        return true ;
    }
    
    @Override
    public boolean isEmpty() {
        return false ;
    }


    @Override
    public Iterator<Row<X>> iterator() {
        return toList().iterator() ;
    }
}
