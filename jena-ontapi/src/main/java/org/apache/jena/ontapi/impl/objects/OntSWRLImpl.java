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

package org.apache.jena.ontapi.impl.objects;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.UnsupportedPolymorphismException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntSWRL;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.SWRL;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Ont SWRL Object Implementation.
 */
@SuppressWarnings("WeakerAccess")
public class OntSWRLImpl extends OntObjectImpl implements OntSWRL {

    public OntSWRLImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static Builtin fetchBuiltinEntity(OntGraphModelImpl model, String uri) {
        Builtin res = model.findNodeAs(NodeFactory.createURI(OntJenaException.notNull(uri, "Null uri.")), Builtin.class);
        if (res == null) {
            res = createBuiltinEntity(model, uri);
        }
        return res;
    }

    public static Builtin createBuiltinEntity(OntGraphModelImpl model, String uri) {
        return model.createOntObject(Builtin.class, Objects.requireNonNull(uri));
    }

    public static Variable createVariable(OntGraphModelImpl model, String uri) {
        return model.createOntObject(Variable.class, uri);
    }

    public static Atom.WithBuiltin createBuiltInAtom(OntGraphModelImpl model,
                                                     Resource predicate,
                                                     Collection<DArg> arguments) {
        Builtin property = fetchBuiltinEntity(model, predicate.getURI());
        OntObject res = model.createResource(SWRL.BuiltinAtom).addProperty(SWRL.builtin, property).as(OntObject.class);
        model.createOntList(res, SWRL.arguments, null, DArg.class, Iterators.create(arguments));
        return model.getNodeAs(res.asNode(), Atom.WithBuiltin.class);
    }

    public static Atom.WithClass createClassAtom(OntGraphModelImpl model, OntClass clazz, IArg arg) {
        OntJenaException.notNull(clazz, "Null class");
        OntJenaException.notNull(arg, "Null i-arg");
        Resource res = model.createResource(SWRL.ClassAtom)
                .addProperty(SWRL.classPredicate, clazz)
                .addProperty(SWRL.argument1, arg);
        return model.getNodeAs(res.asNode(), Atom.WithClass.class);
    }

    public static Atom.WithDataRange createDataRangeAtom(OntGraphModelImpl model, OntDataRange range, DArg arg) {
        OntJenaException.notNull(range, "Null data range");
        OntJenaException.notNull(arg, "Null d-arg");
        Resource res = model.createResource(SWRL.DataRangeAtom)
                .addProperty(SWRL.dataRange, range)
                .addProperty(SWRL.argument1, arg);
        return model.getNodeAs(res.asNode(), Atom.WithDataRange.class);
    }

    public static Atom.WithDataProperty createDataPropertyAtom(OntGraphModelImpl model,
                                                               OntDataProperty dataProperty,
                                                               IArg firstArg,
                                                               DArg secondArg) {
        OntJenaException.notNull(dataProperty, "Null data property");
        OntJenaException.notNull(firstArg, "Null first i-arg");
        OntJenaException.notNull(secondArg, "Null second d-arg");
        Resource res = model.createResource(SWRL.DatavaluedPropertyAtom)
                .addProperty(SWRL.propertyPredicate, dataProperty)
                .addProperty(SWRL.argument1, firstArg)
                .addProperty(SWRL.argument2, secondArg);
        return model.getNodeAs(res.asNode(), Atom.WithDataProperty.class);
    }

    public static Atom.WithObjectProperty createObjectPropertyAtom(OntGraphModelImpl model,
                                                                   OntObjectProperty objectProperty,
                                                                   IArg firstArg,
                                                                   IArg secondArg) {
        OntJenaException.notNull(objectProperty, "Null object property");
        OntJenaException.notNull(firstArg, "Null first i-arg");
        OntJenaException.notNull(secondArg, "Null second i-arg");
        Resource res = model.createResource(SWRL.IndividualPropertyAtom)
                .addProperty(SWRL.propertyPredicate, objectProperty)
                .addProperty(SWRL.argument1, firstArg)
                .addProperty(SWRL.argument2, secondArg);
        return model.getNodeAs(res.asNode(), Atom.WithObjectProperty.class);
    }

    public static Atom.WithDifferentIndividuals createDifferentIndividualsAtom(OntGraphModelImpl model,
                                                                               IArg firstArg,
                                                                               IArg secondArg) {
        OntJenaException.notNull(firstArg, "Null first i-arg");
        OntJenaException.notNull(secondArg, "Null second i-arg");
        Resource res = model.createResource(SWRL.DifferentIndividualsAtom)
                .addProperty(SWRL.argument1, firstArg)
                .addProperty(SWRL.argument2, secondArg);
        model.fetchOntEntity(OntObjectProperty.Named.class, OWL2.differentFrom.getURI());
        return model.getNodeAs(res.asNode(), Atom.WithDifferentIndividuals.class);
    }

