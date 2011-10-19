/******************************************************************
 * File:        StrConcat.java
 * Created by:  Dave Reynolds
 * Created on:  10 Jan 2007
 * 
 * (c) Copyright 2007, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: StrConcat.java,v 1.1 2009-06-29 08:55:36 castagna Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.rulesys.builtins;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;

/**
 * Builtin which concatenates a set of strings. It binds the last argument to 
 * a plain literal which is the concatenation of all the preceeding arguments.
 * For a literal argument we use its lexcical form, for a URI argument its URI,
 * for a bNode argument its internal ID.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $
 */
public class StrConcat extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "strConcat";
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
        if (length < 1) 
            throw new BuiltinException(this, context, "Must have at least 1 argument to " + getName());
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < length-1; i++) {
            buff.append( lex(getArg(i, args, context), context) );
        }
        Node result = Node.createLiteral(buff.toString());
        return context.getEnv().bind(args[length-1], result);
    }
    
    /**
     * Return the appropriate lexical form of a node
     */
    protected String lex(Node n, RuleContext context) {
        if (n.isBlank()) {
            return n.getBlankNodeLabel();
        } else if (n.isURI()) {
            return n.getURI();
        } else if (n.isLiteral()) {
            return n.getLiteralLexicalForm();
        } else {
            throw new BuiltinException(this, context, "Illegal node type: " + n);
        }
    }
    
}


/*
    (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
