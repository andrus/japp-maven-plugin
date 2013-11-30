japp-maven-plugin
=================

_japp-maven-plugin_ is a plugin to assemble desktop Java applications for different platforms, namely OS X, Windows and Linux. Produced Linux version is a generic runnable jar that works on any other platform just as well.

_japp-maven-plugin_ is a direct descendant of [maven-japplication-plugin](http://wiki.wocommunity.org/display/WOL/maven-japplication-plugin). The main motivation for its continuing development is to support the assembly of CayenneModeler application developed by the [Apache Cayenne project](http://cayenne.apache.org/). However there's no Cayenne dependency what so ever. The plugin is generic and anyone is welcome to use it for Java app packaging.

Supported Environments
----------------------

The plugin can package Java apps for the following platforms:

|Target Platform|Build Platform|Build Java Version
|---------------|--------------|-------------------
|OS - Oracle Java (1.7 and newer)|Any|1.7+
|OS - Apple Java (1.5 / 1.5, should be considered legacy)|Any|1.5+
|Windows|Widnows|1.5+
|Generic / Cross-Platform|Any|1.5+

Getting The Plugin
------------------

* You may clone the git repo and do "mvn clean install"
* You may get a released version from ObjectStyle Maven repository. (I may push it to Central if there's popular demand). For this you will need to declare the repo in your POM:

        <repository>
                <id>objectstyle</id>
                <name>ObjectStyle Repository</name>
                <url>http://maven.objectstyle.org/nexus/content/repositories/releases</url>
                <layout>default</layout>
        </repository>
    
* If you are using a repository manager like Nexus, you may add the repository above to the list of proxied repos. This is probably the cleanest option.

Examples
--------

Packaging for OS X:

	<plugin>
		<groupId>org.objectstyle.japp</groupId>
		<artifactId>japp-maven-plugin</artifactId>
		<version>3.0</version>
		<configuration>
			<name>MyApp</name>
			<mainClass>org.foo.Main</mainClass>
			<icon>src/japplication/resources/My.icns</icon>
			<os>mac</os>
			<jvmOptions>-Xmx512m -Dapple.laf.useScreenMenuBar=true</jvmOptions>
		</configuration>
		<executions>
			<execution>
				<phase>generate-resources</phase>
				<goals>
					<goal>japp</goal>
				</goals>
			</execution>
		</executions>
	</plugin>

Packaging for OS X legacy Apple JVM:

	<plugin>
		<groupId>org.objectstyle.japp</groupId>
		<artifactId>japp-maven-plugin</artifactId>
		<version>3.0</version>
		<configuration>
			<name>MyApp</name>
			<mainClass>org.foo.Main</mainClass>
			<icon>src/japplication/resources/My.icns</icon>
			<os>mac</os>
			<flavor>osx_legacy</flavor>
			<jvm>1.5+</jvm>
			<jvmOptions>-Xmx512m -Dapple.laf.useScreenMenuBar=true</jvmOptions>
		</configuration>
		<executions>
			<execution>
				<phase>generate-resources</phase>
				<goals>
					<goal>japp</goal>
				</goals>
			</execution>
		</executions>
	</plugin>
