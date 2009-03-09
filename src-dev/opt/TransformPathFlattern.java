/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package opt;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpPath;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.path.PathCompiler;

public class TransformPathFlattern extends TransformCopy
{
    // Need previous BGP?
    private PathCompiler pathCompiler ;

    public TransformPathFlattern(PathCompiler pathCompiler)
    {
        this.pathCompiler = pathCompiler ;
    }
    
    @Override
    public Op transform(OpPath opPath)
    {
//        if ( true )
            return super.transform(opPath) ;
        
//        Path path = opPath.getTriplePath().getPath() ;
//        
//        // Step 1 : flatten down to triples where possible.
//        // Fix up - need reduce for paths, not syntactic path blocks.
//        pattern = pathCompiler.reduce(pattern) ;
//
//        //Step 2 : gather into OpBGP(BasicPatterns) or OpPath
//        BasicPattern bp = null ;
//        Op op = null ;
//
//        for ( Iterator<TriplePath> iter = pattern.iterator() ; iter.hasNext() ; )
//        {
//            TriplePath obj = iter.next();
//            if ( obj.isTriple() )
//            {
//                if ( bp == null )
//                    bp = new BasicPattern() ;
//                bp.add(obj.asTriple()) ;
//                continue ;
//            }
//            // Path form.
//            op = flush(bp, op) ;
//            bp = null ;
//
//            TriplePath tp = obj ;
//            OpPath opPath2 = new OpPath(tp) ;
//            op = OpSequence.create(op, opPath2) ;
//            continue ;
//        }
//
//        // End.  Finish off any outstanding BGP.
//        op = flush(bp, op) ;
//
//        return op ;
    }
    
    private Op flush(BasicPattern bp, Op op)
    {
        if ( bp == null || bp.isEmpty() )
            return op ;
        
        //Op op2 = PropertyFunctionGenerator.compile(bp, context) ;
        //op = OpSequence.create(op, op2) ;
        return op ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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