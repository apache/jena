#!/bin/bash

export DOC=doc/assembler

rj -Dexamples=$DOC/examples.n3 -Dvocab=vocabularies/assembler.n3 jena.Stitch < $DOC/assembler-howto.phtml > $DOC/assembler-howto.html

