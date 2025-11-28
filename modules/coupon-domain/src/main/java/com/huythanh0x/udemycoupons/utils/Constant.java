package com.huythanh0x.udemycoupons.utils;

public class Constant {
    public static final String AUTHENTICATION_EXCEPTION_HEADER = "authentication_exception_header";
    
    // Redis Keys
    public static final String REDIS_KEY_LAST_FETCH_TIME = "last_fetch_time";
    
    // Pagination Constants
    /**
     * Maximum number of items allowed per page to prevent abuse and excessive response sizes.
     * Requests exceeding this limit will be capped at this value.
     */
    public static final int MAX_PAGE_SIZE = 20;
}
