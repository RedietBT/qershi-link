package com.kab.qershi.loan.management.infrastructure.adapters;

import com.kab.qershi.loan.management.domain.port.out.PricingClientPort;
import com.kab.qershi.loan.management.infrastructure.config.TenantContext;
import com.kab.qershi.pricing.infrastructure.grpc.FeeCalculationProtoRequest;
import com.kab.qershi.pricing.infrastructure.grpc.FeeCalculationProtoResponse;
import com.kab.qershi.pricing.infrastructure.grpc.PricingGrpcServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Infrastructure client adapter implementing PricingClientPort via gRPC.
 * Calls pricing-fee-service on gRPC port 9087.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class PricingGrpcClientAdapter implements PricingClientPort {

    private static final Logger log = LoggerFactory.getLogger(PricingGrpcClientAdapter.class);

    @GrpcClient("pricing-service")
    private PricingGrpcServiceGrpc.PricingGrpcServiceBlockingStub pricingGrpcStub;

    @Override
    public FeeAssessment calculateFee(String transactionType, BigDecimal amount, String customerTier, String currency) {
        log.debug("Calling gRPC CalculateFee on pricing-service: txType={}, amount={}, tier={}",
                transactionType, amount, customerTier);
        try {
            String schema = TenantContext.getTenantSchema();
            FeeCalculationProtoRequest request = FeeCalculationProtoRequest.newBuilder()
                    .setTransactionType(transactionType != null ? transactionType : "LOAN_PROCESSING")
                    .setAmount(amount != null ? amount.toPlainString() : "0")
                    .setCustomerTier(customerTier != null ? customerTier : "STANDARD")
                    .setCurrency(currency != null ? currency : "ETB")
                    .setTenantSchema(schema != null ? schema : "")
                    .build();

            FeeCalculationProtoResponse res = pricingGrpcStub.calculateFee(request);

            return new FeeAssessment(
                    res.getFeeApplicable(),
                    res.getTariffCode(),
                    res.getTariffName(),
                    parseDecimal(res.getFeeAmount()),
                    res.getFeeGlCode(),
                    parseDecimal(res.getTotalDebitAmount())
            );
        } catch (Exception ex) {
            log.error("gRPC call CalculateFee to pricing-service failed: {}", ex.getMessage());
            // Graceful fallback: 0 fee if pricing engine is temporarily unreachable
            return new FeeAssessment(
                    false,
                    "NONE",
                    "Fallback No Fee",
                    BigDecimal.ZERO,
                    "4021",
                    amount != null ? amount : BigDecimal.ZERO
            );
        }
    }

    private BigDecimal parseDecimal(String val) {
        if (val == null || val.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(val.trim());
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }
}
