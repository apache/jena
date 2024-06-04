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

import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A default implementation of {@link OntPersonality}.
 * Mappings from [interface] Class objects of RDFNode to {@link Implementation} factories.
 */
@SuppressWarnings("WeakerAccess")
public class OntPersonalityImpl extends Personality<RDFNode> implements OntPersonality {
    private final String name;
    private final OntConfig config;
    private final Punnings punnings;
    private final Builtins builtins;
    private final Reserved reserved;
    private final Map<Class<? extends OntObject>, Set<String>> forbidden;

    public OntPersonalityImpl(String name,
                              OntConfig config,
                              Punnings punnings,
                              Builtins builtins,
                              Reserved reserved) {
        this.name = name;
        this.config = Objects.requireNonNull(config, "Null config");
        this.builtins = Objects.requireNonNull(builtins, "Null builtins vocabulary");
        this.punnings = Objects.requireNonNull(punnings, "Null punnings vocabulary");
        this.reserved = Objects.requireNonNull(reserved, "Null reserved vocabulary");
        this.forbidden = collectForbiddenResources(reserved, builtins);
    }

    private static Map<Class<? extends OntObject>, Set<String>> collectForbiddenResources(Reserved reserved, Builtins builtins) {
        Map<Class<? extends OntObject>, Set<String>> forbidden = new HashMap<>();
        Set<Node> reservedResources = reserved.getAllResources();
        builtins.supportedTypes().forEach(type -> {
            Set<Node> allowedResources = builtins.get(type);
            Set<String> forbiddenResources = reservedResources.stream()
                    .filter(it -> it.isURI() && !allowedResources.contains(it))
                    .map(Node::getURI)
                    .collect(Collectors.toUnmodifiableSet());
            forbidden.put(type, forbiddenResources);
        });
        return forbidden;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Builtins getBuiltins() {
        return builtins;
    }

    @Override
    public Punnings getPunnings() {
        return punnings;
    }

    @Override
    public Reserved getReserved() {
        return reserved;
    }

    @Override
    public OntConfig getConfig() {
        return config;
    }

    @Override
    public Set<String> forbidden(Class<? extends OntObject> type) {
        return forbidden.getOrDefault(type, Set.of());
    }

    /**
     * Registers new OntObject if needed
     *
     * @param type    Interface (OntObject)
     * @param factory Factory to crete object
     */
    public void register(Class<? extends OntObject> type, EnhNodeFactory factory) {
        register(type, EnhNodeFactory.asJenaImplementation(factory));
    }

    public void register(Class<? extends RDFNode> type, Implementation factory) {
        super.add(Objects.requireNonNull(type, "Null type."), factory);
    }

    /**
     * Removes the factory.
     *
     * @param view Interface (OntObject)
     */
    public void unregister(Class<? extends OntObject> view) {
        getMap().remove(view);
    }

    @Override
    public Stream<Class<? extends RDFNode>> types() {
        return getMap().keySet().stream();
    }

    /**
     * Gets factory for {@link OntObject}.
     *
     * @param type Interface (OntObject type)
     * @return {@link EnhNodeFactory} factory
     */
    @Override
    public EnhNodeFactory getObjectFactory(Class<? extends RDFNode> type) {
        Implementation implementation = getImplementation(type);
        OntJenaException.checkSupported(implementation != null,
                "Profile " + name + " does not support language construct " + OntEnhNodeFactories.viewAsString(type)
        );
        if (implementation instanceof EnhNodeFactory) {
            return (EnhNodeFactory) implementation;
        }
        return OntJenaException.TODO("Not implemented yet");
    }

    @Override
    public boolean supports(Class<? extends RDFNode> type) {
        return getMap().containsKey(type);
    }

    @Override
    public OntPersonalityImpl add(Personality<RDFNode> other) {
        super.add(other);
        return this;
    }

    @Override
    public OntPersonalityImpl copy() {
        OntPersonalityImpl res = new OntPersonalityImpl(getName(), getConfig(), getPunnings(), getBuiltins(), getReserved());
        res.add(this);
        return res;
    }

}
