package fr.breadeater.fileserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FileServer {
    private static final Map<String, String> mimeTypes = new HashMap<>();
    private static final String PROJECT_NAME = "FileServer";
    private static final String PROJECT_VERSION = "1.0.0";
    private static final String INDEX_HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>File Server</title>
            </head>
            <body>
                <h1>Index of [INDEX]</h1>
                <hr><br>
                <div id="filelist">
                    <br>
                    [FILES]
                </div>
                <br>
                <button id="goback">Go back</button>
                <hr>
                <p>[SERVERINFO]</p>
            
                <script>function _0x187d(_0x53091b,_0x103cef){const _0x2d4005=_0x2d40();return _0x187d=function(_0x187d20,_0x5c7269){_0x187d20=_0x187d20-0x1d9;let _0x383aa2=_0x2d4005[_0x187d20];return _0x383aa2;},_0x187d(_0x53091b,_0x103cef);}const _0x10784b=_0x187d;function _0x2d40(){const _0x5e9f11=['style','45282510nhOJMB','location','DOMContentLoaded','startsWith','1669197wDeDPH','8616937hxaRKw','544866XDLkAp','length','getElementById','260xNBfeq','6jqadKE','split','display','href','none','1693274lbzbcm','25596eftowN','goback','12910272BjJJlm'];_0x2d40=function(){return _0x5e9f11;};return _0x2d40();}(function(_0x2623a4,_0x1cb548){const _0x2f5126=_0x187d,_0x40897e=_0x2623a4();while(!![]){try{const _0x3c61b6=parseInt(_0x2f5126(0x1eb))/0x1+-parseInt(_0x2f5126(0x1e6))/0x2*(-parseInt(_0x2f5126(0x1e0))/0x3)+-parseInt(_0x2f5126(0x1ec))/0x4*(parseInt(_0x2f5126(0x1e5))/0x5)+parseInt(_0x2f5126(0x1e2))/0x6+parseInt(_0x2f5126(0x1e1))/0x7+parseInt(_0x2f5126(0x1da))/0x8+-parseInt(_0x2f5126(0x1dc))/0x9;if(_0x3c61b6===_0x1cb548)break;else _0x40897e['push'](_0x40897e['shift']());}catch(_0x32ebca){_0x40897e['push'](_0x40897e['shift']());}}}(_0x2d40,0xe401f),document['addEventListener'](_0x10784b(0x1de),()=>{const _0x11a602=_0x10784b,_0x47e56b=document[_0x11a602(0x1e4)](_0x11a602(0x1d9)),_0x5e920a=window[_0x11a602(0x1dd)]['pathname'],_0x1e3067=_0x5e920a[_0x11a602(0x1e7)]('/');var _0x1a935b='';if(_0x5e920a==='/')_0x47e56b[_0x11a602(0x1db)][_0x11a602(0x1e8)]=_0x11a602(0x1ea);_0x1e3067[_0x1e3067[_0x11a602(0x1e3)]-0x1]=null;for(var _0xd24c0d=0x0;_0xd24c0d<_0x1e3067[_0x11a602(0x1e3)];_0xd24c0d++){if(_0x1e3067[_0xd24c0d]!==null){var _0x31a563=_0x1e3067[_0xd24c0d];if(!_0x31a563[_0x11a602(0x1df)]('/'))_0x31a563='/'+_0x31a563;_0x1a935b=_0x31a563;}}_0x47e56b['onclick']=()=>window[_0x11a602(0x1dd)][_0x11a602(0x1e9)]=_0x1a935b;}));</script>
            </body>
            </html>
            """;

    static {
        // MIME Type Sources: https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types/Common_types
        mimeTypes.put(".aac", "audio/aac");
        mimeTypes.put(".abw", "application/x-abiword");
        mimeTypes.put(".apng", "image/apng");
        mimeTypes.put(".arc", "application/x-freearc");
        mimeTypes.put(".avif", "image/avif");
        mimeTypes.put(".avi", "video/x-msvideo");
        mimeTypes.put(".azw", "application/vnd.amazon.ebook");
        mimeTypes.put(".bin", "application/octet-stream");
        mimeTypes.put(".bmp", "image/bmp");
        mimeTypes.put(".bz", "application/x-bzip");
        mimeTypes.put(".bz2", "application/x-bzip2");
        mimeTypes.put(".cda", "application/x-cdf");
        mimeTypes.put(".csh", "application/x-csh");
        mimeTypes.put(".css", "text/css");
        mimeTypes.put(".csv", "text/csv");
        mimeTypes.put(".doc", "application/msword");
        mimeTypes.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"); // Wtf microsoft decided to make this MIME type long as fuck ?
        mimeTypes.put(".eot", "application/vnd.ms-fontobject");
        mimeTypes.put(".epub", "application/epub+zip");
        mimeTypes.put(".gz", "application/gzip"); // application/x-gzip for Windows / macOS but who cares about the non-standard .gz while zip is better for Windows / macOS
        mimeTypes.put(".gif", "image/gif");
        mimeTypes.put(".html", "text/html");
        mimeTypes.put(".htm", "text/html");
        mimeTypes.put(".ico", "image/vnd.microsoft.icon");
        mimeTypes.put(".ics", "text/calendar");
        mimeTypes.put(".jar", "application/java-archive"); // Fuck 100% NodeJS projects, all my homies uses Java
        mimeTypes.put(".jpeg", "image/jpeg");
        mimeTypes.put(".jpg", "image/jpeg");
        mimeTypes.put(".js", "application/javascript");
        mimeTypes.put(".json", "application/json");
        mimeTypes.put(".jsonld", "application/ld+json");
        mimeTypes.put(".mid", "audio/midi");
        mimeTypes.put(".midi", "audio/x-midi");
        mimeTypes.put(".mjs", "application/javascript");
        mimeTypes.put(".mp3", "audio/mpeg");
        mimeTypes.put(".mp4", "video/mp4");
        mimeTypes.put(".mpeg", "video/mpeg");
        mimeTypes.put(".mpkg", "application/vnd.apple.installer+xml");
        mimeTypes.put(".odp", "application/vnd.oasis.opendocument.presentation");
        mimeTypes.put(".ods", "application/vnd.oasis.opendocument.spreadsheet");
        mimeTypes.put(".odt", "application/vnd.oasis.opendocument.text");
        mimeTypes.put(".oga", "audio/ogg");
        mimeTypes.put(".ogv", "video/ogg");
        mimeTypes.put(".ogx", "application/ogg");
        mimeTypes.put(".opus", "audio/ogg");
        mimeTypes.put(".otf", "font/otf");
        mimeTypes.put(".png", "image/png");
        mimeTypes.put(".pdf", "application/pdf"); // PDF is good but so annoying to use -_-
        mimeTypes.put(".php", "application/x-httpd-php"); // PHP is based.
        mimeTypes.put(".ppt", "application/vnd.ms-powerpoint");
        mimeTypes.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"); // Another long ass MIME type for no reasons
        mimeTypes.put(".rar", "application/vnd.rar"); // rar is rare :>
        mimeTypes.put(".rtf", "application/rtf"); // Markdown copycat ahh
        mimeTypes.put(".sh", "application/x-sh");
        mimeTypes.put(".svg", "image/svg+xml");
        mimeTypes.put(".tar", "application/x-tar");
        mimeTypes.put(".tif", "image/tiff");
        mimeTypes.put(".tiff", "image/tiff");
        mimeTypes.put(".ts", "video/mp2t"); // Also means TypeScript but since its more commonly used as mpeg transport stream then its video/mp2t
        mimeTypes.put(".ttf", "font/ttf");
        mimeTypes.put(".txt", "text/plain");
        mimeTypes.put(".vsd", "application/vnd.visio");
        mimeTypes.put(".wav", "audio/wav");
        mimeTypes.put(".weba", "audio/webm");
        mimeTypes.put(".webm", "video/webm");
        mimeTypes.put(".webp", "image/webp");
        mimeTypes.put(".woff", "font/woff");
        mimeTypes.put(".woff2", "font/woff2");
        mimeTypes.put(".xhtml", "application/xhtml+xml"); // Wtf is XHTML ?
        mimeTypes.put(".xls", "application/vnd.ms-excel");
        mimeTypes.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // Stop letting microsoft creating MIME types, it will be good for everyone
        mimeTypes.put(".xml", "application/xml");
        mimeTypes.put(".xul", "application/vnd.mozilla.xul+xml");
        mimeTypes.put(".zip", "application/zip"); // Why the fuck windows always try to use non-standard MIME types, just use standard ones and shut the fuck up
        mimeTypes.put(".3gp", "video/3gpp");
        mimeTypes.put(".3g2", "video/3gpp2");
        mimeTypes.put(".7z", "application/x-7z-compressed");
    }

    public static void main(String[] args){
        try {
            Executor threadPool = Executors.newCachedThreadPool();
            Map<String, String> config = Config.parse();

            HttpServer server = HttpServer.create();

            server.setExecutor(threadPool);
            server.createContext("/", new Handler(config));
            server.bind(new InetSocketAddress(Integer.parseInt(config.get("HTTP_PORT"))), 0);
            server.start();

            Logger.log("Started on port " + config.get("HTTP_PORT") + " !");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String probeContentType(File file){
        for (Map.Entry<String, String> entry : FileServer.mimeTypes.entrySet()){
            if (file.getName().endsWith(entry.getKey())) return entry.getValue();
        }

        return "text/plain";
    }

    private static class Handler implements HttpHandler {
        private Map<String, String> config;

        public Handler(Map<String, String> config){ this.config = config; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                File publicdir = new File(this.config.get("PUBLIC_DIR"));

                if (!publicdir.exists()) publicdir.mkdir();

                String files = "";
                OutputStream out = exchange.getResponseBody();
                URI uri = exchange.getRequestURI();

                File dir = new File(publicdir.getCanonicalPath() + uri.getPath());

                if (dir.exists() && dir.isDirectory()) for (File file : dir.listFiles()){
                    String filename = file.getName();
                    String url = uri.getPath();

                    if (!url.equals("/")) filename = "/" + filename;

                    String redirectpath = uri.getPath() + filename;

                    files += "<div class=\"files\"><a href=\"" + redirectpath + "\">" + file.getName() + "</a></div><br>";
                }

                if (dir.exists() && dir.isDirectory() && dir.listFiles().length == 0) files = "No files found !<br><br>";

                String content = FileServer.INDEX_HTML;

                content = content.replace("[INDEX]", uri.getPath());
                content = content.replace("[FILES]", files);
                content = content.replace("[SERVERINFO]", FileServer.PROJECT_NAME + " " + FileServer.PROJECT_VERSION + " on port " + config.get("HTTP_PORT") + " (Powered by Java HttpServer)");

                if (dir.isFile()){
                    exchange.getResponseHeaders().add("Content-Type", probeContentType(dir));

                    content = new String(new FileInputStream(dir).readAllBytes(), StandardCharsets.UTF_8);
                } else exchange.getResponseHeaders().add("Content-Type", "text/html");

                exchange.sendResponseHeaders(200, content.getBytes(StandardCharsets.UTF_8).length);

                out.write(content.getBytes(StandardCharsets.UTF_8));
                out.flush();

                exchange.close();

                Logger.log(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath() + " " + exchange.getRemoteAddress().getAddress().toString().replace("/", ""));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Logger {
        public static void log(String message){
            System.out.println("[" + FileServer.PROJECT_NAME.toUpperCase() + "]: " + message);
        }
    }

    private static class Config {
        public static Map<String, String> parse() throws IOException {
            try {
                Map<String, String> config = new HashMap<>();
                File configfile = new File("./.env");

                if (!configfile.exists()){
                    configfile.createNewFile();

                    PrintWriter writer = new PrintWriter(configfile);

                    writer.write("""
                        HTTP_PORT=80
                        PUBLIC_DIR=./public
                        """);
                    writer.close();
                }

                Scanner reader = new Scanner(configfile);

                while (reader.hasNextLine()){
                    String[] line = reader.nextLine().split("=", 2);

                    config.put(line[0], line[1]);
                }

                return config;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
