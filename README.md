# Simple State Machines in Kotlin (KSSM)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/236ca2459f7e4853bc3950be6b365776)](https://app.codacy.com/manual/milosmns/kssm?utm_source=github.com&utm_medium=referral&utm_content=milosmns/kssm&utm_campaign=Badge_Grade_Dashboard)

![Experimental State](https://img.shields.io/badge/state-experimental-orange)
![Workflow Status](https://img.shields.io/github/workflow/status/milosmns/kssm/Pre-release%20check)
![Code Size](https://img.shields.io/github/languages/code-size/milosmns/kssm)
![License](https://img.shields.io/github/license/milosmns/kssm)
![Download](https://img.shields.io/bintray/v/milosmns/maven/kssm?color=green)
![Latest Release](https://img.shields.io/github/v/release/milosmns/kssm?include_prereleases)

## What is this?

**KSSM** provides an easy and simple DSL _(Domain Specific Language)_ for setting up and using [finite-state machines](https://en.wikipedia.org/wiki/Finite-state_machine)
on JVMs. It provides a clear API with built-in threading and conflation mechanisms.
This library **does not** solve all state machine problems, **nor** does it address all possible use-cases related to state machines (although, it would like to).

It's based on the [Flow API](https://kotlinlang.org/docs/reference/coroutines/flow.html#flows) from Kotlin's coroutine package,
more precisely on [State Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/). 
State Flow is currently in `experimental` phase, meaning that the API might be expanded in the future and is not safe for _inheritance_ -- but it is **safe for usage**.

## Quick Intro

Here's a short introduction to show how KSSM works. 
[Click here](https://github.com/milosmns/kssm/blob/master/docs/Intro.pptx?raw=true) to go at your own pace in presentation mode.

![Intro](https://github.com/milosmns/kssm/blob/master/docs/Intro.gif?raw=true)

## Wiki

If you prefer longer documentation, check out the class documentation for
[StateMachine API](https://github.com/milosmns/kssm/blob/master/kssm/src/main/kotlin/me/angrybyte/kssm/api/StateMachine.kt).
It's meant to be used in conjuction with the DSL, described in the
[DSL class docs](https://github.com/milosmns/kssm/blob/master/kssm/src/main/kotlin/me/angrybyte/kssm/dsl/StateMachineDsl.kt).

Website-like documentation is exported into [docs/dokka](https://github.com/milosmns/kssm/tree/master/docs/dokka/kssm)
directory, so you can also pull that directory to your computer and open `index.html`.

You want more? There are code samples [in the demo directory](https://github.com/milosmns/kssm/tree/master/demo/src/main/kotlin)
that showcase the most interesting features of this library.

## How to install KSSM?

It's hosted on JCenter and MavenCentral, and thus easy to depend upon with Gradle:

```gradle
dependencies {
  // find the latest `$kssm_version` in a badge in this page's title
  implementation "me.angrybyte:kssm:$kssm_version"
}
```

## Contributing & Code of Conduct

Please open [a new issue](https://github.com/milosmns/kssm/issues/new) with any requests, issues, complaints or any other type of communication.
For sensitive or secure topics, don't hesitate to reach out to any of the maintainers directly.
