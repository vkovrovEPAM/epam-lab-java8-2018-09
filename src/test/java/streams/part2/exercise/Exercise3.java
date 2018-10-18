package streams.part2.exercise;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings({"unused", "ConstantConditions"})
class Exercise3 {

    @Test
    void createLimitedStringWithOddNumbersSeparatedBySpaces() {
        int countNumbers = 10;

        String result = IntStream.iterate(1, i -> i + 2)
                .limit(countNumbers)
                .mapToObj(String::valueOf)
                .collect(() -> new StringJoiner(" "), StringJoiner::add, StringJoiner::merge)
                .toString();

        assertThat(result, is("1 3 5 7 9 11 13 15 17 19"));
    }

    @Test
    void extractEvenNumberedCharactersToNewString() {
        String source = "abcdefghijklm";
        AtomicInteger index = new AtomicInteger(0);
        String result = Arrays.stream(source.split(""))
                .map(el -> new Pair(index.getAndIncrement(), el))
                .filter(el -> el.getIndex() % 2 == 0)
                .map(Pair::getValue)
                .map(Object::toString)
                .collect(() -> new StringJoiner(""), StringJoiner::add, StringJoiner::merge)
                .toString();

        assertThat(result, is("acegikm"));
    }
}
