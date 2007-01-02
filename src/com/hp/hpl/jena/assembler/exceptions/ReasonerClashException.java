/*
 	(c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: ReasonerClashException.java,v 1.2 2007-01-02 11:49:34 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.exceptions;

import com.hp.hpl.jena.rdf.model.Resource;

public class ReasonerClashException extends AssemblerException
    {
    public ReasonerClashException( Resource root )
        { super( root, "root has both reasonerFactory and reasonerURL properties" ); }
    }

