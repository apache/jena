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
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Tests whether the first argument is an instance of the datatype defined
 * by the resource in the second argument.
 */
public class IsDType extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "isDType";
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
        Node val = getArg(0, args, context);
        Node dt = getArg(1, args, context);
        return isTypeOK(val, dt);
    }
    
    /**
     * Check if a literal value node is a legal value for the given datatype.
     * @param val the literal value node
     * @param dt  the Node designating a datatype URI 
     */
    public static boolean isTypeOK(Node val, Node dt) {
        if (!dt.isURI()) return false;
        if (val.isBlank()) return true;
        if (val.isLiteral()) {
            LiteralLabel ll = val.getLiteral();
            if (ll.getDatatype() != null && (! ll.isWellFormed())) return false;
            if (dt.equals(RDFS.Nodes.Literal)) {
                return true;
            } else {
                RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(dt.getURI());
                return dtype.isValidLiteral(val.getLiteral());   
            }
        }
        return false;
    }
    
}
