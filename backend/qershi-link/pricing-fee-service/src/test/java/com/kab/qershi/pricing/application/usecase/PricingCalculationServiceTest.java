package com.kab.qershi.pricing.application.usecase;

import com.kab.qershi.pricing.domain.model.FeeCalculationResult;
import com.kab.qershi.pricing.domain.model.Tariff;
import com.kab.qershi.pricing.domain.model.TariffSlab;
import com.kab.qershi.pricing.domain.ports.outbound.TariffRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PricingCalculationServiceTest {

    private TariffRepositoryPort tariffRepositoryPort;
    private PricingCalculationService pricingCalculationService;

    @BeforeEach
    void setUp() {
        tariffRepositoryPort = Mockito.mock(TariffRepositoryPort.class);
        pricingCalculationService = new PricingCalculationService(tariffRepositoryPort);
    }

    @Test
    @DisplayName("Should correctly calculate tiered slabs: 0-1k flat 5, 1k-10k flat 15, 10k-50k flat 25, 50k+ 0.25% (min 30, max 100)")
    void testTieredSlabCalculation() {
        UUID tariffId = UUID.randomUUID();
        List<TariffSlab> slabs = List.of(
                new TariffSlab(UUID.randomUUID(), tariffId, 1, new BigDecimal("0.00"), new BigDecimal("1000.00"), "FLAT", new BigDecimal("5.00"), null, null),
                new TariffSlab(UUID.randomUUID(), tariffId, 2, new BigDecimal("1000.01"), new BigDecimal("10000.00"), "FLAT", new BigDecimal("15.00"), null, null),
                new TariffSlab(UUID.randomUUID(), tariffId, 3, new BigDecimal("10000.01"), new BigDecimal("50000.00"), "FLAT", new BigDecimal("25.00"), null, null),
                new TariffSlab(UUID.randomUUID(), tariffId, 4, new BigDecimal("50000.01"), null, "PERCENTAGE", new BigDecimal("0.2500"), new BigDecimal("30.00"), new BigDecimal("100.00"))
        );

        Tariff tieredTariff = new Tariff(
                tariffId, "TAR-WTH-TIER", "Tiered OTC Cash Withdrawal", "WITHDRAWAL_TIERED",
                "TIERED", BigDecimal.ZERO, null, null, "4020", "ETB", true, "Tiered test", slabs
        );

        when(tariffRepositoryPort.findActiveByTransactionType(eq("WITHDRAWAL_TIERED")))
                .thenReturn(List.of(tieredTariff));

        // Test 1: Amount = 500 ETB (falls into Tier 1: 0 - 1000 -> 5.00 Flat)
        FeeCalculationResult r1 = pricingCalculationService.calculateFee("WITHDRAWAL_TIERED", new BigDecimal("500.00"));
        assertTrue(r1.feeApplicable());
        assertEquals(new BigDecimal("5.00"), r1.calculatedFee());
        assertEquals(new BigDecimal("505.00"), r1.totalDebitRequired());
        assertNotNull(r1.matchedSlabDetails());
        assertTrue(r1.matchedSlabDetails().contains("Tier Bracket [0.00 - 1000.00 ETB]: 5.00 ETB Flat"));

        // Test 2: Amount = 5,000 ETB (falls into Tier 2: 1000.01 - 10000 -> 15.00 Flat)
        FeeCalculationResult r2 = pricingCalculationService.calculateFee("WITHDRAWAL_TIERED", new BigDecimal("5000.00"));
        assertTrue(r2.feeApplicable());
        assertEquals(new BigDecimal("15.00"), r2.calculatedFee());
        assertEquals(new BigDecimal("5015.00"), r2.totalDebitRequired());

        // Test 3: Amount = 20,000 ETB (falls into Tier 3: 10000.01 - 50000 -> 25.00 Flat)
        FeeCalculationResult r3 = pricingCalculationService.calculateFee("WITHDRAWAL_TIERED", new BigDecimal("20000.00"));
        assertTrue(r3.feeApplicable());
        assertEquals(new BigDecimal("25.00"), r3.calculatedFee());

        // Test 4: Amount = 60,000 ETB (falls into Tier 4: 50000.01+ -> 0.25% of 60,000 = 150 -> capped at max 100.00)
        FeeCalculationResult r4 = pricingCalculationService.calculateFee("WITHDRAWAL_TIERED", new BigDecimal("60000.00"));
        assertTrue(r4.feeApplicable());
        assertEquals(new BigDecimal("100.00"), r4.calculatedFee());
        assertEquals(new BigDecimal("60100.00"), r4.totalDebitRequired());

        // Test 5: Amount = 50,000.01 ETB (0.25% = 125, but min cap is 30 -> 125 is between 30 and 100, if amount was smaller e.g. 50000.01 -> min cap applies)
    }

    @Test
    @DisplayName("Should maintain backward compatibility for standard FLAT tariff")
    void testFlatTariff() {
        Tariff flatTariff = new Tariff(
                UUID.randomUUID(), "TAR-WTH-01", "Flat Withdrawal", "WITHDRAWAL",
                "FLAT", new BigDecimal("10.00"), new BigDecimal("10.00"), new BigDecimal("10.00"),
                "4020", "ETB", true, "Flat test"
        );

        when(tariffRepositoryPort.findActiveByTransactionType(eq("WITHDRAWAL")))
                .thenReturn(List.of(flatTariff));

        FeeCalculationResult res = pricingCalculationService.calculateFee("WITHDRAWAL", new BigDecimal("1000.00"));
        assertTrue(res.feeApplicable());
        assertEquals(new BigDecimal("10.00"), res.calculatedFee());
        assertEquals(new BigDecimal("1010.00"), res.totalDebitRequired());
    }
}
