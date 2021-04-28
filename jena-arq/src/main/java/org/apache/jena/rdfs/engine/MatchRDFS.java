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

package org.apache.jena.rdfs.engine;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.rdfs.setup.ConfigRDFS;

/**
 * Match a 3-tuple, which might have wildcards, applying a fixed set of inference rules.
 * This class is the core machinery of matching data using an RDFS schema.
 * This is inference on the A-Box (the data) with respect to a fixed T-Box
 * (the vocabulary, ontology).
 * <p>
 * This class implements:
 * <ul>
 * <li>rdfs:subClassOf (transitive)</li>
 * <li>rdfs:subPropertyOf (transitive)</li>
 * <li>rdfs:domain</li>
 * <li>rdfs:range</li>
 * </ul>
 *
 * @see ApplyRDFS ApplyRDFS for the matching algorithm.
 */
public abstract class MatchRDFS<X, T> extends CxtInf<X, T> implements Match<X,T> {
    // [RDFS] Marks places for possible improvements.
    // The primary use case is data access with inference.
    //
    // One area is the need to add "distinct" because the data may itself have RDFS
    // vocabulary usage overlapping with the vocabulary itself.

    // ----

    // Function from a T (a 3-tuples, e.g. a Triple) to all
    // the inferences from the T.
    // Includes the input tuple.
    private final Function<T,Stream<T>> applyInf;

    public MatchRDFS(ConfigRDFS<X> setup, MapperX<X,T> mapper) {
        super(setup, mapper);
        this.applyInf = t-> {
            // Revisit use of applyInf.
            //   used in inf from find_ANY_ANY_ANY
            //   used in inf from infFilter from ANY_ANY_T, ANY_type_ANY, X_ANY_Y
            // [RDFS]  Non-collecting stream engine. Replace?
            List<T> x = new ArrayList<>();
            x.add(t);
            Output<X> dest = (s,p,o) -> x.add(dstCreate(s,p,o));
            ApplyRDFS<X, T> streamInf = new ApplyRDFS<>(setup, mapper);
            // process needed but infer+explicit current triple saves a create.
            streamInf.infer(mapper.subject(t), mapper.predicate(t), mapper.object(t), dest);
            return x.stream();
        };
    }

    @Override
    public final Stream<T> match(X s, X p, X o) { return matchWithInf(s, p ,o); }

    // Access data.
    protected abstract boolean sourceContains(X s, X p, X o);
    protected abstract Stream<T> sourceFind(X s, X p, X o);
    protected abstract T dstCreate(X s, X p, X o);

    protected final X subject(T tuple)        { return mapper.subject(tuple); }
    protected final X predicate(T tuple)      { return mapper.predicate(tuple); }
    protected final X object(T tuple)         { return mapper.object(tuple); }

    /**
     * Break the dispatch match request to different cases so each case can be written clearly.
     */
    private Stream<T> matchWithInf(X _subject, X _predicate, X _object) {
        X subject = any(_subject);
        X predicate = any(_predicate);
        X object = any(_object);

        // find_??_rdf:type_??
        if ( rdfType.equals(predicate) ) {
            if ( isTerm(subject) ) {
                if ( isTerm(object) )
                    return find_X_type_T(subject, object);
                else
                    return find_X_type_ANY(subject);
            } else {
                if ( isTerm(object) )
                    return find_ANY_type_T(object);
                else
                    return find_ANY_type_ANY();
            }
        }

        // find_??_ANY_??
        if ( isANY(predicate) ) {
            if ( isTerm(subject) ) {
                if ( isTerm(object) )
                    return find_X_ANY_Y(subject, object);
                else
                    return find_X_ANY_ANY(subject);
            } else {
                if ( isTerm(object) )
                    return find_ANY_ANY_Y(object);
                else
                    return find_ANY_ANY_ANY();
            }
        }

        // find_ANY_subClassOf_ANY
        // find ANY_subPropertyOf_ANY
        if ( rdfsSubPropertyOf.equals(predicate) || rdfsSubClassOf.equals(predicate) )
                return sourceFind(subject, predicate, object);

        // find_ANY_property_ANY where the property is not special.
        return find_ANY_property_ANY(subject, predicate, object);
    }

