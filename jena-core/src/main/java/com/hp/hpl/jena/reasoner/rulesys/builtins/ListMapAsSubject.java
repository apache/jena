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
import java.util.*;

/**
 * For each element in the RDF list (first argument) it asserts 
 * triples with that as the subject and predicate and object given by arguments
 * two and three. A strange and hacky function, only usable in the head of
 * forward rules.
 */
public class ListMapAsSubject extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "listMapAsSubject";
    }
    
    /**
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
     */
    @Override
    public int getArgLength() {
        return 3;
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
        checkArgs(length, context);
        Node n0 = getArg(0, args, context);
        Node n1 = getArg(1, args, context);
        Node n2 = getArg(2, args, context);
        List<Node>l = Util.convertList(n0, context);
        for ( Node x : l )
        {
            context.add( new Triple( x, n1, n2 ) );
        }
    }
    
}
