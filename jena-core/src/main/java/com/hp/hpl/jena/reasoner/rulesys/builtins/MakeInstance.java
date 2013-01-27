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
import com.hp.hpl.jena.reasoner.rulesys.impl.BBRuleContext;
//import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.graph.*;

/**
 * Create or lookup an anonymous instance of a property value. Syntax of the call is:
 * <pre>
 *    makeInstance(X, P, D, T) or makeInstance(X, P, T)
 * </pre>
 * where X is the instance and P the property for which a temporary
 * value is required, T will be bound to the temp value (a bNode) and D is
 * an optional type cor the T value.
 */
public class MakeInstance extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "makeInstance";
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
//        System.out.println("MakeInstance on ");
//        for (int i = 0; i < length; i++) {
//            System.out.println(" - " + PrintUtil.print(args[i]));
//        }
        if (length == 3 || length == 4) {
            Node inst = getArg(0, args, context);
            Node prop = getArg(1, args, context);
            Node pclass = length == 4 ? getArg(2, args, context) : null;
            if (context instanceof BBRuleContext) {
                Node temp = ((BBRuleContext)context).getTemp(inst, prop, pclass);
                return context.getEnv().bind(args[length-1], temp); 
            } else {
                throw new BuiltinException(this, context, "builtin " + getName() + " only usable in backward/hybrid rule sets");
            }
        } else {
            throw new BuiltinException(this, context, "builtin " + getName() + " requries 3 or 4 arguments");
        }
    }
 
}
