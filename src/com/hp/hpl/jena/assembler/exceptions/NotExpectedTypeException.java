/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: NotExpectedTypeException.java,v 1.1 2007-05-10 14:01:51 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.exceptions;

import com.hp.hpl.jena.rdf.model.Resource;

/**
    Exception to throw when some (dynamically loaded) class isn't of the
    required type.
    
    @author kers
*/
public class NotExpectedTypeException extends AssemblerException
    {
    protected final Class expectedType;
    protected final Class actualType;
    
    public NotExpectedTypeException( Resource root, Class expectedType, Class actualType )
        {
        super( root, "expected class " + expectedType.getName() + ", but had class " + actualType.getName() );
        this.expectedType = expectedType;
        this.actualType = actualType;
        }

    public Class getExpectedType()
        { return expectedType; }

    public Class getActualType()
        { return actualType; }
    }

