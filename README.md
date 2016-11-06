# Blaspheme
A library for interacting with Curse and downloading files from it.

## Source Code
The latest source code can be found here on [GitHub](https://github.com/darkhax/Blaspheme). If you are using Git, you can use the following command to clone the project: `git clone https://github.com/darkhax/Blaspheme`

##Building from Source
This project can be built using the Gradle Wrapper included in the repository. When the `gradlew build` command is executed from within the repo directory, a compiled JAR will be created in `~/build/libs`. Sources and Javadocs will also be generated in the same directory.

##Quick Start
TODO add a quickstart

##Dependency Management
If you are using [Maven](https://maven.apache.org/download.cgi) to manage your dependencies. Add the following into your `pom.xml` file. Make sure to update the version from time to time.
```
<repositories>
    <repository>
        <id>epoxide.xyz</id>
        <url>http://maven.epoxide.xyz</url>
    </repository>
</repositories>

<dependency>
     <groupId>net.darkhax.blaspheme</groupId>
     <artifactId>Blaspheme</artifactId>
     <version>1.0-SNAPSHOT</version>
</dependency>
```

If you are using [Gradle](https://gradle.org) to manage your dependencies, add the following into your `build.gradle` file. Make sure to update the version from time to time.
```
repositories {

    mavenCentral()
    maven { 
    
        url 'http://maven.epoxide.xyz' 
    }
}

dependencies {

    compile "net.darkhax.blaspheme:Blaspheme:1.0-SNAPSHOT"
}
```

##Legal Information
Extremely Simple Storage is licensed under the [LGPL2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html). Please see the `License.md` for more details. 

##Credits
* [Darkhax](https://github.com/darkhax) - Maintainer of Blaspheme.
* [Vazkii](https://github.com/Vazkii) - Author of [CMPDL](https://github.com/Vazkii/CMPDL), which this project is heavily based on!