package org.nsh07.wikireader.parser

/**
 * Remove/simplify parts of wikitext
 *
 * @param wikitext Source Wikitext to clean up
 */
fun cleanUpWikitext(wikitext: String): String {
    return expandMedalsTables(wikitext)
        .replace("<!--.+?-->".toRegex(), "")
        .replace("</?onlyinclude>".toRegex(RegexOption.IGNORE_CASE), "")
        .replace("== \n", "==\n")
        // Convert colon-indented math to display math for proper block rendering
        // This ensures math like ": <math>formula</math>" is extracted as a block element
        .replace(
            "^:+\\s*<math(?![^>]*display)".toRegex(RegexOption.MULTILINE),
            "<math display=\"block\""
        )
        .replace(
            "\\{\\{nobility table header.*?\\}\\}"
                .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
            "{| class=\"wikitable\"\n"
        )
}

private fun expandMedalsTables(wikitext: String): String {
    var result = wikitext
    var start = result.indexOf("{{Medals table", ignoreCase = true)

    while (start >= 0) {
        val template = result.substringMatchingParen('{', '}', start)
        if (!template.endsWith("}}")) break

        val parameters = template.removePrefix("{{").removeSuffix("}}")
            .splitNotInBraces('|', '{', '}')
            .drop(1)
            .mapNotNull {
                val parts = it.split('=', limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
            }
            .toMap()
        val teams = parameters.keys.mapNotNull {
            it.takeIf { key -> key.startsWith("gold_", ignoreCase = true) }
                ?.substringAfter('_')
        }
        val table = buildString {
            append("{| class=\"wikitable\"\n")
            parameters["caption"]?.let { append("|+ $it\n") }
            append("! Rank !! NOC !! Gold !! Silver !! Bronze !! Total\n")
            teams.forEachIndexed { index, team ->
                val gold = parameters.entries.firstOrNull { it.key.equals("gold_$team", true) }?.value ?: "0"
                val silver = parameters.entries.firstOrNull { it.key.equals("silver_$team", true) }?.value ?: "0"
                val bronze = parameters.entries.firstOrNull { it.key.equals("bronze_$team", true) }?.value ?: "0"
                val total = listOf(gold, silver, bronze).sumOf { it.toIntOrNull() ?: 0 }
                append("|-\n| ${index + 1} || $team || $gold || $silver || $bronze || $total\n")
            }
            append("|}")
        }
        result = result.replaceRange(start, start + template.length, table)
        start = result.indexOf("{{Medals table", start + table.length, ignoreCase = true)
    }

    return result
}
