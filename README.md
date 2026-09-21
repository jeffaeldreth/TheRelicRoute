# The Relic Route

A desktop app for cataloguing a personal collection: books, movies, music, art, and any
categories you create yourself. Everything is stored in a MariaDB database that you control.

## Philosophy

Because they can't steal what you are giving away!

## Features

- Built-in categories for Books, Movies, Music and Art, each with its own fields
- **Your own categories:** make a collection for anything (coins, comics, ...) with your own
  text, number and yes/no fields
- Search by any field, from the tree on the left or the search bar
- If something isn't in your collection, one click searches the web for it in the built-in browser
- **Help > Total Collection Value** adds up the value of everything in the collection
- Your database can be on the same computer that runs the app, or on a different computer that you
  reach through an optional SSH tunnel

## What you need

- Java 21 (JDK)
- The JavaFX 21 SDK: https://gluonhq.com/products/javafx/ (download the SDK for your system)
- A MariaDB server you can reach
- Two libraries, saved in a `lib/` folder next to `src/`:

      mkdir -p lib
      curl -L -o lib/mariadb-java-client-3.5.3.jar https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.5.3/mariadb-java-client-3.5.3.jar
      curl -L -o lib/jsch-0.2.25.jar https://repo1.maven.org/maven2/com/github/mwiede/jsch/0.2.25/jsch-0.2.25.jar

## Build and run

Set `JAVAFX` to the `lib` folder of the JavaFX SDK you downloaded, then from this folder:

    JAVAFX=/path/to/javafx-sdk-21/lib

    javac --module-path "$JAVAFX" --add-modules javafx.controls,javafx.fxml,javafx.web \
          -cp "lib/*" -d out $(find src -name '*.java')

    java  --module-path "$JAVAFX" --add-modules javafx.controls,javafx.fxml,javafx.web \
          -cp "out:src:lib/*" libraryCatalog.Main

(On Windows use `;` instead of `:` in the class path and `dir /s /b src\*.java` to list the files.)

In IntelliJ IDEA: open this folder, add the JavaFX SDK `lib` folder and the two jars in `lib/` as
libraries (File > Project Structure > Libraries), then run `libraryCatalog.Main` with the VM options

    --module-path /path/to/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml,javafx.web

## First run

1. Create an empty database and a login for it. In a MariaDB console:

       CREATE DATABASE The_Relic_Route CHARACTER SET utf8mb4;
       CREATE USER 'relic'@'localhost' IDENTIFIED BY 'choose-a-password';
       GRANT ALL PRIVILEGES ON The_Relic_Route.* TO 'relic'@'localhost';

2. Start the app. It asks where your database is. Enter the host, port, database name,
   username and password, then press **Test Connection**. If the database is on a different
   computer than the one running the app, and you reach it over SSH, tick the SSH tunnel box and
   fill in that section.
3. That's all. Each category's table is created the first time you add something to it.

You can change these settings any time under **File > Database Connection...**.

## Your own categories

**Categories > New Category...**: name it, pick an icon and add your fields (text, number or
yes/no). Every category also gets Title, On Hand, Location, Condition and Value.
**Categories > Remove Category...** deletes a category **and everything in it**.

## Where your settings are saved

In `~/.relicroute/connection.properties` in your home folder, readable only by you. They are
never stored in this project, so they can't end up in a git repository. Passwords are saved in
that file in plain text unless you untick **Remember passwords on this computer**, in which case
the app asks for them each time it starts.

## License and credits

The code is released under the [MIT License](LICENSE). See [CREDITS.md](CREDITS.md) for the logo,
the icons, and the libraries this is built on.

## Adapting it

This app grew from a personal collection, so the built-in categories (Books, Movies, Music, Art)
reflect what that collection needed. Each lives in its own controller, pane and model class
(for example `MoviesController`, `MoviesPane.fxml`, `Movie`), so you can change the fields or add
a new built-in category by following the same pattern.
