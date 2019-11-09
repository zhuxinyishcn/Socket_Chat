# Chat Program

Due: November 18, 2019 at 9:30am

In this assignment you will modify a simple 2-way network chat program to
prompt the user and get responses in a language other than English, and to
provide rudimentary protection of the messages passing over the network.

### Objectives

Students will:

-   Demonstrate good software engineering practices
-   Learn two design patterns
    -   Strategy Pattern
    -   Simple Factory Pattern
-   Make use of resource "property" files to support internationalization

## Instructions

This assignment is to be completed individually; **no collaboration is
permitted**.

## Setup

1.  Navigate to the
    [starter code on GitLab](https://git.unl.edu/csce_361/starter-code/socket_chat)
    and click on the "Fork" button to copy it to your account.

1.  Set the copied project to *Private* and add the professor and TAs with
    "Maintainer" access. Verify that GitLab shows a lock icon on the fork.
    Double-check that the professor and all five TAs have "Maintainer" access.

1.  Clone your copied project to your local system and import it into your IDE
    as a Maven project.

## Assignment

Look over the starter code, view
[this short demonstration](https://use.vg/qB6zFZ), and run the program to get a
feel for how the program works.

-   One host, one client, but the same code
-   To avoid having to do any network discovery, the host displays its IP
    address, which must be shared with whoever is running the client so they
    can type in the IP address
-   The host sets the port number to be used, and this must be shared with
    whoever is running the client so they can type in the IP address
    -   If the port number were hard-coded then we'd run the risk of that port
        number already being in use
-   The host sets up a `ServerSocket` to accept the client's connection, and
    the client sets up a `Socket` to connect to the host
-   The host and client alternate turns sending Strings to each other over the
    socket
-   When theres's message from either the host or client consisting solely of
    the keyword `EXIT` in all capital letters, the program terminates.

### Internationalization

For successful software, it's almost certain that there will be people who want
to use your software who don't speak the same language as you. The process of
making your software work with any human language is called
*internationalization*, often abbreviated as *I18N*.

A common technique for I18N is to use a `ResourceBundle` of key-value pairs.
Notice that anyplace that the program outputs something, the argument to
`println()` is `bundle.getString(...)` -- that substitutes the value String
that corresponds to the specified key String. We also use this to compare the
user input to a string.

The key-value pairs are in a properties file in the `.../resources/` directory.
The files are of the form `basename_XX.properties`, where XX is the 2- or 3-
character language code. Example langauge codes are `en` for English, `fr` for
French, `de` for German, `zh` for Chinese, and `tlh` for Klingon. If you decide
to do full localization then the suffix includes a language code, a country
code, and possibly a variant code. We'll limit ourselves to just the language
code. Since the basename we're using is "socketchat", the file with the English-
language strings is `socketchat_en.properties`.

Notice that some of the value strings have numbered arguments such as `{0}` and
`{1}`. When combined with `MessageFormat.format()`, this allows us to create
parameterized strings similar to C's `printf()`.

1.  Create a properties file for another language. If you don't know another
    language, you may use an electronic (or online) translator or bilingual
    dictionary for this assignment.
    -   The language codes can be found in
        [ISO 639-2](https://www.loc.gov/standards/iso639-2/php/code_list.php).
        If both a 2-letter and a 3-letter language code are available for the
        language you choose, you *must* use the 2-letter code.
    -   You *must* use the same keys as the English-language properties file,
        because the program depends on the keys.

1.  Decide on a keyword to indicate that you want to change the language. As
    with the `EXIT` keyword, this keyword will be typed as part of the chat.
    Add this keyword in English to `socket_chat_en.properties` and in the other
    language's properties file, using the key `communicate.keyword.setLocale`.

1.  Un-comment the commented-out code in `Chat.handleKeyword()` and edit it to
    handle the new keyword.
    -   Find out what language the user wants to change to.
    -   At a minimum, you must handle the cases where the user wants to change
        to English and where the user wants to change to the language you chose
        in Step 1.
    -   After you have determined which language the user wants to change to,
        call `Chat.setLocale()`
        -   Use `Locale.ENGLISH` or `Locale.US` if the user wants to change to
            English.
        -   If you earlier chose Chinese, French, German, Italian, Japanese, or
            Korean, you can use `Locale.CHINESE`, `Locale.FRENCH`,
            `Locale.GERMAN`, `Locale.ITALIAN`, `Locale.JAPANESE`, or
            `Locale.KOREAN`.
        -   If you earlier chose a language listed in the first column of the
            table on [this webpage](https://www.oracle.com/technetwork/java/javase/javase7locales-334809.html),
            then you can use the `Locale.Locale(String language)` constructor,
            where `language` is the 2-character language code at the start of
            the Locale ID in the third column of that table. Or use
            `Locale.Locale(String language, String country)` where `country` is
            the 2-character country code at the end of the Locale ID in the
            third column. Or (rarely), `Locale.Locale(String language, String
            country, String variant)`.
        -   Otherwise, create a `Locale` using [`Locale.Builder`](https://docs.oracle.com/javase/8/docs/api/java/util/Locale.Builder.html)
            (yes, this is using the Builder Pattern).

### Strategy Pattern

You will use the *Strategy Pattern* to attach cipher algorithms to the chat
program.[^1]

-   Figure 16.9 on Kung p397
-   HFDP, [Chapter 1](https://learning.oreilly.com/library/view/head-first-design/0596007124/ch01.html)

[^1]:   For this application, you are going to write cipher algorithms and
        attach them to the chat program using the Strategy Pattern, so that you
        can learn the Strategy Pattern. For a real application, I strongly
        advise against writing your own cipher algorithms. Instead use
        `javax.crypto.Cipher` and, if you're streaming text back and forth as
        in this program, attach the algorithms using the Decorator pattern via
        `javax.crypto.CipherInputStream` and `javax.crypto.CipherOutputStream`.

    Decorator Pattern:

    -   Figure 16.34 on Kung p421
    -   HFDP, [Chapter 3](https://learning.oreilly.com/library/view/head-first-design/0596007124/ch03.html)

The original version of this program sends plaintext messages over the network.
The current version passes an outgoing message through `Chat.encipher()` and
incoming messages through `Chat.decipher()`. Right now, all these methods do is
return the original message without enciphering (or deciphering) it.

4.  Create `Cipher.java`, an interface with two methods: `String
    encipher(String plaintext)` whose specification is that it that passes the
    plaintext through a cipher to create ciphertext, and `String
    decipher(String ciphertext)` whose specification is that it passes the
    cipher text through the inverse cipher to produce the original plaintext.

1.  Create `NullCipher.java`, a class that implements the `Cipher` interface.
    `NullCipher.encipher()` and `NullCipher.decipher()` should simply return
    their argument without modification (just like `Chat.encipher()` and
    `Chat.decipher()` do now.)

1.  Create a field in `Chat.java` for the cipher behavior. You might call it
    `Cipher cipherBehavior` or `Cipher cipherStrategy` -- or you might simply
    call it `Cipher cipher`, but by giving it a name with the word *Behavior*
    or *Strategy* in it, you're flagging the existence of the Strategy Pattern
    to anybody who reads your code.
    -   In the `Chat.Chat()` constructor, initialize this field to a
        `NullCipher` object.

1.  Replace the code in `Chat.encipher()` and `Chat.decipher()` with:
    ```
    private String encipher(String plaintext) {
        String ciphertext = cipherBehavior.encipher(plaintext);
        return ciphertext;
    }

    private String decipher (String ciphertext) {
        String plaintext = cipherBehavior.decipher(ciphertext);
        return plaintext;
    }
    ```
    (here I assumed you named the `Cipher` field `cipherBehavior`. If you
    didn't, the substitute the actual name you used.)

At this point, the program should still have the same externally-observable
behavior it had after you finished Step 3. What's different is that,
internally, you've delegated the behavior for `Chat.encipher()` and
`Chat.decipher()` to another object.

This would be a good time to verify that your partial implementation of the
Strategy Pattern hasn't broken anything.

8.  Implement two classical ciphers as Java classes that implement the `Cipher`
    interface. You can include any other methods you feel are necessary;
    however, only `encipher()` and `decipher()` will be exposed to `Chat.java`.
    -   You may use ciphers you implemented in a previous course (but they must
        be written in Java) or [other
        ciphers](http://practicalcryptography.com/ciphers/classical-era/).
    -   You probably want to have the key(s) be arguments to the constructors,
        but feel free to explore other options.

1.  You can replace `NullCipher` in the `Chat` constructor with either of the
    other ciphers you wrote, and your program will still work.

What we need now to complete the Strategy Pattern is a way to replace the
cipher algorithm at runtime. Many options are possible; we'll use the...

### Simple Factory Pattern

You will use the *Simple Factory Pattern* to obtain the cipher algorithms

-   HFDP, [Chapter 4](https://learning.oreilly.com/library/view/head-first-design/0596007124/ch04.html)
-   The authors of *Head First Design Patterns* say it isn't a real pattern
    but instead is a commonly-used idiom. This is a distinction without a
    difference: a pattern is a common solution to a common problem, and an
    idiom is a common way to express an idea in code.

The Simple Factory is not nearly as powerful as the Abstract Factory Pattern,
or even the Factory Method Pattern, but I sure seem to use it often. There is
no one-true form of the Simple Factory. Sometimes it takes no arguments and
solely exists to hide which concrete class is being used. Other times, it will
be parameterized and the particular concrete class that is returned depends on
the arguments used. Another option yet is to have a method that sets the
concrete type, and then the `create` method will always return that concrete
type -- but because the `create` method is declared to return the abstraction,
the client code cannot assume a concrete type.

We will use a parameterized factory.

10. Create `CipherFactory.java`.

11. Write the method
    `public static Cipher createCipher(String name, String[] keys)`. This
    method shall create and return an instance of a `Cipher` implementation
    class that corresponds to `name`. If that cipher only requires one key,
    then this method will use `keys[0]` as that key; if the cipher requires
    multiple keys, then it will use the appropriate number of elements from
    `keys` as the cipher keys. If that cipher requires no keys, then this
    method will ignore `keys`.

12. Write the method `public static Cipher createCipher()` without any
    arguments. This method shall return a default cipher algorithm. (You decide
    what that default is).

That's it. That's your Simple Factory.

Now it's time to use it.

13. Find line line in the `Chat.Chat()` constructor where you wrote
    `cipherBehavior = new NullCipher()` (or something like that). Replace
    `new NullCipher()` with `CipherFactory.createCipher()`. Now your cipher
    algorithm is whatever the default happens to be.

1.  Decide on a keyword to indicate that you want to change the cipher
    algorithm. As with the `EXIT` keyword, this keyword will be typed as part
    of the chat. Add this keyword in English to `socket_chat_en.properties` and
    in the other language's properties file, using a key of your choosing (but
    make sure it starts with `communicate.keyword.` so that the code to detect
    keywords knows to look for it).

1.  Add code to `Chat.handleKeyword()` to handle the new keyword. This code
    should work with the user to change the cipher algorithm to whichever
    cipher algorithm they want, with the key(s) they want.

You can now chat away without worrying about a "l337 h4x0r" being snoopy. (If
you're worried about someone with NSA-level snooping capabilities, don't use a
"classical era" cipher! They're relatively easy to break in general, and the
keywords would help someone trying to break it do so by giving known words to
look for. The "EXIT" keyword especially so, since it occurs at the end of every
conversation.)

You might wonder why we used a Simple Factory to create Cipher objects instead
of just putting the same logic in a method in the `Chat` class. There are three
reasons. One is that there might be other projects that could use these Cipher
objects, and we can simply reuse the `CipherFactory` instead of copy-pasting
code. Another is that in a larger project, there may be several classes that
need such an object, and we'd prefer to have a creation class that all classes
in the system can use.

The other reason isn't about code reuse at all. Remember the *dependency
inversion principle*: depend on abstractions, not concretions. After you've
finished this assignment, there isn't a single line of code that mentions
concrete `Cipher` classes in `Chat.java`. Not one. Now, no matter what we do
with cipher algorithms, we aren't worried about accidentally breaking the
`Chat` class. As we make changes, we'll never have to run regression tests on
`Chat`. It's very comforting to know that you've reduced the coupling in your
system so that you aren't afraid of breaking anything important when you make a
small change to an ancillary piece of code.

## Deliverables

For grading, we will pull the `socket_chat` repositories after the assignment
is due, and we will look in the Maven-conventional directories for:

-   A properties file for your chosen language
-   Updated `socketchat_en.properties` file
-   Updated `Chat.java`
-   `Cipher.java` and `CipherFactory.java`
-   Three `Cipher` implementations (`NullCipher.java` plus two others of your
    choosing)

*It is your responsibility to ensure that your work is in the master branch of
the **correct repository** and that we have the correct level of access to the
repository at the **time the assignment is due**.  We will grade what we can
retrieve from the repository at the time it is due.  Any work that is not in
the correct repository, or that we cannot access, will not be graded.*

## Rubric

The assignment is worth **20 points**:

-   **4 points** for internationalization
    -   1 points for creating the properties file for the other language
    -   3 points for writing the code to change Locales

-   **3 points** for creating the Simple Factory

-   **9 points** for the Strategy Pattern
    -   1 point for creating `Cipher.java` and `NullCipher.java`
    -   2 points each for the 2 other classical cipher algorithms
    -   1 point for delegating `encipher` and `decipher`
    -   3 points for writing the code to change cipher algorithms

-   **1 point** for making regular commits; *i.e.*, not waiting until the end
    of the project to make a massive commit.

-   **3 point** for meaningful and well-formatted commit messages

*If **at any time** your repository is public or has internal visibility then
you will receive a 10% penalty. Further, if another student accesses your
non-private repository and copies your solution then I will assume that you are
complicit in their academic dishonesty.*