    public static Atom.WithSameIndividuals createSameIndividualsAtom(OntGraphModelImpl model,
                                                                     IArg firstArg,
                                                                     IArg secondArg) {
        OntJenaException.notNull(firstArg, "Null first i-arg");
        OntJenaException.notNull(secondArg, "Null second i-arg");
        Resource res = model.createResource(SWRL.SameIndividualAtom)
                .addProperty(SWRL.argument1, firstArg)
                .addProperty(SWRL.argument2, secondArg);
        model.fetchOntEntity(OntObjectProperty.Named.class, OWL2.sameAs.getURI());
        return model.getNodeAs(res.asNode(), Atom.WithSameIndividuals.class);
    }

    public static Imp createImp(OntGraphModelImpl model,
                                Collection<Atom<? extends OntObject>> head,
                                Collection<Atom<? extends OntObject>> body) {
        OntJenaException.notNull(head, "Null head");
        OntJenaException.notNull(body, "Null body");
        OntObject res = model.createResource(SWRL.Imp).as(OntObject.class);
        model.createOntList(res, SWRL.head, SWRL.AtomList, Atom.class, Iterators.create(head));
        model.createOntList(res, SWRL.body, SWRL.AtomList, Atom.class, Iterators.create(body));
        return model.getNodeAs(res.asNode(), Imp.class);
    }

