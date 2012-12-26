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

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.*;

/**
    The NodeVisitor interface is used by Node::visitWith so that an application
    can have type-dispatch on the class of a Node. 	
*/
public interface NodeVisitor
    {
    Object visitAny( Node_ANY it );
    Object visitBlank( Node_Blank it, AnonId id );
    Object visitLiteral( Node_Literal it, LiteralLabel lit );
    Object visitURI( Node_URI it, String uri );
    Object visitVariable( Node_Variable it, String name );
    }
