package com.aether.browser.core.navigation

import java.net.URI

/** Detects addresses that Android 17 protects with ACCESS_LOCAL_NETWORK. */
class LocalNetworkAccess(
    private val interpreter: AddressInterpreter = AddressInterpreter(),
) {
    fun isRequired(input: String): Boolean {
        val target = interpreter.interpret(input) as? AddressTarget.Web ?: return false
        val host = runCatching { URI(target.url).host }.getOrNull()?.trim('[', ']')?.lowercase()
            ?: return false

        if (host.endsWith(".local")) return true
        if (isPrivateIpv4(host)) return true
        return isPrivateIpv6(host)
    }

    private fun isPrivateIpv4(host: String): Boolean {
        val parts = host.split('.').map { it.toIntOrNull() ?: return false }
        if (parts.size != 4 || parts.any { it !in 0..255 }) return false
        return when {
            parts[0] == 10 -> true
            parts[0] == 172 && parts[1] in 16..31 -> true
            parts[0] == 192 && parts[1] == 168 -> true
            parts[0] == 169 && parts[1] == 254 -> true
            else -> false
        }
    }

    private fun isPrivateIpv6(host: String): Boolean {
        if (!host.contains(':')) return false
        val firstGroup = host.substringBefore(':').toIntOrNull(16) ?: return false
        return firstGroup in 0xFC00..0xFDFF || firstGroup in 0xFE80..0xFEBF
    }
}
