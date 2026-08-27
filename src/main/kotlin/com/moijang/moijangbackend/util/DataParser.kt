package com.moijang.moijangbackend.util

class DataParseException(
    val path: String,
    val reason: String,
) : RuntimeException(
    "$path: $reason"
)

data class ParseContext(
    val path: String = "",
) {
    fun child(name: String): ParseContext {
        val childPath = if (path.isEmpty()) {
            name
        } else {
            "$path.$name"
        }

        return copy(path = childPath)
    }

    fun fail(reason: String): Nothing {
        throw DataParseException(
            path = path,
            reason = reason,
        )
    }
}

object DataParser {

    fun string(
        data: Any?,
        context: ParseContext,
    ): String {
        return data as? String ?: context.fail("문자열이어야 합니다.")
    }

    fun boolean(
        data: Any?,
        context: ParseContext,
    ): Boolean {
        return data as? Boolean ?: context.fail("boolean이어야 합니다.")
    }

    fun map(
        data: Any?,
        context: ParseContext,
    ): Map<*, *> {
        return data as? Map<*, *> ?: context.fail("객체여야 합니다.")
    }

    fun list(
        data: Any?,
        context: ParseContext,
    ): List<*> {
        return data as? List<*> ?: context.fail("배열이어야 합니다.")
    }
}