package ECC;

import java.math.BigInteger;

/**
 * 该类是关于椭圆曲线上点的类
 */
public class ECPoint {
    //点的坐标(x,y)
    private BigInteger x;
    private BigInteger y;
    private boolean pointOfInfinity;//值为true是代表无穷远点O

    public ECPoint(){
        this.x = this.y = BigInteger.ZERO;
        this.pointOfInfinity = false;
    }

    public ECPoint(BigInteger x, BigInteger y){
        this.x = x;
        this.y = y;
        this.pointOfInfinity = false;
    }

    public ECPoint(ECPoint p){
        this.x = p.getX();
        this.y = p.getY();
        this.pointOfInfinity = p.isPointOfInfinity();
    }


    public BigInteger getX() {
        return x;
    }

    public BigInteger getY() {
        return y;
    }

    public void setPointOfInfinity(boolean flag){
        pointOfInfinity = flag;
    }

    /**
     * 判断该点是否为无穷远点O
     * @return
     */
    public boolean isPointOfInfinity(){
        return pointOfInfinity;
    }

    /**
     * 生成一个无穷远点
     * @return
     */
    public static ECPoint infinity(){
        ECPoint point =new ECPoint();
        point.setPointOfInfinity(true);
        return point;
    }

    /**
     * 设P(x,y),返回-p(x,-y)
     * @return
     */
    public ECPoint negate(){
        if(isPointOfInfinity())
            return infinity();
        else
            return new ECPoint(x,y.negate());
    }
}
