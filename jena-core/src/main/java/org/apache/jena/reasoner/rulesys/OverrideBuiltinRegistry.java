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

package org.apache.jena.reasoner.rulesys;

import java.util.HashMap;
import java.util.Map;

public class OverrideBuiltinRegistry extends BuiltinRegistry {

    /** Mapping from functor name to Builtin implementing it */
    final protected Map<String,Builtin> builtins = new HashMap<>();

    /** Mapping from URI of builtin to implementation */
    final protected Map<String,Builtin> builtinsByURI = new HashMap<>();

    final protected BuiltinRegistry innerRegistry;

    public OverrideBuiltinRegistry(BuiltinRegistry innerRegistry) {
        this.innerRegistry = innerRegistry;
    }

    @Override
    public void register(String functor, Builtin impl) {
        builtins.put(functor, impl);
        builtinsByURI.put(impl.getURI(), impl);
    }

    @Override
    public void register(Builtin impl) {
        builtins.put(impl.getName(), impl);
        builtinsByURI.put(impl.getURI(), impl);
    }

    @Override
    public Builtin getImplementation(String functor) {
        Builtin that=builtins.get(functor);
        return that==null ? innerRegistry.getImplementation(functor) : that;
    }

    @Override
    public Builtin getImplementationByURI(String uri) {
        Builtin that=builtinsByURI.get(uri);
        return that==null ? innerRegistry.getImplementationByURI(uri) : that;
    }
}
