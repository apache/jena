/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.function;

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.util.MappedLoader;

/** Load ARQ functions ().
 *
 *  Any additional functions from afn: that need special loading.
 *
 * They can be invoked by afn:name without registration because
 * the {@link MappedLoader} maps the names to a "java:" loadable name.
 *
 * @see ARQConstants#ARQFunctionLibraryURI
 * @see ARQConstants#ARQFunctionLibrary
 */
public class ARQFunctions {
    public static void load(FunctionRegistry reg) {}
}
