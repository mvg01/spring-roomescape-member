package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.ReservationRequest;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationControllerTest {

    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    TimeRepository timeRepository;
    @Autowired
    ThemeRepository themeRepository;
    @LocalServerPort
    private int port;

    private ReservationTime time1;
    private ReservationTime time2;
    private Theme theme1;
    private Theme theme2;
    private Reservation reservation1;
    private Reservation reservation2;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        time1 = timeRepository.save(new ReservationTime(null, LocalTime.of(8, 0)));
        time2 = timeRepository.save(new ReservationTime(null, LocalTime.of(9, 0)));
        theme1 = themeRepository.save(new Theme(null, "테스트_테마_1", "테스트1입니다", "FakeURL"));
        theme2 = themeRepository.save(new Theme(null, "테스트_테마_2", "테스트2입니다", "FakeURL"));
        reservation1 = reservationRepository.save(
                new Reservation(null, "포비", LocalDate.now().plusDays(1), time1, theme1));
        reservation2 = reservationRepository.save(
                new Reservation(null, "포비", LocalDate.now().plusDays(1), time1, theme2));
    }

    @Test
    public void 전체_예약_조회_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2));
    }

    @Test
    public void 예약_삭제_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/reservations/" + reservation1.id())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    public void 존재하지_않는_예약_삭제_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/reservations/" + Long.MAX_VALUE)
                .then().log().all()
                .statusCode(404)
                .body("code", is("RESERVATION_NOT_FOUND"));
    }

    @Test
    public void 예약_생성_API() {
        ReservationRequest reservationRequest = new ReservationRequest("새로운사용자", LocalDate.now().plusDays(3),
                time1.id(),
                theme1.id());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("새로운사용자"))
                .body("date", is(LocalDate.now().plusDays(3).toString()));
    }

    @Test
    public void 이미_존재하는_예약_생성_API() {
        ReservationRequest reservationRequest = new ReservationRequest(reservation1.name(), LocalDate.now().plusDays(1),
                reservation1.time().id(), reservation1.theme().id());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("code", is("RESERVATION_DUPLICATE"));
    }

    @Test
    public void 잘못된_형식의_날짜_입력시_예외가_발생한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"포비\", \"date\": \"abc\", \"timeId\": 2, \"themeId\": 2}")
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_REQUEST_BODY"));
    }

    @Test
    public void 사용자_이름으로_예약을_조회할_수_있다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("name", reservation1.name())
                .when().get("/reservations/user")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2));
    }

    @Test
    public void 사용자는_예약에서_시간과_날짜를_변경할_수_있다() {
        String requestBody = String.format("{\"date\": \"%s\", \"timeId\": %d}", LocalDate.now().plusDays(2),
                time2.id());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().patch("/reservations/user/" + reservation1.id())
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void 사용자는_예약에서_시간과_날짜를_변경할_수_있지만_예전_날짜로_변경할_수_없다() {
        String requestBody = String.format("{\"date\": \"%s\", \"timeId\": %d}", LocalDate.now().minusYears(1),
                time1.id());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().patch("/reservations/user/" + reservation1.id())
                .then().log().all()
                .statusCode(400)
                .body("code", is("RESERVATION_WRONG_DATE"));
    }

    @Test
    public void 사용자는_예약에서_시간과_날짜를_변경할_수_있지만_이미_지난_예약은_변경할_수_없다() {
        Reservation reservation3 = reservationRepository.save(
                new Reservation(null, "포비", LocalDate.now().minusDays(1), time1, theme1));
        String requestBody = String.format("{\"date\": \"%s\", \"timeId\": %d}", LocalDate.now().plusDays(2),
                time2.id());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().patch("/reservations/user/" + reservation3.id())
                .then().log().all()
                .statusCode(400)
                .body("code", is("RESERVATION_WRONG_DATE"));
    }

    @Test
    public void 이름을_제와한_예약_생성시_예외가_발생한다() {
        String requestBody = String.format("{\"date\": \"%s\", \"timeId\": %d, \"themeId\": %d}",
                LocalDate.now().plusDays(1), time1.id(), theme1.id());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_REQUEST_BODY"));
    }
}
