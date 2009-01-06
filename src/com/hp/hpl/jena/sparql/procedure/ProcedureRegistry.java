/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.procedure;

import java.util.*;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.MappedLoader;

import com.hp.hpl.jena.query.ARQ;

public class ProcedureRegistry
{
    // Extract a Registry class and do casting and initialization here.
    // Identical to FunctionRegistry except the types.
    // And PropertyFunctionRegistry (which may disappear)
    Map<String, ProcedureFactory> registry = new HashMap<String, ProcedureFactory>() ;
    Set<String> attemptedLoads = new HashSet<String>() ;
    
    public synchronized static ProcedureRegistry standardRegistry()
    {
        ProcedureRegistry reg = new ProcedureRegistry() ;
        StandardProcedures.loadStdDefs(reg) ;
        return reg ;   
    }
    
    public synchronized static ProcedureRegistry get()
    {
        // Intialize if there is no registry already set 
        ProcedureRegistry reg = get(ARQ.getContext()) ;
        if ( reg == null )
        {
            reg = standardRegistry() ;
            set(ARQ.getContext(), reg) ;
        }

        return reg ;
    }

    public static ProcedureRegistry get(Context context)
    {
        if ( context == null )
            return null ;
        return (ProcedureRegistry)context.get(ARQConstants.registryProcedures) ;
    }
    
    public static void set(Context context, ProcedureRegistry reg)
    {
        context.set(ARQConstants.registryProcedures, reg) ;
    }

    
    /** Insert a ProcedureFactory. Re-inserting with the same URI overwrites the old entry. 
     * 
     * @param uri
     * @param f
     */
    public void put(String uri, ProcedureFactory f) { registry.put(uri,f) ; }

    /** Insert a class that is the procedure implementation 
     * 
     * @param uri           String URI
     * @param procClass     Class for the procedure (new instance called).
     */
    public void put(String uri, Class< ? > procClass)
    { 
        if ( ! Procedure.class.isAssignableFrom(procClass) )
        {
            ALog.warn(this, "Class "+procClass.getName()+" is not a Procedure" );
            return ; 
        }
        
        registry.put(uri, new ProcedureFactoryAuto(procClass)) ;
    }
    
    /** Lookup by URI */
    public ProcedureFactory get(String uri)
    {
        ProcedureFactory procedure = registry.get(uri) ;
        if ( procedure != null )
            return procedure ;

        if ( attemptedLoads.contains(uri) )
            return null ;

        Class<?> procedureClass = MappedLoader.loadClass(uri, Procedure.class) ;
        if ( procedureClass == null )
            return null ;
        // Registry it
        put(uri, procedureClass) ;
        attemptedLoads.add(uri) ;
        // Call again to get it.
        return registry.get(uri) ;
    }
    
    public boolean isRegistered(String uri) { return registry.containsKey(uri) ; }
    
    /** Remove by URI */
    public ProcedureFactory remove(String uri) { return registry.remove(uri) ; } 
    
    /** Iterate over URIs */
    public Iterator<String> keys() { return registry.keySet().iterator() ; }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */