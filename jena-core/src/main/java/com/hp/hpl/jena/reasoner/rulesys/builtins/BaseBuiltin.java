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

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;

/**
 * Dummy implementation of the Builtin interface that specific
 * implementations can inherit from.
 */
public abstract class BaseBuiltin implements Builtin {

    /** Base URI for jena builtins */
    public static final String BASE_URI = "http://jena.hpl.hp.com/2003/RuleBuiltin/";
    
    /**
     * Return the full URI which identifies this built in.
     */
    @Override
    public String getURI() {
        return BASE_URI + getName();
    }
    
    /**
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
     */
    @Override
    public int getArgLength() {
        return 0;
    }

    /** 
     * Check the argument length.
     */
    public void checkArgs(int length, RuleContext context) {
        int expected = getArgLength();
        if (expected > 0 && expected != length) {
            throw new BuiltinException(this, context, "builtin " + getName() + " requires " + expected + " arguments but saw " + length);
        }
    }
    
    /**
     * This method is invoked when the builtin is called in a rule body.
     * @param args the array of argument values for the builtin, this is an array 
     * of Nodes, some of which may be Node_RuleVariables.
     * @param length the length of the argument list, may be less than the length of the args array
     * for some rule engines
     * @param context an execution context giving access to other relevant data
     * @return return true if the buildin predicate is deemed to have succeeded in
     * the current environment
     */
    @Override
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
        throw new BuiltinException(this, context, "builtin " + getName() + " not usable in rule bodies");
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
        throw new BuiltinException(this, context, "builtin " + getName() + " not usable in rule heads");
    }
    
    /**
     * Returns false if this builtin has side effects when run in a body clause,
     * other than the binding of environment variables.
     */
    @Override
    public boolean isSafe() {
        // Default is safe!
        return true;
    }
    
    /**
     * Returns false if this builtin is non-monotonic. This includes non-monotonic checks like noValue
     * and non-monotonic actions like remove/drop. A non-monotonic call in a head is assumed to 
     * be an action and makes the overall rule and ruleset non-monotonic. 
     * Most JenaRules are monotonic deductive closure rules in which this should be false.
     */
    @Override
    public boolean isMonotonic() {
        return true;
    }
    
    /**
     * Return the n'th argument node after dererencing by what ever type of
     * rule engine binding environment is appropriate.
     */
    public Node getArg(int n, Node[] args, RuleContext context) {
        return context.getEnv().getGroundVersion(args[n]);
    }

}
