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

package com.hp.hpl.jena.sparql.modify.request;

import java.util.Iterator;

import org.apache.jena.atlas.lib.Closeable;

import com.hp.hpl.jena.sparql.serializer.SerializerRegistry;
import com.hp.hpl.jena.sparql.serializer.UpdateSerializerFactory;
import com.hp.hpl.jena.update.Update;

/**
 * Interface for update serializers which may be registered indirectly with the
 * {@link SerializerRegistry} via a {@link UpdateSerializerFactory} thus
 * allowing the customisation of update serialization.
 * 
 */
public interface UpdateSerializer extends Closeable {

    /**
     * Must be called prior to passing updates to the serializer
     */
    public abstract void open();

    /**
     * Serializes the given update
     * 
     * @param update
     *            Update
     */
    public abstract void update(Update update);

    /**
     * Serializes a sequence of updates
     * 
     * @param updates
     *            Updates
     */
    public abstract void update(Iterable<? extends Update> updates);

    /**
     * Serializes a sequence of updates
     * 
     * @param updateIter
     *            Updates
     */
    public abstract void update(Iterator<? extends Update> updateIter);

}