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

package projects.dsg2;

import java.util.AbstractList ;
import java.util.Collection ;
import java.util.List ;
import java.util.Set ;
import java.util.stream.Collectors ;
import java.util.stream.Stream ;

public class Lib8 {
    /** Iterator to Stream */ 

    public static <X> List<X> toList(Stream<X> stream) {
        return stream.collect(Collectors.toList()) ;
    }
    
    public static <X> Set<X> toSet(Stream<X> stream) {
        return stream.collect(Collectors.toSet()) ;
    }
    
    public static <X> X first(Stream<X> stream) {
        return stream.findFirst().orElse(null) ;
    }
    
    public static <X> X element(Collection<X> collection) {
        return first(collection.stream()) ;
    }

    public static <X> Stream<X> print(Stream<X> stream) {
        stream = stream.map(item -> { System.out.println(item) ; return item ; }) ;
        return toList(stream).stream() ;
    }

    
    /** Immutable list that adds an element to the front of another list without copying */ 
    static class ListFront<T> extends AbstractList<T> {
        private final T elt ;
        private final List<T> tail ;

        ListFront(T elt, List<T> tail) {
            this.elt = elt ;
            this.tail = tail ;
        }
        
        @Override
        public T get(int index) {
            if ( index < 0 )
                throw new IndexOutOfBoundsException("Negative index") ;
            if ( index == 0 )
                return elt ;
            
            if ( index >= tail.size() )
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size()) ;

            return null ;
        }

        @Override
        public int size() {
            return tail.size()+1 ;
        }
        
    }
    
    public static <T> List<T> addHead(T elt, List<T> tail) { return new ListFront<>(elt, tail) ; }
}

