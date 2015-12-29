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

import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleFactory ;
import org.apache.jena.sparql.core.Var ;

/** A predicate-object list of 'X' */
public final class PredicateObjectList<X>
{
    private final Slot<X> graph ;
    private final Slot<X> subject ;
    private final List<X> predicates ;
    private final List<Slot<X>> objects ;
    private boolean multipleSamePredicate = false ; 
    private boolean multipleSameVar = false ;               // Same variable used more then once.
    private final Set<Var> vars = new HashSet<>() ;
    
    public PredicateObjectList(Slot<X> graph, Slot<X> subject) {
        this.graph = graph ;
        this.subject = subject ;
        this.predicates = new ArrayList<>() ;
        this.objects = new ArrayList<>() ;
    }
    
    public PredicateObjectList(X graph, X subject) {
        this(Slot.createTermSlot(graph), Slot.createTermSlot(subject)) ;
    }
    
    // Sublist
    private PredicateObjectList(PredicateObjectList<X> other, int fromIndex, int toIndex) {
        this.graph = other.graph ;
        this.subject = other.subject ;
        this.predicates = new ArrayList<>() ;
        this.objects = new ArrayList<>() ;
        
        for ( int idx = fromIndex ; idx < toIndex ; idx++ ) {
            add(other.predicates.get(idx), other.objects.get(idx)) ;
        }
            
//        this.predicates = other.predicates.subList(fromIndex, toIndex) ;
//        this.objects = other.objects.subList(fromIndex, toIndex) ;
//        for ( Slot<X> o : objects ) {
//            if ( o.isVar() )
//                vars.add(o.var) ;
//        }
    }

    public boolean isEmpty()    { return predicates.isEmpty() ; }
    public int size()           { return predicates.size() ; }

    public PredicateObjectList<X> slice(int fromIndex, int toIndex) { return new PredicateObjectList<>(this, fromIndex, toIndex) ; }
    public PredicateObjectList<X> slice(int fromIndex)              { return slice(fromIndex, this.size()) ; }
    
    public void add(X p, Slot<X> o) {
        int i = predicates.indexOf(p) ;
        if ( i >= 0 ) {
            Slot<X> oFirst = objects.get(i) ;
            if ( oFirst.equals(o) )
                // Same predicate + same variable or constant => no work to do.
                return ;
            // Execution needs to be careful
            multipleSamePredicate = true ;
        }

        predicates.add(p) ;
        objects.add(o) ;
        if ( o.isVar() ) {
            Var v = o.var ;
            if ( vars.contains(v) )
                multipleSameVar = true ;
            else
                vars.add(v) ;
        }
    }

    public void add(Slot<X> p, Slot<X> o) {
        if ( p.isVar() )
            throw new InternalErrorException() ;
        add(p.term, o) ;
    }
    
    public boolean containsPredicate(X predicate)
    { return predicates.contains(predicate) ; }
    
//    public Pair<X, Slot<X>> get(int idx) {
//        return Pair.create(predicates.get(idx) , objects.get(idx)) ;
//    }

    public Slot<X> getGraph() {
        return graph ;
    }

    public Slot<X> getSubject() {
        return subject ;
    }

    public X getPredicate(int idx) {
        return predicates.get(idx) ;
    }
    public List<X> getPredicates() {
        return predicates ;
    }

    public Slot<X> getObject(int idx) {
        return objects.get(idx) ;
    }

    public List<Slot<X>> getObjects() {
        return objects ;
    }

    public Tuple<Slot<X>> createTupleSlot(int idx) {
        Slot<X> g = getGraph() ;
        Slot<X> s = getSubject() ;
        X p = getPredicate(idx) ;
        Slot<X> o = getObject(idx) ;
        if ( g != null )
            return TupleFactory.tuple(g, s, Slot.createTermSlot(p), o) ;
        else
            return TupleFactory.tuple(s, Slot.createTermSlot(p), o) ;
    }
    
    public boolean isMultipleSamePredicate() {
        return multipleSamePredicate ;
    }

    public boolean isMultipleSameVar() {
        return multipleSameVar ;
    }

    public Set<Var> getVars() {
        return vars ;
    }
    
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder() ;
        buff.append("PredicateObjectList: ") ;
        if ( getGraph() != null )
            buff.append(getGraph()).append("/") ;
        buff.append(getSubject()) ;
        
        for ( int i = 0 ; i < this.size() ; i ++ ) {
            buff.append(" ") ;
            X p = getPredicate(i) ;
            Slot<X> o = getObject(i) ;
            buff.append("(").append(p).append(",").append(o).append(")") ;
        }
        return buff.toString() ;
    }


}
