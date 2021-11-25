package segmentedfilesystem;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class UtilsTest {
    @Test
    public void testRangeWorks() {
        assertThat(Utils.range(0, 5)).containsExactlyInAnyOrder(0, 1, 2, 3, 4);
        assertThat(Utils.range(1, 2)).containsExactlyInAnyOrder(1);
        assertThat(Utils.range(-5, 0)).containsExactlyInAnyOrder(-5, -4, -3, -2, -1);
    }

    @Test
    public void testEmptyRangesWork() {
        assertThat(Utils.range(0, 0)).isEmpty();
        assertThat(Utils.range(10, 9)).isEmpty();
    }

    @Test
    public void testFlattenWorks() {
        List<byte[]> input = List.of(
            new byte[] { 1, 2, 3 },
            new byte[] { 4, 5 },
            new byte[] { 6, 7, 8, 9, 10 }
        );
        byte[] expected = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        assertThat(Utils.flatten(input)).containsExactly(expected);
    }

    @Test
    public void testFlattenWorksWithEmptyRows() {
        List<byte[]> input = List.of(
            new byte[] { 1, 2, 3 },
            new byte[0],
            new byte[] { 6, 7, 8, 9, 10 }
        );
        byte[] expected = new byte[] { 1, 2, 3, 6, 7, 8, 9, 10 };
        assertThat(Utils.flatten(input)).containsExactly(expected);
    }

    @Test
    public void testFlattenWorksWithNoRows() {
        Set<byte[]> input = Collections.emptySet();
        assertThat(Utils.flatten(input)).isEmpty();
    }
}
