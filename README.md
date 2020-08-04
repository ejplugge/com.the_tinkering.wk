# com.the_tinkering.wk

com.the_tinkering.wk is an Android app for WaniKani

It's set up as an Android project with Google's default gradle set up.
Use Android Studio or the Gradle command line to build it.

## Preparing to build the code

NOTE: The required fields in Identification.java and strings.xml have changed!
Please refer to the sample files to see what has changed.

Before you can build this code, you will have to provide two files containing identification
information for the app. This is because the open source license covering this app's code
does not cover the name I gave the app, and it also doesn't cover my name. See the file
LICENSE.md for details.

- Copy the file app/Identification.java.sample.txt to app/src/main/java/com/the_tinkering/wk
- Name the copy Identification.java
- Edit the file to supply your own identification for the app
- Copy the file app/strings.xml.sample.txt to app/src/main/res/values
- Name the copy strings.xml
- Edit the file to supply your own identification for the app

After this, you are ready to build the code.