    public static class BuiltinImpl extends OntSWRLImpl implements Builtin {
        public BuiltinImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return Builtin.class;
        }
    }

    public static class VariableImpl extends OntSWRLImpl implements Variable {
        public VariableImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return Variable.class;
        }
    }

    public static class DArgImpl extends OntObjectImpl implements DArg {
        public DArgImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return DArg.class;
        }

        /**
         * Answers the encapsulated node as {@link Literal}.
         *
         * @return {@link Literal}
         * @throws UnsupportedPolymorphismException if not a literal
         */
        @Override
        public Literal asLiteral() throws UnsupportedPolymorphismException {
            return as(Literal.class);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return node.isLiteral() ? NullIterator.instance() : ((VariableImpl) as(Variable.class)).listSpec();
        }
    }

    public static class IArgImpl extends OntObjectImpl implements IArg {
        public IArgImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return IArg.class;
        }
    }

    public static abstract class AtomImpl<P extends OntObject> extends OntSWRLImpl implements Atom<P> {
        public AtomImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return getRequiredRootStatement(this, getResourceType());
        }

        public abstract Resource getResourceType();
    }

    public static class BuiltInAtomImpl extends AtomImpl<Builtin> implements Atom.WithBuiltin {
        public BuiltInAtomImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Resource getResourceType() {
            return SWRL.BuiltinAtom;
        }

        @Override
        public Builtin getPredicate() {
            return getRequiredObject(SWRL.builtin, Builtin.class);
        }

        @Override
        public OntListImpl<DArg> getArgList() {
            return getModel().asOntList(getRequiredObject(SWRL.arguments, RDFList.class),
                    this, SWRL.arguments, DArg.class);
        }

        public ExtendedIterator<OntStatement> listPredicateStatements() {
            OntStatement p = getRequiredProperty(SWRL.builtin);
            OntStatement b = getPredicate().getMainStatement();
            return b == null ? Iterators.of(p) : Iterators.of(p, b);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), listPredicateStatements(), getArgList().listContent());
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return WithBuiltin.class;
        }
    }

    public static abstract class UnaryImpl<O extends OntObject, A extends Arg> extends AtomImpl<O> implements Atom.Unary<O, A> {
        private final Property predicate;
        private final Class<O> objectView;
        private final Class<A> argView;

        UnaryImpl(Node n, EnhGraph m, Property predicate, Class<O> objectView, Class<A> argView) {
            super(n, m);
            this.predicate = predicate;
            this.objectView = objectView;
            this.argView = argView;
        }

        @Override
        public A getArg() {
            return getRequiredObject(SWRL.argument1, argView);
        }

        @Override
        public O getPredicate() {
            return getRequiredObject(predicate, objectView);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), listRequired(predicate, SWRL.argument1));
        }
    }

    public static class OntClassAtomImpl extends UnaryImpl<OntClass, IArg> implements Atom.WithClass {
        public OntClassAtomImpl(Node n, EnhGraph m) {
            super(n, m, SWRL.classPredicate, OntClass.class, IArg.class);
        }

        @Override
        public Resource getResourceType() {
            return SWRL.ClassAtom;
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return WithClass.class;
        }
    }

    public static class DataRangeAtomImpl extends UnaryImpl<OntDataRange, DArg> implements Atom.WithDataRange {
        public DataRangeAtomImpl(Node n, EnhGraph m) {
            super(n, m, SWRL.dataRange, OntDataRange.class, DArg.class);
        }

        @Override
        public Resource getResourceType() {
            return SWRL.DataRangeAtom;
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return WithDataRange.class;
        }
    }

    public static abstract class BinaryImpl<O extends OntObject, F extends Arg, S extends Arg> extends AtomImpl<O> implements Atom.Binary<O, F, S> {
        protected final Property predicate;
        private final Class<O> objectType;
        private final Class<F> firstArgType;
        private final Class<S> secondArgType;

        BinaryImpl(Node n,
                   EnhGraph m,
                   Property predicate,
                   Class<O> objectType,
                   Class<F> firstArgType,
                   Class<S> secondArgType) {
            super(n, m);
            this.predicate = predicate;
            this.objectType = objectType;
            this.firstArgType = firstArgType;
            this.secondArgType = secondArgType;
        }

        @Override
        public O getPredicate() {
            return getRequiredObject(predicate, objectType);
        }

        @Override
        public F getFirstArg() {
            return getRequiredObject(SWRL.argument1, firstArgType);
        }

        @Override
        public S getSecondArg() {
            return getRequiredObject(SWRL.argument2, secondArgType);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), listPredicateSpec(), listRequired(SWRL.argument1, SWRL.argument2));
        }

        protected ExtendedIterator<OntStatement> listPredicateSpec() {
            return listRequired(predicate);
        }
    }

    public static abstract class PropertyAtomImpl<P extends OntProperty, A extends Arg> extends BinaryImpl<P, IArg, A> {

        PropertyAtomImpl(Node n, EnhGraph m, Class<P> objectType, Class<A> secondArgType) {
            super(n, m, SWRL.propertyPredicate, objectType, IArg.class, secondArgType);
        }
    }

    public static class DataPropertyAtomImpl extends PropertyAtomImpl<OntDataProperty, DArg> implements Atom.WithDataProperty {
        public DataPropertyAtomImpl(Node n, EnhGraph m) {
            super(n, m, OntDataProperty.class, DArg.class);
        }

        @Override
        public Resource getResourceType() {
            return SWRL.DatavaluedPropertyAtom;
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return WithDataProperty.class;
        }
    }

    public static class ObjectPropertyAtomImpl extends PropertyAtomImpl<OntObjectProperty, IArg> implements Atom.WithObjectProperty {
        public ObjectPropertyAtomImpl(Node n, EnhGraph m) {
            super(n, m, OntObjectProperty.class, IArg.class);
        }

        @Override
        public Resource getResourceType() {
            return SWRL.IndividualPropertyAtom;
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return WithObjectProperty.class;
        }
    }

    public static abstract class IndividualsAtomImpl extends BinaryImpl<OntObjectProperty.Named, IArg, IArg> {
        public IndividualsAtomImpl(Node n, EnhGraph m, Property predicate) {
            super(n, m, predicate, OntObjectProperty.Named.class, IArg.class, IArg.class);
        }

        @Override
        public OntObjectProperty.Named getPredicate() {
            return getModel().fetchOntEntity(OntObjectProperty.Named.class, predicate.getURI());
        }

        @Override
        protected ExtendedIterator<OntStatement> listPredicateSpec() {
            OntStatement s = getPredicate().getMainStatement();
            return s == null ? NullIterator.instance() : Iterators.of(s);
        }
    }

    public static class DifferentIndividualsAtomImpl extends IndividualsAtomImpl implements Atom.WithDifferentIndividuals {
        public DifferentIndividualsAtomImpl(Node n, EnhGraph m) {
            super(n, m, OWL2.differentFrom);
        }

        @Override
        public Resource getResourceType() {
            return SWRL.DifferentIndividualsAtom;
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return WithDifferentIndividuals.class;
        }
    }

    public static class SameIndividualsAtomImpl extends IndividualsAtomImpl implements Atom.WithSameIndividuals {
        public SameIndividualsAtomImpl(Node n, EnhGraph m) {
            super(n, m, OWL2.sameAs);
        }

        @Override
        public Resource getResourceType() {
            return SWRL.SameIndividualAtom;
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return WithSameIndividuals.class;
        }
    }

    @SuppressWarnings("rawtypes")
    public static class ImpImpl extends OntSWRLImpl implements Imp {

        public ImpImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntListImpl<Atom> getHeadList() {
            return getList(SWRL.head);
        }

        @Override
        public OntListImpl<Atom> getBodyList() {
            return getList(SWRL.body);
        }

        protected OntListImpl<Atom> getList(Property predicate) {
            RDFList list = getRequiredObject(predicate, RDFList.class);
            return getModel().asOntList(list, this, predicate, false, SWRL.AtomList, Atom.class);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), getHeadList().listContent(), getBodyList().listContent());
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return getRequiredRootStatement(this, SWRL.Imp);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return Imp.class;
        }
    }

}

