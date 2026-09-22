package com.aether.browser.core.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalNetworkAccessTest {
    private val access = LocalNetworkAccess()

    @Test fun `private ipv4 addresses require local access`() {
        assertTrue(access.isRequired("192.168.1.1"))
        assertTrue(access.isRequired("http://10.0.0.2:8080"))
        assertTrue(access.isRequired("https://172.31.4.8"))
        assertTrue(access.isRequired("http://169.254.10.20"))
    }

    @Test fun `local domains and private ipv6 require local access`() {
        assertTrue(access.isRequired("printer.local"))
        assertTrue(access.isRequired("http://[fd00::1]"))
        assertTrue(access.isRequired("http://[fe80::12]"))
    }

    @Test fun `public search and loopback do not require local access`() {
        assertFalse(access.isRequired("example.com"))
        assertFalse(access.isRequired("search words"))
        assertFalse(access.isRequired("localhost:8080"))
        assertFalse(access.isRequired("127.0.0.1"))
    }
}
