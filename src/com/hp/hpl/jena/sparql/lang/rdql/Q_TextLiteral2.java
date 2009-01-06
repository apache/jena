/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

/* Text literal but without the possibility of @ or ^^ */

package com.hp.hpl.jena.sparql.lang.rdql ;

import com.hp.hpl.jena.query.Query;

public class Q_TextLiteral2 extends ParsedLiteral {

    String seen = null ;

  Q_TextLiteral2(int id) {
    super(id);
  }

  Q_TextLiteral2(RDQLParser p, int id) {
    super(p, id);
  }

  void set(String s)
  {
    seen = s ; 
    // Remove string quotes
    s = s.substring(1,s.length()-1) ;
    super._setString(unescape(s,'\\')) ;
  }
  
    @Override
    public void jjtClose()
    {
        // Unlike TextLiteral, this has no optional @ or ^^
    }
  
    @Override
    public void postParse2(Query query)
    {
        super.postParse2(query) ;
        com.hp.hpl.jena.graph.Node n = null ;
        n = com.hp.hpl.jena.graph.Node.createLiteral(super.getString(), null, null) ;
        super._setNode(n) ; 
    }
  
    @Override
    public String asQuotedString()
    {
        return seen ;
        //return super.asQuotedString() ;
    }
  
  // Utility to remove escapes
  static String unescape(String s, char escape)
  {
      for ( int i = 0 ; i < s.length() ; i++ )
      {
          if ( s.charAt(i) != escape )
              continue ;
          // Escape
          if ( i >= s.length()-1 )
              // At end - skip.
              continue ;
          char ch2 = s.charAt(i+1) ;
          if ( ch2 == 'n' ) ch2 = '\n' ;
          if ( ch2 == 't' ) ch2 = '\t' ;
          if ( ch2 == 'r' ) ch2 = '\r' ;
          if ( ch2 == 'b' ) ch2 = '\b' ;
          // Unicode \ u XXXX 
//          if ( ch2 == 'u' )
//          {
//             
//          }
          
          
          // Other escapes are just the literal character (e.g. ' ")
          s = s.substring(0,i)+ch2+s.substring(i+2) ;
          // s got shorter so i now points to char after escape and i+1
          // is after ch2.  No fix up needed.
      }
      return s ;
  }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
