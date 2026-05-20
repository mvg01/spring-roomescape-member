package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dto.ReservationPatchRequest;
import roomescape.dto.ReservationRequest;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeRepository;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    private final Clock clock = Clock.fixed(
            LocalDate.now().atTime(14, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC);
    @Mock
    TimeRepository timeRepository;
    @Mock
    ThemeRepository themeRepository;
    @Mock
    ReservationRepository reservationRepository;

    ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository, timeRepository, themeRepository, clock
        );
    }

    @Test
    void 존재하지_않는_예약을_삭제할_경우_예외가_발생한다() {
        assertThatThrownBy(() -> reservationService.removeById(-1L))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 시간_아이디가_빈_예약을_추가할_경우_예외가_발생한다() {
        LocalDate date = LocalDate.now(clock).plusDays(1);
        ReservationRequest request = new ReservationRequest("포비", date, null, 6L);

        when(timeRepository.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.register(request))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 테마_아이디가_빈_예약을_추가할_경우_예외가_발생한다() {
        LocalDate date = LocalDate.now(clock).plusDays(1);
        ReservationRequest request = new ReservationRequest("포비", date, 4L, null);
        ReservationTime time = new ReservationTime(null, LocalTime.of(12, 0));

        when(timeRepository.findById(4L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.register(request))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 존재하는_예약을_추가할_경우_예외가_발생한다() {
        LocalDate date = LocalDate.now(clock).plusDays(1);
        ReservationRequest request = new ReservationRequest("구바", date, 1L, 1L);

        when(timeRepository.findById(1L)).thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(15, 0))));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(new Theme(1L, "테마", "설명", "url")));
        when(reservationRepository.existsByDateAndTimeIdAndThemeId(date, 1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.register(request))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 이미_지난_날짜의_예약은_불가능하다() {
        ReservationRequest reservationRequest = new ReservationRequest("무빙", LocalDate.now(clock).minusDays(1), 2L, 2L);

        when(timeRepository.findById(2L)).thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(15, 0))));

        assertThatThrownBy(() -> reservationService.register(reservationRequest))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 오늘_예약이고_이미_지난_시간의_예약은_불가능하다() {
        ReservationRequest reservationRequest = new ReservationRequest("무빙", LocalDate.now(clock), 1L, 1L);

        when(timeRepository.findById(1L)).thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(9, 0))));

        assertThatThrownBy(() -> reservationService.register(reservationRequest))
                .isInstanceOf(RoomescapeException.class);
    }

    @Test
    void 사용자는_지난_예약을_수정할_수_없다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(18, 0));
        Reservation pastReservation = new Reservation(1L, "유저", LocalDate.now(clock).minusDays(1), time,
                new Theme(1L, "테마", "설명", "url"));

        when(reservationRepository.existsById(1L)).thenReturn(true);
        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(reservationRepository.findById(1L)).thenReturn(pastReservation);

        assertThatThrownBy(() -> reservationService.patchById(1L,
                new ReservationPatchRequest(LocalDate.now(clock).plusDays(1), 1L)))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.RESERVATION_WRONG_DATE);
    }

    @Test
    void 사용자는_없는_예약을_수정할_수_없다() {
        when(reservationRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> reservationService.patchById(1L,
                new ReservationPatchRequest(LocalDate.now(clock).plusDays(1), 1L)))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.RESERVATION_NOT_FOUND);
    }
}
