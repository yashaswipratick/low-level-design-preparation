package com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.pricing;

import com.lld.practice.tutor_sessions.parking_lot.implementation.model.Ticket;

public interface PricingStrategy {

    double calculateFee(Ticket ticket);
}
