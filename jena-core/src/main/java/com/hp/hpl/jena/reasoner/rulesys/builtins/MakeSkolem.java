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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.xerces.impl.dv.util.Base64;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Bind a blank node to the first argument.
 * For any given combination of the remaining arguments
 * the same blank node will be returned. 
 */
public class MakeSkolem extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "makeSkolem";
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
        StringBuilder key = new StringBuilder();
        for (int i = 1; i < length; i++) {
            Node n = getArg(i, args, context);
            if (n.isBlank()) {
                key.append("B"); key.append(n.getBlankNodeLabel());
            } else if (n.isURI()) {
                key.append("U"); key.append(n.getURI());
            } else if (n.isLiteral()) {
                key.append("L"); key.append(n.getLiteralLexicalForm()); 
                if (n.getLiteralLanguage() != null) key.append("@" + n.getLiteralLanguage());
                if (n.getLiteralDatatypeURI() != null) key.append("^^" + n.getLiteralDatatypeURI());
            } else {
                key.append("O"); key.append(n.toString());
            }
        }
        
        try {
            
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.reset();
            byte[] digest = digester.digest(key.toString().getBytes());
            Node skolem = NodeFactory.createAnon( new AnonId( Base64.encode(digest) ) );
            return context.getEnv().bind(args[0], skolem); 
            
        } catch (NoSuchAlgorithmException e) {
            throw new JenaException(e);
        }
    }
        
}
