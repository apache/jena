/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.handlers;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

/**
 * The base interface for handlers.
 *
 */
public interface Handler {
    /**
     * Set the values for variables managed by the handler implementation. This
     * method is called by the builder to set values handled by this Handler
     * implementation.
     * 
     * @param values The map of variable to node value.
     */
    public void setVars(Map<Var, Node> values);

    /**
     * Called by the build process for this handler to perform any adjustments to
     * the query before the build completes. The adjustments are made after
     * setVars() has been called.
     */
    public void build();

}
