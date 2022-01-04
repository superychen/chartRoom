package net.cqyc.sample.server.server.testqiangge;

import java.util.List;

/**
 * @Description:
 * 输入：[1,0,1,1,0]
 * 输出：4
 * 解释：翻转第一个 0 可以得到最长的连续 1。
 *      当翻转以后，最大连续 1 的个数为 4。
 * @author: cqyc
 * @date 2022/1/4
 */
public class FindMaxConsecutiveOnes {

    public int findMaxConsecutiveOnes(List<Integer> nums) {
        int res = 0, cur = 0, cnt = 0;
        for (Integer num : nums) {
            ++cnt;
            if(num == 0) {
                cur = cnt;
                cnt = 0;
            }
            res = Math.max(res, cur + cnt);
        }
        return res;
    }

}
