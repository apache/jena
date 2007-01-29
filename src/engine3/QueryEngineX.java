/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Element;
import com.hp.hpl.jena.query.engine.Plan;
import com.hp.hpl.jena.query.engine.PlanBase;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine2.QueryEngineRef;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.serializer.SerializationContext;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.query.util.IndentedWriter;

public class QueryEngineX extends QueryEngineRef
{

    public QueryEngineX(Query query, Context context)
    { super(query, context) ; }

    public QueryEngineX(Query query)
    { super(query) ; }
    
    //@Override
    protected Plan queryToPlan(Query query, Modifiers mods, Element pattern)
    {
        Op op = getOp() ;
        final QueryIterator qIter = OpCompiler.compile(op, getExecContext()) ;
        return new PlanOp(op, qIter) ;
    }
    
    static class PlanOp extends PlanBase
    {
        private QueryIterator qIter ;
        private Op op ;
        PlanOp(Op op, QueryIterator qIter) { this.op = op ; this.qIter = qIter ; }

        protected QueryIterator iteratorOnce()
        { return qIter ; }

        //@Override
        public void output(IndentedWriter out, SerializationContext sCxt) // or output(IndentedWriter).
        {
            out.println(Plan.startMarker+":EngineX") ;
            out.incIndent() ;
            op.output(out, sCxt) ;
            out.decIndent() ;
            out.println(Plan.finishMarker) ;
        }
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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