# Simple State Machines in Kotlin (KSSM)

![Workflow Status](https://img.shields.io/github/workflow/status/milosmns/kssm/Pre-release%20check)
![Code Size](https://img.shields.io/github/languages/code-size/milosmns/kssm)
![License](https://img.shields.io/github/license/milosmns/kssm)
![Download](https://img.shields.io/bintray/v/milosmns/maven/kssm?color=green)
![Latest Release](https://img.shields.io/github/v/release/milosmns/kssm?include_prereleases)
![Codacy Grade](https://img.shields.io/codacy/grade/0ed57b0d78ef467daa0f534736d6def8)

## What is this?

**KSSM** (reordered: _Kotlin - Simple State Machines_) provides an easy and simple DSL _(Domain Specific Language)_ for setting up [finite-state machines](https://en.wikipedia.org/wiki/Finite-state_machine), and opens a clean API with built-in threading and conflation mechanisms for manipulating states.
This library **does not** solve all state machine problems, **nor** does it aim to address all possible use-cases related to state machines.
It currently works only in the JVM environment.

KSSM is based on the [Flow API](https://kotlinlang.org/docs/reference/coroutines/flow.html#flows) from Kotlin's coroutine package,
more precisely on [State Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/). 

## Quick Intro

Here's a short introduction on how KSSM works. 
[Click here](https://github.com/milosmns/kssm/blob/master/docs/Intro.pptx?raw=true) to go at your own pace in presentation mode.

![Intro](https://github.com/milosmns/kssm/blob/master/docs/Intro.gif?raw=true)

KSSM DSL looks like this (assuming actions and states are defined):

```kotlin
val sm = stateMachine {
  mappings(
    Heat moves Ice to Liquid,
    Chill moves Steam to Liquid,
    Drink moves Liquid to Empty,
    Fill moves Empty to Liquid
  )
  
  transitionsDispatcher = Dispatchers.Main // optional
  
  initialState = Empty // optional
  
  transitionHandler { println("Detected change: $it\n") } // optional
  errorHandler { err.println("Invalid request: $it\n") } // optional
  
  // other config...
}

// send inputs/actions to that instance
val job = sm.transition(Fill)
job.join() // or job.cancel(), or simply ignore
```

## Wiki

If you prefer longer documentation, check out the [longer version of the docs](https://milosmns.github.io/kssm/kssm/me.angrybyte.kssm.api).

You want more? There are code samples [in the demo directory](https://github.com/milosmns/kssm/tree/master/demo/src/main/kotlin)
that showcase the most interesting features of this library.

## How to install KSSM?

It's hosted on JCenter and MavenCentral, and thus easy to depend upon with Gradle:

```gradle
dependencies {
  // find the latest `$kssm_version` on the top of this page
  implementation "me.angrybyte:kssm:$kssm_version"
}
```

## Contributing & Code of Conduct

Please open [a new issue](https://github.com/milosmns/kssm/issues/new) with any requests, issues, complaints or any other type of communication.
For sensitive or security-related topics, don't hesitate to reach out to any maintainer directly.