    private Stream<T> find_ANY_property_ANY(X subject, X predicate, X object) {
        // Predicate is not rdf:type, rdfsSubPropertyOf, rdfsSubClassOf.
        Stream<T> tuples = sourceFind(subject, predicate, object);
        Set<X> predicates = setup.getSubProperties(predicate);
        if ( isEmpty(predicates) )
            return tuples;
        for ( X p : predicates ) {
            Stream<T> stream = sourceFind(subject, p, object);
            // Rewrite with predicate
            stream = stream.map(tuple -> dstCreate(subject(tuple), predicate, object(tuple)) );
            tuples = Stream.concat(tuples, stream);
        }
        // Subproperties in the data. (:s :p :o) and (:s :sub_p :o) -> 2 (:s :p :o)
        tuples = tuples.distinct();
        return tuples;
    }

    private Stream<T> find_X_type_T(X subject, X object) {
        if ( sourceContains(subject, rdfType, object) )
            return Stream.of(dstCreate(subject, rdfType, object));

        if ( setup.hasOnlyPropertyDeclarations() )
            return Stream.empty();

        // Accumulate types, testing whether we can exit early, then calculate supertypes.
        Set<X> types = new HashSet<>();

        accTypesRange(types, subject);
        if ( types.contains(object) )
            return Stream.of(dstCreate(subject, rdfType, object));

        accTypesDomain(types, subject);
        if ( types.contains(object) )
            return Stream.of(dstCreate(subject, rdfType, object));

        accTypes(types, subject);
        if ( types.contains(object) )
            return Stream.of(dstCreate(subject, rdfType, object));

        // Expand with supertypes
        types = superTypes(types);
        if ( types.contains(object) )
            return Stream.of(dstCreate(subject, rdfType, object));

        return Stream.empty();
    }

    private Stream<T> find_X_type_ANY(X subject) {
        Set<X> types = new HashSet<>();
        accTypesRange(types, subject);
        accTypesDomain(types, subject);
        accTypes(types, subject);
        // expand supertypes
        types = superTypes(types);
        return types.stream().map( type -> dstCreate(subject, rdfType, type));
    }

    private Stream<T> find_ANY_type_T(X type) {
        // If in the data and only in the data,

        // Fast path - no rdf:type work.
        if ( setup.hasOnlyPropertyDeclarations() )
            return sourceFind(ANY, rdfType, type);

        // Using a set suppresses duplicates.
        Set<T> tuples = new HashSet<>();
        Set<X> types = subTypes(type);
        // [RDFS] Make these streams?

        accInstances(tuples, types, type);
        accInstancesRange(tuples, types, type);
        accInstancesDomain(tuples, types, type);
        return tuples.stream();
    }

    private Stream<T> find_ANY_type_ANY() {
        if ( setup.hasOnlyPropertyDeclarations() )
            return sourceFind(ANY, rdfType, ANY);
        Stream<T> stream = sourceFind(ANY, ANY, ANY);
        // [RDFS] revisit. These are the only uses of inf/infFilter
        stream = infFilter(stream, null, rdfType, null);
        return stream;
    }

    // The ANY predicate is not a special predicate
    private Stream<T> find_X_ANY_Y(X subject, X object) {
        // Start at X.
        // (X ? ?) - inference - project "X ? T"
        // also (? ? X) if there is a range clause.
        Stream<T> stream = sourceFind(subject, ANY, ANY);
        // + reverse (used in object position and there is a range clause)
        // domain was taken care of above.
        if ( setup.hasRangeDeclarations() )
            stream = Stream.concat(stream, sourceFind(ANY, ANY, subject));
        return infFilter(stream, subject, ANY, object);
    }

    // ANY predicate is not a special predicate
    private Stream<T> find_X_ANY_ANY(X subject) {
        // Can we do better?
        return find_X_ANY_Y(subject, ANY);
    }

