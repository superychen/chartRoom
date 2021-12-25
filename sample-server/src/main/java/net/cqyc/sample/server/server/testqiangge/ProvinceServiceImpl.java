package net.cqyc.sample.server.server.testqiangge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cqyc
 * @Description:
 * @date 2021/11/23
 */
public class ProvinceServiceImpl implements ProvinceService {
    @Override
    public String getFormattedJSONByResource(List<AreaResource> areas) {

        Node pre = new Node();
        Node cur = new Node();
        for (AreaResource area : areas) {
            String[] areaInfos = area.getArea().split(",");
            Node node = new Node();
            for (String areaInfo : areaInfos) {
                cur.setArea(areaInfo);
                cur.setMount(area.getCount());
                pre = cur;
                cur = cur.next;
            }

        }
        return null;
    }
}
