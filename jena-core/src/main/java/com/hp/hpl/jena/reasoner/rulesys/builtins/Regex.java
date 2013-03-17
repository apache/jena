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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.reasoner.rulesys.BindingEnvironment;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;

public class Regex extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "regex";
    }
    
    /**
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
     */
    @Override
    public int getArgLength() {
        return 0;
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
        if (length < 2) 
            throw new BuiltinException(this, context, "Must have at least 2 arguments to " + getName());
        String text = getString( getArg(0, args, context), context );
        String pattern = getString( getArg(1, args, context), context );
        Matcher m = Pattern.compile(pattern).matcher(text);
        if ( ! m.matches()) return false;
        if (length > 2) {
            // bind any capture groups
            BindingEnvironment env = context.getEnv();
            for (int i = 0; i < Math.min(length-2, m.groupCount()); i++) {
                String gm = m.group(i+1);
                Node match =  (gm != null) ? NodeFactory.createLiteral( gm ) : NodeFactory.createLiteral("");
                if ( !env.bind(args[i+2], match) ) return false;
            }
        }
        return true;
    }
    
    /**
     * Return the lexical form of a literal node, error for other node types
     */
    protected String getString(Node n, RuleContext context) {
        if (n.isLiteral()) {
            return n.getLiteralLexicalForm();
        } else {
            throw new BuiltinException(this, context, getName() + " takes only literal arguments");
        }
    }

}
