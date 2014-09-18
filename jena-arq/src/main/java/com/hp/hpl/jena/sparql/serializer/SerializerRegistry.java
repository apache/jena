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

package com.hp.hpl.jena.sparql.serializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.system.IRIResolver;

import com.hp.hpl.jena.query.QueryVisitor;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.modify.request.UpdateSerializer;
import com.hp.hpl.jena.sparql.modify.request.UpdateWriter;
import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode;

/**
 * Provides a registry of serializers for queries and updates
 * <p>
 * By registering custom {@link QuerySerializerFactory} or
 * {@link UpdateSerializerFactory} instances the mapping of a syntax to a
 * serialization can be customised and this allows the serialization of queries
 * and updates to be customised if desired.
 * </p>
 * <p>
 * This feature is primarily intended for system programmers as changing how
 * queries and updates are serialized could have knock on effects particularly
 * if you use ARQ to interact with remote systems. The default registered
 * serializers produce standards compliant SPARQL syntax and should be more than
 * sufficient in most cases
 * </p>
 * 
 */
public class SerializerRegistry {

    private Map<Syntax, QuerySerializerFactory> querySerializers = new HashMap<>();
    private Map<Syntax, UpdateSerializerFactory> updateSerializers = new HashMap<>();

    private SerializerRegistry() {
    }

    private static SerializerRegistry registry;

    private static synchronized void init() {
        SerializerRegistry reg = new SerializerRegistry();

        // Register standard serializers
        QuerySerializerFactory arqQuerySerializerFactory = new QuerySerializerFactory() {

            @Override
            public QueryVisitor create(Syntax syntax, Prologue prologue, IndentedWriter writer) {
                // For the query pattern
                SerializationContext cxt1 = new SerializationContext(prologue, new NodeToLabelMapBNode("b", false));
                // For the construct pattern
                SerializationContext cxt2 = new SerializationContext(prologue, new NodeToLabelMapBNode("c", false));

                return new QuerySerializer(writer, new FormatterElement(writer, cxt1), new FmtExprSPARQL(writer, cxt1),
                        new FmtTemplate(writer, cxt2));
            }

            @Override
            public boolean accept(Syntax syntax) {
                // Since ARQ syntax is a super set of SPARQL 1.1 both SPARQL 1.0
                // and SPARQL 1.1 can be serialized by the same serializer
                return Syntax.syntaxARQ.equals(syntax) || Syntax.syntaxSPARQL_10.equals(syntax)
                        || Syntax.syntaxSPARQL_11.equals(syntax);
            }
        };
        reg.addQuerySerializer(Syntax.syntaxARQ, arqQuerySerializerFactory);
        reg.addQuerySerializer(Syntax.syntaxSPARQL_10, arqQuerySerializerFactory);
        reg.addQuerySerializer(Syntax.syntaxSPARQL_11, arqQuerySerializerFactory);

        UpdateSerializerFactory arqUpdateSerializerFactory = new UpdateSerializerFactory() {

            @Override
            public UpdateSerializer create(Syntax syntax, Prologue prologue, IndentedWriter writer) {
                if (!prologue.explicitlySetBaseURI())
                    prologue = new Prologue(prologue.getPrefixMapping(), (IRIResolver) null);

                SerializationContext context = new SerializationContext(prologue);
                return new UpdateWriter(writer, context);
            }

            @Override
            public boolean accept(Syntax syntax) {
                // Since ARQ syntax is a super set of SPARQL 1.1 both SPARQL 1.0
                // and SPARQL 1.1 can be serialized by the same serializer
                return Syntax.syntaxARQ.equals(syntax) || Syntax.syntaxSPARQL_10.equals(syntax)
                        || Syntax.syntaxSPARQL_11.equals(syntax);
            }
        };
        reg.addUpdateSerializer(Syntax.syntaxARQ, arqUpdateSerializerFactory);
        reg.addUpdateSerializer(Syntax.syntaxSPARQL_10, arqUpdateSerializerFactory);
        reg.addUpdateSerializer(Syntax.syntaxSPARQL_11, arqUpdateSerializerFactory);

        registry = reg;
    }

    /**
     * Gets the serializer registry which is a singleton lazily instantiating it
     * if this is the first time this method has been called
     * 
     * @return Registry
     */
    public static SerializerRegistry get() {
        if (registry == null)
            init();

        return registry;
    }

    /**
     * Adds a query serializer factory for the given syntax
     * 
     * @param syntax
     *            Syntax
     * @param factory
     *            Serializer factory
     * @throws IllegalArgumentException
     *             Thrown if the given factory does not accept the given syntax
     */
    public void addQuerySerializer(Syntax syntax, QuerySerializerFactory factory) {
        if (!factory.accept(syntax))
            throw new IllegalArgumentException("Factory does not accept the specified syntax");
        querySerializers.put(syntax, factory);
    }

    /**
     * Adds an update serializer factory for the given syntax
     * 
     * @param syntax
     *            Syntax
     * @param factory
     *            Serializer factory
     * @throws IllegalArgumentException
     *             Thrown if the given factory does not accept the given syntax
     */
    public void addUpdateSerializer(Syntax syntax, UpdateSerializerFactory factory) {
        if (!factory.accept(syntax))
            throw new IllegalArgumentException("Factory does not accept the specified syntax");
        updateSerializers.put(syntax, factory);
    }

    /**
     * Gets whether a query serializer factory is registered for the given
     * syntax
     * 
     * @param syntax
     *            Syntax
     * @return True if registered, false otherwise
     */
    public boolean containsQuerySerializer(Syntax syntax) {
        return querySerializers.containsKey(syntax) && querySerializers.get(syntax) != null;
    }

    /**
     * Gets whether an update serializer factory is registered for the given
     * syntax
     * 
     * @param syntax
     *            Syntax
     * @return True if registered, false otherwise
     */
    public boolean containsUpdateSerializer(Syntax syntax) {
        return updateSerializers.containsKey(syntax) && updateSerializers.get(syntax) != null;
    }

    /**
     * Gets the query serializer factory for the given syntax which may be null
     * if there is none registered
     * 
     * @param syntax
     *            Syntax
     * @return Query Serializer Factory or null if none registered for the given
     *         syntax
     */
    public QuerySerializerFactory getQuerySerializerFactory(Syntax syntax) {
        return querySerializers.get(syntax);
    }

    /**
     * Gets the update serializer factory for the given syntax which may be null
     * if there is none registered
     * 
     * @param syntax
     *            Syntax
     * @return Update Serializer Factory or null if none registered for the
     *         given syntax
     */
    public UpdateSerializerFactory getUpdateSerializerFactory(Syntax syntax) {
        return updateSerializers.get(syntax);
    }

    /**
     * Removes the query serializer factory for the given syntax
     * 
     * @param syntax
     *            Syntax
     */
    public void removeQuerySerializer(Syntax syntax) {
        querySerializers.remove(syntax);
    }

    /**
     * Removes the update serializer factory for the given syntax
     * 
     * @param syntax
     *            Syntax
     */
    public void removeUpdateSerializer(Syntax syntax) {
        updateSerializers.remove(syntax);
    }
}
