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

package org.apache.jena.atlas.lib;

import static org.apache.jena.atlas.lib.Lib.hashCodeObject ;
import static org.apache.jena.atlas.lib.StrUtils.str ;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Pair<A, B>
{
    public static <L, R> Pair<L,R> create(L x, R y) { return new Pair<>(x,y) ; }
    
    final A a ;
    final B b ;
    public Pair(A a, B b) { this.a = a; this.b = b ; }
    
    public A getLeft()  { return a ; }
    public B getRight() { return b ; }
    
    public A car() { return a ; }
    public B cdr() { return b ; }
    
    public static class OfSameType<T> extends Pair<T, T> {

        public OfSameType(T a, T b) {
            super(a, b);
        }

        public void forEach(Consumer<T> op) {
            op.accept(a);
            op.accept(b);
        }

        public boolean both(Function<T, Boolean> op) {
            return op.apply(a) && op.apply(b);
        }

        public boolean either(Function<T, Boolean> op) {
            return op.apply(a) || op.apply(b);
        }
    }
    
    @Override
    public int hashCode()
    {
        return hashCodeObject(car()) ^ hashCodeObject(cdr())<<1 ; 
    }

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        // If it's a pair of a different <A,B> then .equals
        // Pair<A,B>(null,null) is equal to Pair<C,D>(null ,null)
        // Type erasure makes this hard to check otherwise.
        // Use class X extends Pair<A,B> and implement .equals to do
        // instanceof then call super.equals.
        
        if( ! ( other instanceof Pair<?,?> ) ) return false ;
        Pair<?,?> p2 = (Pair<?,?>)other ;
        return Objects.equals(car(), p2.car()) && Objects.equals(cdr(), p2.cdr()) ;
    }
    
    @Override 
    public String toString() { return "("+str(a)+", "+str(b)+")" ; }
}
