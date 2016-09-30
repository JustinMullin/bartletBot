package xyz.jmullin

import java.io.BufferedReader
import java.io.FileReader
import java.text.BreakIterator
import java.util.stream.Collectors
import xyz.jmullin.Ngramifier.toFrequencyMap
import xyz.jmullin.Ngramifier.sliding

/**
 * This phase of the process reads in Bartlet's lines, tokenizes them into words, calculates
 * n-gram frequencies, and writes those frequencies to disk for later use in the chatbot.
 */
fun main(args: Array<String>) {
    // Collect all lines from the source episodes into a single document.
    val allLines = (1..91).flatMap { Ngramifier.getLines(it) }
    val text = allLines.joinToString("\n")

    println("Tokenizing transcript text...")

    val tokens = Ngramifier.tokenize(text)

    println("Done with word tokenization. Beginning n-gram-ification.")

    val unigrams = tokens.toFrequencyMap()
    val bigrams = tokens.sliding(2).toFrequencyMap()
    val trigrams = tokens.sliding(3).toFrequencyMap()

    OutputUtils.write("ngrams/unigrams.txt", unigrams.map { "[${it.value}] ${it.key}" }.joinToString("\n"))
    println("Wrote unigrams.")
    OutputUtils.write("ngrams/bigrams.txt", bigrams.map { "[${it.value}] ${it.key.joinToString("|")}" }.joinToString("\n"))
    println("Wrote bigrams.")
    OutputUtils.write("ngrams/trigrams.txt", trigrams.map { "[${it.value}] ${it.key.joinToString("|")}" }.joinToString("\n"))
    println("Wrote trigrams.")
}

object Ngramifier {
    /**
     * Loads previously saved lines for a given episode from disk, removes parenthetical bits (which
     * are usually narration/direction rather than dialogue), and does a little bit of cleanup.
     */
    fun getLines(episode: Int): List<String> {
        val transcript = BufferedReader(FileReader("transcripts/episode$episode.txt"))

        return transcript.lines().map {
            it.replace(Regex("\\[.*?\\]"), "")
                .replace(Regex("\\(.*?\\)"), "")
                .filter { it.isDefined() }
                .trim()
        }.collect(Collectors.toList<String>())
    }

    /**
     * Analyzes the given text for word and punctuation boundaries using a [BreakIterator], and returns
     * the tokens from that text.
     */
    fun tokenize(text: String): List<String> {
        val wordIterator = BreakIterator.getWordInstance()
        wordIterator.setText(text)

        var tokenStart = 0
        var tokenEnd = wordIterator.first()
        var tokens = listOf<String>()

        // Loop over the tokens provided by this iterator, noting each until we reach the end.
        while (tokenEnd != BreakIterator.DONE) {
            val token = text.substring(tokenStart, tokenEnd).trim()

            if (token.isNotEmpty()) {
                tokens += token
            }

            tokenStart = tokenEnd
            tokenEnd = wordIterator.next()
        }

        return tokens
    }

    /**
     * Returns a list of sliding windows of a given size from the source list.
     */
    fun <T> List<T>.sliding(window: Int) =
        (0..(size-window-1)).map { i ->
            (0..(window-1)).map { offset ->
                get(i + offset)
            }
        }

    /**
     * Converts a list of elements, returning a map of each unique element to the number of times that
     * element occurs in the list.
     */
    fun <T> List<T>.toFrequencyMap() =
        groupBy { it }
            .map { Pair(it.key, it.value.size) }
            .sortedBy { -it.second }
            .toMap()
}