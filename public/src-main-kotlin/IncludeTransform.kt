import Constants.abstractSeparator
import Constants.includeLink
import Constants.includeSection
import Constants.includeTag
import Constants.paragSeparator
import Constants.sectionSeparator

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
        val withLink = commands.remove(includeLink)
        val asSection = commands.remove(includeSection)
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
        page.abstracts.addAll(page.formatted.split(abstractSeparator).map { supersection ->
            supersection.split(sectionSeparator).map { section ->
                section.split(paragSeparator).map { it }.toMutableList()
            }.toMutableList()
        })
        page.abstracts.forEach { abstract ->
            val sectionsIterator = abstract.listIterator()
            for (section in sectionsIterator) {
                val paragIterator = section.listIterator()
                for (parag in paragIterator) {
                    if (parag.startsWith(includeTag)) {
                        onInclude(page, parag, paragIterator, section, sectionsIterator)
                    }
                }
            }
        }
        page.includeText = page.abstracts.joinToString(separator = abstractSeparator) { abstract ->
            abstract.joinToString(separator = sectionSeparator) { section ->
                section.joinToString(separator = paragSeparator) { it }
            }
        }
    }
}