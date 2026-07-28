package com.thanh0x.coursedeal.utils

object Constant {
    const val AUTHENTICATION_EXCEPTION_HEADER = "authentication_exception_header"

    // Redis Keys
    const val REDIS_KEY_LAST_FETCH_TIME = "last_fetch_time"

    // Pagination Constants
    /**
     * Maximum number of items allowed per page to prevent abuse and excessive response sizes.
     * Requests exceeding this limit will be capped at this value.
     */
    const val MAX_PAGE_SIZE = 20
}
