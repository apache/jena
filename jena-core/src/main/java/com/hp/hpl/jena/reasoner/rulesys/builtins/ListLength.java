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
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.*;

/**
 * Bind the second arg to the length of the first arg treated as a list.
 * Fails if the list is malformed.
 */
public class ListLength extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "listLength";
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
        int len = getLength(getArg(0, args, context), context);
        if (len == -1) {
            return false;
        } else {
            env.bind(args[1], Util.makeIntNode(len));
            return true;
        }
    }
    
    /**
     * Return the length of the RDF list rooted at the given node. 
     * @param node the start of the list
     * @param context the context through which the data values can be found
     * @return the length or -1 for a malformed list.
     */
    protected static int getLength(Node node, RuleContext context ) {
         if (node.equals(RDF.Nodes.nil)) {
             return 0;
         } else {
             Node next = Util.getPropValue(node, RDF.Nodes.rest, context);
             if (next == null) {
                 return -1;
             } else {
                 int sublen = getLength(next, context);
                 if (sublen == -1) {
                     return -1;
                 } else {
                     return 1 + sublen;
                 }
             }
         }
    }
}
