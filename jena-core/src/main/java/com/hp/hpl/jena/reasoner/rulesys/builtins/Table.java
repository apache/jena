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

package com.hp.hpl.jena.reasoner.rulesys.builtins;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;

/**
 * Arrange that the given predicate is tabled by the backchaining engine.
 */
public class Table extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "table";
    }

    /**
     * This method is invoked when the builtin is called in a rule body.
     * @param args the array of argument values for the builtin, this is an array 
     * of Nodes, some of which may be Node_RuleVariables.
     * @param length the length of the argument list, may be less than the length of the args array
     * for some rule engines
     * @param context an execution context giving access to other relevant data
     */
    @Override
    public void headAction(Node[] args, int length, RuleContext context) {
        InfGraph infgraph = context.getGraph();
        if (infgraph instanceof FBRuleInfGraph) {
            for (int i = 0; i < length; i++) {
                ((FBRuleInfGraph)infgraph).setTabled(args[i]);
            }
        } else if (infgraph instanceof LPBackwardRuleInfGraph) {
            for (int i = 0; i < length; i++) {
                ((LPBackwardRuleInfGraph)infgraph).setTabled(args[i]);
            }
        } else {
            // Quietly ignore as an irrelevant directive
            // Could log or throw exception but currently I want to be able to use
            // the same rule base from different contexts which do and do not need
            // to know about this.
        }
    }
 
}
