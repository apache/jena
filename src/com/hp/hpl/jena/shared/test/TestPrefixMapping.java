/*
  (c) Copyright 2002, 2003 Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestPrefixMapping.java,v 1.4 2003-05-03 07:44:50 chris-dollin Exp $
*/

package com.hp.hpl.jena.shared.test;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.shared.impl.*;

import junit.framework.*;

/**
    Tests PrefixMappingImpl by subclassing AbstractTestPrefixMapping, qv.
    @author kers
*/

public class TestPrefixMapping extends AbstractTestPrefixMapping
    {
    public TestPrefixMapping( String name )
        { super( name ); };
        
    public static TestSuite suite()
        { return new TestSuite( TestPrefixMapping.class ); }   
    
    protected PrefixMapping getMapping()
        { return new PrefixMappingImpl(); }        
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