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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.Util;

/**
 * A variant of the "remove" builtin that will delete matched triples
 * from the graph but will not trigger further rule processing for
 * the removed triples. This makes it seriously non-monotonic but
 * useful for rewrite rules.
 */

public class Drop  extends BaseBuiltin  {
    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "drop";
    }    
    
    /**
     * This method is invoked when the builtin is called in a rule head.
     * Such a use is only valid in a forward rule.
     * @param args the array of argument values for the builtin, this is an array 
     * of Nodes.
     * @param length the length of the argument list, may be less than the length of the args array
     * for some rule engines
     * @param context an execution context giving access to other relevant data
     */
    @Override
    public void headAction(Node[] args, int length, RuleContext context) {
        boolean ok = false;
        InfGraph inf = context.getGraph();
        Graph raw = inf.getRawGraph();
        Graph deductions = inf.getDeductionsGraph();
        for (int i = 0; i < length; i++) {
            Node clauseN = getArg(i, args, context);
            if (Util.isNumeric(clauseN)) {
                int clauseIndex = Util.getIntValue(clauseN);
                Object clause = context.getRule().getBodyElement(clauseIndex);
                if (clause instanceof TriplePattern) {
                    Triple t = context.getEnv().instantiate((TriplePattern)clause);
                    raw.delete(t);
                    deductions.delete(t);
                } else {
                    throw new BuiltinException(this, context, "illegal triple to remove non-triple clause");
                }
            } else {
                throw new BuiltinException(this, context, "illegal arg to remove (" + clauseN + "), must be an integer");
            }
        }
    }    
    
    /**
     * Returns false if this builtin is non-monotonic. This includes non-monotonic checks like noValue
     * and non-monotonic actions like remove/drop. A non-monotonic call in a head is assumed to 
     * be an action and makes the overall rule and ruleset non-monotonic. 
     * Most JenaRules are monotonic deductive closure rules in which this should be false.
     */
    @Override
    public boolean isMonotonic() {
        return false;
    }

}
