# JSPC Maven Plugin

## Unmaintained

Alas, this plugin is unmaintained.

[![No maintenance intended](http://unmaintained.tech/badge.svg)](http://unmaintained.tech/)


[![Build Status](https://travis-ci.org/Jasig/jspc-maven-plugin.svg?branch=master)](https://travis-ci.org/Jasig/jspc-maven-plugin)

## Info

A Maven plugin that compiles JSPs into class files, copies these into the final artifact, and updates the web.xml to reference the compiled classes. This is a fork of the [Codehaus jspc-maven-plugin](http://mojo.codehaus.org/jspc/jspc-maven-plugin/) that resolves some long standing issues and gets the Tomcat 7 support released.

## Usage

See the [Maven Project Documentation](http://developer.jasig.org/projects/jspc-maven-plugin/2.0.0/jspc-maven-plugin/plugin-info.html) for goal documentation.

[Plugin Usage](http://developer.jasig.org/projects/jspc-maven-plugin/2.0.0/jspc-maven-plugin/usage.html) is also documented on the maven site.

## JSP Compilers

The available JSP compilers can be found by browsing [org.jasig.mojo.jspc GroupId](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.jasig.mojo.jspc%22).

### Issues

The JSPC plugin is a wrapper around Tomcat's JSP Compiler.
For issues relating to JSP syntax please report the issue to tomcat.
[Tomcat 6 issue tracker](https://bz.apache.org/bugzilla/describecomponents.cgi?product=Tomcat%206)
[Tomcat 7 issue tracker](https://bz.apache.org/bugzilla/describecomponents.cgi?product=Tomcat%207)
[Tomcat 8 issue tracker](https://bz.apache.org/bugzilla/describecomponents.cgi?product=Tomcat%208)

Issues relating to maven or configuration options should reported to the [JSPC issue tracker](https://github.com/Jasig/jspc-maven-plugin/issues)

### Known Issues

Invocation of static methods on interfaces fails with the latest minor version of every tomcat compiler below 9.x.
