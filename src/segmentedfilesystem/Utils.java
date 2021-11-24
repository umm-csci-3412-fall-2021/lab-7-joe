package segmentedfilesystem;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {
    public static Set<Integer> range(int start, int end) {
        return IntStream.range(start, end).boxed().collect(Collectors.toSet());
    }

    public static byte[] flatten(Collection<byte[]> chunks) {
        // Primitive types are the worst :(
        int totalLength = chunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] allBytes = new byte[totalLength];

        int i = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, allBytes, i, chunk.length);
            i += chunk.length;
        }

        return allBytes;
    }
}
