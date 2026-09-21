package com.aether.browser.core.tabs

/** Pure tab-selection policy kept independent of GeckoView so it can be unit tested. */
class TabRegistry {
    private val ids = mutableListOf<String>()
    var selectedId: String? = null
        private set

    fun ids(): List<String> = ids.toList()

    fun add(id: String, select: Boolean = true) {
        require(id !in ids) { "Duplicate tab id: $id" }
        ids += id
        if (select || selectedId == null) selectedId = id
    }

    fun select(id: String): Boolean {
        if (id !in ids) return false
        selectedId = id
        return true
    }

    fun remove(id: String): String? {
        val index = ids.indexOf(id)
        if (index < 0) return selectedId
        val wasSelected = id == selectedId
        ids.removeAt(index)
        if (wasSelected) selectedId = ids.getOrNull(index.coerceAtMost(ids.lastIndex))
        return selectedId
    }
}
