package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.ThemeRequest;
import roomescape.model.Theme;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

public class ThemeControllerTest {

    @Autowired
    ThemeRepository themeRepository;
    @LocalServerPort
    private int port;

    private Theme theme1;
    private Theme theme2;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        theme1 = themeRepository.save(new Theme(null, "테스트_테마_1", "테스트1입니다", "FakeURL"));
        theme2 = themeRepository.save(new Theme(null, "테스트_테마_2", "테스트2입니다", "FakeURL"));
    }

    @Test
    public void 테마_전체_조회_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("themes.size()", is(2));
    }

    @Test
    public void 테마_추가_API() {
        ThemeRequest themeRequest = new ThemeRequest("테스트 테마", "테스트입니다", "url");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .body("name", is("테스트 테마"))
                .body("description", is("테스트입니다"));
    }

    @Test
    public void 특정_테마_삭제_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/" + theme1.id())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    public void 존재하지_않는_테마_삭제_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/" + Long.MAX_VALUE)
                .then().log().all()
                .statusCode(404)
                .body("code", is("THEME_NOT_FOUND"));
    }

    @Test
    public void 인기_테마_조회_API의_limit값이_30초과면_예외() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("limit", 31)
                .when().get("/themes/ranks")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"));
    }

    @Test
    public void 인기_테마_조회_API의_limit값이_1미만이면_예외() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("limit", 0)
                .when().get("/themes/ranks")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_INPUT"));
    }

    @Test
    public void 인기_테마_조회_API의_limit값이_없으면_예외() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes/ranks")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_QUERY_STRING"));
    }

    @Test
    public void 이름_없이_테마_생성시_예외가_발생한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{\"description\": \"테스트설명\", \"url\": \"fakeURL\"}")
                .when().post("/themes")
                .then().log().all()
                .statusCode(400)
                .body("code", is("INVALID_REQUEST_BODY"));
    }
}
