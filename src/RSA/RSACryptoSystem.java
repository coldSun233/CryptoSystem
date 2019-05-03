package RSA;

import java.math.BigInteger;
import java.util.Random;

public class RSACryptoSystem {

    public static byte[] encrypt(byte[] plainText, PublicKey Key) {
        BigInteger e = Key.getE();
        BigInteger n = Key.getN();
        int bitLength = Key.getN().bitLength();
        int plainTextBlockSize = getPlainTextBlockSize(bitLength);
        int cipherTextBlockSize = getCipherTextBlockSize(bitLength);

        byte[] padded = pad(plainText, plainTextBlockSize);

        //分组
        byte[][] block = split(padded, plainTextBlockSize);
        //加密
        BigInteger[] encrypted = new BigInteger[block.length];
        for(int i = 0; i < encrypted.length; ++i) {
            encrypted[i] = new BigInteger(1,block[i]).modPow(e, n);
        }

        //合并
        byte[] cipherText = new byte[encrypted.length * cipherTextBlockSize];
        for(int i = 0; i < encrypted.length; ++i){
            byte[] cipher = encrypted[i].toByteArray();
            int offset = i * cipherTextBlockSize + cipherTextBlockSize - cipher.length;
            for(int j = 0; j < cipher.length; ++j) {
                cipherText[j + offset] = cipher[j];
            }
        }

        return cipherText;

    }

    public static  byte[] decrypt(byte[] cipherText, PrivateKey Key) throws Exception {
        BigInteger d = Key.getD();
        BigInteger n = Key.getN();
        int bitLength = Key.getN().bitLength();
        int plainBlockSize = getPlainTextBlockSize(bitLength);
        int cipherBlockSize = getCipherTextBlockSize(bitLength);

        if(cipherText.length % cipherBlockSize != 0) {
            throw new Exception("密文的长度非法");
        }
        //分组
        byte[][] block = split(cipherText, cipherBlockSize);
        //解密
        BigInteger[] decrypted = new BigInteger[block.length];
        for(int i = 0; i < decrypted.length; ++i) {
            decrypted[i] = new BigInteger(block[i]).modPow(d, n);
        }

        //合并
        byte[] plainText = new byte[decrypted.length * plainBlockSize];
        for(int i = 0; i< decrypted.length; ++i) {
            byte[] plain = decrypted[i].toByteArray();
            for(int j = Math.max(plainBlockSize - plain.length, 0); j < plainBlockSize; ++j) {
                plainText[i * plainBlockSize + j] = plain[j + plain.length - plainBlockSize];
            }
        }

        plainText = unpad(plainText, plainBlockSize);

        return plainText;
    }

    public static KeyPair generateKeyPair(int bitLength) {
        RSA rsa = new RSA(bitLength);
        return new KeyPair(new PublicKey(rsa.getE(), rsa.getN()), new PrivateKey(rsa.getD(), rsa.getN()));
    }

    /**
     * 分组
     * @param text
     * @param blockSize
     * @return
     */
    private static byte[][] split(byte[] text, int blockSize) {
        byte[][] block = new byte[text.length/ blockSize][blockSize];
        for (int i = 0; i < block.length; ++i) {
            for (int j = 0; j < blockSize; ++j){
                block[i][j] = text[i * blockSize + j];
            }
        }
        return block;
    }

    /**
     *  填充
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
     * 去掉填充
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

    private static int getPlainTextBlockSize(int bitLength) {
        return Math.max((bitLength - 1) / 8, 1); //保证加密的m不大于n
    }

    private static int getCipherTextBlockSize(int bitLength) {
        return (bitLength / 8) + 1; //保证数组能存下c的信息额外添加一个字节
    }

    public static void main(String[] args) throws  Exception{
        int failed = 0;
        Random random = new Random(System.currentTimeMillis());
        byte[] test = new byte[1024];
        for(int i = 0; i < 10; ++i) {
            System.out.println("test" + i);
            random.nextBytes(test);

            KeyPair keys = generateKeyPair(1024);
            System.out.println("generate key pair");

            byte[] cipherText = encrypt(test, keys.getPublicKey());
            System.out.println("encrypt");

            byte[] plainText = decrypt(cipherText, keys.getPrivateKey());
            System.out.println("decrypt");

            System.out.println(plainText.length);
            boolean match = test.length == plainText.length;
            System.out.println("result0" + match);
            for (int j = 0; j < test.length; ++j) {
                if(test[j] != plainText[j]) {
                    match = false;
                    failed++;
                }
            }

            System.out.println("result" + match);
            System.out.println("failed" + failed);

        }
    }

}
