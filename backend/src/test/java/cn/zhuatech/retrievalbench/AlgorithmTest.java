/* 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.retrievalbench;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@SuppressWarnings("unchecked")
class AlgorithmTest {
  Map<String, Object> cfg;
  List<Map<String, Object>> data;

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @BeforeEach
  void load() throws Exception {
    var fixture =
        new ObjectMapper()
            .readValue(getClass().getResourceAsStream("/static/catalog.json"), Map.class);
    cfg = (Map<String, Object>) fixture.get("settings");
    data = (List<Map<String, Object>>) fixture.get("rows");
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void boundaryCases() {
    var x =
        Algorithm.compute(
            cfg,
            List.of(
                Map.of(
                    "id",
                    "empty",
                    "relevant",
                    List.of("doc"),
                    "baseline",
                    List.of(),
                    "candidate",
                    List.of())));
    assertEquals(0.0, ((Map<?, ?>) x.get("metrics")).get("candidateNDCG"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void enterpriseEvidenceAndBoundaries() {
    var result=Algorithm.compute(Map.of("k",2),List.of(Map.of("id","Q","relevant",List.of("A","B","C"),
        "baseline",List.of("A","X","C"),"candidate",List.of("Y","B","A"))));
    var row=((List<Map<String,Object>>)result.get("rows")).getFirst();
    assertEquals(List.of("A"),row.get("lostRelevant"));assertEquals(List.of("B"),row.get("gainedRelevant"));
    assertEquals(1,((Map<?,?>)result.get("metrics")).get("queriesWithLostHits"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void knownResults() {
    var result = Algorithm.compute(cfg, data);
    var m = (Map<String, Object>) result.get("metrics");
    var out = (List<Map<String, Object>>) result.get("rows");
    var first = out.getFirst();
    assertEquals(1, ((Number) m.get("regressions")).intValue());
    assertEquals(1.0, (Double) first.get("candidateNDCG"), 1e-9);
    assertEquals(0.5, (Double) first.get("baseRecall"), 1e-9);
    assertThrows(
        Model.Failure.class,
        () ->
            Algorithm.compute(
                cfg,
                List.of(
                    Map.of(
                        "id",
                        "a",
                        "relevant",
                        List.of("x"),
                        "baseline",
                        List.of(),
                        "candidate",
                        List.of("x", "x")))));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsInvalidInput() {
    cfg.put("k", 0);
    assertThrows(Model.Failure.class, () -> Algorithm.compute(cfg, data));
  }
}
