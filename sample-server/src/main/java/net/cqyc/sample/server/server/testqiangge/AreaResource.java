package net.cqyc.sample.server.server.testqiangge;

/**
 * @author cqyc
 * @Description:
 * @date 2021/11/23
 */
public class AreaResource {
    /**

     *  area表示的是地区全路径,最多可能有6级,用分隔符连接,分隔符是 spliter,

     *  例如分隔符是逗号 则area型如  中国,四川,成都    中国,浙江,杭州  中国,浙江,义乌

     *  count表示门店数

     */

    String area;

    String spliter;

    long count;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getSpliter() {
        return spliter;
    }

    public void setSpliter(String spliter) {
        this.spliter = spliter;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
