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

package com.hp.hpl.jena.assembler.exceptions;

import java.util.*;

import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup.Frame;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Assembler Exception class: contains code shared by all the Assembler
    exceptions.
*/
public class AssemblerException extends JenaException
    {
    protected final Resource root;
    protected List<Frame> doing = new ArrayList<>();
    
    public AssemblerException( Resource root, String string, Throwable t )
        { 
        super( string, t ); 
        this.root = root;
        }

    public AssemblerException( Resource root, String message )
        {
        super( message );
        this.root = root;
        }

    /**
        Answer the root object whose model-filling was aborted
    */
    public Resource getRoot()
        { return root; }
    
    /**
        XXX 
    */
    public AssemblerException pushDoing( AssemblerGroup.Frame frame )
        { doing.add( frame ); return this; }
    
    /**
         Answer a "nice" representation of <code>r</code>, suitable for appearance
         within an exception message.
    */
    protected static String nice( Resource r )
        {
        String rString = r.asNode().toString( r.getModel() );
        return r.isAnon() ? rString + getLabels( r ) : rString; 
        }
    
    private static String getLabels( Resource r )
        {
        Model m = r.getModel();
        String labels = "", prefix = "labels: ";
        for (StmtIterator it = r.listProperties( RDFS.label ); it.hasNext();)
            {
            String label = it.nextStatement().getObject().asNode().toString( m, true );
            labels += prefix + label;
            prefix = ", ";
            }
        return labels.equals( "" ) ? getIncomingProperty( r ) : " [" + labels + "]";
        }

    private static String getIncomingProperty( Resource r )
        {
        String incomings = "", prefix = "";
        StmtIterator it = r.getModel().listStatements( null, null, r );
        while (it.hasNext())
            {
            Statement s = it.nextStatement();
            incomings += prefix + nice( s.getPredicate() ) + " of " + nice( s.getSubject() );
            prefix = ", ";
            }
        return incomings.equals( "" ) ? "" : " [" + incomings + "]";
        }

    protected static String nice( RDFNode r )
        { return r.isLiteral() ? r.asNode().toString(): nice( (Resource) r ); }

    public List<Frame> getDoing()
        { return doing; }
    
    @Override
    public String toString()
        { 
        String parent = super.toString();
        String frame = frameStrings();
        return frame.equals(  ""  ) ? parent : parent + "\n  doing:\n" + frame; 
        }
    
    protected String frameStrings()
        {
        StringBuilder result = new StringBuilder();
            for ( Frame aDoing : doing )
            {
                result.append( "    " ).append( aDoing.toString() ).append( "\n" );
            }
        return result.toString();
        }
    }
