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

import com.hp.hpl.jena.sparql.core.Var ;

public interface RowBuilder<X>  { 
    RowBuilder<X> merge(Row<X> other) ;
    RowBuilder<X> add(Var key, X value) ;
    //RowBuilder<X> delete(Var key) ;
    //RowBuilder<X> replace(Var key, X newValue) ;
    boolean contains(Var key) ;
    X get(Var key) ;
    Row<X> build() ;
    RowBuilder<X> reset() ;
    /** Return a different, completely separate, RowBuilder with the same current state */ 
    RowBuilder<X> duplicate() ;
}