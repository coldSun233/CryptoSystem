package RSA;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class RSACryptoSystem {

    private static long executionTime = -1;
    private static long startTime;

    public static byte[] encrypt(byte[] plainText, PublicKey Key) {
        initStartTime();

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
        calculateExecutionTime();
        return cipherText;

    }

    public static  byte[] decrypt(byte[] cipherText, PrivateKey Key) throws Exception {
        initStartTime();

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

        calculateExecutionTime();

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
        if(paddedLength <= 127)
            //最后一位为填充的长度paddedLength
            padded[padded.length - 1] = (byte)paddedLength;
        else{//否则最后两个byte存储填充的数目
            byte[] result = intToByte(paddedLength);
            padded[padded.length - 2] = result[0];
            padded[padded.length - 1] = result[1];
        }

        return padded;
    }

    /**
     * 去掉填充
     * @param text
     * @param blockSize
     * @return
     */
    private static byte[] unpad(byte[] text, int blockSize){
        int paddedLength;
        if((int)text[text.length - 1] >= 0)
            paddedLength = text[text.length - 1];
        else{
            byte[] array = new byte[2];
            array[0] = text[text.length - 2];
            array[1] = text[text.length - 1];
            paddedLength = byteToInt(array);
        }
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

    private static byte[] intToByte(int num){
        byte result[] = new byte[2];//2个byte存储填充的数目对RSA来说已经足够
        result[0] = (byte)((num>>8)&0xFF);
        result[1] = (byte)(num &0xFF);
        return result;
    }

    private static int byteToInt(byte[] array) {
        return (int)(((array[0] & 0xff) << 8)
                | ((array[1] & 0xff) << 0));
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


    public static void main(String[] args) throws  Exception{
        //int failed = 0;
        long encryptTime = 0;
        long decryptTime = 0;
        String plainPath = "D:/test.txt";
        String encryptPath = "D:/encrypt_rsa.txt";
        String decryptPath = "D:/decrypt_rsa.txt";

        KeyPair keys = generateKeyPair(2048);

        byte[] plainText = Files.readAllBytes(Paths.get(plainPath));
        byte[] byteFile = encrypt(plainText, keys.getPublicKey());
        encryptTime = getExecutionTime();
        FileOutputStream fout = new FileOutputStream(encryptPath);
        fout.write(byteFile);
        fout.close();

        byte[] cipherText = Files.readAllBytes(Paths.get(encryptPath));
        byteFile = decrypt(cipherText, keys.getPrivateKey());
        decryptTime = getExecutionTime();
        fout = new FileOutputStream(decryptPath);
        fout.write(byteFile);
        fout.close();

        System.out.println("encrypt" + encryptTime);
        System.out.println("decrypt" + decryptTime);

    }

}
