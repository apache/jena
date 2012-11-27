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

package arq;

import org.apache.jena.atlas.io.IndentedWriter ;
import arq.cmd.TerminationException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ_SSE ;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemWriter ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class sse extends CmdARQ_SSE
{
    protected final ArgDecl numberDecl      = new ArgDecl(ArgDecl.HasValue, "num", "number") ;
    protected final ArgDecl noPrintDecl     = new ArgDecl(ArgDecl.NoValue, "n") ;
    protected final ArgDecl noResolveDecl   = new ArgDecl(ArgDecl.NoValue, "raw") ;

    private boolean         print       = true ;
    private boolean         structural  = true ;
    private boolean         lineNumbers = false ;

    public static void main (String... argv)
    {
        new sse(argv).mainRun() ;
    }

    public sse(String[] argv)
    {
        super(argv) ;
        super.add(noPrintDecl,      "-n",               "Don't print the expression") ;
        super.add(numberDecl,       "--num [on|off]",   "Numbers") ;
        super.add(noResolveDecl,    "--raw", "Don't handle base or prefix names specially") ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        print = !contains(noPrintDecl) ;
        if ( contains(numberDecl) )
            lineNumbers = getValue(numberDecl).equalsIgnoreCase("on") ;
        
        if ( contains(noResolveDecl) )
            SSE.setUseResolver(false) ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }

    @Override
    protected String getSummary() { return getCommandName() ; }

    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    //static final String divider = "" ;

    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }

    @Override
    protected void exec(Item item)
    {
        if ( ! print )
            return ;
        
        if ( item == null )
        {
            System.err.println("No expression") ;
            throw new TerminationException(9) ;
        }
        divider() ;
        IndentedWriter out = new IndentedWriter(System.out, lineNumbers) ;
        
        // Need to check if used.
        //PrefixMapping pmap = SSE.getDefaultPrefixMapWrite() ;
        PrefixMapping pmap = null ;
        SerializationContext sCxt = new SerializationContext(pmap) ;
        ItemWriter.write(out, item, sCxt) ;
        //item.output(out) ;
        out.ensureStartOfLine() ;
        out.flush();
    }
}
