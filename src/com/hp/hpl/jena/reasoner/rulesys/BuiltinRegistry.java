/******************************************************************
 * File:        BuildinRegistry.java
 * Created by:  Dave Reynolds
 * Created on:  11-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BuiltinRegistry.java,v 1.5 2003-06-02 09:03:50 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.rulesys.builtins.*;
import java.util.*;

/** * A registry for mapping functor names on java objects (instances 
 * of subclasses of Builtin) which implement their behvaiour.
 * <p>
 * This is currently implemented as a singleton to simply any future
 * move to support different sets of builtins.
 * 
 * @see Builtin * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a> * @version $Revision: 1.5 $ on $Date: 2003-06-02 09:03:50 $ */
public class BuiltinRegistry {

    /** The single global static registry */
    public static BuiltinRegistry theRegistry;
    
    /** Mapping from functor name to Builtin implementing it */
    protected Map builtins = new HashMap();
    
    // Static initilizer for the singleton instance
    static {
        theRegistry = new BuiltinRegistry();
        
        theRegistry.register(new Print());
        theRegistry.register(new AddOne());
        theRegistry.register(new LessThan());
        theRegistry.register(new NotFunctor());
        theRegistry.register(new IsFunctor());
        theRegistry.register(new NotEqual());
        theRegistry.register(new MakeTemp());
        theRegistry.register(new NoValue());
        theRegistry.register(new AssertDisjointPairs());
        theRegistry.register(new Remove());
        theRegistry.register(new Sum());
        theRegistry.register(new Bound());
        theRegistry.register(new Unbound());
        theRegistry.register(new IsLiteral());
        theRegistry.register(new NotLiteral());
        theRegistry.register(new SchedulePrototypeCheck());
    }
    
    /**
     * Construct an empty registry
     */
    public BuiltinRegistry() {
    }
    
    /**
     * Register an implementation for a given builtin functor.
     * @param functor the name of the functor used to invoke the builtin
     * @param impl the implementation of the builtin
     */
    public void register(String functor, Builtin impl) {
        builtins.put(functor, impl);
    }
   
    /**
     * Register an implementation for a given builtin using its default name.
     * @param impl the implementation of the builtin
     */
    public void register(Builtin impl) {
        builtins.put(impl.getName(), impl);
    }
    
    /**
     * Find the implementation of the given builtin functor.
     * @param functor the name of the functor being invoked.
     * @return a Builtin or null if there is none registered under that name
     */
    public Builtin getImplementation(String functor) {
        return (Builtin)builtins.get(functor);
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
