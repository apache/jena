/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestHashedBunchMap.java,v 1.1 2006-10-30 15:57:15 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.test;

import com.hp.hpl.jena.mem.HashedBunchMap;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestHashedBunchMap extends ModelTestBase
    {
    public TestHashedBunchMap( String name )
        { super( name ); }
    
    public void testSize()
        {
        HashedBunchMap b = new HashedBunchMap();
        }

    }

