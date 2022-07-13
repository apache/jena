package org.apache.jena.sparql.util;
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

import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;

/**
 * Utils to work with {@link Context}.
 */
public class ContextUtils {

    /**
     * Copies the given context also copying its registries
     * ({@link FunctionRegistry}, {@link PropertyFunctionRegistry} and {@link ServiceExecutorRegistry}).
     * If the input context is null, then method just creates a new empty instance.
     *
     * @param from {@link Context} or {@code null}
     * @return a new {@link Context} instance
     */
    public static Context copyWithRegistries(Context from) {
        FunctionRegistry fr = FunctionRegistry.createFrom(FunctionRegistry.get(from));
        PropertyFunctionRegistry pfr = PropertyFunctionRegistry.createFrom(PropertyFunctionRegistry.get(from));
        ServiceExecutorRegistry ser = ServiceExecutorRegistry.createFrom(ServiceExecutorRegistry.get(from));
        Context res = from == null ? new Context() : from.copy();
        FunctionRegistry.set(res, fr);
        PropertyFunctionRegistry.set(res, pfr);
        ServiceExecutorRegistry.set(res, ser);
        return res;
    }
}
