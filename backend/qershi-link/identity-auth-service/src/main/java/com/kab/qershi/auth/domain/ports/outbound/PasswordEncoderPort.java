package com.kab.qershi.auth.domain.ports.outbound;

/**
 * Outbound port for cryptographic hashing and verification of passwords and PINs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface PasswordEncoderPort {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence rawPassword, String encodedPassword);
}
