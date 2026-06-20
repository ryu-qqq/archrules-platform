package com.ryuqqq.archrules.common;

/**
 * 패키지 문자열 프리미티브 — 도메인/컨텍스트 개념에 비종속(neutral).
 * 규칙이 상대 매처(..domain.. 등)로 서드파티 라이브러리를 오인하는 false-positive를
 * 막기 위해, 의존 대상이 origin과 같은 앱 베이스 패키지를 공유하는지 판정한다.
 *
 * <p>가정: 앱 베이스 = 패키지 상위 2세그먼트(예: com.connectly). 표준 group-id 관례에 정합.
 */
public final class AppPackages {

    private AppPackages() {}

    /** 앱 베이스(상위 2세그먼트). 세그먼트가 2개 미만이면 입력 그대로. */
    public static String baseOf(String packageName) {
        int first = packageName.indexOf('.');
        if (first < 0) {
            return packageName;
        }
        int second = packageName.indexOf('.', first + 1);
        return second < 0 ? packageName : packageName.substring(0, second);
    }

    /** target이 origin과 같은 앱 베이스 프리픽스를 공유하는가. 서드파티 제외용. */
    public static boolean sameApp(String originPackage, String targetPackage) {
        return targetPackage.startsWith(baseOf(originPackage) + ".");
    }
}
