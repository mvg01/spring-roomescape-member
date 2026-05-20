package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeException;
import roomescape.model.ReservationTime;

public class ReservationTimeTest {

    @Test
    public void 시작_시간은_정각이다() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
        assertEquals(0, reservationTime.startAt().getMinute());
    }

    @Test
    public void 시작_시간이_정각이_아니면_예외() {
        assertThatThrownBy(() -> ReservationTime.withValidate(1L, LocalTime.of(12, 30)))
                .isInstanceOf(RoomescapeException.class);
    }
}
