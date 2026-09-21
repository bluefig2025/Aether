package com.aether.browser.core.navigation

import java.net.IDN
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface SearchProvider {
    val name: String
    fun searchUrl(query: String): String
}

object DuckDuckGoSearchProvider : SearchProvider {
    override val name = "DuckDuckGo"
    override fun searchUrl(query: String): String =
        "https://duckduckgo.com/?q=${URLEncoder.encode(query, StandardCharsets.UTF_8.name())}"
}

sealed interface AddressTarget {
    data class Web(val url: String) : AddressTarget
    data class Internal(val url: String) : AddressTarget
}

class AddressInterpreter(
    private val searchProvider: SearchProvider = DuckDuckGoSearchProvider,
) {
    fun interpret(rawInput: String): AddressTarget {
        val input = rawInput.trim()
        if (input.isEmpty()) return AddressTarget.Internal("aether://newtab")
        if (input.startsWith("aether://", ignoreCase = true)) {
            return AddressTarget.Internal(input.lowercase())
        }

        parseExplicitUri(input)?.let { return AddressTarget.Web(it) }
        if (looksLikeHost(input)) return AddressTarget.Web("https://$input")
        return AddressTarget.Web(searchProvider.searchUrl(input))
    }

    private fun parseExplicitUri(input: String): String? = try {
        val uri = URI(input)
        when (uri.scheme?.lowercase()) {
            "http", "https" -> if (!uri.host.isNullOrBlank()) input else null
            else -> null
        }
    } catch (_: Exception) {
        null
    }

    private fun looksLikeHost(input: String): Boolean {
        if (input.any(Char::isWhitespace)) return false
        val authority = input.substringBefore('/').substringBefore('?').substringBefore('#')
        val host = authority.substringBefore(':')
        val port = authority.substringAfter(':', "")
        if (port.isNotEmpty() && port.toIntOrNull() == null) return false
        if (host.equals("localhost", true)) return true
        if (host.matches(Regex("\\d{1,3}(\\.\\d{1,3}){3}"))) return true
        return try {
            val ascii = IDN.toASCII(host)
            ascii.contains('.') && ascii.split('.').all {
                it.isNotBlank() && it.length <= 63 &&
                    !it.startsWith('-') && !it.endsWith('-') &&
                    it.all { c -> c.isLetterOrDigit() || c == '-' }
            }
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
