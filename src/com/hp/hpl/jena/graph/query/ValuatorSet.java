/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: ValuatorSet.java,v 1.2 2004-06-30 12:57:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import java.util.*;

import com.hp.hpl.jena.util.HashUtils;

/**
	ValuatorSet - a set of Valuators, which can be added to and evaluated [only].

	@author kers
*/
public class ValuatorSet 
    {
    private Set valuators = HashUtils.createSet();
    
    public ValuatorSet() 
        {}
    
    /**
         Answer this ValuatorSet after adding the Valuator <code>e</code> to it.
    */
    public ValuatorSet add( Valuator e )
        {
        valuators.add( e );
        return this;    
        }
        
    /**
         Answer true iff no Valuator in this set evaluates to <code>false</code>. The
         Valuators are evaluated in an unspecified order, and evaluation ceases as
         soon as any Valuator has returned false.
    */
    public boolean evalBool( IndexValues vv )
        { 
        Iterator it = valuators.iterator();
        while (it.hasNext()) 
            if (((Valuator) it.next()).evalBool( vv ) == false) return false;
        return true;
        }
                    
    }

/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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