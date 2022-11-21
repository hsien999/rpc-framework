package github.hsien.rpc.common.util;

import java.util.Random;

/**
 * Random util
 *
 * @author hsien
 */
public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static int randomInt() {
        return RANDOM.nextInt();
    }
}
