/**
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

package arq.examples.aggregates;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.aggregate.Accumulator ;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory ;
import org.apache.jena.sparql.expr.aggregate.AggCustom ;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.sse.SSE ;

/**
 * Custom aggregate example.
 * <p>
 * Custom aggregates must be registered before parsing the query; custom
 * aggregates and custom functions have the same syntax so the to tell the
 * difference, the parser needs to know which IRIs are custom aggregates.
 * <p>
 * The aggregate is registered as a URI, AccumulatorFactory and default value
 * for the "no groups" case.
 */
public class CustomAggregate {
    static { LogCtl.setLogging(); }
    /**
     * Execution of a custom aggregate is with accumulators. One accumulator is
     * created for the factory for each group in a query execution.
     */
    static AccumulatorFactory myAccumulatorFactory = new AccumulatorFactory() {
        @Override
        public Accumulator createAccumulator(AggCustom agg, boolean distinct) { return new MyAccumulator(agg) ; }
    } ;
    
    /**
     * Example accumulators - counts the number of valid literals
     * of an expression over a group. 
     */
    static class MyAccumulator implements Accumulator {
        int count = 0 ;
        private AggCustom agg ;
        MyAccumulator(AggCustom agg) { this.agg = agg ; }

        /** Function called on each row in a group */
        @Override
        public void accumulate(Binding binding, FunctionEnv functionEnv) {
            ExprList exprList = agg.getExprList() ;
            for(Expr expr: exprList) {
                try {
                    NodeValue nv = expr.eval(binding, functionEnv) ;
                    // Evaluation succeeded.
                    if ( nv.isLiteral())
                        count ++ ;
                } catch (ExprEvalException ex) {}
            }
        }

        /** Function called to retrieve the value for a single group */
        @Override
        public NodeValue getValue() {
            return NodeValue.makeInteger(count) ;
        }
    }
    
    public static void main(String[] args) {
        
        // Example aggregate that counts literals.
        // Returns unbound for no rows. 
        String aggUri = "http://example/countLiterals" ;
        
        
        /* Registration */
        AggregateRegistry.register(aggUri, myAccumulatorFactory, NodeConst.nodeMinusOne);
        
        
        // Some data.
        Graph g = SSE.parseGraph("(graph (:s :p :o) (:s :p 1))") ;
        String qs = "SELECT (<http://example/countLiterals>(?o) AS ?x) {?s ?p ?o}" ;
        
        // Execution as normal.
        Query q = QueryFactory.create(qs) ;
        try ( QueryExecution qexec = QueryExecutionFactory.create(q, ModelFactory.createModelForGraph(g)) ) {
            ResultSet rs = qexec.execSelect() ;
            ResultSetFormatter.out(rs);
        }
    }
    
}
