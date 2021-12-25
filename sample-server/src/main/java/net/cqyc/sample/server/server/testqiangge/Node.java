package net.cqyc.sample.server.server.testqiangge;

/**
 * @author cqyc
 * @Description:
 * @date 2021/11/23
 */
public class Node {

    private String area;

    private Long mount;

    public Node next;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Long getMount() {
        return mount;
    }

    public void setMount(Long mount) {
        this.mount = mount;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }


}
