/******************************************************************
 * File:        MakeInstance.java
 * Created by:  Dave Reynolds
 * Created on:  02-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: MakeInstance.java,v 1.2 2003-06-10 22:25:01 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.builtins;

import com.hp.hpl.jena.reasoner.rulesys.*;
//import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.*;

/**
 * Create a new anonymous instance of an property value inferred from a some(?P,C) rule.
 * The arguments, in order, are:
 * <ul>
 * <li>the instance to which the property should be bound</li>
 * <li>the property to be instantiated</li>
 * <li>the type to assign to the created instance</li>
 * <li>the unbound var through which the resulting value node will be returned</li>
 * </ul>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-06-10 22:25:01 $
 */
public class MakeInstance extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    public String getName() {
        return "makeInstance";
    }

    /**
     * This method is invoked when the builtin is called in a rule body.
     * @param args the array of argument values for the builtin, this is an array 
     * of Nodes, some of which may be Node_RuleVariables.
     * @param context an execution context giving access to other relevant data
     * @return return true if the buildin predicate is deemed to have succeeded in
     * the current environment
     */
    public boolean bodyCall(Node[] args, RuleContext context) {
        if (args.length != 4) {
            throw new BuiltinException(this, context, getName() + " expected 4 arguments");
        }
        if ( ! context.contains(args[0], args[1], null)) {
            Node value = Node.createAnon();
            // This won't work until we a concurrently updatable deductions graph
//            context.silentAdd(new Triple(value, RDF.type.asNode(), args[2]));
            if (! context.getEnv().bind(args[3], value)) return false;
            return true;
        }
        return false;
    }
    
    
    /**
     * This method is invoked when the builtin is called in a rule head.
     * Such a use is only valid in a forward rule.
     * Exected args are the instance to be annotated, the property to use and the type
     * of the resulting bNode.
     * @param args the array of argument values for the builtin, this is an array 
     * of Nodes.
     * @param context an execution context giving access to other relevant data
     * @param rule the invoking rule
     */
    public void headAction(Node[] args, RuleContext context) {
        if (args.length != 4) {
            throw new BuiltinException(this, context, getName() + " expected 4 arguments");
        }
        if ( ! context.contains(args[0], args[1], null)) {
            Node value = Node.createAnon();
            // This won't work until we a concurrently updatable deductions graph
//            context.silentAdd(new Triple(value, RDF.type.asNode(), args[2]));
            context.getEnv().bind(args[3], value);
        }
    }
    
    /**
     * Returns false if this builtin has side effects when run in a body clause,
     * other than the binding of environment variables.
     */
    public boolean isSafe() {
        return false;
    }
    
}



/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/