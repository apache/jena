# Apache Jena Fuseki 2 docker image

**Docker image:** [stain/jena-fuseki](https://registry.hub.docker.com/u/stain/jena-fuseki/)

This is a [Docker](http://docker.io/) image for running 
[Apache Jena Fuseki](http://jena.apache.org/documentation/serving_data/) 2,
which is a [SPARQL 1.1](http://www.w3.org/TR/sparql11-overview/) server with a
web interface, backed by the 
[Apache Jena TDB](http://jena.apache.org/documentation/tdb/) RDF triple store.

Feel free to contact the [jena users
list](http://jena.apache.org/help_and_support/) for any questions on using
Jena or Fuseki.


## Use

To try out this image, try:

    docker run -p 3030:3030 -it stain/jena-fuseki

The Apache Jena Fuseki should then be available at http://localhost:3030/

To expose Fuseki on a different port, modify `-p` and run `./fuseki-server --port=PORT` (see [JENA-868](https://issues.apache.org/jira/browse/JENA-868)):

    docker run -p 8080:8080 -it stain/jena-fuseki ./fuseki-server --port=8080


To load RDF graphs, you will need to log in as the `admin` user. To see the 
automatically generated admin password, see the output from above, or
use `docker logs` with the name of your container.

Note that the password is only generated on the first run, e.g. when the
volume `/fuseki` is an empty directory.

You can override the admin-password using the form 
`-e ADMIN_PASSWORD=pw123`:

    docker run -p 3030:3030 -e ADMIN_PASSWORD=pw123 -it stain/jena-fuseki

To specify Java settings such as the amount of memory to allocate for the
heap (default: 1200 MiB), set the `JVM_ARGS` environment with `-e`:

    docker run -p 3030:3030 -e JVM_ARGS=-Xmx2g -it stain/jena-fuseki


## Data persistence

Fuseki's data is stored in the Docker volume `/fuseki` within the container.
Note that unless you use `docker restart` or one of the mechanisms below, data
is lost between each run of the jena-fuseki image.

To store the data in a named Docker volume container `fuseki-data`
(recommended), create it first as:

    docker run --name fuseki-data -v /fuseki busybox

Then start fuseki using `--volumes-from`. This allows you to later upgrade the
jena-fuseki docker image without losing the data. The command below also uses
`-d` to start the container in the background.

    docker run -d --name fuseki -p 3030:3030 --volumes-from fuseki-data stain/jena-fuseki

If you want to store fuseki data in a specified location on the host (e.g. for
disk space or speed requirements), specify it using `-v`:

    docker run -d --name fuseki -p 3030:3030 -v /ssd/data/fuseki:/fuseki -it stain/jena-fuseki

Note that the `/fuseki` volume must only be accessed from a single Fuseki 
container at a time.    

To check the logs for the container you gave `--name fuseki`, use:

    docker logs fuseki

To stop the named container, use:    

    docker stop fuseki

.. or press Ctrl-C if you started the container with `-it`.    

To restart a named container (it will remember the volume and port config)

    docker restart fuseki

## Upgrading Fuseki

If you want to upgrade the Fuseki container named `fuseki` which use the data
volume `fuseki-data` as recommended above, do:

    docker pull stain/jena-fuseki
    docker stop fuseki
    docker rm fuseki
    docker run -d --name fuseki -p 3030:3030 --volumes-from fuseki-data stain/jena-fuseki


## Data loading

Fuseki allows uploading of RDF datasets through the web interface and web
services, but for large datasets it is more efficient to load them directly
using the command line.

This docker image includes a shell script `load.sh` that invokes the
[tdbloader](https://jena.apache.org/documentation/tdb/commands.html)
command line tool and load datasets from the docker volume `/staging`.


For help, try:

    docker run stain/jena-fuseki ./load.sh

You will most likely want to load from a folder on the host computer by using
`-v`, and into a data volume that you can then use with the regular fuseki.

Before data loading, you must either stop the Fuseki container, or
load the data into a brand new dataset that Fuseki doesn't know about yet. 
To stop the docker container you named `fuseki`:

    docker stop fuseki

The example below assume you want to populate the Fuseki dataset 'chembl19'
from the Docker data volume `fuseki-data` (see above) by loading the two files
`cco.ttl.gz` and `void.ttl.gz` from `/home/stain/ops/chembl19` on the host
computer:

    docker run --volumes-from fuseki-data -v /home/stain/ops/chembl19:/staging \
       stain/jena-fuseki ./load.sh chembl19 cco.ttl.gz void.ttl.gz

**Tip:** You might find it benefitial to run data loading from the data staging
directory in order to use tab-completion etc. without exposing the path on the
host. The `./load.sh` will expand patterns like `*.ttl` - you might have to 
use single quotes (e.g. `'*.ttl'`) on the host to avoid them being expanded
locally.

If you don't specify any filenames to `load.sh`, all filenames directly under
`/staging` that match these GLOB patterns will be loaded:

    *.rdf *.rdf.gz *.ttl *.ttl.gz *.owl *.owl.gz *.nt *.nt.gz *.nquads *.nquads.gz

`load.sh` populates the default graph. To populate named
graphs, see the `tdbloader` section below.

**NOTE**: If you load data into a brand new `/fuseki` volume, a new random
admin password will be set before you have started Fuseki. 
You can either check the output of the data loading, or later override the
password using `-e ADMIN_PASSWORD=pw123`.


## Recognizing the dataset in Fuseki

If you loaded into an existing dataset, Fuseki should find the data after
(re)starting with the same data volume (see [Data
persistence](#Data_persistence) above):

    docker restart fuseki

If you created a brand new dataset, then in Fuseki go to *Manage datasets*,
click **Add new dataset**, tick **Persistent** and provide the database name
exactly as provided to `load.sh`, e.g. `chembl19`. 

Now go to *Dataset*, select from the dropdown menu, and try out *Info* and *Query*.

**Tip**: It is possible to load a new dataset into the volume of a 
running Fuseki server, as long as you don't "create" it in Fuseki before
`load.sh` has finished.


## Loading with tdbloader

If you have more advanced requirements, like loading multiple datasets or named graphs, you can 
use [tdbloader](https://jena.apache.org/documentation/tdb/commands.html) directly together with 
a [TDB assembler file](https://jena.apache.org/documentation/tdb/assembler.html).

Note that Fuseki TDB datasets are sub-folders in `/fuseki/databases/`.

You will need to provide the assembler file on a mounted Docker volume together with the
data:

    docker run --volumes-from fuseki-data -v /home/stain/data:/staging stain/jena-fuseki \
      ./tdbloader --desc=/staging/tdb.ttl

Remember to use the Docker container's data volume paths within the assembler
file, e.g. `/staging/dataset.ttl` instead of `/home/stain/data/dataset.ttl`.


## Customizing Fuseki configuration

If you need to modify Fuseki's configuration further, you can use the equivalent of:

    docker run --volumes-from fuseki-data -it ubuntu bash

and inspect `/fuseki` with the shell. Remember to restart fuseki afterwards:

    docker restart fuseki

