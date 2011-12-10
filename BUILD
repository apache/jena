Apache Jena - download build instructions
=========================================

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

== Source from the Apache release

The current release is available at:

http://www.apache.org/dist/incubator/jena/

Older Apache releases ara available at:

http://archive.apache.org/dist/incubator/jena/

Download all the "source-release" artifacts and unpack these together with
the ".asc" signature and checksums ".md5" and ".sha1".

Download and unzip:

http://archive.apache.org/dist/incubator/jena/jena-top-0-incubating/jena-top-0-incubating-source-release.zip
(directory will be jena-top-0-incubating/)

http://archive.apache.org/dist/incubator/jena/jena-iri-0.9.0-incubating/jena-iri-0.9.0-incubating-source-release.zip
(directory will be jena-iri-0.9.0-incubating)

http://archive.apache.org/dist/incubator/jena/jena-core-2.7.0-incubating/jena-core-2.7.0-incubating-source-release.zip
(directory will be jena-core-2.7.0-incubating)

http://archive.apache.org/dist/incubator/jena/jena-arq-2.9.0-incubating/jena-arq-2.9.0-incubating-source-release.zip
(directory will be jena-arq-2.9.0-incubating)

http://archive.apache.org/dist/incubator/jena/apache-jena-2.7.0-incubating/apache-jena-2.7.0-incubating-source-release.zip
(directory will be apache-jena-2.7.0-incubating)

== Verify

Get the public keys for the signature.  The file KEYS in the distribution
area contains the keys of the release managers.  These only need to be
imported once.  You can check the signatures at http://pgp.mit.edu/

(For Gnu PrivacyGuard)

    gpg --import < KEYS

The file with extension .asc contains the 

For the zip file of the zzz module:

    gpg --verify jena-zzz-VER-incubator-source-release.zip.asc

= Verify a checksum

The .md5 and .sha1 files contain the MD5 and SHA1 checksum of the file
respectively.  Calculate the checksum on the downloaded file 

Exampale (linux):

    md5sum jena-zzz-VER-incubator-source-release.zip

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
cd apache-jena*
mvn clean package

The download will be in apache-jena-2.7.0-incubating/target/

== Sources from subversion

alternativbely, yo can build from the develop codebase.

The Jena2 subversion area is rooted at:

https://svn.apache.org/repos/asf/incubator/jena/Jena2/

The modules are subdirectories of this area.  Check out each one, and
install it into you local maven repository:

svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/JenaTop/trunk jena-top
svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/IRI/trunk jena-iri
svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/jena/trunk jena-core
svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/ARQ/trunk jena-arq
svn co https://svn.apache.org/repos/asf/incubator/jena/Jena2/JenaZip/trunk apache-jena


-------------------------------------------------------
Please do hestiate in contacting the Jena developers:
  jena-users@incubator.apache.org
  jena-dev@incubator.apache.org
