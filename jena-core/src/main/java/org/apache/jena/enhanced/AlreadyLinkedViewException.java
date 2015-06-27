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

package org.apache.jena.enhanced;

import org.apache.jena.shared.JenaException ;

/**
    Exception to throw when adding a view to a Polymorphic discovers that
    the view to add is already on a ring.
*/
public class AlreadyLinkedViewException extends JenaException
    {
    /**
        The polymorphic <code>other</code> has already been linked into
        a sibling ring and hence cannot be linked into a different one.
    */
    public AlreadyLinkedViewException( Polymorphic<?> other )
        { super( other.toString() ); }
    }
