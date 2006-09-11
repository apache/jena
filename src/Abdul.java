import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;
/*
 (c) Copyright 2006 Hewlett-Packard Development Company, LP
 All rights reserved - see end of file.
 $Id: Abdul.java,v 1.1 2006-09-11 15:22:57 chris-dollin Exp $
 */

public class Abdul extends ModelTestBase
    {

    public Abdul( String name )
        {
        super( name );
        }

    public void testLoad()
        {
        Model m = FileManager.get().loadModel( "/home/kers/projects/jena2/owlWithNoIndividuals.owl" );
        Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
        Resource CarriageWithAirLeak = resource( m, ":CarriageWithAirLeak" );
        System.err.println( ">> model loaded : " + CarriageWithAirLeak );
        InfModel im = ModelFactory.createInfModel( reasoner, m );
        long begin = System.currentTimeMillis();
        for (ResIterator it = m.listSubjects(); it.hasNext();) 
            {
            Resource subject = it.nextResource();
            String isOrIsNot = im.contains( subject, RDF.type, CarriageWithAirLeak ) ? "is" : "is not";
            System.err.println( ">> " + subject.getLocalName() + isOrIsNot + " a  CarriageWithAirLeak" );
            System.err.println( "   [accumulated time: " + (System.currentTimeMillis() - begin) + "ms" );
            }
//        for (Iterator it = im.listStatements( null, RDF.type, CarriageWithAirLeak ); it.hasNext();) 
//            {
//            System.err.println( ">> " + it.next() );
//            }
//        long time = System.currentTimeMillis() - begin;
//        System.err.println( ">> done in " + time + "ms" );
        }
    }


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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