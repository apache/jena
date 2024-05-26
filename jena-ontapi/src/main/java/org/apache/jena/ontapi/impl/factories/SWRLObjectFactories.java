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

package org.apache.jena.ontapi.impl.factories;

import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.EnhNodeFinder;
import org.apache.jena.ontapi.common.EnhNodeProducer;
import org.apache.jena.ontapi.common.OntEnhGraph;
import org.apache.jena.ontapi.common.OntEnhNodeFactories;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.impl.objects.OntSWRLImpl;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntSWRL;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.vocabulary.SWRL;

/**
 * A helper-factory to produce (SWRL) {@link EnhNodeFactory} factories;
 * for {@link OntPersonality ont-personalities}
 */
public final class SWRLObjectFactories {

    public static final EnhNodeFactory VARIABLE_SWRL = OntEnhNodeFactories.createCommon(
            new EnhNodeProducer.WithType(OntSWRLImpl.VariableImpl.class, SWRL.Variable),
            new EnhNodeFinder.ByType(SWRL.Variable),
            OntSWRLs.VARIABLE_FILTER
    );
    public static final EnhNodeFactory DARG_SWRL = OntEnhNodeFactories.createCommon(
            OntSWRLImpl.DArgImpl.class,
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntSWRLs.VARIABLE_FILTER.or(LiteralImpl.factory::canWrap)
    );
    public static final EnhNodeFactory IARG_SWRL = OntEnhNodeFactories.createCommon(
            OntSWRLImpl.IArgImpl.class,
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntSWRLs.VARIABLE_FILTER.or((n, g) -> OntEnhGraph.canAs(OntIndividual.class, n, g))
    );
    public static final EnhNodeFactory BUILTIN_SWRL = OntEnhNodeFactories.createCommon(
            new EnhNodeProducer.WithType(OntSWRLImpl.BuiltinImpl.class, SWRL.Builtin),
            new EnhNodeFinder.ByType(SWRL.Builtin),
            OntSWRLs.BUILTIN_FILTER
    );
    public static final EnhNodeFactory BUILT_IN_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.BuiltInAtomImpl.class,
            SWRL.BuiltinAtom
    );
    public static final EnhNodeFactory CLASS_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.OntClassAtomImpl.class,
            SWRL.ClassAtom
    );
    public static final EnhNodeFactory DATA_RANGE_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.DataRangeAtomImpl.class,
            SWRL.DataRangeAtom
    );
    public static final EnhNodeFactory DATA_VALUED_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.DataPropertyAtomImpl.class,
            SWRL.DatavaluedPropertyAtom
    );
    public static final EnhNodeFactory INDIVIDUAL_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.ObjectPropertyAtomImpl.class,
            SWRL.IndividualPropertyAtom
    );
    public static final EnhNodeFactory DIFFERENT_INDIVIDUALS_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.DifferentIndividualsAtomImpl.class,
            SWRL.DifferentIndividualsAtom
    );
    public static final EnhNodeFactory SAME_INDIVIDUALS_ATOM_SWRL = OntSWRLs.makeAtomFactory(
            OntSWRLImpl.SameIndividualsAtomImpl.class,
            SWRL.SameIndividualAtom
    );
    public static final EnhNodeFactory IMPL_SWRL = new OntSWRLs.SWRLImplFactory();
    public static final EnhNodeFactory ANY_ARG_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntSWRL.DArg.class,
            OntSWRL.IArg.class
    );
    public static final EnhNodeFactory ANY_ATOM_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_TYPED,
            OntSWRL.Atom.WithBuiltin.class,
            OntSWRL.Atom.WithClass.class,
            OntSWRL.Atom.WithDataRange.class,
            OntSWRL.Atom.WithDataProperty.class,
            OntSWRL.Atom.WithObjectProperty.class,
            OntSWRL.Atom.WithDifferentIndividuals.class,
            OntSWRL.Atom.WithSameIndividuals.class
    );
    public static final EnhNodeFactory ANY_BINARY_ATOM_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_TYPED,
            OntSWRL.Atom.WithDataProperty.class,
            OntSWRL.Atom.WithObjectProperty.class,
            OntSWRL.Atom.WithDifferentIndividuals.class,
            OntSWRL.Atom.WithSameIndividuals.class
    );
    public static final EnhNodeFactory ANY_UNARY_ATOM_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_TYPED,
            OntSWRL.Atom.WithClass.class,
            OntSWRL.Atom.WithDataRange.class
    );
    public static final EnhNodeFactory ANY_OBJECT_SWRL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_TYPED,
            OntSWRL.Atom.WithBuiltin.class,
            OntSWRL.Atom.WithClass.class,
            OntSWRL.Atom.WithDataRange.class,
            OntSWRL.Atom.WithDataProperty.class,
            OntSWRL.Atom.WithObjectProperty.class,
            OntSWRL.Atom.WithDifferentIndividuals.class,
            OntSWRL.Atom.WithSameIndividuals.class,
            OntSWRL.Builtin.class,
            OntSWRL.Variable.class,
            OntSWRL.Imp.class
    );
}
