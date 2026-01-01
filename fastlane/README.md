fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Runs all the tests

### android build_release

```sh
[bundle exec] fastlane android build_release
```

Builds the release APK

### android git_version_code

```sh
[bundle exec] fastlane android git_version_code
```



### android git_version_name

```sh
[bundle exec] fastlane android git_version_name
```



### android set_version

```sh
[bundle exec] fastlane android set_version
```



### android release_notes_from_git

```sh
[bundle exec] fastlane android release_notes_from_git
```



### android upload_firebase

```sh
[bundle exec] fastlane android upload_firebase
```

Upload the APK to Firebase App Distribution

### android beta

```sh
[bundle exec] fastlane android beta
```

Build and upload a new beta version to Firebase App Distribution

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Deploy a new version to the Google Play

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
