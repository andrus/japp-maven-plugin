japp-maven-plugin
=================

_japp-maven-plugin_ is a plugin to assemble desktop Java applications for different platforms, namely OS X, Windows and Linux. Produced Linux version is a generic runnable jar that works on any other platform just as well.

_japp-maven-plugin_ is a direct descendant of [maven-japplication-plugin](http://wiki.wocommunity.org/display/WOL/maven-japplication-plugin). The main motivation for its continuing development is to support the assembly of CayenneModeler application developed by the [Apache Cayenne project](http://cayenne.apache.org/). However there's no Cayenne dependency what so ever. The plugin is generic and anyone is welcome to use it for Java app packaging.

Supported Environments
----------------------

The plugin can package Java apps for the following platforms:

* OS X - Oracle Java (1.7 and newer). Build can be done on any platform (not just Mac), at least Java 7 must be used.
* OS X - Apple Java (1.6 or 1.5, should be considered legacy). Build can be done on any platform (not just Mac). 
* Windows. Build must be performed on Windows, as it invokes an .exe.
* Generic cross platform runnable jar. Build can be done anywhere.
