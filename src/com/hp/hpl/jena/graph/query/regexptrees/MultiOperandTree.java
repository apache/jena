/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: MultiOperandTree.java,v 1.2 2005-02-21 11:52:29 andy_seaborne Exp $
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
    public boolean equals( Object other )
        {
        // TODO Auto-generated method stub
        return false;
        }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
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


/*
(c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/