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

package org.apache.jena.atlas.iterator;

/**
 * Add a filter to a chain - the original filter is called after this new sub-filter.
 */
public abstract class FilterStack<T> implements  Filter<T>
{
    private final Filter<T> other ;
    private final boolean subFilterLast ;
    
    public FilterStack(Filter<T> other) { this(other, false) ; }
    
    public FilterStack(Filter<T> other, boolean callOldFilterFirst)
    {
        this.other = other ;
        this.subFilterLast = callOldFilterFirst ;
    }
   
    @Override
    public final boolean accept(T item)
    {
        if ( subFilterLast )
            return acceptAdditionaOther(item) ;
        else
            return acceptOtherAdditional(item) ;
    }
    
    private boolean acceptAdditionaOther(T item)
    {
        if ( ! acceptAdditional(item) )
            return false ;
        
        if ( other != null && ! other.accept(item) )
            return false ;
        
        return true ;
    }

    private boolean acceptOtherAdditional(T item)
    {
        if ( other != null && ! other.accept(item) )
            return false ;
        return acceptAdditional(item) ;
    }

    
    /** Additional filter condition to apply */
    public abstract boolean acceptAdditional(T item) ;
}
