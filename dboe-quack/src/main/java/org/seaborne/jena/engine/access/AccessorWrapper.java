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

package org.seaborne.jena.engine.access;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.jena.engine.PredicateObjectList ;
import org.seaborne.jena.engine.Row ;
import org.seaborne.jena.engine.Slot ;


public class AccessorWrapper<X> implements Accessor<X> {
    private final Accessor<X> accessor ;

    public AccessorWrapper(Accessor<X> accessor) {
        this.accessor = accessor ;
    }

    @Override
    public Iterator<Tuple<X>> accessTuples(Tuple<X> pattern) {
        return accessor.accessTuples(pattern) ;
    }
    
    @Override
    public Iterator<Row<X>> accessRows(Tuple<Slot<X>> pattern) {
        return accessor.accessRows(pattern) ;
    }
    
    @Override
    public Iterator<Row<X>> fetch(PredicateObjectList<X> accessList) {
        return accessor.fetch(accessList) ;
    }
}

