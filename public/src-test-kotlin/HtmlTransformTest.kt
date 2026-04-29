
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTransformTest {

    private val htmlTransform: HtmlTransform = HtmlTransform()

    @Test
    fun transformLine() {
        var lineIn = "\"The Map of Mathematics\" by Domain of Science:"
        var lineOut = lineIn
        assertEquals(lineOut, htmlTransform.transformLine(TestUtils.url, lineIn))

        lineIn = " — https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"
        lineOut = "&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>" +
                "?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz</a>"
        assertEquals(lineOut, htmlTransform.transformLine(TestUtils.url, lineIn))

        lineIn = "    — https://youtu.be/OJ4B"
        lineOut = "&nbsp;&nbsp;&nbsp;&nbsp;— <a href=\"https://youtu.be/OJ4B\">" +
                "https://youtu.be/OJ4B</a>"
        assertEquals(lineOut, htmlTransform.transformLine(TestUtils.url, lineIn))

        lineIn = "    — https://youtu.be/OJ4BLKJIRGKSHDKFSHAG"
        lineOut = "&nbsp;&nbsp;&nbsp;&nbsp;— <a href=\"https://youtu.be/OJ4BLKJIRGKSHDKFSHAG\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OJ4BLKJIRGKSHDKFSHAG</a>"
        assertEquals(lineOut, htmlTransform.transformLine(TestUtils.url, lineIn))

        lineIn = "<///><///><///><///><///><///>"
        lineOut = "&lt;///&gt;&lt;///&gt;&lt;///&gt;&lt;///&gt;&lt;///&gt;&lt;///&gt;"
        assertEquals(lineOut, htmlTransform.transformLine(TestUtils.url, lineIn))
    }

    @Test
    fun isFootnote() {
        var input = "world![1]"
        assertEquals(true, htmlTransform.footnote.matches(input))
        input = "world![1][2][3]"
        assertEquals(true, htmlTransform.footnote.matches(input))
        input = "world![132122][3][111]"
        assertEquals(true, htmlTransform.footnote.matches(input))
        input = "world![132122][3][111]"
        assertEquals(true, htmlTransform.footnote.matches(input))
        input = "world!"
        assertEquals(false, htmlTransform.footnote.matches(input))
        input = "wo[rl]d!"
        assertEquals(false, htmlTransform.footnote.matches(input))
        input = "world[1]!"
        assertEquals(false, htmlTransform.footnote.matches(input))
        input = "world[1]."
        assertEquals(false, htmlTransform.footnote.matches(input))
        input = "[1]"
        assertEquals(false, htmlTransform.footnote.matches(input))
        input = "[1]word!"
        assertEquals(false, htmlTransform.footnote.matches(input))
        input = "world![.]"
        assertEquals(false, htmlTransform.footnote.matches(input))
    }

    @Test
    fun transformParagraph() {
        val paragraphIn = "\"The Map of Mathematics\" by Domain of Science:" +
                "\n" +
                " — https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"

        // xhmtlCompatibleVoidElements = false
        var paragraphOut = "<p>\"The Map of Mathematics\" by Domain of Science:" +
                "\n" +
                "        <br>&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>" +
                "?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz</a></p>"
        // Test.
        assertEquals(paragraphOut, HtmlTransform(false).transformParagraph(TestUtils.url, paragraphIn))

        // xhmtlCompatibleVoidElements = true
        paragraphOut = "<p>\"The Map of Mathematics\" by Domain of Science:" +
                "\n" +
                "        <br/>&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr/>//<wbr/>youtu<wbr/>.be<wbr/>/OmJ<wbr/>-4B<wbr/>-mS<wbr/>-Y<wbr/>" +
                "?si<wbr/>=<wbr/>bBWOSbdlpQ7kV9Bz</a></p>"
        // Test.
        assertEquals(paragraphOut, HtmlTransform(true).transformParagraph(TestUtils.url, paragraphIn))
    }

    @Test
    fun textToHtml() {
        /*
        val resourcesDir = Paths.get("src/test/resources")
        assert(resourcesDir.exists())
        val srcRelPath = Path.of("test1.txt")
        val srcAbsPath = resourcesDir.resolve(srcRelPath)
        val textString = srcAbsPath.toFile().readText()
        val expectedHtmlString = resourcesDir.resolve("test1.html").toFile().readText()

        val page = Page(RatUrl(srcAbsPath, srcRelPath, resourcesDir))
        val pages = mapOf(page.relativeUrl to page)
        val includeTransform = IncludeTransform()
        includeTransform.transform(pages, page)
        page.beautyText = TextBeautifier().transform(TestUtils.url, page.includeText)
        val bodyHtml = htmlTransform.textToHtml(TestUtils.url, page.beautyText)
        val htmlPage = htmlTransform.htmlPage(page.title, bodyHtml, page.navigation)
        assertEquals(expectedHtmlString, htmlPage)
        */
    }
}