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

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.rulesys.builtins.*;
import java.util.*;

/** * A registry for mapping functor names on java objects (instances 
 * of subclasses of Builtin) which implement their behvaiour.
 * <p>
 * This is currently implemented as a singleton to simply any future
 * move to support different sets of builtins.
 * 
 * @see Builtin
 */
public class BuiltinRegistry {

    /** The single global static registry */
    public static BuiltinRegistry theRegistry;
    
    /** Mapping from functor name to Builtin implementing it */
    protected Map<String,Builtin> builtins = new HashMap<>();
    
    /** Mapping from URI of builtin to implementation */
    protected Map<String,Builtin> builtinsByURI = new HashMap<>();
    
    // Static initilizer for the singleton instance
    static {
        theRegistry = new BuiltinRegistry();
        
        theRegistry.register(new Print());
        theRegistry.register(new AddOne());
        theRegistry.register(new LessThan());
        theRegistry.register(new GreaterThan());
        theRegistry.register(new LE());
        theRegistry.register(new GE());
        theRegistry.register(new Equal());
        theRegistry.register(new NotFunctor());
        theRegistry.register(new IsFunctor());
        theRegistry.register(new NotEqual());
        theRegistry.register(new MakeTemp());
        theRegistry.register(new NoValue());
        theRegistry.register(new Remove());
        theRegistry.register(new Drop());
        theRegistry.register(new Sum());
        theRegistry.register(new Difference());
        theRegistry.register(new Product());
        theRegistry.register(new Quotient());
        theRegistry.register(new Bound());
        theRegistry.register(new Unbound());
        theRegistry.register(new IsLiteral());
        theRegistry.register(new NotLiteral());
        theRegistry.register(new IsBNode());
        theRegistry.register(new NotBNode());
        theRegistry.register(new IsDType());
        theRegistry.register(new NotDType());
        theRegistry.register(new CountLiteralValues());
        theRegistry.register(new Max());
        theRegistry.register(new Min());
        theRegistry.register(new ListLength());
        theRegistry.register(new ListEntry());
        theRegistry.register(new ListEqual());
        theRegistry.register(new ListNotEqual());
        theRegistry.register(new ListContains());
        theRegistry.register(new ListNotContains());
        theRegistry.register(new ListMapAsSubject());
        theRegistry.register(new ListMapAsObject());
        
        theRegistry.register(new MakeInstance());
        theRegistry.register(new Table());
        theRegistry.register(new TableAll());
        
        theRegistry.register(new MakeSkolem());
        
        theRegistry.register(new Hide());
        
        theRegistry.register(new StrConcat());
        theRegistry.register(new UriConcat());
        theRegistry.register(new Regex());
        
        theRegistry.register(new Now());
        
        // Special purposes support functions for OWL
        theRegistry.register(new AssertDisjointPairs());
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
        builtinsByURI.put(impl.getURI(), impl);
    }
   
    /**
     * Register an implementation for a given builtin using its default name.
     * @param impl the implementation of the builtin
     */
    public void register(Builtin impl) {
        builtins.put(impl.getName(), impl);
        builtinsByURI.put(impl.getURI(), impl);
    }
    
    /**
     * Find the implementation of the given builtin functor.
     * @param functor the name of the functor being invoked.
     * @return a Builtin or null if there is none registered under that name
     */
    public Builtin getImplementation(String functor) {
        return builtins.get(functor);
    }
    
    /**
     * Find the implementation of the given builtin functor.
     * @param uri the URI of the builtin to be retrieved
     * @return a Builtin or null if there is none registered under that name
     */
    public Builtin getImplementationByURI(String uri) {
        return builtinsByURI.get(uri);
    }
    
}
