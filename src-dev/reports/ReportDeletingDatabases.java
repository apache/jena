/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package reports;

import java.io.File ;

import org.openjena.atlas.lib.FileOps ;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class ReportDeletingDatabases
{
    public static void main(String ...argv)
    {
        String DB = "TDBTest7";
        FileOps.clearDirectory(DB) ;
        
        SystemTDB.setFileMode(FileMode.mapped);
        
        if ( false )
        {
            // Create new DB (assuming it's empty now)
            Dataset ds = TDBFactory.createDataset(DB);
            PrefixMapping pm = ds.getDefaultModel() ;
            pm.setNsPrefix("test", "http://test");
            ds.close();
        }

        if ( true )
        {
            // Create new DB (assuming it's empty now)
            Graph g = TDBFactory.createGraph(DB);
            PrefixMapping pm = g.getPrefixMapping() ;
            pm.setNsPrefix("test", "http://test");
            g.close();
            ((GraphTDB)g).getDataset().close() ;
        }
    
        TDB.closedown();
    
        File file = new File(DB + "/prefixIdx.dat");
        System.out.println("File exists " + file.exists());
        //FileOps.clearDirectory(DB) ;
        file.delete();
        System.out.println("File exists " + file.exists());
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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