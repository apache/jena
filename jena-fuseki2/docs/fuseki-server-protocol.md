# Fuseki HTTP Administration Protocol

> _These functions are available in version 2.0 and later._

This page describes the HTTP Protocol used to control an Fuseki server via its administrative interface.  See "[Fuseki Administration](admin.html)" for an overview of server administration.

* [Operations](#operations)
* [Server Information](#server-information)
* [Datasets and Services](#datasets-and-services)
    * Adding a Dataset and its Services
    * Removing a Dataset
    * Dormant and Active
* [Removing a dataset](#remove-dataset)

These are enabled by starting the server with argument `--mgt`.



All admin operations have URL paths starting `/$/` to avoid clashes
with dataset names and this prefix is reserved for the Fuseki control functions.
Further operations may be added within this naming scheme.

## Operations

| Method          |  URL pattern           | Description   |
|-----------------|------------------------|---------------|
||
| <tt>GET</tt>    | `/$/ping`              |               | 
| <tt>POST</tt>   | `/$/ping`              |               | 
| <tt>GET</tt>    | `/$/server`            |               | 
| <tt>POST</tt>   | `/$/server`            |               | 
||
| <tt>POST</tt>   | `/$/datasets/`         |               | 
| <tt>GET</tt>    | `/$/datasets/`         |               |
| <tt>DELETE</tt> | `/$/datasets/*{name}*` |               |
| <tt>GET</tt>    | `/$/datasets/*{name}*` |               |
| <tt>POST</tt>   | `/$/datasets/*{name}*?state=offline` |               |
| <tt>POST</tt>   | `/$/datasets/*{name}*?state=active`  |               |
||
| <tt>POST</tt>   | `/$/server/shutdown`   | Not yet implemented  | 
||
| <tt>GET</tt>    | `/$/stats/`            |               | 
| <tt>GET</tt>    | `/$/stats/*{name}*`    |               |
||
| <tt>POST</tt>   | `/$/backup/*{name}*`   |             |
| <tt>POST</tt>   | `/$/sleep`             |             |
||
| <tt>GET</tt>    | `/$/tasks/`            |               | 
| <tt>GET</tt>    | `/$/tasks/*{name}*`    |               |

## Ping
Pattern: `/$/ping`

The URL `/$/ping` is a guaranteed low cost point to test whether a server
is running or not.  It returns no other information other than to respond to the
request over `GET` or `POST` (to avoid any HTTP caching) with a 200 response.

Return: empty body

## Server Information 
Pattern: `/$/server`

The URL `/$/server` returns details about the server and it's current status in JSON.

_@@details of JSON format._

## Datasets and Services
Pattern: `/$/datasets/`

`/$/datasets/` is a container representing all datasets present in the server. 
`/$/datasets/*{name}*` names a specific dataset.  As a container, operations on items
in the container, via `GET`, `POST` and `DELETE`, operate on specific dataset.

### Adding a Dataset and its Services.

> _@@ May add server-managed templates_

A dataset set can be added to a running server. There ae several methods
for doing this: 

* Post the assembler file
* HTML Form upload the assembler file 
* Use a built-in template (in-memory or persistent)

All require HTTP `POST`.

Changes to the server state are carried across restarts.  

For persistent datasets, for example [TDB](/documentation/tdb),
the dataset is persists across restart.

For in-memory datasets, the dataset is rebuilt from it's description
(this may include loading data from a file) but any changes are lost.

#### Templates

A short-cut form for some common set-ups is provided by <tt>POST</tt>ing with
the following parameters (query string or HTML form):

| Parameter |                 |
|-----------|-----------------|
| `dbType`  | Either `mem` or `tdb` |
| `dbName`  | URL path name   |

The dataset name must not be already in-use.

Datasets are created in director `databases/`.

#### Assembler example

The assembler description contains data and service.  It can be sent by posting the assembler RDF graph
in any RDF format or by posting from an HTML form (the syntax must be Turtle).

The assembler file is stored by the server will be used on restart or when makign the datsset active again.

> _@@_

### Removing a Dataset

Note: `DELETE` means "gone for ever".  The dataset name and the details of its
configuration are completely deleted and can not be recovered.  

The data of a TDB dataset is not deleted.

### Active and Offline

A dataset is in one of two modes: "active", meaning it is services request over HTTP
(subject to configuration and security), or "offline", meaning the configuration and name 
is known about by the server but the dataset is not attached to the server.  When "offline",
any persistent data can be manipulated outside the server.

Datasets are initially "active".  The transition from "active" to "offline" is graceful - all outstanding requests are completed.

## Statistics
> **`/$/stats/*{name}*`**

Statistics can be obtained for each dataset or all datasets in a single response.
`/$/stats/` is  treated as a container for this information.

> _@@ stats details_
> See [Fuseki Statistics](fuseki-stats.html) for details of statistics kept by a Fuseki server.

### Backup 
Pattern: `/$/backup/*{name}*`

This operation initiates a backup and returns a JSON object with the task Id in it.

Backups are written to the server local directory 'backups' as  gzip-compressed N-Quads files.

See [Tasks](#tasks) for how to monitor a backups progress.

Return: A task is allocated a identifier (usually, a number).
```
{ "taskId" : "*{taskId}*" }
```
The task id can be used to construct a URL to get details of the task:
```
/$/tasks/*{taskId}*
```

## Tasks
Some operations cause a backgrond task to be executed, backup is an example.
The result of such operations includes a json object with teh task id and
also a `Location:` header with the URL of the task created.

The progress of the task can be monitored with HTTP GET operations:

Pattern: `/$/tasks` &ndash; All asynchronous tasks.<br/>
Pattern: `/$/tasks/*{taskId}*` &ndash; A particular task.

The URL `/$/tasks` returns a description of all running and recently tasks. A finished task can be identified by having a "finishPoint" field.

Each background task has an id.  The URL `/$/tasks/*{taskId}*` gets a description about one single task.

Details of the last few completed tasks are retained, up to a fixed number. The records will eventually be removed as later tasks complete, and the task URL will then return 404.

Pattern: `/$/tasks` ; example:
```
[ { 
    "finished" : "2014-05-28T12:52:51.860+01:00" ,
    "started" : "2014-05-28T12:52:50.859+01:00" ,
    "task" : "sleep" ,
    "taskId" : "1"
  } ,
  { 
    "finished" : "2014-05-28T12:53:24.718+01:00" ,
    "started" : "2014-05-28T12:53:14.717+01:00" ,
    "task" : "sleep" ,
    "taskId" : "2"
  }
]
```
Pattern: `/$/tasks/1` : example:
```
[ { 
    "finished" : "2014-05-28T13:54:13.608+01:00" ,
    "started" : "2014-05-28T13:54:03.607+01:00" ,
    "task" : "backup" ,
    "taskId" : "1"
  }
]
```
This is inside an array to make the format returned the same as `/$/tasks`.

