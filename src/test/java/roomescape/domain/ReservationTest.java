package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

public class ReservationTest {

    Theme theme;
    ReservationTime reservationTime;

    @BeforeEach
    void setUp() {
        theme = new Theme(1L, "테스트 테마", "테스트 설명.", "fakeurl");
        reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
    }

    @Test
    public void 사용자_이름은_2자_이상_20자_이하를_가진다() {
        Reservation reservation = new Reservation(1L, "홍길동", LocalDate.now(), reservationTime, theme);

        assertEquals(3, reservation.name().length());
    }

    @Test
    public void 사용자_이름이_20자_초과_될_경우_예외가_발생한다() {
        assertThrows(RoomescapeException.class, () ->
                new Reservation(1L, "a".repeat(21), LocalDate.now(), reservationTime, theme));
    }

    @Test
    public void 사용자_이름이_2자_미만_일_경우_예외가_발생한다() {
        assertThrows(RoomescapeException.class,
                () -> new Reservation(1L, "a", LocalDate.now(), reservationTime, theme));
    }
}
