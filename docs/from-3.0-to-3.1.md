# From Fact Graph v3.0 to v3.1
An older version of the fact graph (v3.0) developed for Direct File is used in production code today, see [here](https://github.com/IRS-Public/direct-file/tree/main/direct-file/fact-graph-scala). 

The core v3.1 included in the initial commit of this repository expands on the 3.0 version in several important ways

The main changes are:

- converting the fact-graph to a standalone library
- enabling ways to import a fact dictionary, allowing import directly from XML

Some later changes:

- enable overriding derived facts when testing
- additional debugging functionality built into the library
- new CompNodes for more complex calculations

