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

import java.util.*;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;

/**
 * Test if the two argument lists contain the same semantic elements.
 */
public class ListEqual extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "listEqual";
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
        Node n0 = getArg(0, args, context);
        Node n1 = getArg(1, args, context);
        return listEqual(n0, n1, context);
    }
    
    /**
     * Test two RDF lists for semantic equality. Expensive.
     */
    protected static boolean listEqual(Node list1, Node list2, RuleContext context ) {
        List<Node> elts1 = Util.convertList(list1, context);
        List<Node> elts2 = Util.convertList(list2, context);
        if (elts1.size() != elts2.size()) return false;
        for ( Node elt : elts1 )
        {
            boolean matched = false;
            for ( Iterator<Node> j = elts2.iterator(); j.hasNext(); )
            {
                Node elt2 = j.next();
                if ( elt.sameValueAs( elt2 ) )
                {
                    // Found match, consume it
                    j.remove();
                    matched = true;
                    break;
                }
            }
            if ( !matched )
            {
                return false;
            }
        }
        return true;
    }
}
