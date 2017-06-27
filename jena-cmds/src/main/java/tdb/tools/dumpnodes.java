/*
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

package tdb.tools ;

import java.io.OutputStream ;
import java.nio.ByteBuffer ;
import java.util.Iterator ;
import java.util.function.Function ;

import arq.cmdline.CmdARQ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Node_Literal ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.apache.jena.tdb.base.file.FileFactory ;
import org.apache.jena.tdb.base.file.FileSet ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.base.objectfile.ObjectFile ;
import org.apache.jena.tdb.lib.NodeLib ;
import org.apache.jena.tdb.setup.StoreParams ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.sys.Names ;
import tdb.cmdline.ModLocation ;

public class dumpnodes extends CmdARQ {
    ModLocation modLocation = new ModLocation() ;

    static public void main(String... argv) {
        LogCtl.setLog4j() ;
        new dumpnodes(argv).mainRun() ;
    }

    @Override
    protected void exec() {
        Location loc = modLocation.getLocation() ;
        ObjectFile objFile = determineNodeTable(loc);
        dump(System.out, objFile) ;
    }
    
    private ObjectFile determineNodeTable(Location loc) {
        // Directly open the nodes.dat file.
        StoreParams storeParams = StoreParams.getDftStoreParams();
        FileSet fsId2Node = new FileSet(loc, storeParams.getIndexId2Node()) ;
        
        String file = fsId2Node.filename(Names.extNodeData);
        ObjectFile objFile = FileFactory.createObjectFileDisk(file);
        return objFile;
    }

    protected dumpnodes(String[] argv) {
        super(argv) ;
        super.addModule(modLocation) ;
    }

    // Taken from NodeTableNative.
    private static Iterator<Pair<NodeId, Node>> all(ObjectFile objFile)
    {
        Iterator<Pair<Long, ByteBuffer>> objs = objFile.all() ; 
        Function<Pair<Long, ByteBuffer>, Pair<NodeId, Node>> transform = item -> {
            NodeId id = NodeId.create(item.car().longValue());
            ByteBuffer bb = item.cdr();
            Node n = NodeLib.decode(bb);
            return new Pair<>(id, n);
        };
        return Iter.map(objs, transform) ;
    }
    
    public static void dump(OutputStream w, ObjectFile objFile) {
        // Better to hack the indexes?
        Iterator<Pair<NodeId, Node>> iter = all(objFile) ;
        long count = 0 ;
        try (IndentedWriter iw = new IndentedWriter(w)) {
            if ( ! iter.hasNext() ) {
                iw.println("No nodes in the .dat file");
                return ;
            }
            
            for ( ; iter.hasNext() ; ) {
                Pair<NodeId, Node> pair = iter.next() ;
                iw.print(pair.car().toString()) ;
                iw.print(" : ") ;
                // iw.print(pair.cdr()) ;
                Node n = pair.cdr() ;
                String $ = stringForNode(n) ;
                iw.print($) ;
                iw.println() ;
                count++ ;
            }
            iw.println() ;
            iw.printf("Total: " + count) ;
            iw.println() ;
            iw.flush() ;
        }
    }

    private static String stringForNode(Node n) {
        if ( n == null )
            return "<<null>>" ;

        if ( n.isBlank() )
            return "_:" + n.getBlankNodeLabel() ;

        if ( n.isLiteral() )
            return stringForLiteral((Node_Literal)n) ;

        if ( n.isURI() ) {
            String uri = n.getURI() ;
            return stringForURI(uri) ;
        }

        if ( n.isVariable() )
            return "?" + n.getName() ;

        if ( n.equals(Node.ANY) )
            return "ANY" ;

        Log.warn(FmtUtils.class, "Failed to turn a node into a string: " + n) ;
        return n.toString() ;
    }

    public static String stringForURI(String uri) {
        return "<" + uri + ">" ;
    }

    public static String stringForLiteral(Node_Literal literal) {
        String datatype = literal.getLiteralDatatypeURI() ;
        String lang = literal.getLiteralLanguage() ;
        String s = literal.getLiteralLexicalForm() ;

        StringBuilder sbuff = new StringBuilder() ;
        sbuff.append("\"") ;
        FmtUtils.stringEsc(sbuff, s, true) ;
        sbuff.append("\"") ;

        // Format the language tag
        if ( lang != null && lang.length() > 0 ) {
            sbuff.append("@") ;
            sbuff.append(lang) ;
        }

        if ( datatype != null ) {
            sbuff.append("^^") ;
            sbuff.append(stringForURI(datatype)) ;
        }

        return sbuff.toString() ;
    }

    @Override
    protected void processModulesAndArgs() {
        if ( modVersion.getVersionFlag() )
            modVersion.printVersionAndExit() ;
        if ( modLocation.getLocation() == null )
            cmdError("Location required") ;
    }

    @Override
    protected String getSummary() {
        return getCommandName() + " --loc=DIR IndexName" ;
    }

    @Override
    protected String getCommandName() {
        return Lib.className(this) ;
    }

}
