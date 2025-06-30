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

package org.apache.jena.ontapi.common;

import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An {@link OntPersonality} builder.
 * This must be the only place to create various {@code OntPersonality} objects.
 */
@SuppressWarnings("WeakerAccess")
public class OntObjectPersonalityBuilder {
    private final Map<Class<? extends OntObject>, Function<OntConfig, EnhNodeFactory>> extFactories = new HashMap<>();
    private final Map<Class<? extends RDFNode>, Implementation> stdFactories = new HashMap<>();

    private String name;
    private OntPersonality.Punnings punnings;
    private OntPersonality.Builtins builtins;
    private OntPersonality.Reserved reserved;
    private OntConfig config;

    /**
     * Makes a full copy of the given {@link OntPersonality}
     * in the form of modifiable {@link OntObjectPersonalityBuilder builder}.
     *
     * @param from {@link OntPersonality} to copy settings, not {@code null}
     * @return {@link OntObjectPersonalityBuilder}
     */
    public static OntObjectPersonalityBuilder from(OntPersonality from) {
        return new OntObjectPersonalityBuilder()
                .addPersonality(OntPersonality.asJenaPersonality(from))
                .setPunnings(from.getPunnings())
                .setBuiltins(from.getBuiltins())
                .setReserved(from.getReserved())
                .setConfig(from.getConfig());
    }

    private static <X> X require(X obj, Class<X> type) {
        if (obj == null) {
            throw new IllegalStateException("The " + type.getSimpleName() + " Vocabulary must be present in builder.");
        }
        return obj;
    }

    @SuppressWarnings("rawtypes")
    private static <V extends ResourceVocabulary> V hasSpec(V voc, Class... types) {
        Objects.requireNonNull(voc);
        @SuppressWarnings("unchecked") Set<?> errors = Arrays.stream(types).filter(x -> !voc.supports(x)).collect(Collectors.toSet());
        if (errors.isEmpty()) return voc;
        throw new IllegalArgumentException("The vocabulary " + voc + " has missed required types: " + errors);
    }

    /**
     * Makes a full copy of this builder.
     *
     * @return {@link OntObjectPersonalityBuilder}, a copy
     */
    public OntObjectPersonalityBuilder copy() {
        OntObjectPersonalityBuilder res = new OntObjectPersonalityBuilder();
        res.stdFactories.putAll(this.stdFactories);
        res.extFactories.putAll(this.extFactories);
        res.setName(name);
        if (punnings != null) res.setPunnings(punnings);
        if (builtins != null) res.setBuiltins(builtins);
        if (reserved != null) res.setReserved(reserved);
        if (config != null) res.setConfig(config);
        return res;
    }

    /**
     * Adds all factories from the specified builder.
     * @param other {@link OntObjectPersonalityBuilder}
     * @return this builder
     */
    public OntObjectPersonalityBuilder add(OntObjectPersonalityBuilder other) {
        extFactories.putAll(other.extFactories);
        stdFactories.putAll(other.stdFactories);
        return this;
    }

    /**
     * Associates the specified {@link EnhNodeFactory factory} with the specified {@link OntObject object} type.
     * If the builder previously contained a mapping for the object type,
     * the old factory is replaced by the specified factory.
     * <p>
     * Note: the {@link EnhNodeFactory factory} must not explicitly refer to another factory,
     * instead it may contain implicit references through
     * {@link OntEnhGraph#asPersonalityModel(EnhGraph)} method.
     * For example, if you need a check, that some {@link Node node} is an OWL-Class inside your factory,
     * you can use {@link OntEnhGraph#canAs(Class, Node, EnhGraph)}
     * with the type {@link OntClass.Named}.
     *
     * @param type    {@code Class}-type of the concrete {@link OntObject}.
     * @param factory {@link EnhNodeFactory} the factory to produce the instances of the {@code type}
     * @return this builder
     */
    public OntObjectPersonalityBuilder add(Class<? extends OntObject> type, EnhNodeFactory factory) {
        return add(type, config -> factory);
    }

