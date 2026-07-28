package com.thanh0x.coursedeal.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Idiomatic Kotlin logging utility.
 * Usage: private val log = logger()
 */
fun <T : Any> T.logger(): Logger = LoggerFactory.getLogger(javaClass)
