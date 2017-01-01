package com.maxdemarzi;

import com.codahale.metrics.health.HealthCheck;

public class DatabaseHealthCheck extends HealthCheck {

    @Override
    public HealthCheck.Result check() throws Exception {
        if (GuancialeDB.getInstance().isAvailable()) {
            return HealthCheck.Result.healthy();
        } else {
            return HealthCheck.Result.unhealthy("Database is not available");
        }
    }
}