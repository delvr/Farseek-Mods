package farseek.utils

static def error(String... msg) { throw new RuntimeException(msg.joinWords()) }

static String envVar(String name, String defaultValue = null) { System.getenv(name) ?: defaultValue }

static String sysProp(String name, String defaultValue = null) { System.getProperty(name, defaultValue) }
