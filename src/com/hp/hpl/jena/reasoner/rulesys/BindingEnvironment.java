/******************************************************************
 * File:        BindingEnvironment.java
 * Created by:  Dave Reynolds
 * Created on:  30-Mar-03
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: BindingEnvironment.java,v 1.8 2005-02-21 12:16:56 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * Interface through which the current bound values of variables
 * can be found. Many of the details vary between the forward and
 * backward chaining system - this interface is the minimal one needed
 * by most builtins the specific implementations offer richer functionality.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.8 $ on $Date: 2005-02-21 12:16:56 $
 */
public interface BindingEnvironment {
        
    /**
     * Return the most ground version of the node. If the node is not a variable
     * just return it, if it is a varible bound in this environment return the binding,
     * if it is an unbound variable return the variable.
     */
    public Node getGroundVersion(Node node);
    
    /**
     * Bind a variable in the current envionment to the given value.
     * Checks that the new binding is compatible with any current binding.
     * @param var a Node_RuleVariable defining the variable to bind
     * @param value the value to bind
     * @return false if the binding fails
     */
    public boolean bind(Node var, Node value);
    
    /**
      * Instantiate a triple pattern against the current environment.
      * This version handles unbound varibles by turning them into bNodes.
      * @param pattern the triple pattern to match
      * @return a new, instantiated triple
      */
     public Triple instantiate(TriplePattern pattern);
         
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
