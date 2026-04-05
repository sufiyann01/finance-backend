package com.zorvyn.finance.model;

public enum Role {
    VIEWER,   // Can only view dashboard summary data
    ANALYST,  // Can view records and access insights/summaries
    ADMIN     // Full access: manage users and financial records
}
