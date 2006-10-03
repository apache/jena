/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: ReasonerClashException.java,v 1.1 2006-10-03 14:48:52 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.exceptions;

import com.hp.hpl.jena.rdf.model.Resource;

public class ReasonerClashException extends AssemblerException
    {
    public ReasonerClashException( Resource root )
        { super( root, "root has both reasonerFactory and reasonerURL properties" ); }
    }

