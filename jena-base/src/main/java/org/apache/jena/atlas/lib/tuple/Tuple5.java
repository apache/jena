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

/**
 * A tuple of 5 items.
 */
public class Tuple5<X> extends TupleBase<X> {
    protected final X x1 ; 
    protected final X x2 ;
    protected final X x3 ;
    protected final X x4 ;
    protected final X x5 ;

    protected Tuple5(X x1, X x2, X x3, X x4, X x5) {
        this.x1 = x1 ;
        this.x2 = x2 ;
        this.x3 = x3 ;
        this.x4 = x4 ;
        this.x5 = x5 ;
    }

    @Override
    public final X get(int i) {
        switch (i) {
            case 0: return x1 ;
            case 1: return x2 ;
            case 2: return x3 ;
            case 3: return x4 ;
            case 4: return x5 ;
        }
        throw new IndexOutOfBoundsException() ;
    }

    @Override
    public final int len() {
        return 5 ;
    }
}