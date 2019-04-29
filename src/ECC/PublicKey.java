package ECC;


import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

    /**
     * 从指定文件中读取参数进行公钥初始化
     * @param filePath
     * @throws Exception
     */
    public PublicKey (String filePath) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        if(lines.size() != 7)
            throw new Exception("选择的公钥密码文件存在问题!\n" +
                    "请按照a, b, p,  gx, gy, pub_x, pub_y的顺序逐行存储");
        else {
            BigInteger a = new BigInteger(lines.get(0), 16);
            BigInteger b = new BigInteger(lines.get(1), 16);
            BigInteger p = new BigInteger(lines.get(2), 16);
            BigInteger gx = new BigInteger(lines.get(3), 16);
            BigInteger gy = new BigInteger(lines.get(4), 16);
            BigInteger pub_x = new BigInteger(lines.get(5), 16);
            BigInteger pub_y = new BigInteger(lines.get(6), 16);
            EllipticCurve c = new EllipticCurve(a, b, p, new ECPoint(gx, gy));
            ECPoint pubKey = new ECPoint(pub_x, pub_y);
            this.c = c;
            this.pubKey = pubKey;
        }
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
