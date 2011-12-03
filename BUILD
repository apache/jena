Apache Jena
===========

This is how to build the Jena download from source.

The Jena download contains the IRI, API and SPARQL modules as jar files,
together with all the necssary dependencies.  Put all the jars in lib/ on
the java classpath.

The build of the Jena download collects all the dependencies together. It
assumes that dependences are already built so if you want to build from
scratc, you need to build the dependencies first.  This can be done from
the Apache "source-release" artifacts or from subversion.

== Modules

JenaTop - the parent POM
IRI     - The IRI library
jena    - the main RDF and OWL APIs, and memory storage subsystem.
ARQ     - SPARQL query engine


== Sources from subversion

The Jena2 subversion area is rooted at:

https://svn.apache.org/repos/asf/incubator/jena/Jena2/

The modules are subdirectories of this area.  Check out each one, and
install it into you local maven repository:

svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/JenaTop/trunk jena-top
svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/IRI/trunk jena-iri
svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/jena/trunk jena-core
svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/ARQ/trunk jena-arq
svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/JenaZip/trunk apache-jena


== Source from the Apache release

The current release is available at:

http://www.apache.org/dist/incubator/jena/

Older Apache releases ara available at:

http://archive.apache.org/dist/incubator/jena/

Download all the "source-release" artifacts and unpack these.


== Build

(if building from a specific set of source-release files, directory names
will have version numbers in them).

# Build modules.
for module in jena-top* jena-iri* jena-core* jena-arq* apache-jena*
do
  cd $module
  mvn clean install
  cd ..
  done

# Build download.
cd jena-zip*
mvn clean package

The download will be in jena-zip*/target/

-------------------------------------------------------
Please do hestiate in contacting the Jena developers:
  jena-users@incubator.apache.org
  jena-dev@incubator.apache.org
