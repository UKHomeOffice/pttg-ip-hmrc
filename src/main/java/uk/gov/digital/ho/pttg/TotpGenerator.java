package uk.gov.digital.ho.pttg;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import static org.apache.commons.lang3.StringUtils.leftPad;

public class TotpGenerator {
    private static Duration totpTimeInterval = Duration.ofSeconds(30);
    public static String getTotpCode(String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        return getTotpCode(secret, System.currentTimeMillis());
    }
    private static String getTotpCode(String secret, long totpGenerationTimeInMillis) throws InvalidKeyException, NoSuchAlgorithmException {
        long timeWindow = totpGenerationTimeInMillis / totpTimeInterval.toMillis();
        int codeLength = 8;
        String crypto = "HmacSHA512";
        byte[] msg = BigInteger.valueOf(timeWindow).toByteArray();
        byte[] padded = new byte[8];
        System.arraycopy(msg, 0, padded, 8 - msg.length, msg.length);
        byte[] hash = hmacSha(crypto, new Base32().decode(secret), padded);

        int offset = hash[hash.length - 1] & 0xf;
        long binary = ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8 |
                        (hash[offset + 3] & 0xff));

        long otp = binary % (long)Math.pow(10, codeLength);
        return StringUtils.left(leftPad(String.valueOf(otp), codeLength, '0'), codeLength);
    }


    private static byte[] hmacSha(String crypto, byte[] keyBytes, byte[] text) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(crypto);
        Key macKey = new SecretKeySpec(keyBytes, "RAW");
        hmac.init(macKey);
        return hmac.doFinal(text);
    }
}
