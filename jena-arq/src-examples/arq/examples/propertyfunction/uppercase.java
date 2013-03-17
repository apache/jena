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

package arq.examples.propertyfunction;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.pfunction.PFuncAssignToObject ;

/** Example property function that uppercases the lexical form of a literal.
 *  The subject must be bound, the object not bound. {@link localname} shows a
 *  property function that handles more cases of subject or object bing bound or unbound.
 *  <pre>
 *     PREFIX ext: <java:arq.examples.propertyfunction.>
 *  </pre>   
 *  <pre>
 *     { ?string ext:uppercase ?uppercase }
 *  </pre>
 *  <pre>
 *     { "lower case" ext:uppercase ?uppercase }
 *  </pre>
 *  Else fails to match.
 */

public class uppercase extends PFuncAssignToObject
{
    @Override
    public Node calc(Node node)
    {
        if ( ! node.isLiteral() ) 
            return null ;
        String str = node.getLiteralLexicalForm().toUpperCase() ;
        return NodeFactory.createLiteral(str) ;
    }
}
