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

package org.apache.jena.ontapi.model;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SWRL;
import org.apache.jena.vocabulary.SWRLB;

import java.util.Collection;

/**
 * A technical interface to generate SWRL Objects (Variable, Atoms, Imp).
 */
interface CreateSWRL {

    /**
     * Creates a SWRL Variable,
     * that is a URI resource with a type {@link SWRL#Variable swrl:Variable}.
     *
     * @param uri String, not {@code null}
     * @return {@link OntSWRL.Variable}
     */
    OntSWRL.Variable createSWRLVariable(String uri);

    /**
     * Creates a BuiltIn Atom.
     * An input predicate can be taken from {@link SWRLB SWRL Builins Vocabulary}.
     * Turtle syntax:
     * <pre>{@code
     * 	_:x rdf:type swrl:BuiltinAtom .
     * _:x swrl:arguments ( d1 ... d2 ) .
     * _:x swrl:builtin U .
     * }</pre>
     *
     * @param predicate an <b>URI</b>, {@link Resource}, not {@code null}
     * @param arguments {@code Collection} of {@link OntSWRL.DArg}s
     * @return {@link OntSWRL.Atom.WithBuiltin}
     * @see SWRLB
     */
    OntSWRL.Atom.WithBuiltin createBuiltInSWRLAtom(Resource predicate, Collection<OntSWRL.DArg> arguments);

    /**
     * Creates a Class Atom.
     * Turtle syntax:
     * <pre>{@code
     * _:x rdf:type swrl:ClassAtom .
     * _:x swrl:argument1 i .
     * _:x swrl:classPredicate C .
     * }</pre>
     *
     * @param clazz {@link OntClass}, not {@code null}
     * @param arg   {@link OntSWRL.IArg} (either {@link OntIndividual} or {@link OntSWRL.Variable}), not {@code null}
     * @return {@link OntSWRL.Atom.WithClass}
     */
    OntSWRL.Atom.WithClass createClassSWRLAtom(OntClass clazz, OntSWRL.IArg arg);

    /**
     * Creates a Data Range Atom.
     * Turtle syntax:
     * <pre>{@code
     * _:x rdf:type swrl:DataRangeAtom .
     * _:x swrl:argument1 d .
     * _:x swrl:dataRange D .
     * }</pre>
     *
     * @param range {@link OntDataRange}, not {@code null}
     * @param arg   {@link OntSWRL.DArg} (either {@link OntSWRL.Variable}
     *              or {@link org.apache.jena.rdf.model.Literal}), not {@code null}
     * @return {@link OntSWRL.Atom.WithDataRange}
     */
    OntSWRL.Atom.WithDataRange createDataRangeSWRLAtom(OntDataRange range, OntSWRL.DArg arg);

    /**
     * Creates a Data Property Atom.
     * Turtle syntax:
     * <pre>{@code
     * _:x rdf:type swrl:DatavaluedPropertyAtom .
     * _:x swrl:argument1 i .
     * _:x swrl:argument2 d .
     * _:x swrl:propertyPredicate R .
     * }</pre>
     *
     * @param property {@link OntDataProperty}, not {@code null}
     * @param first    {@link OntSWRL.IArg} (either {@link OntIndividual} or {@link OntSWRL.Variable}), not {@code null}
     * @param second   {@link OntSWRL.DArg} (either {@link OntSWRL.Variable}
     *                 or {@link org.apache.jena.rdf.model.Literal}), not {@code null}
     * @return {@link OntSWRL.Atom.WithDataProperty}
     */
    OntSWRL.Atom.WithDataProperty createDataPropertySWRLAtom(OntDataProperty property, OntSWRL.IArg first, OntSWRL.DArg second);

    /**
     * Creates an Object Property Atom.
     * Turtle syntax:
     * <pre>{@code
     * _:x rdf:type swrl:IndividualPropertyAtom .
     * _:x swrl:argument1 i1 .
     * _:x swrl:argument2 i2 .
     * _:x swrl:propertyPredicate P .
     * }</pre>
     *
     * @param property {@link OntObjectProperty}, not {@code null}
     * @param first    {@link OntSWRL.IArg} (either {@link OntIndividual} or {@link OntSWRL.Variable}), not {@code null}
     * @param second   {@link OntSWRL.IArg} (either {@link OntIndividual} or {@link OntSWRL.Variable}), not {@code null}
     * @return {@link OntSWRL.Atom.WithObjectProperty}
     */
    OntSWRL.Atom.WithObjectProperty createObjectPropertySWRLAtom(OntObjectProperty property, OntSWRL.IArg first, OntSWRL.IArg second);

    /**
     * Creates a Different Individuals Atom.
     * Turtle syntax:
     * <pre>{@code
     * _:x rdf:type swrl:DifferentIndividualsAtom .
     * _:x swrl:argument1 i1 .
     * _:x swrl:argument2 i2 .
     * }</pre>
     *
     * @param first  {@link OntSWRL.IArg} (either {@link OntIndividual} or {@link OntSWRL.Variable}), not {@code null}
     * @param second {@link OntSWRL.IArg} (either {@link OntIndividual} or {@link OntSWRL.Variable}), not {@code null}
     * @return {@link OntSWRL.Atom.WithDifferentIndividuals}
     */
    OntSWRL.Atom.WithDifferentIndividuals createDifferentIndividualsSWRLAtom(OntSWRL.IArg first, OntSWRL.IArg second);

    /**
     * Creates a Same Individuals Atom.
     * Turtle syntax:
     * <pre>{@code
     * _:x rdf:type swrl:SameIndividualAtom .
     * _:x swrl:argument1 i1 .
     * _:x swrl:argument2 i2 .
     * }</pre>
     *
     * @param first  {@link OntSWRL.IArg} (either {@link OntIndividual} or {@link OntSWRL.Variable}), not {@code null}
     * @param second {@link OntSWRL.IArg} (either {@link OntIndividual} or {@link OntSWRL.Variable}), not {@code null}
     * @return {@link OntSWRL.Atom.WithSameIndividuals}
     */
    OntSWRL.Atom.WithSameIndividuals createSameIndividualsSWRLAtom(OntSWRL.IArg first, OntSWRL.IArg second);

    /**
     * Creates a SWRL Rule.
     * A rule consists of a head and a body. Both the head and the body consist of a conjunction of Atoms.
     * In RDF, instead of a regular []-list, a typed version of []-lis is used,
     * where {@code rdf:type} is {@link SWRL#AtomList swrl:AtomList}.
     * Turtle syntax:
     * <pre>{@code
     * _:x rdf:type swrl:Impl .
     * _:x swrl:body (swrl:AtomList: A1 ... An ) .
     * _:x swrl:head (swrl:AtomList: A1 ... Am ) .
     * }</pre>
     *
     * @param head {@code Collection} of {@link OntSWRL.Atom}s
     * @param body {@code Collection} of {@link OntSWRL.Atom}s
     * @return {@link OntSWRL.Imp}
     */
    OntSWRL.Imp createSWRLImp(Collection<OntSWRL.Atom<?>> head,
                              Collection<OntSWRL.Atom<?>> body);
}
