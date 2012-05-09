/**
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

package tdb;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openjena.atlas.lib.FileOps;
import org.openjena.atlas.logging.Log;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDBLoader;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

@RunWith(Parameterized.class)
public class TestTDBLoader3 {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "src/test/resources/tdbloader3/test-01", "target/tdbloader3-output-test-01"}, 
                { "src/test/resources/tdbloader3/test-02", "target/tdbloader3-output-test-02" }, 
                { "src/test/resources/tdbloader3/test-03", "target/tdbloader3-output-test-03" },
                { "src/test/resources/tdbloader3/test-04", "target/tdbloader3-output-test-04" },
        });
    }

    private String input ;
    private String output ;
    
    public TestTDBLoader3 ( String input, String output ) {
        this.input = input ;
        this.output = output ;
    }

    @Before public void setup() {
    	// Disable any log during tests...
    	Log.disable(TDB.logLoaderName);
    	Log.disable(TDB.logInfoName);
    	Log.disable(TDB.logLoader.getClass());
    	Log.disable(tdbloader3.class);
    	Log.disable(TDBLoader.class);
    	
        if ( FileOps.exists(output) ) {
            FileOps.clearDirectory(output) ;            
        } else {
            FileOps.ensureDir(output);          
        }
    }
    
    @Test public void test() throws Exception { 
        run (input, output); 
    }
    
    private void run ( String input, String output ) throws Exception {
        List<String> urls = new ArrayList<String>();
        for (File file : new File(input).listFiles()) {
            if (file.isFile()) {
                urls.add(file.getAbsolutePath());
            }
        }
        
        DatasetGraphTransaction dsgtMem = (DatasetGraphTransaction)TDBFactory.createDatasetGraph();
        TDBLoader.load(dsgtMem.getDatasetGraphToQuery(), urls, false);
        
        String[] args;
        File path = new File(input);
        if ( path.isDirectory() ) {
            ArrayList<String> arguments = new ArrayList<String>();
            arguments.add("--loc");
            arguments.add(output);
            ArrayList<String> datafiles = new ArrayList<String>();
            File files[] = path.listFiles();
            for (File file : files) {
                if ( file.isFile() ) 
                    datafiles.add(file.getAbsolutePath());
            }
            arguments.addAll(datafiles);
            args = arguments.toArray(new String[]{}) ;
        } else {
            args = new String[] { "--loc", output, input };
        }

        tdbloader3.main(args);

        Location location = new Location(output);
        DatasetGraph dsgDisk = TDBFactory.createDatasetGraph(location);
        
        assertTrue ( dump(dsgtMem, dsgDisk), isomorphic ( dsgtMem, dsgDisk ) );
    }
    
    private boolean isomorphic(DatasetGraph dsgMem, DatasetGraph dsgDisk) {
        if (!dsgMem.getDefaultGraph().isIsomorphicWith(dsgDisk.getDefaultGraph()))
            return false;
        Iterator<Node> graphsMem = dsgMem.listGraphNodes();
        Iterator<Node> graphsDisk = dsgDisk.listGraphNodes();
        
        Set<Node> seen = new HashSet<Node>();

        while (graphsMem.hasNext()) {
            Node graphNode = graphsMem.next();
            if (dsgDisk.getGraph(graphNode) == null) return false;
            if (!dsgMem.getGraph(graphNode).isIsomorphicWith(dsgDisk.getGraph(graphNode))) return false;
            seen.add(graphNode);
        }

        while (graphsDisk.hasNext()) {
            Node graphNode = graphsDisk.next();
            if (!seen.contains(graphNode)) {
                if (dsgMem.getGraph(graphNode) == null) return false;
                if (!dsgMem.getGraph(graphNode).isIsomorphicWith(dsgDisk.getGraph(graphNode))) return false;
            }
        }

        return true;
    }

    private String dump(DatasetGraph dsgMem, DatasetGraph dsgDisk) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n");
        
        if (!dsgMem.getDefaultGraph().isIsomorphicWith(dsgDisk.getDefaultGraph())) {
            sb.append("Default graphs are not isomorphic [FAIL]\n");
            sb.append("    First:\n");
            dump(sb, dsgMem.getDefaultGraph());
            sb.append("    Second:\n");
            dump(sb, dsgDisk.getDefaultGraph());
        } else {
            sb.append("Default graphs are isomorphic [OK]\n");
        }
            
        return sb.toString();
    }
    
    private static void dump (StringBuffer sb, Graph graph) {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY);
        while ( iter.hasNext() ) {
            Triple triple = iter.next();
            sb.append(triple).append("\n");
        }
    }
    
}
