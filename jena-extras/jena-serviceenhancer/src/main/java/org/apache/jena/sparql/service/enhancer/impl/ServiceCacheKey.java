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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.binding.Binding;

public class ServiceCacheKey {

    // Note: The reason why serviceNode and Op are not combined into an OpService here
    // is because for a cache key, the serviceNode has to be concrete (i.e. substitution applied), whereas the
    // service's subOp (here 'op') has to be as given (without substitution).
    protected Node serviceNode;
    protected Op op;
    protected Binding binding;

    /**
     * Key object for service cache entries.
     *
     * @param serviceNode The node used with the SERVICE clause (typically an IRI).
     * @param op A SERVICE clause's algebra expression. Typically with noremalized variable scopes.
     * @param binding A binding holding substitutions of op's variables with concrete values.
     */
    public ServiceCacheKey(Node serviceNode, Op op, Binding binding) {
        super();
        this.serviceNode = serviceNode;
        this.op = op;
        this.binding = binding;
    }

    public Node getServiceNode() {
        return serviceNode;
    }

    public Op getOp() {
        return op;
    }

    public Binding getBinding() {
        return binding;
    }

    @Override
    public int hashCode() {
        return Objects.hash(binding, op, serviceNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceCacheKey other = (ServiceCacheKey) obj;
        return Objects.equals(binding, other.binding) && Objects.equals(op, other.op)
                && Objects.equals(serviceNode, other.serviceNode);
    }

    @Override
    public String toString() {
        return "ServiceCacheKey [serviceNode=" + serviceNode + ", op=" + op + ", binding=" + binding + "]";
    }
 }
