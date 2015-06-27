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

import java.util.Iterator ;

public class IteratorInteger implements Iterator<Long>
{
    private final long start ;
    private final long finish ;
    private long current ;

    public static IteratorInteger range(long start, long finish)
    {
        return new IteratorInteger(start, finish) ;
    }
    
    
    /** [start, finish) */
    public IteratorInteger(long start, long finish)
    {
        this.start = start ;
        this.finish = finish ;
        this.current = start ;
    }
    
    @Override
    public boolean hasNext()
    {
        return ( current < finish ) ;
    }

    @Override
    public Long next()
    {
        Long v = current;
        current++ ;
        return v ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

}
