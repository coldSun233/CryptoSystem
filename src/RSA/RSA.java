package RSA;

import java.math.BigInteger;
import java.security.SecureRandom;


public class RSA {
    private BigInteger n;
    private BigInteger p;
    private BigInteger q;
    private BigInteger phin;
    private BigInteger e;
    private BigInteger d;

    public RSA(int bitLength) {
        SecureRandom rand = new SecureRandom();
        p = new BigInteger(bitLength / 2, 500, rand);
        q = new BigInteger(bitLength / 2, 500, rand);
        n = p.multiply(q);
        phin = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = new BigInteger(bitLength, rand);
        while (e.compareTo(phin) >= 0 || !e.gcd(phin).equals(BigInteger.ONE))
            e = new BigInteger(bitLength, rand);
        d = e.modInverse(phin);
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getE() {
        return e;
    }

    public BigInteger getD() {
        return d;
    }

}
