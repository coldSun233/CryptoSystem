package Math;

import java.math.BigInteger;

public class MyMath {
    private static BigInteger x;
    private static BigInteger y;
    public static BigInteger modInverse(BigInteger a, BigInteger b){
        if(exgcd(a, b).equals(BigInteger.ONE)){
            System.out.println(x.mod(b));
            return x.mod(b);
        } else {
            System.out.println("error" + exgcd(a, b));
            return null;
        }
    }

    private static BigInteger exgcd(BigInteger a, BigInteger b){
        if(b.equals(BigInteger.ZERO)) {
            x = BigInteger.ONE;
            y = BigInteger.ZERO;
            return a;
        }
        BigInteger r = exgcd(b, a.mod(b));
        BigInteger temp = x;
        x = y;
        y = temp.subtract(a.divide(b).subtract(y));
        return r;
    }

    public static void main(String[] args){
        BigInteger a = new BigInteger("2");
        BigInteger b = new BigInteger("8");
        BigInteger c = new BigInteger("3");
        modInverse(a, b);
        modInverse(c, b);

    }
}