    // ANY predicate is not a special predicate
    private Stream<T> find_ANY_ANY_Y(X object) {
        Stream<T> stream = sourceFind(ANY, ANY, object);

        // Remove rdf:type because we want to drive that through the inference processing.
        stream = stream.filter( triple -> ! predicate(triple).equals(rdfType)) ;
        // rdf:type with inference.
        // Does not includes "type subClassOf type"
        stream = Stream.concat(stream, find_ANY_type_T(object));
        stream = withSuperProperties(stream);
        return stream;
    }

    private Stream<T> find_ANY_ANY_ANY() {
        Stream<T> stream = sourceFind(ANY, ANY, ANY);

        // Remove rdf:type because we want to drive that through the inference processing.
        stream = stream.filter( triple -> ! predicate(triple).equals(rdfType)) ;

        // rdf:type with inference.
        // Does not includes "type subClassOf type"
        stream = Stream.concat(stream, find_ANY_type_ANY());

        // Super properties expansion.
        stream = withSuperProperties(stream);

        return stream;
    }

    /**
     * Apply super properties on a data stream (no interpretation of rdfs:subPropertyOf).
     * If (s p o) then (s q o) is also a data item
     * for an super property q of p.
     */
    private Stream<T> withSuperProperties(Stream<T> stream) {
        return stream.flatMap(triple->{
            X predicate = predicate(triple);
            Set<X> predicates = setup.getSuperProperties(predicate);
            if ( isEmpty(predicates) )
                return Stream.of(triple);
            List<T> acc = new ArrayList<>();
            acc.add(triple);
            X subject = subject(triple);
            X object = object(triple);
            predicates.forEach(p->acc.add(dstCreate(subject, p, object)));
            return acc.stream();
        });
    }

    /**
     * Apply super classes on a data stream (no interpretation of rdfs:subClassOf).
     * If (s rdf:type C) then (s rdf:type D) for any super class D of C.
     */
    private Stream<T> withSuperClasses(Stream<T> stream) {
        // [RDFS] Unused - should it be used?
        return stream.flatMap(triple->{
            X predicate = predicate(triple);
            if ( ! rdfType.equals(predicate) )
                return Stream.of(triple);
            X objType = object(triple);
            Set<X> subClasses = setup.getSuperClasses(objType);
            if ( isEmpty(subClasses) )
                return Stream.of(triple);
            List<T> acc = new ArrayList<>();
            acc.add(triple);
            X subject = subject(triple);
            subClasses.forEach(c->acc.add(dstCreate(subject, rdfType, c)));
            return acc.stream();
        });
    }

    /** Get subClasses or subPropertes of node */
    private Set<X> subOf(X predicate, X node) {
        if ( predicate.equals(rdfsSubClassOf) )
            return setup.getSubClassesInc(node);
        if ( predicate.equals(rdfsSubPropertyOf) )
            return setup.getSubPropertiesInc(node);
        throw new InternalErrorException("MatchRDFS.subOf called with "+predicate);
    }

    /** Get superClasses or supersPropertes of node */
    private Set<X> superOf(X node, X predicate) {
        Objects.requireNonNull(node);
        if ( predicate.equals(rdfsSubClassOf) )
            return setup.getSuperClassesInc(node);
        if ( predicate.equals(rdfsSubPropertyOf) )
            return setup.getSuperPropertiesInc(node);
        throw new InternalErrorException("MatchRDFS.superOf called with "+predicate);
    }

    /**
     * Convert {@literal Map<X, Set<X>>} to 3-tuples (key, predicate, value)
     * and concatenate to the {@literal Stream<T>}.
     */
    private Stream<T> expand(Stream<T> stream, Map<X, Set<X>> map, X predicate) {
        if ( isEmpty(map) )
            return stream;
        Stream<T> stream2 = map.entrySet().stream()
                .flatMap(e->
                    e.getValue().stream().map(x->dstCreate(e.getKey(), predicate, x)
                ));
        return Stream.concat(stream, stream2);
    }

