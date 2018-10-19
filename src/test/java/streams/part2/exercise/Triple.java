package streams.part2.exercise;

import lombok.Value;

@Value
public class Triple<A, B, C>
{
    A a;
    B b;
    C c;
}
