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

package org.apache.jena.reasoner.rulesys.builtins;

import org.apache.jena.graph.* ;
import org.apache.jena.reasoner.rulesys.* ;

/**
 * Tests if the first argument is greater than the second.
 */
public class GreaterThan extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "greaterThan";
    }

    /**
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
     */
    @Override
    public int getArgLength() {
        return 2;
    }

    /**
     * This method is invoked when the builtin is called in a rule body.
     * @param args the array of argument values for the builtin, this is an array
     * of Nodes, some of which may be Node_RuleVariables.
     * @param context an execution context giving access to other relevant data
     * @return return true if the buildin predicate is deemed to have succeeded in
     * the current environment
     */
    @Override
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
        checkArgs(length, context);
        Node n1 = getArg(0, args, context);
        Node n2 = getArg(1, args, context);

        if (Util.comparable(n1, n2)) {
            return Util.compareTypedLiterals(n1, n2) > 0;
        } else {
            return false;
        }
    }

}
