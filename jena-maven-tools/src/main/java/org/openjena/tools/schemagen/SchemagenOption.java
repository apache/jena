package org.openjena.tools.schemagen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jena.schemagen.SchemagenOptions;

/**
 * Annotation to designate linkages between Maven configured properties
 * in Source and options in SchemagenOptions.  
 */
@Target(ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SchemagenOption {
    SchemagenOptions.OPT opt();
}
