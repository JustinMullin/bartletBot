package xyz.jmullin

import org.jsoup.Jsoup

/**
 * This phase of the process downloads West Wing transcripts,
 * finds lines from President Bartlet, and saves them for later n-gram-ification.
 */
fun main(args: Array<String>) {
    // Download and scrape the transcripts of the first 90 episodes of The West Wing.
    // This corresponds to seasons 1-4, aka the Sorkin Seasons, aka the really good ones.
    for(i in 1..91) {
        DownloadTranscripts.downloadEpisode(i)
    }
}

object DownloadTranscripts {
    /**
     * Downloads and scrapes a single episode transcript.
     */
    fun downloadEpisode(episode: Int) {
        // Big thanks to westwingtranscripts.com for the awesome service!
        // We're scraping our text straight from their HTML 'cause I couldn't find downloads.
        val url = "http://www.westwingtranscripts.com/search.php?flag=getTranscript&id=$episode"

        println("Reading episode $episode from $url ...")

        // Connect to the URL, grab the big block of transcript text.
        val transcript = Jsoup.connect(url)
            .get()
            .select("blockquote")
            .select("pre")
            .text()

        // We only want to read lines said by the man himself.
        val bartletNames = setOf(
            "BARTLET",
            "PRESIDENT BARTLET",
            "JED BARTLET",
            "PRESIDENT JED BARTLET",
            "BARTLET [cont.]"
        )

        // Find all the lines spoken by President Bartlet, then take all the dialogue associated with them.
        val bartletLines = transcript.lines().withIndex()
            .filter { bartletNames.contains(it.value) }
            .map {
                transcript.lines()
                    .drop(it.index+1)
                    .takeWhile { it.trim().isNotEmpty() }
                    .joinToString(" ")
            }

        // Write our transcript to disk for later n-gram-ification.
        OutputUtils.write("transcripts/episode$episode.txt", bartletLines.joinToString("\n"))

        println("Episode $episode downloaded and saved.")
    }
}