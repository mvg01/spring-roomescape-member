package roomescape.controller;


import static io.restassured.RestAssured.given;
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
import roomescape.dto.TimeRequest;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

public class ReservationTimeControllerTest {

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
    public void 전체_시간_조회_API() {
        given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(2));
    }

    @Test
    public void 테마_별_예약가능한_시간_조회_API() {
        LocalDate date = LocalDate.now().plusDays(1);

        given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", date.toString())
                .when().get("/times/" + theme1.id())
                .then().log().all()
                .statusCode(200)
                .body("times.size()", is(1));
    }

    @Test
    public void 예약_가능한_시간_삭제_API() {
        ReservationTime time3 = timeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/times/" + time3.id())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    public void 존재하지_않는_시간_삭제_API() {
        given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/times/" + Long.MAX_VALUE)
                .then().log().all()
                .statusCode(404)
                .body("code", is("TIME_NOT_FOUND"));
    }

    @Test
    public void 예약이_존재하는_시간은_삭제할_수_없다() {
        given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/times/" + time1.id())
                .then().log().all()
                .statusCode(409)
                .body("code", is("TIME_CANNOT_DELETE"));
    }

    @Test
    public void 예약_가능한_시간_추가_API() {
        TimeRequest timeRequest = new TimeRequest(LocalTime.of(12, 0));

        given().log().all()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", is("12:00:00"));
    }

    @Test
    public void 잘못된_형식의_시간_입력시_예외가_발생한다() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body("{\"startAt\": \"abc\"}")
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_REQUEST_BODY"));
    }

    @Test
    public void 시작시간_없이_예약시간_생성시_예외가_발생한다() {
        given().log().all()
                .contentType(ContentType.JSON)
                .when().post("/times")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_REQUEST_BODY"));
    }
}
