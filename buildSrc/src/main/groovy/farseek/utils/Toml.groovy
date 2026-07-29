package farseek.utils

// https://toml.io
abstract class Toml {
    static List<String> lines(Map entries) { entries?
        entries.collectMany { lines(it.key, it.value) }: []
    }
    static List<String> lines(key, value) { value instanceof Map?
        (["[$key]"] + lines(value)): [entry(key, value)]
    }
    static String entry(key, value) { "$key = " + (value instanceof List?
        value.collect { stringValue(it) }.toListString(): stringValue(value))
    }
    static String stringValue(value) { "'$value'" }
}
