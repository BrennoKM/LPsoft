package io.lpsoft.features.relatorios;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerador de PDF mínimo (uma página, fonte Helvetica) sem dependências
 * externas. Suficiente para um relatório textual; o foco desta feature é o
 * mecanismo de dependência opcional, não a tipografia.
 */
final class MiniPdf {

    private MiniPdf() {}

    static byte[] gerar(List<String> linhas) {
        String texto = montarStream(linhas);
        byte[] streamBytes = texto.getBytes(StandardCharsets.ISO_8859_1);

        List<String> objs = new ArrayList<>();
        objs.add("<< /Type /Catalog /Pages 2 0 R >>");
        objs.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objs.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                + "/Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>");
        objs.add("<< /Length " + streamBytes.length + " >>\nstream\n" + texto + "\nendstream");
        objs.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder();
        sb.append("%PDF-1.4\n");

        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objs.size(); i++) {
            offsets.add(sb.length());
            sb.append(i + 1).append(" 0 obj\n").append(objs.get(i)).append("\nendobj\n");
        }

        int xrefStart = sb.length();
        sb.append("xref\n0 ").append(objs.size() + 1).append('\n');
        sb.append("0000000000 65535 f \n");
        for (int off : offsets) {
            sb.append(String.format("%010d 00000 n \n", off));
        }
        sb.append("trailer\n<< /Size ").append(objs.size() + 1).append(" /Root 1 0 R >>\n");
        sb.append("startxref\n").append(xrefStart).append("\n%%EOF");

        out.writeBytes(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        return out.toByteArray();
    }

    private static String montarStream(List<String> linhas) {
        StringBuilder s = new StringBuilder();
        s.append("BT\n/F1 12 Tf\n14 TL\n72 780 Td\n");
        for (String linha : linhas) {
            s.append('(').append(escapar(linha)).append(") Tj\nT*\n");
        }
        s.append("ET");
        return s.toString();
    }

    private static String escapar(String in) {
        return in.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
