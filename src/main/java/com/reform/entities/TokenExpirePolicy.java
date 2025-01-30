package com.reform.entities;

public enum TokenExpirePolicy {
    NONE,       // No expiration (e.g., single sessions)
    ONE_MONTH,  // Expires 1 month after first use
    TWO_MONTHS  // Expires 2 months after first use
}
