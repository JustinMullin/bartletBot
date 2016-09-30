package xyz.jmullin

import java.io.BufferedReader
import java.io.FileReader
import java.security.SecureRandom
import java.util.stream.Collectors

/**
 * Stores an n-gram frequencies table and allows weighted n-gram selection.
 */
data class NgramFrequencyTable(val entries: Map<List<String>, Int>) {
    /**
     * Total entries in this n-gram table, for calculating frequencies.
     */
    val total = entries.values.sum()

    /**
     * Filters the entries in this table.
     */
    fun filter(p: (List<String>) -> Boolean) = NgramFrequencyTable(entries.filter { p(it.key) })

    /**
     * Choosed an n-gram from this table uniformly.
     */
    fun chooseOneUniform(random: SecureRandom): List<String> {
        return entries.keys.toList()[random.nextInt(entries.size-1)]
    }

    /**
     * Chooses an n-gram from this table weighted based on relative frequency.
     *
     * @param multiplier A multiplier (0-1 inclusive) to apply to the random roll to bias it to the
     * start or end of the table. If the table is sorted lower this can bias towards
     */
    fun chooseOneWeighted(random: SecureRandom, multiplier: Double=1.0): List<String> {
        val roll = random.nextInt((total * multiplier).toInt())

        // Step through until we've reached the roll position.
        var i = 0
        val selected = entries.toList().find {
            i += it.second
            i >= roll
        }

        return selected?.first ?: entries.keys.first()
    }

    companion object {
        /**
         * Loads an NgramFrequencyTable from disk, given a storage format regex pattern.
         */
        fun loadNgrams(path: String, pattern: Regex): NgramFrequencyTable {
            val lines = BufferedReader(FileReader(path)).lines()
            return NgramFrequencyTable(lines.collect(Collectors.toList<String>())
                .map { pattern.matchEntire(it) }
                .map { Pair(it!!.groupValues.drop(2), it.groupValues[1].toInt()) }
                .toMap())
        }
    }
}