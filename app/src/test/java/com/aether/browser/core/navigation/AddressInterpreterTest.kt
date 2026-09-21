package com.aether.browser.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AddressInterpreterTest {
    private val interpreter = AddressInterpreter(
        object : SearchProvider {
            override val name = "Test"
            override fun searchUrl(query: String) = "https://search.test/?q=${query.replace(" ", "+")}" 
        },
    )

    @Test fun `full https url remains a url`() {
        assertEquals(AddressTarget.Web("https://example.com/path"), interpreter.interpret("https://example.com/path"))
    }

    @Test fun `bare domain receives https`() {
        assertEquals(AddressTarget.Web("https://example.com"), interpreter.interpret("example.com"))
    }

    @Test fun `localhost and ip addresses are urls`() {
        assertEquals(AddressTarget.Web("https://localhost:8080"), interpreter.interpret("localhost:8080"))
        assertEquals(AddressTarget.Web("https://192.168.1.10"), interpreter.interpret("192.168.1.10"))
    }

    @Test fun `words become a search`() {
        assertEquals(
            AddressTarget.Web("https://search.test/?q=how+does+linux+scheduling+work"),
            interpreter.interpret("how does linux scheduling work"),
        )
    }

    @Test fun `internal pages remain internal`() {
        assertEquals(AddressTarget.Internal("aether://version"), interpreter.interpret("AETHER://VERSION"))
    }
}
