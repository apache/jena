Jena Integration Tests
======================

This module provides testing of components where testing depends on
infrastructure not avilable, due to dependency relationships, to the
module.

For example, tests of RDF Connection for remote access that use embedded
Fuseki.  RDF Connection code does not itself depend on embedded Fuseki
and exists earlier in the build order than Fuseki.

