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

package org.apache.jena.sparql.expr;


import org.apache.jena.graph.NodeFactory;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sys.JenaSystem;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;

import java.util.Random;


@State(Scope.Benchmark)
public class TestCoalesce {

    static {
        JenaSystem.init();
        org.apache.shadedJena550.sys.JenaSystem.init();
    }

    final static Random rnd = new Random();

    @Benchmark
    public Long benchmarkCoalesceJenaCurrent() {
        var checksum = 0L;
        // Create a Coalesce expression with some dummy expressions
        var expressions = new ExprList();
        expressions.add(new ExprVar("var1"));
        expressions.add(new ExprVar("var2"));
        expressions.add(new ExprVar("var3"));
        expressions.add(new ExprVar("var4"));
        expressions.add(new ExprVar("var5"));

        var var3BoundLiteral =  NodeFactory.createLiteralString("Value5");
        var bindingBuilder = Binding.builder();
        bindingBuilder.add(expressions.get(4).asVar(), var3BoundLiteral);
        var binding = bindingBuilder.build();

        FunctionEnv env = null;

        // Coalesce expression
        var coalesceExpr = new E_Coalesce(expressions);

        for(var i=0; i<1000000; i++) {
            var result = coalesceExpr.eval(binding, env);
            if( result != null ) {
                checksum += i;
            }
        }
        return checksum;
    }

    @Benchmark
    public Long benchmarkCoalesceJena550() {
        var checksum = 0L;
        // Create a Coalesce expression with some dummy expressions
        var expressions = new org.apache.shadedJena550.sparql.expr.ExprList();
        expressions.add(new org.apache.shadedJena550.sparql.expr.ExprVar("var1"));
        expressions.add(new org.apache.shadedJena550.sparql.expr.ExprVar("var2"));
        expressions.add(new org.apache.shadedJena550.sparql.expr.ExprVar("var3"));
        expressions.add(new org.apache.shadedJena550.sparql.expr.ExprVar("var4"));
        expressions.add(new org.apache.shadedJena550.sparql.expr.ExprVar("var5"));

        var var3BoundLiteral =  org.apache.shadedJena550.graph.NodeFactory.createLiteralString("Value5");
        var bindingBuilder = org.apache.shadedJena550.sparql.engine.binding.Binding.builder();
        bindingBuilder.add(expressions.get(4).asVar(), var3BoundLiteral);
        var binding = bindingBuilder.build();
        org.apache.shadedJena550.sparql.function.FunctionEnv env = null;

        // Coalesce expression
        var coalesceExpr =
                new org.apache.shadedJena550.sparql.expr.E_Coalesce(expressions);

        for(var i=0; i<1000000; i++) {
            var result = coalesceExpr.eval(binding, env);
            if( result != null ) {
                checksum += i;
            }
        }
        return checksum;
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JMHDefaultOptions.getDefaults(this.getClass())
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }
}
