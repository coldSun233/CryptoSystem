package ECC;

import java.math.BigInteger;

/**
 * 该类是有限域GF(p)上的椭圆曲线类
 * 该类同时也实现了椭圆曲线上点的一些基础操作，包括加，减，乘
 */
public class EllipticCurve {
    //椭圆曲线y^2 = x^3 + ax + b的参数
    private BigInteger a;
    private BigInteger b;

    //有限域Fq的特征P
    private BigInteger p;

    //基点g
    private ECPoint g = null;

    //用于计算的一些数，如判别式的计算
    private  BigInteger Two = new BigInteger("2");
    private BigInteger Three = new BigInteger("3");
    private BigInteger Four = new BigInteger("4");
    private BigInteger TwentySeven = new BigInteger("27");

    public EllipticCurve(BigInteger a,BigInteger b,BigInteger p,ECPoint g) throws Exception{
        this.a = a;
        this.b = b;
        this.p = p;
        this.g = g;
        //如果p一定是合数，或该椭圆曲线是奇异的，或g不在椭圆曲线上，则初试化失败
        if(!p.isProbablePrime(500) || isSingular(a, b) || !isPointOnCurve(g)){
            throw new Exception("椭圆曲线的初始化参数不对");
        }
    }

    public EllipticCurve(BigInteger b, BigInteger p, ECPoint g) {
        this.a = new BigInteger("-3"); //NIST建议的椭圆曲线a = -3
        this.b = b;
        this.p =p;
        this.g = g;
    }

    public ECPoint getBasePoint() {
        return g;
    }

    public void setBasePoint(ECPoint g) {
        this.g = g;
    }

    public BigInteger getA() {
        return a;
    }

    public BigInteger getB() {
        return b;
    }

    public BigInteger getP() {
        return p;
    }


    /**
     * 判断点point是否在椭圆曲线上
     * @param point
     * @return
     */
    public boolean isPointOnCurve(ECPoint point){
        BigInteger x = point.getX();
        BigInteger y = point.getY();
        return x.multiply(x).mod(p).add(a).multiply(x).add(b).mod(p)
                .subtract(y.multiply(y)).mod(p).compareTo(BigInteger.ZERO) == 0;
    }

    /**
     * 判断该椭圆曲线是否是奇异的，即4a^3+27b^2是否等于0,是则返回true
     * @return
     */
    public boolean isSingular(BigInteger a, BigInteger b){
        return Four.multiply(a.pow(3)).mod(p).add(TwentySeven.multiply(b.pow(2)).mod(p))
                .mod(p).compareTo(BigInteger.ZERO) == 0;
    }

    /**
     * 椭圆曲线上两点的加法
     * 若有一个为空则返回空
     * 如果其中一个为无穷远点O，则返回另一个点
     * 否则执行返回相加的结果
     * @param p1
     * @param p2
     * @return
     */
    public ECPoint add(ECPoint p1,ECPoint p2){
        if(p1 == null || p2 == null)
            return null;
        if(p1.isPointOfInfinity())
            return p2;
        else if(p2.isPointOfInfinity())
            return p1;

        /*加法规则如下:设P=(x1,y1) Q=(x2,y2),P != -Q 则p + Q = (x3, y3)
            x3 ≡ λ^2 - x1 - x2(mod p)
            y3 ≡ λ(x1 - x3) - y1(mod p)
          其中
          P != Q 时 λ = (y2 - y1)/(x2 -x1)
          P == Q 时 λ = (3*x^2 + a)/(2y1)
         */
        BigInteger lambda;//即λ
        BigInteger x1 = p1.getX();
        BigInteger y1 = p1.getY();
        BigInteger x2 = p2.getX();
        BigInteger y2 = p2.getY();

        if(x1.subtract(x2).mod(p).compareTo(BigInteger.ZERO) == 0){
            if(y1.subtract(y2).mod(p).compareTo(BigInteger.ZERO) == 0){
                //P == Q时
                //BigInteger num = x1.modPow(Two,p).multiply(Three).add(a).mod(p);
                //BigInteger den = y1.add(y1);
                //lambda = num.multiply(den.modInverse(p)).mod(p);
                return doublePoint(p1);
            } else{
                //P,Q横坐标相同纵坐标不同，此时P,Q位于一条直线上，此时直接返回无穷远点O
                return ECPoint.infinity();
            }
        } else{
            //P != Q 时
            BigInteger num = y2.subtract(y1);
            BigInteger den = x2.subtract(x1);
            lambda = num.multiply(den.modInverse(p));
        }

        //计算x3，y3
        BigInteger x3 = lambda.modPow(Two,p).subtract(x1).subtract(x2).mod(p);
        BigInteger y3 = lambda.multiply(x1.subtract(x3)).subtract(y1).mod(p);

        return new ECPoint(x3,y3);
    }

