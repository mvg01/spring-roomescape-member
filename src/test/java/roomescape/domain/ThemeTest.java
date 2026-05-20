package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeException;
import roomescape.model.Theme;

public class ThemeTest {

    @Test
    public void 테마_이름은_1자_이상_20자_이하를_가진다() {
        Theme theme = new Theme(1L, "테스트 테마", "테스트 설명.", "fakeurl");

        assertEquals(6, theme.name().length());
    }

    @Test
    public void 테마_이름이_20자_초과_될_경우_예외가_발생한다() {
        String name = "a".repeat(21);
        assertThrows(RoomescapeException.class, () -> new Theme(1L, name, "테스트 설명.", "fakeurl"));
    }
}
