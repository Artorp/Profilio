# Profilio
Profile manager for Factorio

## Features

* Keep your saves and mods in sync with profiles, each profile is a folder on your disk
* Seemlessly switch between different profiles
* Tie your profiles to a specific Factorio version, and switch between different versions with a single click
* Supports symlinks and junctions; no files are moved, only their links

## Prerequisites

This program requires Java 8, make sure you have the latest version installed. To avoid bloatware from the official page, Windows users are urged to download a Java installer from https://ninite.com/java8/

### Linux

The required library JavaFX might not come preinstalled by default, to install type in

    sudo apt-get install openjdk-8-jre openjfx

Then start the application with

    java -jar the_jar_file.jar

## Installing

Download the latest version from the [releases](https://github.com/Artorp/Profilio/releases) page. The downloaded file does not require an installation, place it where it suits you.

## Usage

With Java install, simply double click the jar file. To launch through the command line (ie for debugging purposes) run:

    java -jar the_jar_file.jar

If you use Steam, make sure to disable Steam Cloud to prevent saves from being mixed between profiles. You can use a cloud service to host the profile folder to keep them secure and synced.

### Quick-start guide

1. When first starting, open the settings and select your preferred move method (junction or symlink recommended)
2. Verify the paths, you can always change them later. By default the profile folder is located under 
   -  `%AppData%\Factorio\fpm` on Windows
   -  `~/.factorio/fpm` on Linux
   -  `~/Library/Application Support/factorio/fpm` on Mac
3. Verify the Factorio Installation, and add the correct directory if needed
4. Click the First-Time save transfer and click through the options
5. Close the settings screen, a new profile called "profile_default" is created with the default saves and mods
6. Done! Activate the profile and click "Start Factorio"

### General use

- Rename by double-clicking on the profile name
  - You can also rename the folder directly, but Profilio will register this as deleting the old profile and creating a new one
- Assign a new factorio version by clicking on the factorio version cell and selecting one from the drop-down menu
- Create a new profile by clicking the "New profile" button
  - You can also create a new folder inside the profile directory, make sure it contains the ´mods´ and ´saves´ folders
- Delete a profile by deleting the folder from within your file explorer.
  - Press the "Open folder" button to open its directory.
  - Make sure you don't delete the saves and mods you need!

## Compile

Requires `gson-2.8.0`

If using maven, use the included pom.xml and run

    mvn clean install

A fat jar is placed in the target folder.

## Licence

Licenced under GNU Lesser General Public License v3.0, see the [LICENSE.md](LICENSE.md) file for details
