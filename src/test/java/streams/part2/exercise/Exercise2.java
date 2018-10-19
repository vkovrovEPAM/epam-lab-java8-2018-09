package streams.part2.exercise;

import lambda.data.Employee;
import lambda.data.JobHistoryEntry;
import lambda.data.Person;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("ConstantConditions")
class Exercise2 {

    /**
     * Преобразовать список сотрудников в отображение [компания -> множество людей, когда-либо работавших в этой компании].
     *
     * Входные данные:
     * [
     *     {
     *         {Иван Мельников 30},
     *         [
     *             {2, dev, "EPAM"},
     *             {1, dev, "google"}
     *         ]
     *     },
     *     {
     *         {Александр Дементьев 28},
     *         [
     *             {2, tester, "EPAM"},
     *             {1, dev, "EPAM"},
     *             {1, dev, "google"}
     *         ]
     *     },
     *     {
     *         {Дмитрий Осинов 40},
     *         [
     *             {3, QA, "yandex"},
     *             {1, QA, "EPAM"},
     *             {1, dev, "mail.ru"}
     *         ]
     *     },
     *     {
     *         {Анна Светличная 21},
     *         [
     *             {1, tester, "T-Systems"}
     *         ]
     *     }
     * ]
     *
     * Выходные данные:
     * [
     *    "EPAM" -> [
     *       {Иван Мельников 30},
     *       {Александр Дементьев 28},
     *       {Дмитрий Осинов 40}
     *    ],
     *    "google" -> [
     *       {Иван Мельников 30},
     *       {Александр Дементьев 28}
     *    ],
     *    "yandex" -> [ {Дмитрий Осинов 40} ]
     *    "mail.ru" -> [ {Дмитрий Осинов 40} ]
     *    "T-Systems" -> [ {Анна Светличная 21} ]
     * ]
     */
    @Test
    void employersStuffList() {
        List<Employee> employees = getEmployees();

        Map<String, Set<Person>> result  = employees.stream()
                .flatMap(emp -> emp.getJobHistory().stream()
                        .map(entry -> new Tuple<>(emp.getPerson(), entry.getEmployer())))
                .collect(Collectors.groupingBy(Tuple::getB, Collectors.mapping(Tuple::getA, Collectors.toSet())));

        assertThat(result, hasEntry((is("yandex")), contains(employees.get(2).getPerson())));
        assertThat(result, hasEntry((is("mail.ru")), contains(employees.get(2).getPerson())));
        assertThat(result, hasEntry((is("google")), containsInAnyOrder(employees.get(0).getPerson(), employees.get(1).getPerson())));
        assertThat(result, hasEntry((is("T-Systems")), containsInAnyOrder(employees.get(3).getPerson(), employees.get(5).getPerson())));
        assertThat(result, hasEntry((is("EPAM")), containsInAnyOrder(
                employees.get(0).getPerson(),
                employees.get(1).getPerson(),
                employees.get(4).getPerson(),
                employees.get(5).getPerson()))
        );
    }

    /**
     * Преобразовать список сотрудников в отображение [компания -> множество людей, начавших свою карьеру в этой компании].
     *
     * Пример.
     *
     * Входные данные:
     * [
     *     {
     *         {Иван Мельников 30},
     *         [
     *             {2, dev, "EPAM"},
     *             {1, dev, "google"}
     *         ]
     *     },
     *     {
     *         {Александр Дементьев 28},
     *         [
     *             {2, tester, "EPAM"},
     *             {1, dev, "EPAM"},
     *             {1, dev, "google"}
     *         ]
     *     },
     *     {
     *         {Дмитрий Осинов 40},
     *         [
     *             {3, QA, "yandex"},
     *             {1, QA, "EPAM"},
     *             {1, dev, "mail.ru"}
     *         ]
     *     },
     *     {
     *         {Анна Светличная 21},
     *         [
     *             {1, tester, "T-Systems"}
     *         ]
     *     }
     * ]
     *
     * Выходные данные:
     * [
     *    "EPAM" -> [
     *       {Иван Мельников 30},
     *       {Александр Дементьев 28}
     *    ],
     *    "yandex" -> [ {Дмитрий Осинов 40} ]
     *    "T-Systems" -> [ {Анна Светличная 21} ]
     * ]
     */
    @Test
    void indexByFirstEmployer() {
        List<Employee> employees = getEmployees();

        Map<String, Set<Person>> result = employees.stream()
                .flatMap(employee -> employee.getJobHistory().stream()
                        .limit(1)
                        .map(entry -> new Tuple<>(employee.getPerson(), entry.getEmployer())))
                .collect(Collectors.groupingBy(Tuple::getB, Collectors.mapping(Tuple::getA, Collectors.toSet())));

        assertThat(result, hasEntry(is("yandex"), contains(employees.get(2).getPerson())));
        assertThat(result, hasEntry(is("T-Systems"), containsInAnyOrder(employees.get(3).getPerson(), employees.get(5).getPerson())));
        assertThat(result, hasEntry(is("EPAM"), containsInAnyOrder(
                employees.get(0).getPerson(),
                employees.get(1).getPerson(),
                employees.get(4).getPerson()
        )));
    }

    /**
     * Преобразовать список сотрудников в отображение [компания -> сотрудник, суммарно проработавший в ней наибольшее время].
     * Гарантируется, что такой сотрудник будет один.
     */
    @Test
    void greatestExperiencePerEmployer() {
        List<Employee> employees = getEmployees();

        Map<String, Person> collect = employees.stream()
                .flatMap(emp -> emp.getJobHistory().stream()
                        .map(entry -> new Triple<>(emp.getPerson(), entry.getEmployer(), entry.getDuration())))
                .collect(Collectors.toMap(triple -> new Tuple<>(triple.getA(), triple.getB()), Triple::getC, (t1, t2) -> t1 + t2))
                .entrySet()
                .stream()
                .map(entry -> new Triple<>(entry.getKey().getA(), entry.getKey().getB(), entry.getValue()))
                .collect(Collectors.toMap(Triple::getB, t1 -> new Tuple<>(t1.getA(), t1.getC()),
                        (tuple1, tuple2) -> tuple1.getB() > tuple2.getB() ? tuple1 : tuple2))
                .entrySet().stream()
                .map(entry -> new Tuple<>(entry.getKey(), entry.getValue().getA()))
                .collect(Collectors.toMap(Tuple::getA, Tuple::getB));

        assertThat(collect, hasEntry("EPAM", employees.get(4).getPerson()));
        assertThat(collect, hasEntry("google", employees.get(1).getPerson()));
        assertThat(collect, hasEntry("yandex", employees.get(2).getPerson()));
        assertThat(collect, hasEntry("mail.ru", employees.get(2).getPerson()));
        assertThat(collect, hasEntry("T-Systems", employees.get(5).getPerson()));
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
                                new JobHistoryEntry(2, "dev", "EPAM"),
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