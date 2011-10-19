/******************************************************************
 * File:        Regexp.java
 * Created by:  Dave Reynolds
 * Created on:  10 Jan 2007
 * 
 * (c) Copyright 2007, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: Regex.java,v 1.2 2009-10-07 14:35:39 der Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.rulesys.builtins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.graph.Node;
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
                Node match =  (gm != null) ? Node.createLiteral( gm ) : Node.createLiteral("");
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
