package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dto.TimeAllResponse;
import roomescape.dto.TimeRequest;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
public class TimeServiceTest {

    @Mock
    TimeRepository timeRepository;
    @Mock
    ReservationRepository reservationRepository;

    TimeService timeService;

    @BeforeEach
    void setUp() {
        timeService = new TimeService(timeRepository, reservationRepository);
    }

    @Test
    public void 존재하지_않는_시간를_삭제하는경우_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> timeService.removeById(-1L))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.TIME_NOT_FOUND);
    }

    @Test
    void 존재하는_시간인데_그_시간에_예약이_존재하는_경우_삭제되지_않는다() {
        when(reservationRepository.existsByTimeId(2L)).thenReturn(true);

        assertThatThrownBy(() -> timeService.removeById(2L))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.TIME_CANNOT_DELETE);
    }

    @Test
    public void 이미_있는_시간에_대한_추가하는_예외가_발생한다() {
        TimeRequest timeRequest = new TimeRequest(LocalTime.of(12, 0));
        when(timeRepository.existsByStartAt(LocalTime.of(12, 0))).thenReturn(true);

        Assertions.assertThatThrownBy(() -> timeService.register(timeRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.TIME_DUPLICATE);
    }

    @Test
    public void 정각이_아닌_시간을_등록하는_경우_예외가_발생한다() {
        TimeRequest timeRequest = new TimeRequest(LocalTime.of(10, 30));

        when(timeRepository.existsByStartAt(LocalTime.of(10, 30))).thenReturn(false);

        Assertions.assertThatThrownBy(() -> timeService.register(timeRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.TIME_WRONG_STARTAT);
    }

    @Test
    void 정각이_아닌_시간은_전체_조회에서_제외된다() {
        when(timeRepository.findAll()).thenReturn(List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(10, 30))
        ));

        TimeAllResponse response = timeService.readAll();

        assertThat(response.times()).hasSize(1);
    }

    @Test
    void 정각이_아닌_시간은_테마_날짜_조회에서_제외된다() {
        when(timeRepository.findAllByThemeIdAndDate(1L, "2026-05-20")).thenReturn(List.of(
                new ReservationTime(1L, LocalTime.of(15, 0)),
                new ReservationTime(2L, LocalTime.of(15, 30))
        ));

        TimeAllResponse response = timeService.readAllByThemeIdAndDate(1L, "2026-05-20");

        assertThat(response.times()).hasSize(1);
    }
}
