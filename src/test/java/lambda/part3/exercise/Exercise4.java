package lambda.part3.exercise;

import lambda.data.Employee;
import lambda.data.JobHistoryEntry;
import lambda.data.Person;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"unused", "ConstantConditions"})
class Exercise4 {

    private static class LazyCollectionHelper<T, R> {

        private List<T> source;
        private Optional<Function<T, List<R>>> flatFunction;

        private LazyCollectionHelper(List<T> source, Optional<Function<T, List<R>>> flatFunction) {
            this.source = source;
            this.flatFunction = flatFunction;
        }

        public static <T> LazyCollectionHelper<T, T> from(List<T> list) {
            return new LazyCollectionHelper<>(list, Optional.empty());
        }

        public <U> LazyCollectionHelper<T, U> flatMap(Function<R, List<U>> flatMapping) {
            return new LazyCollectionHelper<>(source, Optional.of(flatFunction.map(func -> func.andThen(list ->{
                List<U> result = new ArrayList<>();
                list.forEach(flatMapping.andThen(result::addAll)::apply);
                return result;
            })).orElse((Function<T, List<U>>)flatMapping)));

        }

        public <U> LazyCollectionHelper<T, U> map(Function<R, U> mapping) {
            return new LazyCollectionHelper<>(source, Optional.of(flatFunction.map(func -> func.andThen(list -> {
                List<U> result = new ArrayList<>();
                list.forEach(mapping.andThen(result::add)::apply);
                return result;
            })).orElse((Function<T, List<U>>)mapping)));
        }

        public List<R> force() {
            List<R> result = new ArrayList<>();
            flatFunction.ifPresent(func -> source.forEach(func.andThen(result::addAll)::apply));
            return result.isEmpty() ? (List<R>) source : result;
        }
    }

    @Test
    void mapEmployeesToCodesOfLetterTheirPositionsUsingLazyFlatMapHelper() {
        List<Employee> employees = getEmployees();

        List<Integer> codes = LazyCollectionHelper.from(employees)
                                          .flatMap(Employee::getJobHistory)
                                          .map(JobHistoryEntry::getPosition)
                                          .flatMap(Exercise4::calcCodes)
                                          .map(i -> i)
                                          .force();
        assertThat(codes, Matchers.contains(calcCodes("dev", "dev", "tester", "dev", "dev", "QA", "QA", "dev", "tester", "tester", "QA", "QA", "QA", "dev").toArray()));
    }

    @Test
    void mapEmployeesUsingLazyFlatMapHelper() {
        List<Employee> employees = getEmployees();

        List<Employee> employeeResultList = LazyCollectionHelper.from(employees).force();
        assertEquals(employees, employeeResultList);
    }

    private static List<Integer> calcCodes(String...strings) {
        List<Integer> codes = new ArrayList<>();
        for (String string : strings) {
            for (char letter : string.toCharArray()) {
                codes.add((int) letter);
            }
        }
        return codes;
    }

    private static List<Employee> getEmployees() {
        return Arrays.asList(
                new Employee(
                        new Person("Иван", "Мельников", 30),
                        Arrays.asList(
                                new JobHistoryEntry(2, "dev", "EPAM"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(
                        new Person("Александр", "Дементьев", 28),
                        Arrays.asList(
                                new JobHistoryEntry(1, "tester", "EPAM"),
                                new JobHistoryEntry(1, "dev", "EPAM"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(
                        new Person("Дмитрий", "Осинов", 40),
                        Arrays.asList(
                                new JobHistoryEntry(3, "QA", "yandex"),
                                new JobHistoryEntry(1, "QA", "mail.ru"),
                                new JobHistoryEntry(1, "dev", "mail.ru")
                        )),
                new Employee(
                        new Person("Анна", "Светличная", 21),
                        Collections.singletonList(
                                new JobHistoryEntry(1, "tester", "T-Systems")
                        )),
                new Employee(
                        new Person("Игорь", "Толмачёв", 50),
                        Arrays.asList(
                                new JobHistoryEntry(5, "tester", "EPAM"),
                                new JobHistoryEntry(6, "QA", "EPAM")
                        )),
                new Employee(
                        new Person("Иван", "Александров", 33),
                        Arrays.asList(
                                new JobHistoryEntry(2, "QA", "T-Systems"),
                                new JobHistoryEntry(3, "QA", "EPAM"),
                                new JobHistoryEntry(1, "dev", "EPAM")
                        ))
        );
    }

}
