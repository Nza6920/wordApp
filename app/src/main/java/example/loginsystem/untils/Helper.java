package example.loginsystem.untils;

/**
 * Created by Administrator on 2018/12/20.
 */

public class Helper {

    // 生成随机数
    public static int[] randomCommon(int min, int max, int n){
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n];
        int count = 0;
        while(count < n) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if(num == result[j]){
                    flag = false;
                    break;
                }
            }
            if(flag){
                result[count] = num;
                count++;
            }
        }
        return result;
    }
}
