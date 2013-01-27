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
 *  Bind the third arg to the sum of the first two args.
 */
public class Sum extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "sum";
    }
    
    /**
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
     */
    @Override
    public int getArgLength() {
        return 3;
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
        checkArgs(length, context);
        BindingEnvironment env = context.getEnv();
        Node n1 = getArg(0, args, context);
        Node n2 = getArg(1, args, context);
        if (n1.isLiteral() && n2.isLiteral()) {
            Object v1 = n1.getLiteralValue();
            Object v2 = n2.getLiteralValue();
            Node sum = null;
            if (v1 instanceof Number && v2 instanceof Number) {
                Number nv1 = (Number)v1;
                Number nv2 = (Number)v2;
                if (v1 instanceof Float || v1 instanceof Double 
                ||  v2 instanceof Float || v2 instanceof Double) {
                    sum = Util.makeDoubleNode(nv1.doubleValue() + nv2.doubleValue());
                } else {
                    sum = Util.makeLongNode(nv1.longValue() + nv2.longValue());
                }
                return env.bind(args[2], sum);
            }
        }
        // Doesn't (yet) handle partially bound cases
        return false;
    }
    
}
