package org.nsh07.wikireader.parser

/**
 * Remove/simplify parts of wikitext
 *
 * @param wikitext Source Wikitext to clean up
 */
fun cleanUpWikitext(wikitext: String): String {
    return wikitext
        .replace("<!--.+?-->".toRegex(), "")
        .replace("</?onlyinclude>".toRegex(RegexOption.IGNORE_CASE), "")
        .replace(
            "<section\\s+(?:begin|end)\\s*=.*?/>".toRegex(RegexOption.IGNORE_CASE),
            ""
        )
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
