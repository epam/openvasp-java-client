package org.openvasp.client.service;

/**
 * @author Jan_Juraszek@epam.com
 */
public interface SignService {
    
    /**
     * Sign the message with given private key
     *
     * @param payload  a hex-encoded message
     * @param privateKey a hex-encoded private key
     * @return hex-encoded signature
     */
    String signPayload(String payload, String privateKey);
    
    /**
     * Verify whether the provided signature for the given message is valid 
     *
     * @param payload  a hex-encoded message
     * @param sign a hex-encoded signature
     * @param pubKey a hex-encoded public key
     * @return boolean true if the signature is valid, false otherwise
     */
    boolean verifySign(String payload, String sign, String pubKey);

}
