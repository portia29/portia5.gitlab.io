import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpsServer
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.nio.file.Path

class SimpleServerMain {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val port = if (args.isNotEmpty()) args[0].toInt() else SimpleServerMain().defaultPort
            val home = if (args.size > 1) Path.of(args[1])
                .toRealPath() else UtilsMy.projectDir.resolve("public")
            SimpleServerMain().start(port, home, false)
        }
    }

    val defaultPort = 8080
    private val defaultHome: Path = UtilsMy.projectDir.resolve("public")

    fun start() {
        start(defaultPort, defaultHome, false)
    }

    fun start(port: Int, home: Path, https: Boolean) {
        println("Home: $home")
        val server = if (https) {
            HttpsServer.create(InetSocketAddress(port), 0)
        } else {
            HttpServer.create(InetSocketAddress(port), 0)
        }
        val context = server.createContext("/")
        val redirect = HashMap<String, String>()
        val map = HashMap<String, File>()
        val homeFile = home.toFile()
        homeFile.walk().forEach {
            if (homeFile == it) {
                return@forEach
            }
            val name = it.name
            if (name.startsWith(".")) throw IllegalStateException(it.toString())
            var relativePath = it.relativeTo(home.toFile()).path
            if (relativePath == "index.html") {
                map["/"] = it
                return@forEach
            }
            relativePath = "/$relativePath"
            if (relativePath.endsWith("/index.html")) {
                val stripped = relativePath.removeSuffix("/index.html")
                map[stripped] = it
                redirect["$stripped/"] = stripped
            } else if (name.endsWith(".html")) {
                map[relativePath] = it
                map[relativePath.removeSuffix(".html")] = it
            } else if (name.endsWith(".css") || name.endsWith(".png") || name.endsWith(".svg")) {
                map[relativePath] = it
            }
        }
        context.setHandler { exchange ->
            println("Request: ${exchange.requestURI.path}")
            val redirectLocation = redirect[exchange.requestURI.path]
            if (redirectLocation != null) {
                exchange.responseHeaders["Location"] = redirectLocation
                exchange.sendResponseHeaders(302, 0)
            } else {
                val file = map[exchange.requestURI.path]
                if (file != null) {
                    if (file.name.endsWith(".svg")) {
                        // Chrome don't displays SVGs without proper Content-Type. PNGs without
                        // specified Content-Type displayed normally.
                        exchange.responseHeaders["Content-Type"] = listOf("image/svg+xml")
                    }
                    exchange.sendResponseHeaders(200, file.length())
                    FileInputStream(file).use {
                        it.copyTo(exchange.responseBody)
                    }
                } else {
                    exchange.sendResponseHeaders(404, 0)
                }
            }
            // Will hang indefinitely without explicit "close()".
            exchange.responseBody.close()
        }
        server.executor = null
        println("Listening at http${if (https) "s" else ""}://localhost:$port/")
        server.start()
    }
}