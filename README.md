# COBPjs: A Context-Oriented Behavioral Programming Environment based on BPjs

This repository contains a javascript-based [Context-Oriented Behavioral Programming (COBP)](https://www.sciencedirect.com/science/article/pii/S095058492030094X) library.

[![](https://jitpack.io/v/bThink-BGU/BPjs-Context.svg)](https://jitpack.io/#bThink-BGU/BPjs-Context)


#### License
* COBPjs is open sourced under the [MIT license](http://www.opensource.org/licenses/mit-license.php). If you use it in a system, please provide
  a link to this page somewhere in the documentation/system about section.
* COBPjs uses the BPjs. Project page and source code can be found [here](https://github.com/bThink-BGU/BPjs).
* COBPjs uses the Mozilla Rhino JavaScript engine. Project page and source code can be found [here](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino).

---

## Getting COBPjs
* For Maven projects: Add [JitPack](https://jitpack.io) repository and COBPjs dependency. Note that the version number changes.

````xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
...
<dependencies>
    ...
    <dependency>
        <groupId>com.github.bThink-BGU</groupId>
        <artifactId>BPjs-Context</artifactId>
        <version>0.1.7</version>
    </dependency>
    ...
</dependencies>
````

## Documentation & Relevant links
* [COBP Paper](https://www.sciencedirect.com/science/article/pii/S095058492030094X) introducing COBP, COLSC, and COBPjs.
* [A presentation at MORSE18](https://youtu.be/eqwhFPQfDjk) introduces COLSC and the paper "A Context-Based Behavioral Language for IoT".
* [COLSC paper](http://ceur-ws.org/Vol-2245/morse_paper_6.pdf) introduces the LCS implementation for COBP.
* [SampleProgram](src/test/resources/SampleProgram) sample usage of many language idioms.
* [Tic-Tac-Toe](src/test/resources/TTT) introduces a COBP implementation of the game. 
* [BPjs documentation and tutorials](http://bpjs.readthedocs.io/en/master/) - a great starting point for BP.
* [Behavioral Programming](http://www.b-prog.org/) - introduces behavioral programming.