    private Stream<T> infFilter(Stream<T> input, X subject, X predicate, X object) {
        Stream<T> stream = input.flatMap(applyInf::apply);
        boolean check_s = isTerm(subject);
        boolean check_p = isTerm(predicate);
        boolean check_o = isTerm(object);
        if ( !check_s && ! check_p && ! check_o )
            return stream;
        Predicate<T> filter = triple ->{
            return (! check_s || subject(triple).equals(subject)) &&
                   (! check_p || predicate(triple).equals(predicate)) &&
                   (! check_o || object(triple).equals(object)) ;
        };
        stream = stream.filter(filter);
        return stream;
    }

    private void accInstances(Set<T> tuples, Set<X> types, X requestedType) {
        for ( X type : types ) {
            Stream<T> stream = sourceFind(ANY, rdfType, type);
            stream.forEach(triple -> tuples.add(dstCreate(subject(triple), rdfType, requestedType)) );
        }
    }

    private void accInstancesDomain(Set<T> tuples, Set<X> types, X requestedType) {
        for ( X type : types ) {
            Set<X> predicates = setup.getPropertiesByDomain(type);
            if ( isEmpty(predicates) )
                continue;
            predicates.forEach(p -> {
                Stream<T> stream = sourceFind(ANY, p, ANY);
                stream.forEach(triple -> tuples.add(dstCreate(subject(triple), rdfType, requestedType)) );
            });
        }
    }

    private void accInstancesRange(Set<T> tuples, Set<X> types, X requestedType) {
        for ( X type : types ) {
            Set<X> predicates = setup.getPropertiesByRange(type);
            if ( isEmpty(predicates) )
                continue;
            predicates.forEach(p -> {
                Stream<T> stream = sourceFind(ANY, p, ANY);
                stream.forEach(triple -> tuples.add(dstCreate(object(triple), rdfType, requestedType)) );
            });
        }
    }

    private void accTypes(Set<X> types, X subject) {
        Stream<T> stream = sourceFind(subject, rdfType, ANY);
        stream.forEach(triple -> types.add(object(triple)));
    }

    private void accTypesDomain(Set<X> types, X X) {
        Stream<T> stream = sourceFind(X, ANY, ANY);
        stream.forEach(triple -> {
            X p = predicate(triple);
            Set<X> x = setup.getDomain(p);
            types.addAll(x);
        });
    }

    private void accTypesRange(Set<X> types, X X) {
        Stream<T> stream = sourceFind(ANY, ANY, X);
        stream.forEach(triple -> {
            X p = predicate(triple);
            Set<X> x = setup.getRange(p);
            types.addAll(x);
        });
    }

    private Set<X> subTypes(Set<X> types) {
        Set<X> x = new HashSet<>();
        for ( X type : types ) {
            Set<X> y = setup.getSubClasses(type);
            x.addAll(y);
            x.add(type);
        }
        return x;
    }

    private Set<X> superTypes(Set<X> types) {
        Set<X> x = new HashSet<>();
        for ( X type : types ) {
            Set<X> y = setup.getSuperClasses(type);
            x.addAll(y);
            x.add(type);
        }
        return x;
    }

    // Subtypes of a type, always include the type given.
    private Set<X> subTypes(X type) {
        Set<X> y = setup.getSubClassesInc(type);
        if ( isEmpty(y) )
            // type not in the vocabulary.
            return Collections.singleton(type);
        return y;
    }

    // Supertypes of a type, always include the type given.
    private Set<X> superTypes(X type) {
        Set<X> y = setup.getSuperClassesInc(type);
        if ( isEmpty(y) )
            // type not in the vocabulary.
            return Collections.singleton(type);
        return y;
    }

    private static <S> boolean isEmpty(Set<S> set) {
        return set == null || set.isEmpty();
    }

    private static <S> boolean isEmpty(Map<S, ?> map) {
        return map == null || map.isEmpty();
    }

//  private void print(Map<X, Set<X>> map) {
//      System.out.println("{");
//      CollectionUtils.forEach(map, (k,v)->System.out.printf("  %-20s  %s\n", k, v));
//      System.out.println("}");
//  }
//
//  private <A> Stream<A> print(Stream<A> stream) {
//      return StreamOps.debug(stream);
//  }
}
