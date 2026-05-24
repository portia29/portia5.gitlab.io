
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTransformTest {

    private val htmlTransform: HtmlTransform = HtmlTransform()
    
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
    fun longUrlLineBreaks() {
        val linkIn = "https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"
        // xhmtlCompatibleVoidElements = false
        var linkOut = "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>" +
                "?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz"
        assertEquals(linkOut, HtmlTransform(false).longUrlLineBreaks(linkIn))
        // xhmtlCompatibleVoidElements = true
        linkOut = "https:<wbr/>//<wbr/>youtu<wbr/>.be<wbr/>/OmJ<wbr/>-4B<wbr/>-mS<wbr/>-Y<wbr/>" +
                "?si<wbr/>=<wbr/>bBWOSbdlpQ7kV9Bz"
        assertEquals(linkOut, HtmlTransform(true).longUrlLineBreaks(linkIn))
    }

    @Test
    fun transformLink() {
        val linkIn = "https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"

        // xhmtlCompatibleVoidElements = false
        var linkOut = "<a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr>//<wbr>youtu<wbr>.be<wbr>/OmJ<wbr>-4B<wbr>-mS<wbr>-Y<wbr>" +
                "?si<wbr>=<wbr>bBWOSbdlpQ7kV9Bz</a>"
        assertEquals(linkOut, HtmlTransform(false).transformLink(TestUtils.url, linkIn))

        // xhmtlCompatibleVoidElements = true
        linkOut = "<a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr/>//<wbr/>youtu<wbr/>.be<wbr/>/OmJ<wbr/>-4B<wbr/>-mS<wbr/>-Y<wbr/>" +
                "?si<wbr/>=<wbr/>bBWOSbdlpQ7kV9Bz</a>"
        assertEquals(linkOut, HtmlTransform(true).transformLink(TestUtils.url, linkIn))
    }

    @Test
    fun splitLine() {
        var lineIn = "\"The Map of"
        var lineOut = listOf("\"The", " ", "Map", " ", "of")
        assertEquals(lineOut, UtilsMy.splitLine(lineIn))
        lineIn = " — https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz"
        lineOut = listOf(" ", "—", " ", "https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz")
        assertEquals(lineOut, UtilsMy.splitLine(lineIn))
        lineIn = "    — https://youtu.be/OJ4B F! "
        lineOut = listOf("    ", "—", " ", "https://youtu.be/OJ4B", " ", "F!", " ")
        assertEquals(lineOut, UtilsMy.splitLine(lineIn))
        lineIn = "    — https://youtu.be/   OJ4B F! "
        lineOut = listOf("    ", "—", " ", "https://youtu.be/", "   ", "OJ4B", " ", "F!", " ")
        assertEquals(lineOut, UtilsMy.splitLine(lineIn))
        lineIn = "<///><///><///><///><///><///>"
        lineOut = listOf("<///><///><///><///><///><///>")
        assertEquals(lineOut, UtilsMy.splitLine(lineIn))
    }
    
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
        assertEquals(paragraphOut, HtmlTransform(false).transformParagraph(TestUtils.url, paragraphIn))

        // xhmtlCompatibleVoidElements = true
        paragraphOut = "<p>\"The Map of Mathematics\" by Domain of Science:" +
                "\n" +
                "        <br/>&nbsp;— <a href=\"https://youtu.be/OmJ-4B-mS-Y?si=bBWOSbdlpQ7kV9Bz\">" +
                "https:<wbr/>//<wbr/>youtu<wbr/>.be<wbr/>/OmJ<wbr/>-4B<wbr/>-mS<wbr/>-Y<wbr/>" +
                "?si<wbr/>=<wbr/>bBWOSbdlpQ7kV9Bz</a></p>"
        assertEquals(paragraphOut, HtmlTransform(true).transformParagraph(TestUtils.url, paragraphIn))
    }

    @Test
    fun textToHtml() {
        val resourcesDir = Paths.get("src-test-res")
        assert(resourcesDir.exists())
        val srcRelPath = Path.of("test1.txt")
        val srcAbsPath = resourcesDir.resolve(srcRelPath)
        val page = Page(UrlMy(srcAbsPath, srcRelPath, resourcesDir))
        Generator().processPage(page)
        val expectedHtmlString = resourcesDir.resolve("test1-expected.html").toFile().readText()
        val actualHtmlString = resourcesDir.resolve("test1.html").toFile().readText()
        assertEquals(expectedHtmlString, actualHtmlString)
    }
}