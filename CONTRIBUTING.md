Apache Jena : Contributing
==========================

The project welcomes contributions, large and small, from anyone.

The mailing list for project-wide discussions is dev@jena.apache.org and all
development work happens in public, using that list.

The processes described here are guidelines, rather than absolute
requirements.


## Contributions

Contributions can be made by:

* Github pull requests (preferred)
* JIRA and patches
* Other

Contributions should include:

* Tests
* Documentation as needed

Documentation is kept and published via a git repository:

   https://github.com/apache/jena-site/

## Workflow

### JIRA and Github issues

The project uses a JIRA and also githb issues to track work.  Please create one
of a JIRA issue or a github issue so that we can track a contribution.

JIRA:

    https://issues.apache.org/jira/browse/JENA

Github issue:

    https://github.com/apache/jena/issues

### Github

It is useful to create a JIRA then use the JIRA number (e.g. JENA-9999 or GH-9999)
in the Pull Request title. This activates the automated mirroring of
discussions onto the project developers mailing list.

To make a contribution:

* On github, fork https://github.com/apache/jena into you github account.
* Create a branch in your fork for the contribution.
* Make your changes. Include the Apache source header at the top of each file.
* Generate a pull request via github. Further changes to your branch will automatically
  show up in the pull request

The project development mailing list is automatically notified of new pull
requests and JIRA is also automatically updated if the JIRA id is in the pull request
title.

### Discussion and Merging

A project committer will review the contribution and coordinate any project-wide discussion
needed. Review and discussion of the pull request itself takes place on
github.

The committer review guide:

    https://jena.apache.org/getting_involved/reviewing_contributions.html

### Patches

An alternative is to upload a patch/diff to JIRA.

### Code

Code style is about making the code clear for the next person
who looks at the code.

The project prefers code to be formatted in the common java style with
sensible deviation for short forms.

The project does not enforce a particular style but asks for:

* Kernighan and Ritchie style "Egyptian brackets" braces.
* Spaces for indentation
* No `@author` tags.
* One statement per line
* Indent level 4 for Java
* Indent level 2 for XML

See, for illustration:
https://google.github.io/styleguide/javaguide.html#s4-formatting

The codebase has a long history - not all of it follows this style.

The code should have no warnings, in particular, use `@Override` and types
for generics, and don't declared checked exceptions that are not used.
Use `@SuppressWarnings("unused")` as necessary.

Please don't mix reformatting and functional changes; it makes it harder
to review.

### Legal

When you contribute, you affirm that the contribution is your original work and
that you license the work to the Apache Software Foundation. You agree to license the
material under the terms and conditions of the 
[Contributor's Agreement](https://www.apache.org/licenses/contributor-agreements.html).

You, as an individual, must be entitled to make the contribution to the
project. If the contribution is part of your employment, please arrange
this before making the contribution.

For a large contribution, the project may ask for a specific Software
Grant from the contributor.

If in doubt, or if you have any questions, ask on the dev@jena.apache.org
mailing list. Legal issues are easier to deal with if done before
contributing, rather than after.

The project cannot accept contributions with unclear ownership nor
contributions containing work by other people without a clear agreement
from those people.
