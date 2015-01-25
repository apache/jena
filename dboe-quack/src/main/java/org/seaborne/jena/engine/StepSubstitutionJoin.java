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

package org.seaborne.jena.engine;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.jena.engine.access.AccessRows ;
import org.seaborne.jena.engine.join.SubstitutionJoin ;

public class StepSubstitutionJoin<X> implements Step<X> {

    private Tuple<Slot<X>> pattern ;
    private AccessRows<X> accessor ;
    private RowBuilder<X> builder ;

    public StepSubstitutionJoin(Tuple<Slot<X>> tuple, AccessRows<X> accessor, RowBuilder<X> builder) { 
        this.pattern = tuple ;
        this.accessor = accessor ;
        this.builder = builder ;
    }
    
    @Override
    public RowList<X> execute(RowList<X> input) {
        return SubstitutionJoin.substitutionJoin(input, pattern, accessor, builder) ;
    }

    @Override
    public String toString() { return "Step/SubstitutionJoin:"+pattern ; }
}
