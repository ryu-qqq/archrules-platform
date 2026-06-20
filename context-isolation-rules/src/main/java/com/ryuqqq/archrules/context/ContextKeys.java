package com.ryuqqq.archrules.context;

import com.tngtech.archunit.core.domain.JavaClass;
import java.util.List;

/**
 * 패키지명에서 컨텍스트 경계를 식별 — 레이어 마커 세그먼트 직전까지가 "컨텍스트 키".
 *
 * <h3>가정(Assumptions) — 이 클래스의 올바른 동작은 다음 약속에 의존한다</h3>
 * <ul>
 *   <li><b>예약어:</b> 레이어 마커 단어(domain/application/api/adapter/internal)는 <b>예약어</b>다.
 *       컨텍스트/서비스 패키지 세그먼트를 이 단어로 이름 짓지 않는다.
 *       (예: {@code ...fileflow.api...}의 {@code api}가 컨텍스트명이면 오분류된다.)</li>
 *   <li><b>레이어 소속 보장:</b> 한 컨텍스트 키 아래의 모든 클래스는 다섯 레이어 중 하나로
 *       해석돼야 한다. 마커 세그먼트가 전혀 없는 패키지(예: 다리 어댑터를 {@code .adapter} 밖에 둠)는
 *       {@code contextKeyOf}가 null이라 격리 검사에서 보이지 않는다 — 다리 어댑터는 반드시
 *       {@code .adapter} 아래 둔다.</li>
 * </ul>
 */
final class ContextKeys {

    private ContextKeys() {}

    static final List<String> LAYER_MARKERS =
            List.of(".domain", ".application", ".api", ".adapter", ".internal");

    /** 레이어 마커 직전까지의 패키지. 컨텍스트 클래스가 아니면 null. */
    static String contextKeyOf(String packageName) {
        int best = -1;
        for (String marker : LAYER_MARKERS) {
            int idx = segmentIndex(packageName, marker);
            if (idx >= 0 && (best < 0 || idx < best)) {
                best = idx;
            }
        }
        return best < 0 ? null : packageName.substring(0, best);
    }

    /** 레이어 종류("domain"/"application"/...) 또는 null. */
    static String layerOf(String packageName) {
        String key = contextKeyOf(packageName);
        if (key == null) {
            return null;
        }
        String rest = packageName.substring(key.length()); // 예: ".application.svc"
        for (String marker : LAYER_MARKERS) {
            if (rest.equals(marker) || rest.startsWith(marker + ".")) {
                return marker.substring(1);
            }
        }
        return null;
    }

    static String contextKeyOf(JavaClass clazz) {
        return contextKeyOf(clazz.getPackageName());
    }

    static String layerOf(JavaClass clazz) {
        return layerOf(clazz.getPackageName());
    }

    /** marker가 세그먼트 경계(뒤가 끝이거나 '.')로 등장하는 첫 위치. 없으면 -1. */
    private static int segmentIndex(String pkg, String marker) {
        int from = 0;
        while (true) {
            int idx = pkg.indexOf(marker, from);
            if (idx < 0) {
                return -1;
            }
            int end = idx + marker.length();
            boolean segmentBoundary = end == pkg.length() || pkg.charAt(end) == '.';
            if (segmentBoundary) {
                return idx;
            }
            from = end;
        }
    }
}
