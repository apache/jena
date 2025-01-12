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

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelCon;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.List;

/**
 * A technical interface that describes model modify operations.
 * Contains overridden methods inherited from {@link Model} and {@link ModelCon}:
 *
 * @param <R> - a subtype of {@link Model}, the type to return
 */
interface MutableModel<R extends Model> extends Model {

    @Override
    R add(Statement s);

    @Override
    R add(Resource s, Property p, RDFNode o);

    @Override
    R add(Model m);

    @Override
    R add(StmtIterator it);

    @Override
    R add(Statement[] statements);

    @Override
    R add(List<Statement> statements);

    @Override
    R remove(Statement s);

    @Override
    R remove(Resource s, Property p, RDFNode o);

    @Override
    R remove(Model m);

    @Override
    R remove(StmtIterator it);

    @Override
    R removeAll(Resource s, Property p, RDFNode o);

    @Override
    R remove(Statement[] statements);

    @Override
    R remove(List<Statement> statements);

    @Override
    R removeAll();

    @Override
    R addLiteral(Resource s, Property p, boolean v);

    @Override
    R addLiteral(Resource s, Property p, long v);

    @Override
    R addLiteral(Resource s, Property p, int v);

    @Override
    R addLiteral(Resource s, Property p, char v);

    @Override
    R addLiteral(Resource s, Property p, float v);

    @Override
    R addLiteral(Resource s, Property p, double v);

    @Override
    R addLiteral(Resource s, Property p, Literal o);

    @Override
    R add(Resource s, Property p, String lex);

    @Override
    R add(Resource s, Property p, String lex, RDFDatatype datatype);

    @Override
    R add(Resource s, Property p, String lex, String lang);

    /**
     * Registers a listener for model-changed events on this model.
     * The methods on the listener will be called when API add/remove calls on the model succeed
     * [in whole or in part].
     * The same listener may be registered many times;
     * if so, its methods will be called as many times as it's registered for each event.
     *
     * @param listener {@link ModelChangedListener}, not null
     * @return this model, for cascading
     */
    @Override
    R register(ModelChangedListener listener);

    /**
     * Unregisters a listener from model-changed events on this model.
     * The listener is detached from the model.
     * The model is returned to permit cascading.
     * If the listener is not attached to the model, then nothing happens.
     *
     * @param listener {@link ModelChangedListener}, not null
     */
    @Override
    R unregister(ModelChangedListener listener);

    /**
     * Notifies any listeners that the {@code event} has occurred.
     *
     * @param event the event, which has occurred, e.g. {@code GraphEvents#startRead}
     * @return this model, for cascading
     * @see ModelChangedListener
     * @see org.apache.jena.graph.GraphEvents
     */
    @Override
    R notifyEvent(Object event);
}
