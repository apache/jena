/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */


/** Old name for ResultBinding - for compatibility only
 * @author		Andy Seaborne
 * @version 	$Id: Environment.java,v 1.5 2003-08-27 12:25:58 andy_seaborne Exp $
 */

package com.hp.hpl.jena.rdql;

import com.hp.hpl.jena.rdf.model.* ;

public class Environment extends ResultBinding
{
   /**
    * @deprecated Renamed as {@link ResultBinding ResultBinding}
    */
    public Environment() { super() ; }
    
   /**
    * @deprecated This class has been renamed as {@link ResultBinding ResultBinding}
    */
    public int add(String varName, Value value)
    {
        return super.add(varName, value) ;
    }

   /**
    * @deprecated This class has been renamed as {@link ResultBinding ResultBinding}
    */
    public int add(String varName, RDFNode node)
    {
        return super.add(varName, node) ;
    }

    
   /**
    * @deprecated This class has been renamed as {@link ResultBinding ResultBinding}
    */
    public ResultBindingIterator iterator()
    {
        return super.iterator() ;
    }

   /**
    * @deprecated This class has been renamed as {@link ResultBinding ResultBinding}
    */
    public Object get(String varName)
    {
        return super.get(varName) ;
    }

   /**
    * @deprecated This class has been renamed as {@link ResultBinding ResultBinding}
    */
    public Value getValue(String varName)
    {
        return super.getValue(varName) ;
    }
    
   /**
    * @deprecated This class has been renamed as {@link ResultBinding ResultBinding}
    */
    public int size()
    {
        return super.size() ;
    }

   /** Check for multiple bindings of the same variable.
    * @deprecated This class has been renamed as {@link ResultBinding ResultBinding}
    */

    public void check()
    {
        super.check() ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
