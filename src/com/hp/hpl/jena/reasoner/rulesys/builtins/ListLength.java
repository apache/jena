/******************************************************************
 * File:        ListLength.java
 * Created by:  Dave Reynolds
 * Created on:  22-Sep-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
 * [See end of file]
 * $Id: ListLength.java,v 1.3 2003-12-04 14:08:22 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.builtins;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.*;

/**
 * Bind the second arg to the length of the first arg treated as a list.
 * Fails if the list is malformed.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-12-04 14:08:22 $
 */
public class ListLength extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    public String getName() {
        return "listLength";
    }
    
    /**
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
     */
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



/*
    (c) Copyright Hewlett-Packard Development Company, LP 2003
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