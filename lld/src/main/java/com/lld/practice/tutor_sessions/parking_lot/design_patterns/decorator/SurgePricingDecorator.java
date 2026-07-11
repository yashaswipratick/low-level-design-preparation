package com.lld.practice.tutor_sessions.parking_lot.design_patterns.decorator;

import com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.pricing.PricingStrategy;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.Ticket;

public class SurgePricingDecorator implements PricingStrategy {

    private final PricingStrategy pricingStrategy;
    private final double multiplier;

    public SurgePricingDecorator(PricingStrategy pricingStrategy, double multiplier) {
        this.pricingStrategy = pricingStrategy;
        this.multiplier = multiplier;
    }

    @Override
    public double calculateFee(Ticket ticket) {
        return pricingStrategy.calculateFee(ticket) * this.multiplier;
    }
}
