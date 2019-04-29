package ECC;

import java.math.BigInteger;
import java.util.Random;


public class ECCryptoSystem {

    //k是在明文镶嵌到椭圆曲线上是用于计算x = mk + j
    private static final long k_long = 1000;
    private static final BigInteger k = BigInteger.valueOf(k_long);

    private static long executionTime = -1;
    private static long startTime;

    /**
     * ECC的加密功能
     * @param plainText 要加密的内容
     * @param Key 用于加密的公钥
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] plainText, PublicKey Key) throws Exception {
        initStartTime();

        EllipticCurve c = Key.getC();
        ECPoint g = c.getBasePoint();
        BigInteger p = c.getP();
        int numBits = p.bitLength();
        ECPoint publicKey = Key.getPubKey();

        int plainTextBlockSize = getPlainTextBlockSize(c);
        int cipherTextBlockSize = getCipherTextBlockSize(c);

        //填充plainText，缺少部分补0，使填充后的其长度是plainTextBlockSize的整数倍
        byte[] padded = pad(plainText,plainTextBlockSize);

        //将padded装入一个二维数组，该操作的目的是将填充后的明文padded进行分组
        byte[][] block = new byte[padded.length / plainTextBlockSize][plainTextBlockSize];
        for(int i = 0; i < block.length; ++i){
            for(int j = 0; j < plainTextBlockSize; ++j){
                block[i][j] = padded[i * plainTextBlockSize + j];
            }
        }

        //将明文信息嵌入到椭圆曲线上
        ECPoint[] encoded = new  ECPoint[block.length];
        for(int i = 0; i < encoded.length; ++i){
            encoded[i] = encode(block[i], c);
        }

        //对得到的每一个点做加密变换
        //密文为{C1, C2} = {kG, P_m + kP_A}
        //其中k是一个随机数且1 <= k <p-1
        //G是椭圆曲线的基点
        //P_m是明文对应的椭圆曲线上的点
        //P_G是提供的公钥
        ECPoint[][] encrypted = new ECPoint[block.length][2];
        Random random = new Random(System.currentTimeMillis());
        for(int i = 0; i < encrypted.length; ++i){
            BigInteger k1;
            //生成一个随机数k1，且k1不能整除p
            do{
                k1 = new BigInteger(numBits,random);
            } while (k1.mod(p).compareTo(BigInteger.ZERO) == 0);
            encrypted[i][0] = c.multiply(k1, g);
            encrypted[i][1] = c.add(encoded[i], c.multiply(k1, publicKey));
        }

        //将加密后得到的各个密文块拼接到一起
        byte[] cipherText = new byte[encrypted.length * cipherTextBlockSize * 4];
        for(int i = 0; i < encrypted.length; ++i){
            //encrypted[i][0].x
            byte[] cipher = encrypted[i][0].getX().toByteArray();
            int offset = i * cipherTextBlockSize * 4 + cipherTextBlockSize * 0 + (cipherTextBlockSize - cipher.length);
            for(int j = 0; j < cipher.length; ++j){
                cipherText[j + offset] = cipher[j];
            }

            //encrypted[i][0].y
            cipher = encrypted[i][0].getY().toByteArray();
            offset = i * cipherTextBlockSize * 4 + cipherTextBlockSize * 1 + (cipherTextBlockSize - cipher.length);
            for(int j = 0; j < cipher.length; ++j){
                cipherText[j + offset] = cipher[j];
            }

            //encrypted[i][1].x
            cipher = encrypted[i][1].getX().toByteArray();
            offset = i * cipherTextBlockSize * 4 + cipherTextBlockSize * 2 + (cipherTextBlockSize - cipher.length);
            for(int j = 0; j < cipher.length; ++j){
                cipherText[j + offset] = cipher[j];
            }

            //encrypted[i][1].y
            cipher = encrypted[i][1].getY().toByteArray();
            offset = i * cipherTextBlockSize * 4 + cipherTextBlockSize * 3 + (cipherTextBlockSize - cipher.length);
            for(int j = 0; j < cipher.length; ++j){
                cipherText[j + offset] = cipher[j];
            }
        }

        calculateExecutionTime();

        return cipherText;

    }


    /**
     * ECC解密函数
     * @param cipherText
     * @param Key
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] cipherText, PrivateKey Key) throws Exception {
        initStartTime();

        EllipticCurve c = Key.getC();
        ECPoint g = c.getBasePoint();
        BigInteger p = c.getP();
        BigInteger privateKey = Key.getPrikey();

        int plainTextBlockSize = getPlainTextBlockSize(c);
        int cipherTextBlockSize = getCipherTextBlockSize(c);

        //将密文分组
        if (cipherText.length % cipherTextBlockSize != 0 || (cipherText.length / cipherTextBlockSize) % 4 != 0) {
            throw new Exception("密文的长度是非法的");
        }
        byte block[][] = new byte[cipherText.length / cipherTextBlockSize][cipherTextBlockSize];
        for(int i = 0; i< block.length; ++i){
            for(int j = 0; j < cipherTextBlockSize; ++j){
                block[i][j] = cipherText[i * cipherTextBlockSize + j];
            }
        }

        //计算明文对应的椭圆曲线上的点 P_m
        //P_m = C2 - kC1
        //其中[C1, C2]是密文，k是私钥
        ECPoint encoded[] = new ECPoint[block.length / 4];
        for(int i = 0; i < block.length; i += 4){
            ECPoint c1 = new ECPoint(new BigInteger(block[i]), new BigInteger(block[i + 1]));
            ECPoint c2 = new ECPoint(new BigInteger(block[i + 2]), new BigInteger(block[i + 3]));
            encoded[i / 4] = c.subtract(c2, c.multiply(privateKey, c1));
        }

        //从椭圆曲线上的点得到明文消息
        byte plainText[] = new byte[encoded.length * plainTextBlockSize];
        for(int i = 0; i < encoded.length; ++i){
            byte decoded[] = decoded(encoded[i], c);
            for(int j = Math.max(plainTextBlockSize - decoded.length, 0); j < plainTextBlockSize; ++j){
                plainText[i * plainTextBlockSize + j] = decoded[j + decoded.length - plainTextBlockSize];
            }
        }
        plainText = unpad(plainText, plainTextBlockSize);

        calculateExecutionTime();

        return plainText;

    }

    /**
     * 产生一个随机的密钥对
     * @param c
     * @param random
     * @return
     * @throws Exception
     */
    public static KeyPair generateKeyPair(EllipticCurve c, Random random) throws Exception{

        //随机选取一个整数作为私钥，且私钥与p互素
        BigInteger p = c.getP();
        BigInteger privateKey;
        do{
            privateKey = new BigInteger(p.bitLength(), random);
        } while(privateKey.mod(p).compareTo(BigInteger.ZERO) == 0);

        //计算公钥 k * g, 其中k为私钥
        ECPoint g = c.getBasePoint();
        if(g == null) {
            //随机产生一个g点
            BigInteger x = new BigInteger(p.bitLength(),random);
            g = legendre(c, x);
            c.setBasePoint(g);
        }
        ECPoint publicKey = c.multiply(privateKey, g);

        KeyPair result = new KeyPair(
                new PublicKey(c, publicKey),
                new PrivateKey(c, privateKey)
        );

        return result;
    }

