package roomescape.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class ReservationTimeRepositoryTest {

    ReservationTime time1;
    ReservationTime time2;
    ReservationTime time3;
    ReservationTime time4;
    Theme theme1;
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
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock), time1, theme1));
        reservationRepository.save(new Reservation(null, "포비", LocalDate.now(clock), time3, theme1));
    }

    @Test
    void 전쳬_시간_조회를_할_수_있다() {
        List<ReservationTime> times = timeRepository.findAll();
        assertEquals(4, times.size());
    }

    @Test
    void 특정_시간을_삭제할_수_있다() {
        ReservationTime time = new ReservationTime(null, LocalTime.of(12, 0));
        ReservationTime savedTime = timeRepository.save(time);

        timeRepository.deleteById(savedTime.id());

        Optional<ReservationTime> result = timeRepository.findById(savedTime.id());
        assertTrue(result.isEmpty());
    }

    @Test
    void timeId로_시간을_저장하고_조회할_수_있다() {
        ReservationTime time = new ReservationTime(null, LocalTime.of(12, 0));
        ReservationTime savedTime = timeRepository.save(time);

        Optional<ReservationTime> findTime = timeRepository.findById(savedTime.id());
        assertEquals(time.startAt(), findTime.get().startAt());
    }

    @Test
    void 테마ID와_날짜로_예약_가능한_시간을_조회할_수_있다() {
        List<ReservationTime> times = timeRepository.findAllByThemeIdAndDate(theme1.id(),
                LocalDate.now(clock).toString());
        assertEquals(2, times.size());
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
