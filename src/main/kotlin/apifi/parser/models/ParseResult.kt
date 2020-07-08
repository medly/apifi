package apifi.parser.models

data class ParseResult<T>(val result: T, val models: List<Model>)