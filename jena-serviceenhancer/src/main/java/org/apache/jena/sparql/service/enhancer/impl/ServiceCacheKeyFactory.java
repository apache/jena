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

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.graph.NodeTransform;

public class ServiceCacheKeyFactory
{
    // Needed to resolve 'self' references
    protected OpServiceInfo serviceInfo;
    protected Map<Var, Var> joinVarMap;

    // Used to remap self-id
    protected NodeTransform serviceNodeRemapper;

    public ServiceCacheKeyFactory(
            OpServiceInfo serviceInfo,
            Map<Var, Var> joinVarMap,
            NodeTransform serviceNodeRemapper) {
        super();
        this.serviceInfo = serviceInfo;
        this.joinVarMap = joinVarMap;
        this.serviceNodeRemapper = serviceNodeRemapper;
    }

    public Map<Var, Var> getJoinVarMap() {
        return joinVarMap;
    }

    public ServiceCacheKey createCacheKey(Binding binding) {
        Node serviceNode = Substitute.substitute(serviceInfo.getServiceNode(), binding);

        Node effServiceNode = serviceNodeRemapper.apply(serviceNode);
        if (effServiceNode == null) {
            effServiceNode = serviceNode;
        }

        Op op = serviceInfo.getNormedQueryOp();

        BindingBuilder joinbb = BindingFactory.builder();
        binding.forEach((v, n) -> {
            Var effectiveVar = joinVarMap.get(v);
            if (effectiveVar != null) {
                joinbb.add(effectiveVar, n);
            }
        });
        Binding joinBinding = joinbb.build();


        ServiceCacheKey result = new ServiceCacheKey(effServiceNode, op, joinBinding);
        return result;
    }

    // Intersection between lhs vars and mentioned(!) rhs vars with subsequent normalization against serviceInfo
    public static Map<Var, Var> createJoinVarMapScopedToNormed(OpServiceInfo serviceInfo, Set<Var> lhsBindingVarsScoped) {
        Map<Var, Var> rhsVarsScopedToNormed = serviceInfo.getMentionedSubOpVarsScopedToNormed();
        Map<Var, Var> joinVarMap = Sets.intersection(lhsBindingVarsScoped, rhsVarsScopedToNormed.keySet()).stream()
                // .map(rhsVarsScopedToNorm::get)
                .collect(Collectors.toMap(x -> x, rhsVarsScopedToNormed::get));

        return joinVarMap;
    }

    public static ServiceCacheKeyFactory createCacheKeyFactory(
            OpServiceInfo serviceInfo,
            // boolean isLoopJoin,
            Set<Var> lhsBindingVarsScoped,
            NodeTransform serviceNodeRemapper
            ) {


        Map<Var, Var> joinVarMap = createJoinVarMapScopedToNormed(serviceInfo, lhsBindingVarsScoped);


        return new ServiceCacheKeyFactory(serviceInfo, joinVarMap, serviceNodeRemapper);
    }
}
