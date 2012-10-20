Apache Jena - download build instructions
=========================================

This is how to build the Jena download from source.

== Modules

All modules are releases as maven artifacts.

jena-parent

jena-iri
jena-core
jena-arq
jena-tdb
  These are assembled into the apache-jena download.

jena-fuseki
  This is a separate downloadable 

== Source from the Apache release

The current release is available at:

http://www.apache.org/dist/jena/

Older Apache releases are available at:

http://archive.apache.org/dist/jena/

Download the "source-release" artifact and unpack it together with
the ".asc" signature and checksums ".md5" and ".sha1".

== Verify

Get the public keys for the signature.  The file KEYS in the distribution
area contains the keys of the release managers.  These only need to be
imported once.  You can check the signatures at http://pgp.mit.edu/

(For Gnu PrivacyGuard)

    gpg --import < KEYS

The file with extension .asc contains the 

For the zip file of the zzz module:

    gpg --verify jena-VER-source-release.zip.asc

= Verify a checksum

The .md5 and .sha1 files contain the MD5 and SHA1 checksum of the file
respectively.  Calculate the checksum on the downloaded file 

Example (linux):

    md5sum jena-VER-source-release.zip

== Build

(if building from a specific set of source-release files, directory names
will have version numbers in them).

# Build modules.
mvn clean package

The downloads will be in apache-jena/target/ and jena-fuseki/target.

== Sources from subversion

Alternatively, you can build from the development codebase.

The Jena subversion area is rooted at:

https://svn.apache.org/repos/asf/jena/trunk/


-------------------------------------------------------
If you have any questions, please do not hesitate in contacting the Jena project:
  users@jena.apache.org
  dev@jena.apache.org