    /**
     * 将明文信息嵌入到椭圆去向上
     * @param block
     * @param c
     * @return
     */
    private static ECPoint encode(byte[] block, EllipticCurve c) throws Exception {
        //填充两个byte，因为后面需要计算x = km，这里取的k = 1000
        byte[] paddedBlock = new byte[block.length + 2];
        for(int i = 0; i< block.length; ++i){
            paddedBlock[i + 2] = block[i];
        }

        return legendre(c, new BigInteger(paddedBlock));
    }

    /**
     * 由椭圆曲线上的一点得到明文消息
     * @param point
     * @param c
     * @return
     */
    private static byte[] decoded(ECPoint point, EllipticCurve c){
        return point.getX().divide(k).toByteArray();
    }


    /**
     * 结合勒让德符号的计算去得到椭圆曲线上的一点(x, √(x^3+ax+b))
     * 该函数也可以用于随机生成椭圆曲线上的一个点
     * @param c
     * @param x
     * @return
     */
    private static ECPoint legendre(EllipticCurve c, BigInteger x) throws Exception {
        BigInteger p = c.getP();

        //计算(p-1)/2
        BigInteger p1 = p.subtract(BigInteger.ONE).shiftRight(1);

        //计算mk
        BigInteger tempX = x.multiply(k).mod(p);

        //计算一系列的x = {mk + j, j = 0, 1, 2....}
        for(long j = 0;j < k_long; ++j){
            BigInteger newX = tempX.add(BigInteger.valueOf(j)).mod(p);
            BigInteger a = c.calculateR(newX);

            //计算a^(p-1)/2,判断其是否与p互素，如果是则a是p的平方剩余
            if(a.modPow(p1,p).compareTo(BigInteger.ONE) == 0){
                //计算y = a ^ ((p + 1) / 4)，该结论只适用于p = 3 mod 4
                BigInteger y = a.modPow(p.add(BigInteger.ONE).shiftRight(2), p);
                return new ECPoint(newX, y);
            }
        }

        //如果找不到则返回异常；
        throw new Exception("在范围k = 1000中找不到明文对应的点");
    }

