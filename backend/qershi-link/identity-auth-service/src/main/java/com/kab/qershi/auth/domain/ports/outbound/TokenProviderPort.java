package com.kab.qershi.auth.domain.ports.outbound;

import java.util.List;

/**
 * Outbound port for cryptographic JWT access token issuance.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TokenProviderPort {

    String createToken(String msisdn, String userId, String saccoId, List<String> permissions);

    String createToken(String msisdn, String saccoId, List<String> permissions);
}
