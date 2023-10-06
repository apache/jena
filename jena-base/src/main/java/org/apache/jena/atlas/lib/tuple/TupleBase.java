/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.lib.tuple;

import java.util.Objects ;

abstract class TupleBase<X> implements Tuple<X> {
    protected TupleBase() {}

    @Override
    public final
    int hashCode() {
        final int prime = 31;
        int result = 1;
        for ( int i = 0 ; i < len() ; i++ )
            result = prime * result + Objects.hashCode(get(i)) ;
        return result;
    }

    @Override
    public final
    boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false ;
        if ( ! ( obj instanceof Tuple<?> other) )
            return false ;
        if ( this.len() != other.len() )
            return false ;
        for ( int i = 0 ; i < this.len() ; i++ )
            if ( ! Objects.equals(this.get(i), other.get(i)) )
                return false ;
        return true;
    }

    @Override
    public final String toString() {
        return asList().toString() ;
    }
}