    /**
     * 填充byte型数组text，使其长度是blockSize的整数倍
     * 填充后的数组最后一个元素记录被填充的长度
     * @param text
     * @param blockSize
     * @return
     */
    private static byte[] pad(byte[] text, int blockSize) {
        int paddedLength = blockSize - (text.length % blockSize);
        byte[] padded = new byte[text.length + paddedLength];
        for(int i = 0; i < text.length; ++i){
            padded[i] = text[i];
        }
        //填充部分置0,只填充到倒数第二位
        for(int i = 0; i < paddedLength - 1; ++i){
            padded[text.length + i] = 0;
        }

        //最后一位为填充的长度paddedLength
        padded[padded.length - 1] = (byte)paddedLength;

        return padded;
    }

    /**
     * 去掉数组test中的填充内容
     * @param text
     * @param blockSize
     * @return
     */
    private static byte[] unpad(byte[] text, int blockSize){
        int paddedLength = text[text.length - 1];
        byte[] unpadded = new byte[text.length - paddedLength];
        for(int i = 0; i < unpadded.length; ++i){
            unpadded[i] = text[i];
        }
        return unpadded;
    }

    /**
     * 以字节为单位计算每个密文块的大小
     * @param c
     * @return
     */
    private static int getCipherTextBlockSize(EllipticCurve c) {
        return c.getP().bitLength() / 8 + 5;
    }

    /**
     * 以字节为单位计算明文分组后每一块的大小，至少为1
     * @param c
     * @return
     */
    private static int getPlainTextBlockSize(EllipticCurve c) {
        return Math.max(c.getP().bitLength() / 8 - 5, 1);
    }

    private static void initStartTime() {
        startTime = System.currentTimeMillis();
    }

    private static void calculateExecutionTime() {
        executionTime = System.currentTimeMillis() - startTime;
    }

    public static long getExecutionTime() {
        return executionTime;
    }



    public static void main(String[] args) throws Exception{
        EllipticCurve c = new EllipticCurve(
                new BigInteger("-3"),
                new BigInteger("64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1", 16),
                new BigInteger("6277101735386680763835789423207666416083908700390324961279"),
                new ECPoint(
                        new BigInteger("188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012", 16),
                        new BigInteger("07192b95ffc8da78631011ed6b24cdd573f977a11e794811", 16)
                )
        );

        Random rnd = new Random(System.currentTimeMillis());

        int nTest = 10;
        int failed = 0;
        int size = 1024;

        byte[] test = new byte[size];


        for (int itest = 0; itest < nTest; ++itest) {
            System.out.println("Test " + itest + ": " + size + " Bytes");
            // randomize test
            rnd.nextBytes(test);

            // generate pair of keys
            KeyPair keys = generateKeyPair(c, rnd);
            System.out.println("\tGenerating key pair: ");

            // encrypt test
            byte[] cipherText = encrypt(test, keys.getPublicKey());
            System.out.println("\tEncrypting         : ");

            // decrypt the result
            byte[] plainText = decrypt(cipherText, keys.getPrivateKey());
            System.out.println("\tDecrypting         : ");

            // compare them
            boolean match = test.length == plainText.length;
            for (int i = 0; i < test.length && i < plainText.length && match; ++i) {
                if (test[i] != plainText[i]) {
                    match = false;
                    failed++;
                }
            }
            System.out.println("\tResult             : " + match);

        }

        System.out.println("Failed: " + failed + " of " + nTest);


    }

}
