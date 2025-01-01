Protocol Buffers parser and code generator
------------------------------------------

[![Build Status](https://travis-ci.org/protostuff/protostuff-compiler.svg?branch=master)](https://travis-ci.org/protostuff/protostuff-compiler)

This project contains following modules:

1. `protostuff-parser` - ANTLR4-based parser for proto2/proto3 files. Parsing result is represented as an object model, containing all the information from proto files. 
2. `protostuff-generator` - code generator; current version can generate java code (for `protostuff` runtime library) and HTML documentation.
4. `protostuff-cli` - command-line interface.
5. `protostuff-maven-plugin` - maven plugin.

Usage
-----

* [maven plugin](https://github.com/protostuff/protostuff-compiler/wiki/Maven-Plugin)
* [command-line interface](https://github.com/protostuff/protostuff-compiler/wiki/Command-line-interface)
* [gradle](https://github.com/protostuff/protostuff-compiler/wiki/Gradle)
 
```xml
    <build>
        <plugins>
            <plugin>
                <artifactId>protostuff-maven-plugin</artifactId>
                <groupId>io.protostuff</groupId>
                <version>2.2.18</version>
                <executions>
                    <execution>
                        <id>generate-java-sources</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>java</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

Java Source Code Generator
--------------------------

Current status: development in progress.

Generated code API: [draft](http://protostuff.github.io/documentation/compiler/java/generated-code).

Documentation Generator
-----------------------

`protostuff-compiler` can generate html from proto files.

Sample output: http://protostuff.github.io/samples/protostuff-compiler/html/#com.example.Address

This generator is an alternative to https://github.com/estan/protoc-gen-doc

Requirements
------------

| Component                                 | Version   |
|-------------------------------------------|-----------|
| JDK                                       | 11+       |  
| [Apache Maven](https://maven.apache.org/) | 3.6+       |

Build
-----

```
mvn clean install
```
