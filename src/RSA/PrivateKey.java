package RSA;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PrivateKey {
    private BigInteger d;
    private BigInteger n;

    public  PrivateKey(BigInteger d, BigInteger n) {
        this.d = d;
        this.n = n;
    }

    public PrivateKey(String filePath) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        if(lines.size() != 2)
            throw new Exception("选择的私钥密码文件存在问题!\n" +
                    "请按照d, n的顺序逐行存储");
        else {
            BigInteger d = new BigInteger(lines.get(0), 16);
            BigInteger n = new BigInteger(lines.get(1), 16);
            this.d = d;
            this.n = n;
        }
    }

    public void saveToFile(String path){
        BigInteger d = this.d;
        BigInteger n = this.n;
        try {
            PrintStream ps = new PrintStream(new File(path));
            ps.println(d.toString(16));
            ps.println(n.toString(16));
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public BigInteger getD() {
        return d;
    }

    public BigInteger getN() {
        return n;
    }

}
