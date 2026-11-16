/* 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.retrievalbench;

import static cn.zhuatech.retrievalbench.Checks.*;

import java.util.*;

/**
 * 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 按标注相关文档比较基线和候选排行，计算 Recall@K、MRR、nDCG 及退步查询清单。
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Algorithm {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  static double[] score(List<String> order, Set<String> relevant, int k) {
    int hits = 0;
    double rr = 0, dcg = 0, ideal = 0;
    for (int i = 0; i < Math.min(k, relevant.size()); i++)
      ideal += 1 / (Math.log(i + 2) / Math.log(2));
    for (int i = 0; i < Math.min(k, order.size()); i++)
      if (relevant.contains(order.get(i))) {
        hits++;
        if (rr == 0) rr = 1.0 / (i + 1);
        dcg += 1 / (Math.log(i + 2) / Math.log(2));
      }
    return new double[] {(double) hits / relevant.size(), rr, dcg / ideal};
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  static Map<String, Object> compute(Map<String, Object> cfg, List<Map<String, Object>> rows) {
    unique(rows);
    int k = integer(cfg, "k", 1, 1000);
    var out = new ArrayList<>();
    int reg = 0, losses = 0;
    double[] sums = new double[6];
    for (var r : rows) {
      var truth = Set.copyOf(strings(r, "relevant"));
      must(!truth.isEmpty(), "相关性标注不能为空");
      var base = strings(r, "baseline");
      var candidate = strings(r, "candidate");
      var a = score(base, truth, k);
      var b = score(candidate, truth, k);
      var baseHits = new LinkedHashSet<>(base.subList(0, Math.min(k,base.size())));
      baseHits.retainAll(truth);
      var candidateHits = new LinkedHashSet<>(candidate.subList(0, Math.min(k,candidate.size())));
      candidateHits.retainAll(truth);
      var lost = baseHits.stream().filter(id -> !candidateHits.contains(id)).toList();
      var gained = candidateHits.stream().filter(id -> !baseHits.contains(id)).toList();
      if (!lost.isEmpty()) losses++;
      boolean down = b[2] + 1e-10 < a[2];
      if (down) reg++;
      for (int i = 0; i < 3; i++) {
        sums[i] += a[i];
        sums[i + 3] += b[i];
      }
      out.add(
          Map.of(
              "id",
              r.get("id"),
              "baseRecall",
              a[0],
              "candidateRecall",
              b[0],
              "baseRR",
              a[1],
              "candidateRR",
              b[1],
              "baseNDCG",
              a[2],
              "candidateNDCG",
              b[2],
              "regression",
              down, "lostRelevant", lost, "gainedRelevant", gained));
    }
    var metrics = new LinkedHashMap<String, Object>();
    int idx = 0;
    for (String key :
        List.of(
            "baseRecall",
            "baseMRR",
            "baseNDCG",
            "candidateRecall",
            "candidateMRR",
            "candidateNDCG")) metrics.put(key, sums[idx++] / rows.size());
    metrics.put("regressions", reg);
    metrics.put("queriesWithLostHits", losses);
    return result(metrics, out);
  }
}
