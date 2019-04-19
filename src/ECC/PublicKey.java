package ECC;


import java.io.*;
import java.math.BigInteger;

/**
 * 椭圆曲线加密的公钥类
 */
public class PublicKey {
    private EllipticCurve c; //产生公钥的椭圆曲线
    private ECPoint pubKey; //公钥

    public PublicKey(EllipticCurve c, ECPoint pubKey){
        this.c = c;
        this.pubKey = pubKey;
    }

    public EllipticCurve getC() {
        return c;
    }

    public ECPoint getPubKey() {
        return pubKey;
    }

    /**
     * 将公钥信息保存到指定路径
     * @param path
     */
   public void saveToFile(String path){
       BigInteger a = c.getA();
       BigInteger b = c.getB();
       BigInteger p = c.getP();
       BigInteger gx = c.getBasePoint().getX();
       BigInteger gy = c.getBasePoint().getY();
       BigInteger pubkey1 = pubKey.getX();
       BigInteger pubkey2 = pubKey.getY();
       try {
           PrintStream ps = new PrintStream(new File(path));
           ps.println(a.toString(16));
           ps.println(b.toString(16));
           ps.println(p.toString(16));
           ps.println(gx.toString(16));
           ps.println(gy.toString(16));
           ps.println(pubkey1.toString(16));
           ps.println(pubkey2.toString(16));
           ps.close();
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
   }
}
