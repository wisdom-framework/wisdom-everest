import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.wisdom.test.http.HttpRequest;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;


public class EverestControllerIT extends WisdomBlackBoxTest {

    @Test
    public void testEverestRoot() throws Exception {
        HttpResponse<JsonNode> response = get("/everest").asJson();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().get("path").asText()).isEqualTo("/");
        assertThat(response.body().get("canonicalPath").asText()).isEqualTo("/");
        assertThat(response.body().get("metadata").size()).isEqualTo(0);
    }

}