    /**
     * 椭圆曲线上两点之间的减法，p1-p2
     * 若p1或p2有一个为null，则返回null
     * 否则返回p1+(-P2)
     * @param p1
     * @param p2
     * @return
     */
    public ECPoint subtract(ECPoint p1,ECPoint p2){
        if(p1 == null || p2 == null)
            return null;
        return add(p1, p2.negate());
    }

    public ECPoint doublePoint(ECPoint point){
        if(point == null)
            return null;
        if(point.isPointOfInfinity())
            return ECPoint.infinity();
        BigInteger x = point.getX();
        BigInteger y = point.getY();

        BigInteger num = x.modPow(Two,p).multiply(Three).add(a).mod(p);
        BigInteger den = y.add(y);
        BigInteger lambda = num.multiply(den.modInverse(p)).mod(p);

        BigInteger x3 = lambda.modPow(Two,p).subtract(x).subtract(x).mod(p);
        BigInteger y3 = lambda.multiply(x.subtract(x3)).subtract(y).mod(p);

        return new ECPoint(x3,y3);
    }


    /**
     * 设P(x,y)，返回nP的结果
     * @param n
     * @param p
     * @return
     */
    public ECPoint multiply(BigInteger n,ECPoint p){
        //return binaryExp(n,p);
        return slidingWindow(n, p);

    }

    //二进制展开法求倍点运算
    private ECPoint binaryExp(BigInteger n,ECPoint p) {
        if(p.isPointOfInfinity())
            return ECPoint.infinity();

        ECPoint result = ECPoint.infinity();
        int bitLength = n.bitLength();
        for(int i = bitLength - 1; i >= 0; i--){
            //result = add(result, result);
            result = doublePoint(result);
            if(n.testBit(i)){
                result = add(result, p);
            }
        }

        return result;
    }

    //滑动窗口法求倍点运算
    private ECPoint slidingWindow(BigInteger n, ECPoint p){
        if(p.isPointOfInfinity())
            return ECPoint.infinity();

        int h = 3; //窗口的指定大小
        //预处理
        int j = 2 << (h - 1) - 1;
        ECPoint[] points = new ECPoint[j + 2];
        points[0] = doublePoint(p);
        points[1] = p;
        for(int i = 2; i <= j + 1; ++i) {
            points[i] = this.add(points[i - 1], points[0]);
        }

        //主运算
        ECPoint q = ECPoint.infinity();
        for(int i = n.bitLength() - 1; i >= 0;){
            if(n.testBit(i)){
                int l = i - h + 1;
                if(l < 0)
                    l = 0;
                while(!n.testBit(l))
                    l++;
                for(int m = 1; m <= i - l + 1; ++m)
                    q = doublePoint(q);

                int offset = 0;
                int temp = 1;
                for(int x = l; x <= i; ++x){
                    if(n.testBit(x))
                        offset += temp;
                    temp = 2 * temp;
                }

                q = this.add(q, points[offset / 2 + 1]);
                i= l - 1;
            }
            else{
                q = doublePoint(q);
                i--;
            }
        }

        return q;
    }
    /**
     * 计算椭圆曲线y^2 = x^3 + ax + b的右半部分
     * @param x
     * @return
     */
    public BigInteger calculateR(BigInteger x){
        return x.multiply(x).mod(p).add(a).multiply(x).add(b).mod(p);
    }

}
