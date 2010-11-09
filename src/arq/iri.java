/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.util.Iterator ;

import org.openjena.riot.system.IRIResolver ;

import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.Violation ;

public class iri
{

    public static void main(String[] args)
    {
        IRIFactory iriFactory = IRIFactory.iriImplementation() ;
        iriFactory = IRIResolver.iriFactory ;
        
        boolean first = true ;
        for ( String iriStr : args )
        {
            if ( ! first )
                System.out.println() ;
            first = false ;
            
            IRI iri = iriFactory.create(iriStr) ;
            System.out.println(iriStr + " ==> "+iri) ;
            if ( iri.isRelative() )
                System.out.println("Relative: "+iri.isRelative()) ;

            Iterator<Violation> vIter = iri.violations(true) ;
            for ( ; vIter.hasNext() ; )
            {
                System.out.println(vIter.next().getShortMessage()) ;
            }
        }
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
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