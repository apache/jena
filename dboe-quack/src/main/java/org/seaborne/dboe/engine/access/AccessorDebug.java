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

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.PredicateObjectList ;
import org.seaborne.dboe.engine.Row ;
import org.seaborne.dboe.engine.Slot ;
import org.slf4j.Logger ;

/** Debugging wrapper for an Accessor */
public class AccessorDebug<X> implements Accessor<X> {
    private final Logger log ;
    private final Accessor<X> accessor ;

    public AccessorDebug(Logger log, Accessor<X> accessor) {
        this.log = log ;
        this.accessor = accessor ;
    }

    @Override
    public Iterator<Tuple<X>> accessTuples(Tuple<X> pattern) {
        log.info("Accessor.accessTuples("+pattern+")");
        return accessor.accessTuples(pattern) ;
    }
    
    @Override
    public Iterator<Row<X>> accessRows(Tuple<Slot<X>> pattern) {
        log.info("Accessor.accessRows("+pattern+")");
        return accessor.accessRows(pattern) ;
    }
    
    @Override
    public Iterator<Row<X>> fetch(PredicateObjectList<X> accessList) {
        log.info("Accessor.fetch("+accessList+")");
        return accessor.fetch(accessList) ;
    }
}

