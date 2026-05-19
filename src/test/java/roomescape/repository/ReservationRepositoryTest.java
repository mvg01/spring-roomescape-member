package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

@JdbcTest
@Import({ReservationRepository.class, TimeRepository.class, ThemeRepository.class})
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationRepositoryTest {

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
    }

    @Test
    void 모든_예약을_조회한다() {
        List<Reservation> reservations = reservationRepository.findAll();
        assertEquals(4, reservations.size());
    }

    @Test
    void 예약을_삭제할_수_있다() {
        Reservation reservation = new Reservation(null, "무빙", LocalDate.now(clock), time1, theme4);
        Reservation saved = reservationRepository.save(reservation);
        reservationRepository.deleteById(saved.id());

        assertThatThrownBy(() -> reservationRepository.findById(saved.id()))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void 예약을_생성할_수_있다() {
        Reservation reservation = new Reservation(null, "무빙", LocalDate.now(clock), time1, theme4);

        Reservation saved = reservationRepository.save(reservation);

        assertAll(
                () -> assertEquals(5, reservationRepository.findAll().size()),
                () -> assertNotNull(saved.id()),
                () -> assertEquals("무빙", saved.name()),
                () -> assertEquals(LocalDate.now(clock), saved.date()),
                () -> assertEquals(time1.id(), saved.time().id()),
                () -> assertEquals(theme4.id(), saved.theme().id())
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
