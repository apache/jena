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

package com.hp.hpl.jena.graph.query.regexptrees;

import java.util.List;

/**
     A sequence of regular expressions. The only access to the constructor is
     through the <code>create</code> method, which does not construct unit
     sequences.
     
     @author hedgehog
*/
public class Sequence extends MultiOperandTree
    {
    protected Sequence( RegexpTree [] operands )
        { super( operands ); }
    
    public static RegexpTree create( List<? extends RegexpTree> operands )
        {
        if (operands.size() == 0)
            return NON;
        else if (operands.size() == 1) 
            return operands.get(0);
        else
            return new Sequence( operands.toArray( new RegexpTree [operands.size()] ));
        }
    
    @Override
    public boolean equals( Object other )
        {
        return other instanceof Sequence && sameOperands( (MultiOperandTree) other );
        }

    @Override
    public int hashCode()
        { return hashCode( 0 ); }
    
    @Override
    public String toString()
        { return toString( "seq" ); }

    }
