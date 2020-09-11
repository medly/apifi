# Contributing to apifi

Thanks for your interest in improving apifi. Please go through following guidelines while you make your contribution. 

## Project setup

1.  Fork and clone the repo
2.  Create a branch for your PR with `git checkout -b pr/your-branch-name`

> **Tip:** Keep your `master` branch pointing at the original repository and make pull requests from branches on your fork. To do this, run:
>
> ```properties
> git remote add upstream https://github.com/medly/apifi.git
> git fetch upstream
> git branch --set-upstream-to=upstream/master master
> ```
>
> This will add the original repository as a "remote" called "upstream," Then fetch the git information from that remote, then set your local `master` branch to use the upstream master branch whenever you run `git pull`. Then you can make all of your pull request branches based on this `master` branch. Whenever you want to update your version of `master`, do a regular `git pull`.

## Commit messages

Make sure your commit message follows following pattern:

`<module-name>: <one liner indicating the change>`

For example,

`runtime: Added support for UnauthorizedException`

## Development workflow

After setting the project, you can run several commands:

-   `./gradlew build` to build including tests
-   `./gradlew build -x test` to build excluding tests
-   `./gradlew test` to run tests for all modules
-   `./gradlew <module-name>:test` to run tests for individual module (e.g `./gradlew codegen:test` )

## Unit tests

We are using [`kotest`](https://kotest.io/) for writing unit & integration test cases. We are trying to maintain test coverage as high as possible. So please make sure to add unit/integrations tests for the changes.

## Pull request

PLease fill in all the information asked in the pull request [template](https://github.com/medly/apifi/blob/master/.github/PULL_REQUEST_TEMPLATE.md), this will help us tto understand your changes in the PR. Before you submit a new PR, please make sure that there is no lint error and none of the tests are failing.

If there is an issue opened for your PR, please refer that issue number in your PR.