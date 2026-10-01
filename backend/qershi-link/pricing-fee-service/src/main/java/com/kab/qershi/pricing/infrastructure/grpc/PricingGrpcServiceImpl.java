package com.kab.qershi.pricing.infrastructure.grpc;

import com.kab.qershi.pricing.domain.model.FeeCalculationResult;
import com.kab.qershi.pricing.domain.model.Tariff;
import com.kab.qershi.pricing.domain.model.WithholdingTaxResult;
import com.kab.qershi.pricing.domain.ports.inbound.TariffCalculationUseCase;
import com.kab.qershi.pricing.domain.ports.inbound.TariffManagementUseCase;
import com.kab.qershi.pricing.domain.ports.inbound.TaxAssessmentUseCase;
import com.kab.qershi.pricing.infrastructure.config.TenantContext;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * gRPC Service Server implementation for Enterprise Pricing, Tariff & Statutory Tax calculations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@GrpcService
public class PricingGrpcServiceImpl extends PricingGrpcServiceGrpc.PricingGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(PricingGrpcServiceImpl.class);

    private final TariffCalculationUseCase tariffCalculationUseCase;
    private final TariffManagementUseCase tariffManagementUseCase;
    private final TaxAssessmentUseCase taxAssessmentUseCase;

    public PricingGrpcServiceImpl(TariffCalculationUseCase tariffCalculationUseCase,
                                  TariffManagementUseCase tariffManagementUseCase,
                                  TaxAssessmentUseCase taxAssessmentUseCase) {
        this.tariffCalculationUseCase = tariffCalculationUseCase;
        this.tariffManagementUseCase = tariffManagementUseCase;
        this.taxAssessmentUseCase = taxAssessmentUseCase;
    }

    @Override
    public void calculateFee(FeeCalculationProtoRequest request, StreamObserver<FeeCalculationProtoResponse> responseObserver) {
        log.debug("gRPC CalculateFee received: type={}, amount={}, tenant={}",
                request.getTransactionType(), request.getAmount(), request.getTenantSchema());
        try {
            if (request.getTenantSchema() != null && !request.getTenantSchema().isBlank()) {
                TenantContext.setTenantSchema(request.getTenantSchema().trim());
            }

            BigDecimal amount = new BigDecimal(request.getAmount());
            FeeCalculationResult result = tariffCalculationUseCase.calculateFee(request.getTransactionType(), amount);

            FeeCalculationProtoResponse response = FeeCalculationProtoResponse.newBuilder()
                    .setFeeApplicable(result.feeApplicable())
                    .setTariffCode(result.tariffCode() != null ? result.tariffCode() : "")
                    .setTariffName(result.tariffName() != null ? result.tariffName() : "")
                    .setFeeType(result.feeType() != null ? result.feeType() : "FLAT")
                    .setFeeAmount(result.calculatedFee().toPlainString())
                    .setMinFee(result.minFee() != null ? result.minFee().toPlainString() : "")
                    .setMaxFee(result.maxFee() != null ? result.maxFee().toPlainString() : "")
                    .setFeeGlCode(result.feeGlCode() != null ? result.feeGlCode() : "4020")
                    .setTotalDebitAmount(result.totalDebitRequired().toPlainString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("gRPC CalculateFee failed: {}", ex.getMessage(), ex);
            FeeCalculationProtoResponse response = FeeCalculationProtoResponse.newBuilder()
                    .setFeeApplicable(false)
                    .setFeeAmount("0.00")
                    .setFeeGlCode("4020")
                    .setTotalDebitAmount(request.getAmount())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public void calculateWithholdingTax(TaxCalculationProtoRequest request, StreamObserver<TaxCalculationProtoResponse> responseObserver) {
        log.debug("gRPC CalculateWithholdingTax received: account={}, gross={}, date={}",
                request.getAccountNo(), request.getGrossAmount(), request.getBusinessDate());
        try {
            if (request.getTenantSchema() != null && !request.getTenantSchema().isBlank()) {
                TenantContext.setTenantSchema(request.getTenantSchema().trim());
            }

            BigDecimal grossAmount = new BigDecimal(request.getGrossAmount());
            LocalDate date = request.getBusinessDate() != null && !request.getBusinessDate().isBlank()
                    ? LocalDate.parse(request.getBusinessDate())
                    : LocalDate.now();

            WithholdingTaxResult result = taxAssessmentUseCase.assessSavingsInterestTax(
                    request.getAccountNo(),
                    date,
                    grossAmount
            );

            TaxCalculationProtoResponse response = TaxCalculationProtoResponse.newBuilder()
                    .setTaxRatePct(result.taxRatePct().toPlainString())
                    .setGrossAmount(result.grossInterest().toPlainString())
                    .setTaxWithheld(result.taxWithheld().toPlainString())
                    .setNetCredited(result.netInterest().toPlainString())
                    .setTaxGlCode(result.whtGlCode())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("gRPC CalculateWithholdingTax failed: {}", ex.getMessage(), ex);
            responseObserver.onError(ex);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public void getActiveTariffs(TariffListProtoRequest request, StreamObserver<TariffListProtoResponse> responseObserver) {
        try {
            if (request.getTenantSchema() != null && !request.getTenantSchema().isBlank()) {
                TenantContext.setTenantSchema(request.getTenantSchema().trim());
            }

            List<Tariff> tariffs = tariffManagementUseCase.listAllTariffs();
            TariffListProtoResponse.Builder builder = TariffListProtoResponse.newBuilder();

            for (Tariff t : tariffs) {
                builder.addTariffs(TariffProtoItem.newBuilder()
                        .setTariffCode(t.getTariffCode())
                        .setTariffName(t.getTariffName())
                        .setTransactionType(t.getTransactionType())
                        .setFeeType(t.getFeeType())
                        .setFeeValue(t.getFeeValue().toPlainString())
                        .setMinFee(t.getMinFee() != null ? t.getMinFee().toPlainString() : "")
                        .setMaxFee(t.getMaxFee() != null ? t.getMaxFee().toPlainString() : "")
                        .setFeeGlCode(t.getFeeGlCode() != null ? t.getFeeGlCode() : "4020")
                        .setIsActive(t.isActive())
                        .build());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("gRPC GetActiveTariffs failed: {}", ex.getMessage(), ex);
            responseObserver.onError(ex);
        } finally {
            TenantContext.clear();
        }
    }
}
