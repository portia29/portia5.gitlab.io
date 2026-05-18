import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TextTypographyTest {

    @Test
    fun beautifyLine() {
        val lineIn = " - Start space-dash. And - dash in middle... And - another dash..."
        val lineOut = " — Start space-dash. And — dash in middle… And — another dash…"
        assertEquals(lineOut, TextTypography().transformLine(TestUtils.url, lineIn))
    }

    @Test
    fun beautifyParagraphs() {
        val textIn = " - Start space-dash. And - dash in middle... And - another dash...\n" +
                "- Start dash. Hello!\n\n" +
                "- Another start dash. And - dash..."
        val textOut = " — Start space-dash. And — dash in middle… And — another dash…\n" +
                "— Start dash. Hello!\n\n" +
                "— Another start dash. And — dash…"
        assertEquals(textOut, TextTypography().transform(TestUtils.url, textIn))
    }

    @Test
    fun stringSplit() {
        var lineIn = "    4 spaces."
        var lineOut = listOf("", "", "", "", "4", "spaces.")
        assertEquals(lineOut, lineIn.split(' '))
        lineIn = "   3 spaces  2 spaces."
        lineOut = listOf("", "", "", "3", "spaces", "", "2", "spaces.")
        assertEquals(lineOut, lineIn.split(' '))
        lineIn = "  2 spaces   3 spaces."
        lineOut = listOf("", "", "2", "spaces", "", "", "3", "spaces.")
        assertEquals(lineOut, lineIn.split(' '))
        lineIn = " 1 space."
        lineOut = listOf("", "1", "space.")
        assertEquals(lineOut, lineIn.split(' '))
    }

    @Test
    fun lineTransformer() {
        var transformer = LineTransform(false)
        var lineIn = "0 spaces."
        var lineOu = "0 spaces."
        assertEquals(lineOu, transformer.transform(TestUtils.url, lineIn) {
                url: UrlMy, word: String -> word
        })
        lineIn = " 1 space."
        lineOu = " 1 space."
        assertEquals(lineOu, transformer.transform(TestUtils.url, lineIn) {
                url: UrlMy, word: String -> word
        })
        lineIn = "   3 spaces,  2 spaces."
        lineOu = "   3 spaces,  2 spaces."
        assertEquals(lineOu, lineIn.split(' ').joinToString(" "))
        assertEquals(lineOu, transformer.transform(TestUtils.url, lineIn) {
                url: UrlMy, word: String -> word
        })
        lineIn = "    4 spaces."
        lineOu = "    4 spaces."
        assertEquals(lineOu, transformer.transform(TestUtils.url, lineIn) {
                url: UrlMy, word: String -> word
        })

        transformer = LineTransform(false, LineTransform().simpleSpacesTransformer)
        lineIn = "   3 spaces,  2 spaces."
        lineOu = "&nbsp;&nbsp;&nbsp;3 spaces,&nbsp;&nbsp;2 spaces."
        assertEquals(lineOu, transformer.transform(TestUtils.url, lineIn) {
                url: UrlMy, word: String -> word
        })

        transformer = LineTransform(true, LineTransform().simpleSpacesTransformer)
        lineIn = " 1 space."
        lineOu = "&nbsp;1 space."
        assertEquals(lineOu, transformer.transform(TestUtils.url, lineIn) {
                url: UrlMy, word: String -> word
        })

        transformer = LineTransform()
        lineIn = "   3 spaces,  2 spaces."
        assertFailsWith(LineTransform.MultispacesOnlyAtStart::class) {
            transformer.transform(TestUtils.url, lineIn) {
                    url: UrlMy, word: String -> word
            }
        }
    }
}