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

/**
 MultiOperandTree
 @author kers
 */
public abstract class MultiOperandTree extends RegexpTree
    {
    protected RegexpTree [] operands;
    
    protected MultiOperandTree( RegexpTree [] operands )
        { this.operands = operands; }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object other )
        {
        // TODO Auto-generated method stub
        return false;
        }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
        {
        // TODO Auto-generated method stub
        return 0;
        }

    public String toString( String kind )
        { StringBuffer result = new StringBuffer();
        result.append( "<" );
        result.append( kind );
        for (int i = 0; i < operands.length; i += 1) result.append( " " ).append( operands[i] );
        result.append( ">" );
        return result.toString(); }

    protected boolean sameOperands( MultiOperandTree other )
        {
        if (other.operands.length == operands.length)
            {
            for (int i = 0; i < operands.length; i += 1)
                if (operands[i].equals( other.operands[i] ) == false) return false;
            return true;
            }
        else
            return false;
        }

    public int hashCode( int base )
        {
        int result = base;
        for (int i = 0; i < operands.length; i += 1) 
            result = (result << 1) ^ operands[i].hashCode();
        return result;
        }
    }
