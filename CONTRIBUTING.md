# Apache Jena : Contributing

The project welcomes contributions, large and small, from anyone.

The mailing list for project-wide discussions is dev@jena.apache.org and all
development work happens in public, using GitHub isues and that mailing list.

The processes described here are guidelines, rather than fixed requirements.


## Contributions

Contributions can be made by:

* Github pull requests (preferred)
* Emailing a patch file to the developers list

Contributions should include:

* Tests
* Documentation as needed

Documentation is kept and published via a git repository:

   https://github.com/apache/jena-site/

### Making a contribution

To make a contribution:

* On github, fork https://github.com/apache/jena into your github account.
* Create a branch in your fork for the contribution (eg GH-issue#).
* Make changes that pertain only to the issue in the ticket.
* Create a pull request via GitHub. Further changes to your branch will automatically
  show up in the pull request.

#### GitHub issues

The project uses GitHub issues to track work. New work should be linked
to an open issue so that we can track contributions. This also gives the community
a chance for input and ideation before work begins.

GitHub issue:

    https://github.com/apache/jena/issues

* Bug reports should be reproduced with a minimal, complete, and runnable example
* Bug reports should explain the expected behavior and the deviation
* Longer discussions that aren't expected to link to code changes can go in 'Discussions'

#### Pull Requests

Pull requests are an opportunity to submit fixes and new features to Jena. The main
audience is project committers who will be reviewing the solution. AI assistance is
allowed. However, code reviews are handled by people and discussions between people
are preferred. Responding to reviewer comments with AI is discouraged.

* Pull request titles should be "GH-<issue#>: Concise title". This connects the mailing list.
* Pull request bodies should be to the point, describe the changes, risks, and known gaps.
* Pull request bodies should be thought out and not a dump from AI output.
* Pull requests should be made after the full test suite has been run and build tested.
* Pull request owners should be able to completely explain the solution.
* If AI was used to generate code or the PR, mention that the work was AI Assisted.
* When appropriate, add a mermaid diagram to show architecture changes.
* Refer to already-reviewed pull requests for examples.

##### Discussion and Merging

A project committer will review the contribution and coordinate any project-wide discussion
needed. Review and discussion of the pull request itself takes place on
GitHub.

The committer review guide:

    https://jena.apache.org/getting_involved/reviewing_contributions.html

#### Code

Code style is about making the code clear for the next person
who looks at the code.

The project prefers code to be formatted in the common java style with
sensible deviation for short forms.

The project does not enforce a particular style but asks for:

* Kernighan and Ritchie style "Egyptian brackets" braces.
* Spaces for indentation.
* No `@author` tags.
* One statement per line.
* Indent level 4 for Java.
* Indent level 2 for XML.
* Avoid calling deprecated methods.
* Reformatting a file that has functional changes is acceptable. If the reformatting
  is extensive, put it into a new commit before the functional change so that the
  changes are easy to review.
* Don't reformat files that don't contain functional changes.
* Consider the overall architecture of the subsystem you're working in.
* Use `@SuppressWarnings` only if you need to call code that causes a warning.
* Use `@Override` and types for generics, don't declare unused checked exceptions.
* Include the Apache source header at the top of new files.
* Avoid adding to the public API unless discussed.
* Reuse existing code rather than duplicating.
* Code needs to be supported by multiple platforms (Windows, Mac, Linux).

See, for illustration:
https://google.github.io/styleguide/javaguide.html#s4-formatting

The codebase has a long history - not all of it follows this style.

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
