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

package org.apache.jena.riot.checker;

import com.hp.hpl.jena.graph.NodeVisitor ;
import com.hp.hpl.jena.graph.Node_ANY ;
import com.hp.hpl.jena.graph.Node_Blank ;
import com.hp.hpl.jena.graph.Node_Literal ;
import com.hp.hpl.jena.graph.Node_URI ;
import com.hp.hpl.jena.graph.Node_Variable ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.rdf.model.AnonId ;

public class CheckerVisitor implements NodeVisitor
{
    @Override
    public Object visitAny(Node_ANY it)
    {
        return null ;
    }

    @Override
    public Object visitBlank(Node_Blank it, AnonId id)
    {
        return null ;
    }

    @Override
    public Object visitLiteral(Node_Literal it, LiteralLabel lit)
    {
        return null ;
    }

    @Override
    public Object visitURI(Node_URI it, String uri)
    {
        return null ;
    }

    @Override
    public Object visitVariable(Node_Variable it, String name)
    {
        return null ;
    }

}
