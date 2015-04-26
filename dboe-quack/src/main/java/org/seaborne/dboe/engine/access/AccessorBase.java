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

package org.seaborne.dboe.engine.access;

import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.join.SubstitutionJoin ;
import org.seaborne.dboe.engine.row.RowBuilderBase ;

import org.apache.jena.sparql.core.Var ;

/** Build a simple Accessor on top of the "accessRows" operation.
 * Also provies, as statiuc, functions to build the operations on topof simpler ones
 * (not necessarily the most efficient way but functionally correct
 */
public abstract class AccessorBase<X> implements Accessor<X> {
    // -- AccessData
    /** Access by constant/wildcard  pattern to get tuples of data.
     *  AccessorBase builds everythign on top of this. */ 
    @Override abstract public Iterator<Tuple<X>> accessTuples(Tuple<X> pattern) ;
    
    //---- AccessRows
    /** Access data via the pattern, using builder to create rows.
     * @param  pattern (elements may not be null)
     * @return Rows, as an iterator
     */
    @Override
    public Iterator<Row<X>> accessRows(Tuple<Slot<X>> pattern) {
        return accessRows(this, pattern) ;
    }
    
    public static <X> Iterator<Row<X>> accessRows(AccessData<X> access, Tuple<Slot<X>> pattern) {
        RowBuilder<X> builder = new RowBuilderBase<>() ;
        Tuple<X> tuple = EngLib.convertTupleToAny(pattern, null) ;
        Set<Var> vars = EngLib.vars(pattern) ;
        Iterator<Tuple<X>> iter = access.accessTuples(tuple) ;
        Iterator<Row<X>> x = EngLib.convertIterTuple(iter, pattern, builder) ;
        return x ;
    }

    // ---- AccessRows
    /** Access with a predicate-object list */ 
    @Override public Iterator<Row<X>> fetch(PredicateObjectList<X> predObjs) {
        // Divide into term and var cases for subclases to handle. 
        Slot<X> gSlot = predObjs.getGraph() ;
        X g = (gSlot==null) ? null : gSlot.term ;
        Slot<X> subject = predObjs.getSubject() ;
        if ( subject.isVar() )
            return fetchVarSubject(predObjs, g, subject.var) ;
        else
            return fetchTermSubject(predObjs, g, subject.term) ;
    }
    
    public static <X> RowList<X> fetch(AccessRows<X> accessRows , PredicateObjectList<X> predObjs ) {
        RowList<X> chain = RowLib.identityRowList() ;
        for ( int i = 0 ; i < predObjs.size() ; i++ ) {
            RowBuilder<X> builder = RowLib.createRowBuilder() ;
            Tuple<Slot<X>> tuple = predObjs.createTupleSlot(i) ;
            chain = SubstitutionJoin.substitutionJoin(chain, tuple, accessRows, builder) ;
        }
        return chain ;
    }
    
    protected Iterator<Row<X>> fetchTermSubject(PredicateObjectList<X> predObjs, X g, X subject) {
        // Ignore split and simply execute
        return fetch(this, predObjs).iterator() ;
    }

    protected Iterator<Row<X>> fetchVarSubject(PredicateObjectList<X> predObjs, X g, Var var) {
        // Ignore split and simply execute
        return fetch(this, predObjs).iterator() ;
    }
    
}
