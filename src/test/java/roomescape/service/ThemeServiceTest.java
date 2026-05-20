package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
public class ThemeServiceTest {

    @Mock
    ThemeRepository themeRepository;
    @Mock
    ReservationRepository reservationRepository;

    ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @Test
    void 존재하지_않는_테마를_삭제하는경우_예외가_발생한다() {
        assertThatThrownBy(() -> themeService.removeById(-1L))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.THEME_NOT_FOUND);
    }
}
