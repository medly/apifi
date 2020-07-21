package apifi.parser.models

import apifi.models.Model

data class ParseResult<T>(val result: T, val models: List<Model>)