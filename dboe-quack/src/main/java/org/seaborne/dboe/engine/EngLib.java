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

package org.seaborne.dboe.engine;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.DS ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.Tuple ;

import org.apache.jena.sparql.core.Var ;

/** Library of functions for the generic join engine */

public class EngLib {
    /** Replace occurences of a variable by the value from a Row */ 
    public static <X> Tuple<Slot<X>> substitute(Tuple<Slot<X>> ts, Row<X> row) {
        if ( row.isIdentity() )
            return ts ;
        final int N = ts.size() ; 
        @SuppressWarnings("unchecked")
        Slot<X>[] slots = (Slot<X>[])new Slot<?>[N] ;
        for ( int i = 0 ; i < N ; i++ )
            slots[i] = substitute(ts.get(i), row) ;
        return Tuple.create(slots) ;
    }
    
    public static <X> Slot<X> substitute(Slot<X> slot, Row<X> row) {
        if ( slot == null )
            return null ;
        if ( slot.isVar() && row.contains(slot.var) )
            return Slot.createTermSlot(row.get(slot.var)) ;
        return slot ;
    }
    
    public static <X> PredicateObjectList<X> substitute(PredicateObjectList<X> predObjs, Row<X> row) {
        Slot<X> s = substitute(predObjs.getSubject(), row) ;
        Slot<X> g = substitute(predObjs.getGraph(), row) ;
        PredicateObjectList<X> predObjs2 = new PredicateObjectList<X>(g, s) ;
        for ( int i = 0 ; i < predObjs.size() ; i++ ) {
            X p = predObjs.getPredicate(i) ;
            Slot<X> o = predObjs.getObject(i) ;
            o = substitute(o, row) ;
            predObjs2.add(p, o); 
        }
        return predObjs2 ;
    }

    /** Accumulate Vars from a tuple */ 
    public static <X> void accVars(Collection<Var> vars, Tuple<Slot<X>> tuple) {
        for ( Slot<X> s : tuple ) {
            if ( s.isVar() )
                vars.add(s.var) ;
        }           
    }
    
    /** Vars from a tuple */ 
    public static <X> Set<Var> vars(Tuple<Slot<X>> tuple) {
        Set<Var> x = DS.set() ;
        for ( Slot<X> s : tuple ) {
            if ( s.isVar() && ! x.contains(s.var) )
                x.add(s.var) ; 
        }        
        return x ;
    }
    
    /** Count the number of occurences of variables in a Tuple.
     * Repeated use of the same variable counts multiple times. 
     */
    public static <X> int countVars(Tuple<Slot<X>> tuple) {
        int x = 0 ;
        for ( Slot<X> s : tuple ) {
            if ( s.isVar() )
                x++ ; 
        }        
        return x ;
    }
    
    /** Extract a Tuple of X or the default value */ 
    public static <X> Tuple<X> convertTupleToAny(Tuple<Slot<X>> slots, X anyMark) {
        int N = slots.size() ;
        @SuppressWarnings("unchecked")
        X n[] = (X[])new Object[N] ;
        for ( int i = 0 ;  i < N ; i++ )
            if ( slots.get(i).isVar() )
                n[i] = anyMark ;
            else
                n[i] = slots.get(i).term ;
        return Tuple.create(n) ;
    }
    
    /** Convert Iterator<Tuple<X>> to Iterator<Row<X>>, given a pattern.
     * Deals with multiple use of the same variable. 
     */  
    public static <X> Iterator<Row<X>> convertIterTuple(Iterator<Tuple<X>> iter, 
                                                        final Tuple<Slot<X>> pattern, 
                                                        final RowBuilder<X> builder) {
       Transform<Tuple<X>, Row<X>> transform = new Transform<Tuple<X>, Row<X>>(){
           @Override
           public Row<X> convert(Tuple<X> item) {
               builder.reset() ;
               for ( int i = 0 ; i < pattern.size() ; i++ ) {
                   Slot<X> s = pattern.get(i) ;
                   if ( s.isVar() ) {
                       Var var = s.var ;
                       X val = item.get(i) ;
                       if ( builder.contains(var) ) {
                           X valPrev = builder.get(var) ;
                           if ( ! val.equals(valPrev) )
                               return null ;
                           else
                               continue ;
                       }
                       builder.add(var, val);
                   }
               }
               return builder.build()  ;
           }
       } ; 
       return Iter.removeNulls(Iter.map(iter, transform)) ;
   }

    public static final <X> X subject(Tuple<X> tuple) {
        switch(tuple.size()) {
            case 3: return tuple.get(0) ;
            case 4: return tuple.get(1) ;
            default: throw new InternalErrorException("Tuple size not 3 or 4") ;
        }
    }

    public static final <X> X predicate(Tuple<X> tuple) {
        switch(tuple.size()) {
            case 3: return tuple.get(1) ;
            case 4: return tuple.get(2) ;
            default: throw new InternalErrorException("Tuple size not 3 or 4") ;
        }
    }

    public static final <X> X object(Tuple<X> tuple) {
        switch(tuple.size()) {
            case 3: return tuple.get(2) ;
            case 4: return tuple.get(3) ;
            default: throw new InternalErrorException("Tuple size not 3 or 4") ;
        }
    }

    public static final <X> X graph(Tuple<X> tuple) {
        switch(tuple.size()) {
            case 3: return null ;
            case 4: return tuple.get(0) ;
            default: throw new InternalErrorException("Tuple size not 3 or 4") ;
        }
    }
}
