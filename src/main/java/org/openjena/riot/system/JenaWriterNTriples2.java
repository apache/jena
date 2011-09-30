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

package org.openjena.riot.system;

import java.io.Writer ;
import java.util.Iterator ;

import org.openjena.riot.out.NodeToLabel ;
import org.openjena.riot.out.OutputLangUtils ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

public class JenaWriterNTriples2 extends JenaWriterBase
{
    // See also SinkTripleOutput.
    // This is only here because it needs to cover the "Writer" path from JenaWriterBase < RDFWriter
    @Override
    protected void write(Graph graph, Writer out, String base)
    {
        NodeToLabel labels = SyntaxLabels.createNodeToLabel() ;
        Iterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext() ; )
        {
            Triple triple = iter.next() ;
            OutputLangUtils.output(out, triple, null, labels) ;
        }
    }
}
