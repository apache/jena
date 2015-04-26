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

import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.sparql.core.Var ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.seaborne.dboe.engine.row.* ;

public class RowLib {
    public static <X> Row<X> identityRow() { return RowIdentity.identityRow() ; }
    
    public static <X> RowList<X> identityRowList() { return RowListIdentity.identityRowList() ; }

    public static <X> RowList<X> emptyRowList() { return RowListEmpty.emptyRowList() ; }

    public static <X> RowList<X> createRowList(Set<Var> varsLeft, Iterator<Row<X>> iterator) {
        return new RowListBase<>(varsLeft, iterator) ;
    }

    public static <X> Row<X> mergeRows(Row<X> row1, Row<X> row2, RowBuilder<X> builder) {
        builder.reset() ;
        builder.merge(row1) ;
        builder.merge(row2) ;
        return builder.build() ;
    }
    
    public static <X> RowBuilder<X> createRowBuilder() {
        return new RowBuilderBase<X>() ;
    }

    public static <X> RowListBuilder<X> createRowListBuilder() {
        return new RowListBuilderBase<X>() ;
    }

    /** Merge one row into a stream of rows */
    public static <X> Iterator<Row<X>> mergeRows(Iterator<Row<X>> rows1, Row<X> row2) {
        // RowVarBinding better?
        // Special row of two rows?
        
        RowBuilder<X> builder = new RowBuilderBase<>() ;
        MergeRow<X> t = new MergeRow<X>(row2) ; 
        return Iter.map(rows1, t) ;
    }

    static class MergeRow<X> implements Transform<Row<X>, Row<X>> {
        public final Row<X> row ;
        public final RowBuilder<X> builder = new RowBuilderBase<>() ;
        public MergeRow(Row<X> row) { 
            this.row = row ;
        }

        @Override
        public Row<X> convert(Row<X> item) {
            return mergeRows(item, row, builder) ;
        }
    }
}
