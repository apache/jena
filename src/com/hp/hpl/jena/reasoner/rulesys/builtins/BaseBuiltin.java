/******************************************************************
 * File:        BaseBuiltin.java
 * Created by:  Dave Reynolds
 * Created on:  10-Jun-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: BaseBuiltin.java,v 1.8 2004-12-07 09:53:54 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.builtins;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;

/**
 * Dummy implementation of the Builtin interface that specific
 * implementations can inherit from.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.8 $ on $Date: 2004-12-07 09:53:54 $
 */
public abstract class BaseBuiltin implements Builtin {

    /** Base URI for jena builtins */
    public static final String BASE_URI = "http://jena.hpl.hp.com/2003/RuleBuiltin/";
    
    /**
     * Return the full URI which identifies this built in.
     */
    public String getURI() {
        return BASE_URI + getName();
    }
    
    /**
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
     */
    public int getArgLength() {
        return 0;
    }

    /** 
     * Check the argument length.
     */
    public void checkArgs(int length, RuleContext context) {
        int expected = getArgLength();
        if (expected > 0 && expected != length) {
            throw new BuiltinException(this, context, "builtin " + getName() + " requires " + expected + " arguments but saw " + length);
        }
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
        throw new BuiltinException(this, context, "builtin " + getName() + " not usable in rule bodies");
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
    public void headAction(Node[] args, int length, RuleContext context) {
        throw new BuiltinException(this, context, "builtin " + getName() + " not usable in rule heads");
    }
    
    /**
     * Returns false if this builtin has side effects when run in a body clause,
     * other than the binding of environment variables.
     */
    public boolean isSafe() {
        // Default is safe!
        return true;
    }
    
    /**
     * Return the n'th argument node after dererencing by what ever type of
     * rule engine binding environment is appropriate.
     */
    public Node getArg(int n, Node[] args, RuleContext context) {
        return context.getEnv().getGroundVersion(args[n]);
    }

}



/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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