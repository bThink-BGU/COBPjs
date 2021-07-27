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
* For Maven projects: Add [JitPack](https://jitpack.io) repository:

````xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
````
Also add COBPjs dependency. Note that the version number changes....
````xml
<dependencies>
    <dependency>
        <groupId>com.github.bThink-BGU</groupId>
        <artifactId>BPjs-Context</artifactId>
        <version>0.2.1</version>
    </dependency>
</dependencies>
````

## Writing COBP Programs
This section assumes that you are already familiar with the COBP paradigm. If this is not the case, you should start by reading the [COBP Paper](https://www.sciencedirect.com/science/article/pii/S095058492030094X).

A COBP program usually includes at least two files: 
* dal.js: The specification of the context schema, the effect functions, the queries, and the initial contextual data.
* bl.js: The specification of the context-dependent behaviors.

Examples for such programs are in the [src/test](src/test/resources) directory.

## Running a COBP Program
Running a COBP program is similar to the execution of a BP program:
```java
BProgram bprog = new ContextBProgram("dal.js", "bl.js"); //you can change the files names...
BProgramRunner rnr = new BProgramRunner(bprog);
rnr.addListener(new PrintCOBProgramRunnerListener(Level.CtxChanged, new PrintBProgramRunnerListener()));
```
In BP, we usually use the ```PrintBProgramRunnerListener``` for printing to the screen the selected events and the log messages. Since COBP has many internal events that are in charge of holding the execution semantics, you will might want to filter these events, at least some of them. The ```PrintCOBProgramRunnerListener``` will help you do that. The [Level](https://github.com/bThink-BGU/BPjs-Context/blob/6137d5d5ccbcf569921c73a283162396c1b1aeb4/src/main/java/il/ac/bgu/cs/bp/bpjs/context/PrintCOBProgramRunnerListener.java#L82) can be:
* Level.ALL : prints all context events
* Level.NONE : does not print context events
* Level.CtxChanged: prints only CTX.Changed events (i.e., filter the transaction lock/release events)

There is a full example for a main file - [here](https://github.com/bThink-BGU/BPjs-Context/blob/master/src/test/java/il/ac/bgu/cs/bp/bpjs/context/Main.java).

## Documentation & Relevant links
* [COBP Paper](https://www.sciencedirect.com/science/article/pii/S095058492030094X) introducing COBP, COLSC, and COBPjs.
* [A presentation at MORSE18](https://youtu.be/eqwhFPQfDjk) introduces COLSC and the paper "A Context-Based Behavioral Language for IoT".
* [COLSC paper](http://ceur-ws.org/Vol-2245/morse_paper_6.pdf) introduces the LCS implementation for COBP.
* [SampleProgram](src/test/resources/SampleProgram) sample usage of many language idioms.
* [Tic-Tac-Toe](src/test/resources/TicTacToe) introduces a COBP implementation of the game.
* [Hot-Cold](src/test/resources/HotCold) introduces a COBP implementation of an extended hot cold example.
* [BPjs documentation and tutorials](http://bpjs.readthedocs.io/en/master/) - a great starting point for BP.
* [Behavioral Programming](http://www.b-prog.org/) - introduces behavioral programming.
* [BPjs Google group](https://groups.google.com/forum/#!forum/bpjs)
