# JSPC Maven Plugin

[![Build Status](https://travis-ci.org/Jasig/jspc-maven-plugin.svg?branch=master)](https://travis-ci.org/Jasig/jspc-maven-plugin)

## Info

A Maven plugin that compiles JSPs into class files, copies these into the final artifact, and updates the web.xml to reference the compiled classes. This is a fork of the [Codehaus jspc-maven-plugin](http://mojo.codehaus.org/jspc/jspc-maven-plugin/) that resolves some long standing issues and gets the Tomcat 7 support released.

## Usage

See the [Maven Project Documentation](http://developer.jasig.org/projects/jspc-maven-plugin/2.0.0/jspc-maven-plugin/plugin-info.html) for goal documentation.

[Plugin Usage](http://developer.jasig.org/projects/jspc-maven-plugin/2.0.0/jspc-maven-plugin/usage.html) is also documented on the maven site.

## JSP Compilers

The available JSP compilers can be found by browsing [org.jasig.mojo.jspc GroupId](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.jasig.mojo.jspc%22).

### Issues

The plugin is limited to supporting JSP syntax that is supported by the corresponding compiler, which you should make sure also matches the compiler found on your servlet container installation. For any issues refer to the issue tracker for the corresponding compiler.

### Known Issues

Invocation of static methods on interfaces fails with the latest minor version of every tomcat compiler below 9.x.
