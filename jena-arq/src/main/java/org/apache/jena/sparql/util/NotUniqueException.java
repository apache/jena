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

package org.apache.jena.sparql.util;

import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.shared.JenaException ;

public class NotUniqueException extends JenaException
{
    protected final Property property;

    public NotUniqueException( Resource root, Property property )
    {
        super( makeMessage( root, property ) );
        this.property = property; 
    }

    private static String makeMessage( Resource root, Property property )
    {
        return 
        "The object " + FmtUtils.stringForResource(root)
        + " has multiple values for the unique property " + 
        FmtUtils.stringForResource( property )
        ;
    }

    public Property getProperty()
    { return property; }
}
