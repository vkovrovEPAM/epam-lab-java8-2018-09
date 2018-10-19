package streams.part2.exercise;

import lombok.Value;

@Value
public class Tuple<A, B>
{
    A a;
    B b;
}
