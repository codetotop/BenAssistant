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

### android notify_debug_success

```sh
[bundle exec] fastlane android notify_debug_success
```

Notify success for debug build

### android notify_release_success

```sh
[bundle exec] fastlane android notify_release_success
```

Notify success for release build

### android notify_build_failure

```sh
[bundle exec] fastlane android notify_build_failure
```

Notify build failure

### android build_test

```sh
[bundle exec] fastlane android build_test
```

Runs all the tests

### android build_debug

```sh
[bundle exec] fastlane android build_debug
```

Builds the debug APK

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



### android upload_debug_firebase

```sh
[bundle exec] fastlane android upload_debug_firebase
```

Upload Debug APK to Firebase App Distribution

### android upload_release_firebase

```sh
[bundle exec] fastlane android upload_release_firebase
```

Upload Release APK to Firebase App Distribution

### android debug

```sh
[bundle exec] fastlane android debug
```

Build debug APK and upload to Firebase App Distribution

### android release

```sh
[bundle exec] fastlane android release
```

Build release APK and upload to Firebase App Distribution

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Deploy a new version to the Google Play

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
