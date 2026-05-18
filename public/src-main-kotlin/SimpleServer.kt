import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpsServer
import java.io.FileInputStream
import java.net.InetSocketAddress

class SimpleServer {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            SimpleServer().main()
        }
    }

    class UrlsMap {
        private val redirect = mutableMapOf<String, UrlMy>()
        private val serve: Map<String, UrlMy>

        init {
            val sitemap = Sitemap(Context())
            sitemap.updateUrls()
            serve = sitemap.urls.associateBy { it.relativeUrl }
            sitemap.urls.forEach { url ->
                redirect.putAll(url.redirects.associateWith { url })
            }
        }

        fun handle(exchange: HttpExchange): Boolean {
            val redirectResult = redirect[exchange.requestURI.path]
            if (redirectResult != null) {
                exchange.responseHeaders["Location"] = redirectResult.relativeUrl
                exchange.sendResponseHeaders(302, 0)
                return true
            }
            val serveResult = serve[exchange.requestURI.path] ?: return false
            val file = if (serveResult.isRaw) {
                serveResult.srcAbsolutePath.toFile()
            } else {
                serveResult.dstAbsolutePath.toFile()
            }
            if (!file.exists()) {
                return false
            }
            if (file.name.endsWith(".svg")) {
                // Chrome don't displays SVGs without proper Content-Type. PNGs without
                // specified Content-Type displayed normally.
                exchange.responseHeaders["Content-Type"] = listOf("image/svg+xml")
            }
            exchange.sendResponseHeaders(200, file.length())
            FileInputStream(file).use { it.copyTo(exchange.responseBody) }
            return true
        }
    }

    private val secure = false
    private val port = 8080
    private lateinit var urlsMap: UrlsMap

    fun main() {
        urlsMap = UrlsMap()
        val server = if (secure) {
            HttpsServer.create(InetSocketAddress(port), 0)
        } else {
            HttpServer.create(InetSocketAddress(port), 0)
        }
        val context = server.createContext("/")
        context.setHandler { exchange ->
            handle(exchange, false)
            // Will hang indefinitely without explicit "close()".
            exchange.responseBody.close()
        }
        server.executor = null
        println("Home is '${UtilsMy.dstMainDir}'," +
                " listening at http${if (secure) "s" else ""}://localhost:$port/")
        server.start()
    }

    private fun handle(exchange: HttpExchange, mapsUpdated: Boolean) {
        println("Request: ${exchange.requestURI.path}")
        if (!urlsMap.handle(exchange)) {
            if (mapsUpdated) {
                exchange.sendResponseHeaders(404, 0)
            } else {
                urlsMap = UrlsMap()
                handle(exchange, true)
            }
        }
    }
}