package streams.part2.exercise;

import lombok.Value;

@Value
public class Pair<T>
{
    int index;
    T value;
}
