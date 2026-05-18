
class IncludeTransform(private val generator: Generator) {

    private fun onInclude(page: Page,
        parag: String,
        paragIterator: MutableListIterator<String>,
        section: MutableList<String>,
        sectionsIterator: MutableListIterator<MutableList<String>>
    ) {
        if (page.isMap && generator.firstRun) return
        val commands = parag.split(" ").toMutableList()
        if (!commands.remove("#include")) throw IllegalStateException()
        val withLink = commands.remove(UtilsMy.includeLink)
        val asSection = commands.remove(UtilsMy.includeSection)
        val path = commands.removeLast()
        val e = "Missing page for path \"$path\" in page \"${page.relativeUrl}\""
        val includedPage = generator.sitemap.srcPages[path] ?: throw IllegalStateException(e)
        transform(includedPage)
        if (commands.isEmpty()) {
            if (!asSection && includedPage.summaryParag.isNotEmpty()) {
                paragIterator.remove()
                paragIterator.add(includedPage.summaryParag(withLink))
            } else if (includedPage.summarySection.isNotEmpty()) {
                paragIterator.remove()
                includedPage.summarySection(withLink).forEach {
                    paragIterator.add(it)
                }
            } else {
                throw IllegalStateException("$path in ${page.relativeUrl}")
            }
        } else {
            throw IllegalStateException("${page.relativeUrl} $parag")
        }
    }

    fun transform(page: Page) {
        if (page.abstracts.isNotEmpty()) return
        page.abstracts.addAll(page.formatted.split(UtilsMy.abstractSeparator).map { supersection ->
            supersection.split(UtilsMy.sectionSeparator).map { section ->
                section.split(UtilsMy.paragSeparator).map { it }.toMutableList()
            }.toMutableList()
        })
        page.abstracts.forEach { abstract ->
            val sectionsIterator = abstract.listIterator()
            for (section in sectionsIterator) {
                val paragIterator = section.listIterator()
                for (parag in paragIterator) {
                    if (parag.startsWith(UtilsMy.includeTag)) {
                        onInclude(page, parag, paragIterator, section, sectionsIterator)
                    }
                }
            }
        }
        page.includeText = page.abstracts.joinToString(separator = UtilsMy.abstractSeparator) { abstract ->
            abstract.joinToString(separator = UtilsMy.sectionSeparator) { section ->
                section.joinToString(separator = UtilsMy.paragSeparator) { it }
            }
        }
    }
}