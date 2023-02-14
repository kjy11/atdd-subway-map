package subway;

import static org.assertj.core.api.Assertions.assertThat;
import static subway.LineAcceptanceTest.지하철_노선_생성;
import static subway.LineAcceptanceTest.지하철_노선_조회;
import static subway.StationAcceptanceTest.지하철역_생성;
import static subway.common.ResponseUtils.ID_추출;
import static subway.common.ResponseUtils.적절한_응답_코드를_받을_수_있다;
import static subway.fixtures.LineFixtures.신분당선_파라미터_생성;
import static subway.fixtures.LineSectionFixtures.구간_등록_파라미터_생성;
import static subway.fixtures.StationFixtures.강남역;
import static subway.fixtures.StationFixtures.방배역;
import static subway.fixtures.StationFixtures.역삼역;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import subway.common.DatabaseCleanup;

@DisplayName("지하철 구간 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LineSectionAcceptanceTest {

    @Autowired
    private DatabaseCleanup databaseCleanup;

    private String 강남역_ID;
    private String 방배역_ID;
    private String 역삼역_ID;

    private Long 신분당선_ID;

    /**
     * Given 하나의 구간을 가진 지하철 노선을 생성하고
     */
    @BeforeEach
    void setUp() {
        databaseCleanup.execute();
        강남역_ID = Long.toString(ID_추출(지하철역_생성(강남역)));
        방배역_ID = Long.toString(ID_추출(지하철역_생성(방배역)));
        역삼역_ID = Long.toString(ID_추출(지하철역_생성(역삼역)));
        신분당선_ID = ID_추출(지하철_노선_생성(신분당선_파라미터_생성(강남역_ID, 방배역_ID)));
    }

    /**
     * When 지하철 구간을 등록하면
     * Then 지하철 노선 조회 시 등록된 구간의 역을 순서대로 찾을 수 있다
     */
    @DisplayName("지하철 구간을 등록한다.")
    @Test
    void createSection() {
        // when
        Map<String, String> 방배역_역삼역_구간_파라미터 = 구간_등록_파라미터_생성(방배역_ID, 역삼역_ID);
        ExtractableResponse<Response> 지하철_구간_생성_응답 = 지하철_구간_등록(신분당선_ID, 방배역_역삼역_구간_파라미터);

        // then
        적절한_응답_코드를_받을_수_있다(지하철_구간_생성_응답, HttpStatus.CREATED);
        등록된_구간의_역을_순서대로_찾을_수_있다(지하철_노선_조회(신분당선_ID), Arrays.asList(강남역_ID, 방배역_ID, 역삼역_ID));
    }

    /**
     * When 해당 노선의 하행 종점역이 아닌 상행역 파라미터로 지하철 구간을 등록하면
     * Then 등록에 실패한다.
     */
    @DisplayName("지하철 구간 등록에 실패한다-잘못된 상행역")
    @Test
    void createSectionWithInvalidUpStationOfParameter() {
        // when
        Map<String, String> 강남역_역삼역_구간_파라미터 = 구간_등록_파라미터_생성(강남역_ID, 역삼역_ID);
        ExtractableResponse<Response> 지하철_구간_생성_응답 = 지하철_구간_등록(신분당선_ID, 강남역_역삼역_구간_파라미터);

        // then
        실패한다_BAD_REQUEST(지하철_구간_생성_응답);
    }

    /**
     * When 해당 노선에 등록된 역을 하행역 파라미터로 지하철 구간을 등록하면
     * Then 등록에 실패한다.
     */
    @DisplayName("지하철 구간 등록에 실패한다-잘못된 하행역")
    @Test
    void createSectionWithInvalidDownStationOfParameter() {
        // when
        Map<String, String> 방배역_강남역_구간_파라미터 = 구간_등록_파라미터_생성(방배역_ID, 강남역_ID);
        ExtractableResponse<Response> 지하철_구간_생성_응답 = 지하철_구간_등록(신분당선_ID, 방배역_강남역_구간_파라미터);

        // then
        실패한다_BAD_REQUEST(지하철_구간_생성_응답);
    }

    /**
     * Given 지하철 노선에 구간을 두 개 등록하고
     * When 노선에 등록된 하행 종점역을 제거하면
     * Then 노선에서 해당 역은 제거된다.
     */
    @DisplayName("지하철 구간을 삭제한다.")
    @Test
    void deleteSection() {
        // given
        Map<String, String> 방배역_역삼역_구간_파라미터 = 구간_등록_파라미터_생성(방배역_ID, 역삼역_ID);
        지하철_구간_등록(신분당선_ID, 방배역_역삼역_구간_파라미터);

        // when
        ExtractableResponse<Response> 지하철_구간_삭제_응답 = 지하철_구간_삭제(신분당선_ID, 역삼역_ID);

        // then
        적절한_응답_코드를_받을_수_있다(지하철_구간_삭제_응답, HttpStatus.NO_CONTENT);
        해당_역을_찾을_수_없다(지하철_노선_조회(신분당선_ID), 역삼역_ID);
    }

    /**
     * Given 지하철 노선에 구간을 두 개 등록하고
     * When 노선에 등록된 하행 종점역이 아닌 역을 제거하면
     * Then 실패한다.
     */
    @DisplayName("지하철 구간을 삭제에 실패한다-하행종점역이 아닌 경우")
    @Test
    void deleteSectionWithStationThatIsNotDownwardEndPoint() {
        // given
        Map<String, String> 방배역_역삼역_구간_파라미터 = 구간_등록_파라미터_생성(방배역_ID, 역삼역_ID);
        지하철_구간_등록(신분당선_ID, 방배역_역삼역_구간_파라미터);

        // when
        ExtractableResponse<Response> 지하철_구간_삭제_응답 = 지하철_구간_삭제(신분당선_ID, 방배역_ID);

        // then
        실패한다_BAD_REQUEST(지하철_구간_삭제_응답);
    }

    /**
     * When 하나뿐인 구간을 제거하면
     * Then 실패한다.
     */
    @DisplayName("지하철 구간을 삭제에 실패한다-구간이 하나인 경우")
    @Test
    void deleteSectionWhenLineHasOnlyOneSection() {
        // when
        ExtractableResponse<Response> 지하철_구간_삭제_응답 = 지하철_구간_삭제(신분당선_ID, 방배역_ID);

        // then
        실패한다_BAD_REQUEST(지하철_구간_삭제_응답);
    }

    private ExtractableResponse<Response> 지하철_구간_등록(Long lineId, Map<String, String> params) {
        return RestAssured.given().log().all()
            .body(params)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().post("/lines/{lineId}/sections", lineId)
            .then().log().all()
            .extract();
    }

    private ExtractableResponse<Response> 지하철_구간_삭제(Long lineId, String stationId) {
        return RestAssured.given().log().all()
            .queryParam("stationId", stationId)
            .when().delete("/lines/{lineId}/sections", lineId)
            .then().log().all()
            .extract();
    }

    private void 등록된_구간의_역을_순서대로_찾을_수_있다(ExtractableResponse<Response> response,
        List<String> stationIds) {

        for (int i = 0; i < stationIds.size(); i++) {
            assertThat(response.jsonPath().getString("stations.id[" + i + "]"))
                .isEqualTo(stationIds.get(i));
        }
    }

    private void 해당_역을_찾을_수_없다(ExtractableResponse<Response> response, String stationId) {
        assertThat(response.jsonPath().getList("stations.id")).doesNotContain(stationId);
    }

    private void 실패한다_BAD_REQUEST(ExtractableResponse<Response> response) {
        적절한_응답_코드를_받을_수_있다(response, HttpStatus.BAD_REQUEST);
    }

}