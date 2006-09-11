/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestLiteralEncoding.java,v 1.1 2006-09-11 13:10:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.xmloutput.test;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

/**
     Tests to ensure that certain literals are either encoded properly or reported
     as exceptions.
    @author kers
*/
public class TestLiteralEncoding extends ModelTestBase
    {
    public TestLiteralEncoding( String name )
        { super( name ); }
    
    public void testPlaceHolder()
        {}
    }

