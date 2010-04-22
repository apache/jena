/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.sys.DatasetGraphMakerTDB;
import com.hp.hpl.jena.tdb.sys.TDBMaker;

public class TestTDBFactory extends BaseTest
{
    @Test public void factory1()
    {
        DatasetGraphTDB dg1 = TDBFactory.createDatasetGraph(Location.mem()) ;
        DatasetGraphTDB dg2 = TDBFactory.createDatasetGraph(Location.mem()) ;
        assertSame(dg1, dg2) ;
    }
    
    @Test public void factory2()
    {
        DatasetGraphMakerTDB f = TDBMaker.getImplFactory() ;

        TDBMaker.clearDatasetCache() ;
        DatasetGraphTDB dg0 = TDBFactory.createDatasetGraph(Location.mem()) ;

        // Uncached.
        TDBMaker.setImplFactory(TDBMaker.uncachedFactory) ;
        DatasetGraphTDB dg1 = TDBFactory.createDatasetGraph(Location.mem()) ;
        DatasetGraphTDB dg2 = TDBFactory.createDatasetGraph(Location.mem()) ;
        assertNotSame(dg1, dg2) ;
        
        // Switch back to cached.
        TDBMaker.setImplFactory(f) ;
        DatasetGraphTDB dg3 = TDBFactory.createDatasetGraph(Location.mem()) ;
        assertNotSame(dg3, dg1) ;
        assertNotSame(dg3, dg2) ;
        assertSame(dg3, dg0) ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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