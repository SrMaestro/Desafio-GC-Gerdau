package com.Gerdau.Desafio_GC_Gerdau_App.service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Guarda contra o falso bloqueio por detalhe curto: "Manutenção de bomba 10 cv" e "Manutenção de bomba 20 cv" têm
 * similaridade alta, mas são serviços diferentes. Dois textos só são compatíveis (e portanto podem ser bloqueados como
 * duplicados) se tiverem os mesmos números e se a última palavra de cada um (no singular) aparecer no outro. Assim "Solda TIG aço
 * inox" ainda casa com "Solda TIG em aço inox" e com "Aço inox solda TIG" (a ordem não importa), mas "pintura interna"
 * não casa com "pintura externa".
 */
final class DetalheDoTexto {

    private static final Pattern NUMERO = Pattern.compile("\\d+");
    private static final Pattern ACENTOS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NAO_ALFANUMERICO = Pattern.compile("[^a-z0-9]+");

    private DetalheDoTexto() {
    }

    static boolean compativeis(String a, String b) {
        if (a == null || b == null) {
            return true;
        }
        String na = normalizar(a);
        String nb = normalizar(b);
        if (!numeros(na).equals(numeros(nb))) {
            return false;
        }
        List<String> pa = palavras(na);
        List<String> pb = palavras(nb);
        if (pa.isEmpty() || pb.isEmpty()) {
            return true;
        }
        return pb.contains(pa.get(pa.size() - 1)) && pa.contains(pb.get(pb.size() - 1));
    }

    private static String normalizar(String texto) {
        String semAcento = ACENTOS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }

    private static TreeSet<String> numeros(String texto) {
        TreeSet<String> numeros = new TreeSet<>();
        NUMERO.matcher(texto).results().forEach(m -> numeros.add(m.group()));
        return numeros;
    }

    private static List<String> palavras(String texto) {
        return Arrays.stream(NAO_ALFANUMERICO.split(texto)).filter(p -> !p.isEmpty())
                .map(DetalheDoTexto::singular).toList();
    }

    /** Plural simples (já sem acento): "tubulacoes" e "tubulacao", "motores" e "motor", "chapas" e "chapa". */
    private static String singular(String palavra) {
        if (palavra.endsWith("oes") || palavra.endsWith("aes")) {
            return palavra.substring(0, palavra.length() - 3) + "ao";
        }
        if (palavra.endsWith("res") || palavra.endsWith("zes")) {
            return palavra.substring(0, palavra.length() - 2);
        }
        if (palavra.length() > 3 && palavra.endsWith("s")) {
            return palavra.substring(0, palavra.length() - 1);
        }
        return palavra;
    }
}
