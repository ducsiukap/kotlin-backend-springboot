package vduczz.userservice.domain.utils;

import java.text.Normalizer;

public class Utilities {
    public static String normalizeString(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD) // Đức -> Đŭć (Đ + u + ̆  + ́ + c)
                .replaceAll("\\p{M}", "") // Đŭć -> Đuc
                .toLowerCase()
                .replaceAll("đ", "d");
    }
}
