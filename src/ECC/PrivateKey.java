package ECC;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PrivateKey {
    private EllipticCurve c;
    private BigInteger prikey;

    public PrivateKey(EllipticCurve c, BigInteger prikey){
        this.c = c;
        this.prikey = prikey;
    }

    /**
     * 从指定文件中读取参数对私钥进行初始化
     * @param filePath
     * @throws Exception
     */
    public PrivateKey(String filePath) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        if(lines.size() != 6)
            throw new Exception("选择的私钥密码文件存在问题!\n" +
                    "请按照a, b, p,  gx, gy, prikey的顺序逐行存储");
        else {
            BigInteger a = new BigInteger(lines.get(0), 16);
            BigInteger b = new BigInteger(lines.get(1), 16);
            BigInteger p = new BigInteger(lines.get(2), 16);
            BigInteger gx = new BigInteger(lines.get(3), 16);
            BigInteger gy = new BigInteger(lines.get(4), 16);
            BigInteger prikey = new BigInteger(lines.get(5), 16);
            EllipticCurve c = new EllipticCurve(a, b, p, new ECPoint(gx, gy));
            this.c = c;
            this.prikey = prikey;
        }
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
    public void saveToFile(String path){
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
