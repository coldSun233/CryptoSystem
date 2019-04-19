package ECC;

import java.io.*;
import java.math.BigInteger;

public class PrivateKey {
    private EllipticCurve c;
    private BigInteger prikey;

    public PrivateKey(EllipticCurve c, BigInteger prikey){
        this.c = c;
        this.prikey = prikey;
    }

    public EllipticCurve getC() {
        return c;
    }

    public BigInteger getPrikey() {
        return prikey;
    }

    /**
     * 将私钥信息保存到指定路径
     * @param path
     */
    public void saveToFlie(String path){
        BigInteger a = c.getA();
        BigInteger b = c.getB();
        BigInteger p = c.getP();
        BigInteger gx = c.getBasePoint().getX();
        BigInteger gy = c.getBasePoint().getY();
        BigInteger prikey = this.prikey;
        try {
            PrintStream ps = new PrintStream(new File(path));
            ps.println(a.toString(16));
            ps.println(b.toString(16));
            ps.println(p.toString(16));
            ps.println(gx.toString(16));
            ps.println(gy.toString(16));
            ps.println(prikey.toString(16));
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
