/******************************************************************
 * File:        MakeInstance.java
 * Created by:  Dave Reynolds
 * Created on:  02-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: MakeInstance.java,v 1.11 2005-02-21 12:17:29 andy_seaborne Exp $
 *****************************************************************/
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
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.11 $ on $Date: 2005-02-21 12:17:29 $
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
     * @param length the length of the argument list, may be less than the length of the args array
     * for some rule engines
     * @param context an execution context giving access to other relevant data
     * @return return true if the buildin predicate is deemed to have succeeded in
     * the current environment
     */
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


/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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