/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestCaseBasic.java,v 1.1 2003-02-21 15:45:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose;

/**
	@author kers
*/

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.regression.*;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestCase;

import java.io.FileReader;
import java.io.PrintWriter;

import java.util.HashSet;
import java.util.Iterator;

import java.lang.reflect.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.regression.*;

/**
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.1 $ $Date: 2003-02-21 15:45:00 $
 */
public class TestCaseBasic extends com.hp.hpl.jena.regression.TestCaseBasic 
	{
    private Class graphClass;

    public TestCaseBasic(String name, Class graphClass) 
    	{
        super(name);
        this.graphClass = graphClass;
    	}
    
    private Graph newGraph( Constructor cons )
    	{
    	try { return (Graph) cons.newInstance( new Object [] { new GraphMem(), new GraphMem() } ); }
    	catch (Exception e) { throw new RuntimeException( "Alas: " + e ); }
    	}
    	
    public void setUp() 
    	{
    	try 
    		{
    		Constructor constructor = graphClass.getConstructor(new Class[]{
    		   Graph.class, Graph.class });
        	m1 = new ModelMem( newGraph( constructor ) );
        	m2 = new ModelMem( newGraph( constructor ) );
        	m3 = new ModelMem( newGraph( constructor ) );
        	m4 = new ModelMem( newGraph( constructor ) );
    		}
    	catch (Exception e)
			{}
    	}
	}

/*
    (c) Copyright Hewlett-Packard Company 2002
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
