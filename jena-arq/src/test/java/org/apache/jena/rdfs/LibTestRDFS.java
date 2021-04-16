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

package org.apache.jena.rdfs;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfs.engine.ConstRDFS;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileUtils;

public class LibTestRDFS {

    /**
     * Remove triples with a predicate which is RDFS schema vocabulary.
     */
    public static List<Triple> removeRDFS(List<Triple> x) {
        return StreamOps.toList(x.stream().filter(ConstRDFS.filterNotRDFS));
    }

    /** Create a Jena-rules backed graph */
    static Graph createRulesGraph(Graph data, Graph vocab, String rulesFile) {
        try {
            String rules = FileUtils.readWholeFileAsUTF8(rulesFile);
            rules = rules.replaceAll("#[^\\n]*", "");
            Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
            return reasoner.bindSchema(vocab).bind(data);
        }
        catch (IOException ex) { IO.exception(ex) ; return null ; }
    }

    static Node node(String str) { return NodeFactory.createURI("http://example/"+str) ; }

    static List<Triple> findInGraph(Graph graph, Node s, Node p, Node o) {
        return graph.find(s,p,o).toList();
    }

    static <X> void printDiff(PrintStream out, List<X> expected, List<X> actual) {
        if ( actual.size() < 10 ) {
            out.println("Actual:");
            if ( actual.isEmpty() )
                out.println("  Empty");
            else
                actual.forEach(t->out.println("  "+t));
        }

        Pair<List<X>, List<X>> diff = ListUtils.listDiffBoth(expected, actual);
        out.println("  Diff expected\\actual");
        diff.getLeft().forEach(t->out.println("    Expected, not actual: "+t));
        out.println("  Diff actual\\expected:");
        diff.getRight().forEach(t->out.println("   Actual, not expected: "+t));
        out.println();
    }

    static List<Triple> print(PrintStream out, List<Triple> x) {
        return LibTestRDFS.print(out, "  ", x);
    }

    static List<Triple> print(PrintStream out, String leader, List<Triple> x) {
        List<Triple> list = Iter.toList(x.iterator());

        if ( list.isEmpty() )
            out.println(leader+"<empty>");
        else
            list.stream().forEach(triple -> {out.println(leader+triple) ; });
        return list;
    }

}
