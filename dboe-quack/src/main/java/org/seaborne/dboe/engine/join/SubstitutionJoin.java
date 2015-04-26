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

package org.seaborne.dboe.engine.join;

import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.RepeatApplyIterator ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.access.AccessRows ;

import org.apache.jena.sparql.core.Var ;

/** Index join done by substituting for variables */
public class SubstitutionJoin {

    public static <X> RowList<X> substitutionJoin(RowList<X> left, Tuple<Slot<X>> pattern, AccessRows<X> accessor, RowBuilder<X> builder) {
        Iterator<Row<X>> iter = new SubstituteApply<X>(accessor, pattern, left.iterator(), builder) ;
        Set<Var> vars = null ;
        if ( left.vars() != null ) {
            vars = DS.set() ;
            vars.addAll(left.vars()) ;
            EngLib.accVars(vars, pattern) ;
        }
        return RowLib.createRowList(vars, iter) ;
    }

    private static class SubstituteApply<X> extends RepeatApplyIterator<Row<X>> {
        private long s_countLHS = 0 ;
        //private long s_countRHS = 0 ;
        private long s_countResults = 0 ;
        
        private final Tuple<Slot<X>> pattern ;
        private final AccessRows<X> accessor ;
        private final RowBuilder<X> builder ;

        protected SubstituteApply(AccessRows<X> accessor, Tuple<Slot<X>> pattern, Iterator<Row<X>> input, RowBuilder<X> builder) {
            super(input) ;
            this.pattern = pattern ;
            this.accessor = accessor ;
            this.builder = builder ;
        }

        @Override
        protected Iterator<Row<X>> makeNextStage(final Row<X> row) {
            s_countLHS ++ ;
            Tuple<Slot<X>> subst = EngLib.substitute(pattern, row) ;
            Iterator<Row<X>> iter1 = accessor.accessRows(subst) ;
            Transform<Row<X>, Row<X>> addIncoming = new Transform<Row<X>, Row<X>>(){
                @Override
                public Row<X> convert(Row<X> item) {
                    Row<X> r = Join.merge(item, row, builder) ;
                    if ( r != null )
                        s_countResults ++ ;
                    return r ;
                }
            } ;
            Iterator<Row<X>> iter2 = Iter.map(iter1, addIncoming) ;
            return iter2 ;
        }
        
        @Override
        protected void hasFinished() {
            if ( Quack.JOIN_EXPLAIN ) {
                FmtLog.debug(Quack.joinStatsLog,
                             "SubstitutionJoin: LHS=%d Results=%d",
                             s_countLHS, s_countResults
                             ) ;
            }
        }
    }
}