    /**
     * Associates the specified {@link EnhNodeFactory factory} producer with the specified {@link OntObject object} type.
     * If the builder previously contained a mapping for the object type,
     * the old factory is replaced by the specified factory.
     * <p>
     * Note: the {@link EnhNodeFactory factory} must not explicitly refer to another factory,
     * instead it may contain implicit references through
     * {@link OntEnhGraph#asPersonalityModel(EnhGraph)} method.
     * For example, if you need a check, that some {@link Node node} is an OWL-Class inside your factory,
     * you can use {@link OntEnhGraph#canAs(Class, Node, EnhGraph)}
     * with the type {@link OntClass.Named}.
     *
     * @param type    {@code Class}-type of the concrete {@link OntObject}.
     * @param factory {@code Function}, providing {@link EnhNodeFactory} by the {@link OntConfig}
     * @return this builder
     */
    public OntObjectPersonalityBuilder add(Class<? extends OntObject> type, Function<OntConfig, EnhNodeFactory> factory) {
        extFactories.put(type, factory);
        return this;
    }

    /**
     * Removes object factory.
     *
     * @param type {@code Class}-type of {@link OntObject}
     * @return this builder
     */
    public OntObjectPersonalityBuilder remove(Class<? extends OntObject> type) {
        extFactories.remove(type);
        stdFactories.remove(type);
        return this;
    }

    /**
     * Adds everything from the specified {@link Personality Jena Personality} to the existing internal collection.
     *
     * @param from {@link Personality} with generic type {@link RDFNode}, not {@code null}
     * @return this builder
     * @see Personality#add(Personality)
     */
    public OntObjectPersonalityBuilder addPersonality(Personality<RDFNode> from) {
        stdFactories.putAll(new JenaPersonalityAccessor(from).getMap());
        return this;
    }

    /**
     * Sets identifier of language profile ("OWL", "RDF").
     *
     * @param profileName String, can be {@code null}
     * @return this builder
     */
    public OntObjectPersonalityBuilder setName(String profileName) {
        this.name = profileName;
        return this;
    }

    /**
     * Sets a new punnings personality vocabulary.
     *
     * @param punnings {@link OntPersonality.Punnings}, not {@code null}
     * @return this builder
     */
    public OntObjectPersonalityBuilder setPunnings(OntPersonality.Punnings punnings) {
        this.punnings = hasSpec(punnings, getEntityTypes());
        return this;
    }

    /**
     * Sets a new builtins personality vocabulary.
     *
     * @param builtins {@link OntPersonality.Builtins}, not {@code null}
     * @return this builder
     */
    public OntObjectPersonalityBuilder setBuiltins(OntPersonality.Builtins builtins) {
        this.builtins = hasSpec(builtins, getEntityTypes());
        return this;
    }

    /**
     * Sets a new reserved personality vocabulary.
     *
     * @param reserved {@link OntPersonality.Reserved}, not {@code null}
     * @return this builder
     */
    public OntObjectPersonalityBuilder setReserved(OntPersonality.Reserved reserved) {
        this.reserved = hasSpec(reserved, Resource.class, Property.class);
        return this;
    }

    /**
     * Sets config, which controls OntModel behaviour.
     *
     * @param config {@link OntConfig}, not {@code null}
     * @return this builder
     */
    public OntObjectPersonalityBuilder setConfig(OntConfig config) {
        this.config = Objects.requireNonNull(config);
        return this;
    }

    /**
     * Builds a new personality configuration.
     *
     * @return {@link OntPersonality}, fresh instance
     * @throws IllegalStateException in case the builder does not contain require components
     */
    public OntPersonality build() throws IllegalStateException {
        OntConfig config = config();
        OntPersonality.Punnings punnings = punnings();
        OntPersonality.Builtins builtins = builtins();
        OntPersonality.Reserved reserved = reserved();
        OntPersonalityImpl res = new OntPersonalityImpl(name, config, punnings, builtins, reserved);
        stdFactories.forEach(res::add);
        extFactories.forEach((type, factory) -> res.register(type, factory.apply(config)));
        return res;
    }

    private Class<?>[] getEntityTypes() {
        return OntEntity.TYPES.toArray(Class[]::new);
    }

    private OntPersonality.Punnings punnings() {
        return require(punnings, OntPersonality.Punnings.class);
    }

    private OntPersonality.Builtins builtins() {
        return require(builtins, OntPersonality.Builtins.class);
    }

    private OntPersonality.Reserved reserved() {
        return require(reserved, OntPersonality.Reserved.class);
    }

    private OntConfig config() {
        return Objects.requireNonNull(config, "No config is set");
    }

    private static class JenaPersonalityAccessor extends Personality<RDFNode> {

        public JenaPersonalityAccessor(Personality<RDFNode> other) {
            super(other);
        }

        @Override
        protected Map<Class<? extends RDFNode>, Implementation> getMap() {
            return super.getMap();
        }
    }

}
