package roomescape.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;


@JdbcTest
@Import({ReservationRepository.class, TimeRepository.class, ThemeRepository.class})
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ThemeRepositoryTest {

    ReservationTime time1;
    ReservationTime time2;
    ReservationTime time3;
    ReservationTime time4;
    Theme theme1;
    Theme theme2;
    Theme theme3;
    Theme theme4;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private Clock clock;

    @BeforeEach
    void setUp() {
        time1 = timeRepository.save(new ReservationTime(null, LocalTime.of(8, 0)));
        time2 = timeRepository.save(new ReservationTime(null, LocalTime.of(9, 0)));
        time3 = timeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        time4 = timeRepository.save(new ReservationTime(null, LocalTime.of(11, 0)));

        theme1 = themeRepository.save(new Theme(null, "테스트_테마_1", "테스트1입니다", "FakeURL"));
        theme2 = themeRepository.save(new Theme(null, "테스트_테마_2", "테스트2입니다", "FakeURL"));
        theme3 = themeRepository.save(new Theme(null, "테스트_테마_3", "테스트3입니다", "FakeURL"));
        theme4 = themeRepository.save(new Theme(null, "테스트_테마_4", "테스트4입니다", "FakeURL"));

        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock), time1, theme1));
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock), time2, theme2));
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock), time3, theme3));
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock), time4, theme4));

        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock).minusDays(2), time1, theme4));
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock).minusDays(2), time2, theme4));
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock).minusDays(2), time3, theme4));
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock).minusDays(2), time1, theme2));
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock).minusDays(2), time2, theme2));
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock).minusDays(2), time1, theme1));
    }

    @Test
    public void 새로운_테마를_등록할_수_있다() {
        Theme theme = new Theme(null, "테스트_테마_5", "테스트5입니다", "FakeURL");
        Theme newTheme = themeRepository.save(theme);

        assertAll(
                () -> assertNotNull(newTheme.id()),
                () -> assertEquals("테스트_테마_5", newTheme.name()),
                () -> assertEquals("테스트5입니다", newTheme.description()),
                () -> assertEquals("FakeURL", newTheme.url())
        );
    }

    @Test
    public void 저장된_테마를_모두_조회할_수_있다() {
        List<Theme> themes = themeRepository.findAll();
        assertEquals(4, themes.size());
    }

    @Test
    public void 저장된_테마를_삭제할_수_있다() {
        Theme theme = new Theme(null, "테스트_테마_5", "테스트5입니다", "FakeURL");
        Theme newTheme = themeRepository.save(theme);

        themeRepository.deleteById(newTheme.id());

        Optional<Theme> result = themeRepository.findById(newTheme.id());
        assertTrue(result.isEmpty());
    }

    @Test
    public void 특정_테마_아이디를_통해서_테마를_조회할_수_있다() {
        Optional<Theme> theme = themeRepository.findById(theme2.id());

        assertAll(
                () -> assertEquals(theme2.id(), theme.get().id()),
                () -> assertEquals(theme2.name(), theme.get().name()),
                () -> assertEquals(theme2.description(), theme.get().description()),
                () -> assertEquals(theme2.url(), theme.get().url())
        );
    }

    @Test
    public void 지난_일주일간_가장_예약이_많았던_상위_10개_테마를_가져온다() {
        LocalDate currentDate = LocalDate.now(clock).minusDays(1);
        LocalDate lastWeekDate = LocalDate.now(clock).minusDays(8);
        int limit = 10;

        List<Theme> themes = themeRepository.findByCurrentDateAndLastWeekDateAndLimit(currentDate.toString(),
                lastWeekDate.toString(), limit);

        assertAll(
                () -> assertEquals(themes.get(0).id(), theme4.id()),
                () -> assertEquals(themes.get(1).id(), theme2.id()),
                () -> assertEquals(themes.get(2).id(), theme1.id())
        );
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public Clock clock() {
            return Clock.fixed(
                    LocalDate.now().atTime(14, 0).toInstant(ZoneOffset.UTC),
                    ZoneOffset.UTC
            );
        }
    }
}
