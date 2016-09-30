# President Josiah Edward "Jed" Bartlet

This is a submission to a Nerdery JVM Code Challenge. The challenge is to produce a CLI "chatbot" which responds to
2016 presidential debate questions (or really anything you care to throw at it).

This application utilizes transcripts of The West Wing (thanks westwingtranscripts.com!) to develop a probabilistic
(n-gram) model of the dialogue of President Bartlet. From that dialogue debate responses are generated. No, it doesn't
really respond to the questions in any coherent manner, but hey, that's pretty realistic isn't it?

## How does it work?

Three main steps:

   * Scrape The West Wing transcripts from westwingtranscripts.com, filter down to just Bartlet lines.
   * Generate n-gram frequency tables from Bartlet's dialogue.
   * Generate responses probabilistically based on said n-gram frequency tables.

There's a little bit of special sauce on top of that to format sentences nicely, structure things a little bit
better, and try to pretend to be a little bit more on-topic, but that's the essentials.

## Instructions

The bot relies on n-gram tables, and generation of n-gram tables relies on scraped dialogue. Both of these steps
are present in the source, but not exposed through gradle tasks. You can view and run the scripts if you'd like at
DownloadTranscripts.kt and Ngramifier.kt respectively.

None of that is necessary to run the bot, however, as this repository comes preloaded with the requisite artifacts.

To simply run the pre-packaged jar:

```
java -jar build/libs/bartletBot-1.0-all.jar [question]
```

If you'd rather run from IDE, execute the main method in BartletBot.kt. Optionally provide to it a single argument containing the debate question
to pass.

Finally, if you want to package the jar yourself:

```
gradle shadowJar
```

## Transcript download and n-gram-ification

The chatbot depends on n-grams for Bartlet's speech, which further depends on show transcripts. These files are
included in this repository and need not be generated, but the scripts to generate them are also included in
case you wish to peruse them or try them out. The transcript download script is at DownloadTranscripts.kt, while
the script which generates the n-gram tables is at Ngramifier.kt. Running the main methods of either of these
with no arguments is sufficient to kick it off.