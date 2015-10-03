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

package org.apache.jena.sparql.serializer;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.modify.request.UpdateSerializer ;

/**
 * Interface for update serializer factories, these may be registered with the
 * {@link SerializerRegistry} thus allowing update serialization to be
 * customised
 * 
 */
public interface UpdateSerializerFactory {

    /**
     * Return true if this factory can create a serializer for the given syntax
     */
    public boolean accept(Syntax syntax);

    /**
     * Return a serializer for the given syntax
     */
    public UpdateSerializer create(Syntax syntax, Prologue prologue, IndentedWriter writer);
}
