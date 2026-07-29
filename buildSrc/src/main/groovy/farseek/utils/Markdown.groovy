package farseek.utils

// https://www.markdownguide.org
abstract class Markdown {
    // https://www.markdownguide.org/basic-syntax/#links
    static String link(String caption, URI uri) { "[$caption]($uri)" }
    static String link(URI uri) { link(uri.toString(), uri) }
    static String imageLink(URI imageUri, URI targetUri, String altText = "") {
        link("![$altText]($imageUri)", targetUri)
    }
}
