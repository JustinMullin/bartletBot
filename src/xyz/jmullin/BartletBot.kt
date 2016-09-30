package xyz.jmullin

import java.security.SecureRandom

/**
 * Main entry point to the Bartlet chatbot.
 */
fun main(args: Array<String>) {
    // Load frequency tables from disk.
    val unigrams = NgramFrequencyTable.loadNgrams("ngrams/unigrams.txt", Regex("\\[(\\d+)\\] (.*)"))
    val bigrams = NgramFrequencyTable.loadNgrams("ngrams/bigrams.txt", Regex("\\[(\\d+)\\] (.*?)\\|(.*)"))
    val trigrams = NgramFrequencyTable.loadNgrams("ngrams/trigrams.txt", Regex("\\[(\\d+)\\] (.*?)\\|(.*?)\\|(.*)"))

    val question = args.getOrElse(0, { "" } )

    BartletBot.generateText(question, unigrams, bigrams, trigrams)
}

/**
 * Generates text responses to chat queries, using n-gram frequency tables to probabilistically
 * determine which word should follow in a given sentence.
 */
object BartletBot {
    /**
     * Random number generator.
     */
    private val random = SecureRandom()

    /**
     * List of tokens which signal the termination of a sentence.
     */
    private val SentenceTerminators = setOf(".", "!", "?")

    /**
     * Given a set of n-grams of order 1-3, and a seed question, generate a response.
     */
    fun generateText(seedQuestion: String, unigrams: NgramFrequencyTable, bigrams: NgramFrequencyTable, trigrams: NgramFrequencyTable) {
        val seedWords = seedQuestion.split(Regex("\\s+")).map { it.toLowerCase() }

        // Try to pick a start word that was in the seed question. Probably this doesn't make our response very
        // relevant in actuality, but hey! It's politics.
        val startWord = pickStartWord(seedWords, unigrams)

        // Every speech starts with a single word.
        print(startWord.capitalize())

        var twoWordsAgo = ""
        var oneWordAgo = startWord

        var atStartOfSentence = false
        var doneWithResponse = false
        var sentenceLength = 0
        var sentencesLeftInParagraph = randomParagraphSentences()
        var paragraphsLeftInResponse = randomParagraphs()

        // Keep generating words until we've filled out a sufficiently long response.
        while (!doneWithResponse) {
            // Populate a list of bi-grams and tri-grams which match the current context (last two words spoken).
            val trigramCandidates = trigrams.filter { it.take(2).map { it.toLowerCase() }.equals(listOf(twoWordsAgo, oneWordAgo).map { it.toLowerCase() }) }
            val bigramCandidates = bigrams.filter { it.first().toLowerCase().equals(oneWordAgo.toLowerCase()) }

            // Figure out what the next word to speak should be.
            val nextToken = chooseNextToken(bigramCandidates, trigramCandidates)

            // Do a little formatting depending on where we are in our sentence.
            val formatted = formatToken(nextToken, atStartOfSentence)

            // Speak!
            print(formatted)

            // If we just used a sentence-terminator, we must be done with this sentence.
            if (SentenceTerminators.contains(nextToken) || sentenceLength > 50) {
                atStartOfSentence = true
                sentenceLength = 0
                sentencesLeftInParagraph -= 1

                // That's enough sentences. End the paragraph.
                if (sentencesLeftInParagraph < 0) {
                    println("\n")
                    sentencesLeftInParagraph = randomParagraphSentences()
                    paragraphsLeftInResponse -= 1

                    twoWordsAgo = ""
                    oneWordAgo = pickStartWord(seedWords, unigrams)

                    // That's enough paragraphs. We've proved our point. Let's let the next candidate speak.
                    if (paragraphsLeftInResponse < 0) doneWithResponse = true
                } else {
                    // Next sentence in this paragraph. Leave a little room.
                    print(" ")
                }
            } else {
                // Still speaking this sentence.
                atStartOfSentence = false
                sentenceLength += 1
            }

            // Remember the last two words we've spoken.
            twoWordsAgo = oneWordAgo
            oneWordAgo = nextToken
        }
    }

    /**
     * Chooses the start word for a sentence, attempting to pick a seed/contextual word first, then
     * grabbing one from our vocabulary as a fallback.
     */
    private fun pickStartWord(seedWords: List<String>, unigrams: NgramFrequencyTable): String {
        // Select candidates from seed words which match unigrams in our vocabulary.
        val startWordCandidates = unigrams.filter { seedWords.contains(it.first().toLowerCase()) }

        // If we can pick a seed word, do it sometimes, otherwise just choose a random vocabulary word.
        return if (startWordCandidates.entries.isNotEmpty() && random.nextBoolean()) {
            startWordCandidates.chooseOneWeighted(random, 0.4)
        } else {
            unigrams.chooseOneUniform(random)
        }.first()
    }

    /**
     * Pick a random number of sentences for a paragraph.
     */
    private fun randomParagraphSentences() = 4 + random.nextInt(4)

    /**
     * Pick a random number of paragraphs for a response.
     */
    private fun randomParagraphs() = 2 + random.nextInt(3)

    /**
     * Given a set of n-gram candidates, picks the best token to output next.
     */
    private fun chooseNextToken(bigramCandidates: NgramFrequencyTable, trigramCandidates: NgramFrequencyTable): String {
        return if (trigramCandidates.entries.isNotEmpty()) {
            // Prefer tri-gram candidates if available.
            trigramCandidates.chooseOneWeighted(random).last()
        } else if (bigramCandidates.entries.isNotEmpty()) {
            // Otherwise take a matching bi-gram.
            bigramCandidates.chooseOneWeighted(random).last()
        } else {
            // If no n-grams match, just finish the sentence.
            "."
        }
    }

    /**
     * Formats a token based on sentence context and what sort of token we're printing.
     */
    private fun formatToken(token: String, atStartOfSentence: Boolean): String {
        return if (atStartOfSentence) {
            // Capitalize the first word of a sentence.
            token.capitalize()
        } else {
            if (token.first().isLetterOrDigit() || token.length > 1) {
                // Leave some space before the next token if it's a word.
                " " + token
            } else {
                // Otherwise it's punctuation, no space needed.
                token
            }
        }
    }
}
