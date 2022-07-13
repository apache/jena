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

package org.apache.jena.sparql.util;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.bulk.ChainingServiceExecutorBulk;
import org.apache.jena.sparql.service.single.ChainingServiceExecutor;
import org.apache.jena.sys.JenaSystem;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * To test utility {@link ContextUtils} methods.
 */
public class TestContextUtils {
    @BeforeClass
    public static void beforeClass() {
        JenaSystem.init();
    }

    private static Function mockFunction() {
        return new FunctionBase1() {
            @Override
            public NodeValue exec(NodeValue v) {
                return null;
            }
        };
    }

    private static PropertyFunction mockPropertyFunction() {
        return new PFuncSimple() {
            @Override
            public QueryIterator execEvaluated(Binding b, Node s, Node p, Node o, ExecutionContext ec) {
                return null;
            }
        };
    }

    private static ChainingServiceExecutorBulk mockChainingServiceExecutorBulk() {
        return (opService, input, execCxt, chain) -> null;
    }

    private static ChainingServiceExecutor mockChainingServiceExecutor() {
        return (opExecute, opOriginal, binding, execCxt, chain) -> null;
    }

    @Test
    public void testCopyWithRegistries() {
        Context givenContext = new Context();
        FunctionFactory givenFunctionFactory = uri -> mockFunction();
        PropertyFunctionFactory givenPFunctionFactory = uri -> mockPropertyFunction();
        ChainingServiceExecutorBulk givenServiceExecutionFactory = mockChainingServiceExecutorBulk();
        ChainingServiceExecutor givenChainingServiceExecutor = mockChainingServiceExecutor();

        PropertyFunctionRegistry givenPFunctionRegistry = new PropertyFunctionRegistry();
        FunctionRegistry givenFunctionRegistry = new FunctionRegistry();
        ServiceExecutorRegistry givenServiceExecutorRegistry = new ServiceExecutorRegistry();
        givenFunctionRegistry.put("x", givenFunctionFactory);
        givenPFunctionRegistry.put("y", givenPFunctionFactory);
        givenServiceExecutorRegistry.addBulkLink(givenServiceExecutionFactory);
        givenServiceExecutorRegistry.addSingleLink(givenChainingServiceExecutor);

        givenContext.put(ARQConstants.registryFunctions, givenFunctionRegistry);
        givenContext.put(ARQConstants.registryPropertyFunctions, givenPFunctionRegistry);
        givenContext.put(ARQConstants.registryServiceExecutors, givenServiceExecutorRegistry);

        Context actualContext = ContextUtils.copyWithRegistries(givenContext);
        Assert.assertNotNull(actualContext);
        Assert.assertNotSame(givenContext, actualContext);

        PropertyFunctionRegistry actualPFunctionRegistry = PropertyFunctionRegistry.get(actualContext);
        FunctionRegistry actualFunctionRegistry = FunctionRegistry.get(actualContext);
        ServiceExecutorRegistry actualServiceExecutorRegistry = ServiceExecutorRegistry.get(actualContext);

        Assert.assertNotNull(actualPFunctionRegistry);
        Assert.assertNotNull(actualFunctionRegistry);
        Assert.assertNotNull(actualServiceExecutorRegistry);

        Assert.assertNotSame(givenFunctionRegistry, actualFunctionRegistry);
        Assert.assertNotSame(givenPFunctionRegistry, actualPFunctionRegistry);
        Assert.assertNotSame(givenServiceExecutorRegistry, actualServiceExecutorRegistry);

        List<FunctionFactory> actualFunctionFactories = new ArrayList<>();
        actualFunctionRegistry.keys().forEachRemaining(k -> actualFunctionFactories.add(actualFunctionRegistry.get(k)));
        List<PropertyFunctionFactory> actualPFunctionFactories = new ArrayList<>();
        actualPFunctionRegistry.keys().forEachRemaining(k -> actualPFunctionFactories.add(actualPFunctionRegistry.get(k)));

        Assert.assertSame(givenPFunctionFactory, actualPFunctionRegistry.get("y"));
        Assert.assertSame(givenFunctionFactory, actualFunctionRegistry.get("x"));
        Assert.assertEquals(List.of(givenPFunctionFactory), actualPFunctionFactories);
        Assert.assertEquals(List.of(givenFunctionFactory), actualFunctionFactories);
        Assert.assertEquals(List.of(givenServiceExecutionFactory), actualServiceExecutorRegistry.getBulkChain());
        Assert.assertEquals(List.of(givenChainingServiceExecutor), actualServiceExecutorRegistry.getSingleChain());
    }
}
