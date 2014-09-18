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

import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.query.QueryVisitor;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;

/**
 * Interface for query serializer factories, these may be registered with the
 * {@link SerializerRegistry} thus allowing the serialization of queries to be
 * customised
 * 
 */
public interface QuerySerializerFactory {

    /**
     * Return true if this factory can create a serializer for the given syntax
     */
    public boolean accept(Syntax syntax);

    /**
     * Return a serializer for the given syntax
     */
    public QueryVisitor create(Syntax syntax, Prologue prologue, IndentedWriter writer);
}
