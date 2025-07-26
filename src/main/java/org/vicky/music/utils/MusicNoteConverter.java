/* Licensed under Apache-2.0 2024. */
package org.vicky.music.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vicky.utilities.Pair;

public class MusicNoteConverter {

  private static final Map<Character, String> NOTE_MAP = new HashMap<>();

  static {
    NOTE_MAP.put(' ', ".");
    NOTE_MAP.put('1', "C--");
    NOTE_MAP.put('2', "D--");
    NOTE_MAP.put('3', "E--");
    NOTE_MAP.put('4', "F--");
    NOTE_MAP.put('5', "G--");
    NOTE_MAP.put('6', "A--");
    NOTE_MAP.put('7', "B--");
    NOTE_MAP.put('8', "C-");
    NOTE_MAP.put('9', "D-");
    NOTE_MAP.put('0', "E-");
    NOTE_MAP.put('q', "F-");
    NOTE_MAP.put('w', "G-");
    NOTE_MAP.put('e', "A-");
    NOTE_MAP.put('r', "B-");
    NOTE_MAP.put('t', "C");
    NOTE_MAP.put('y', "D");
    NOTE_MAP.put('u', "E");
    NOTE_MAP.put('i', "F");
    NOTE_MAP.put('o', "G");
    NOTE_MAP.put('p', "A");
    NOTE_MAP.put('a', "B");
    NOTE_MAP.put('s', "C+");
    NOTE_MAP.put('d', "D+");
    NOTE_MAP.put('f', "E+");
    NOTE_MAP.put('g', "F+");
    NOTE_MAP.put('h', "G+");
    NOTE_MAP.put('j', "A+");
    NOTE_MAP.put('k', "B+");
    NOTE_MAP.put('l', "C++");
    NOTE_MAP.put('z', "D++");
    NOTE_MAP.put('x', "E++");
    NOTE_MAP.put('c', "F++");
    NOTE_MAP.put('v', "G++");
    NOTE_MAP.put('b', "A++");
    NOTE_MAP.put('n', "B++");
    NOTE_MAP.put('m', "C++");

    NOTE_MAP.put('!', "C--#");
    NOTE_MAP.put('@', "D--#");
    NOTE_MAP.put('$', "F--#");
    NOTE_MAP.put('%', "G--#");
    NOTE_MAP.put('^', "A--#");
    NOTE_MAP.put('*', "C-#");
    NOTE_MAP.put('(', "D-#");
    NOTE_MAP.put('Q', "F-#");
    NOTE_MAP.put('W', "G-#");
    NOTE_MAP.put('E', "A-#");
    NOTE_MAP.put('T', "C#");
    NOTE_MAP.put('Y', "D#");
    NOTE_MAP.put('I', "F#");
    NOTE_MAP.put('O', "G#");
    NOTE_MAP.put('P', "A#");
    NOTE_MAP.put('S', "C+#");
    NOTE_MAP.put('D', "D+#");
    NOTE_MAP.put('G', "F+#");
    NOTE_MAP.put('H', "G+#");
    NOTE_MAP.put('J', "A+#");
    NOTE_MAP.put('L', "C++#");
    NOTE_MAP.put('Z', "D++#");
    NOTE_MAP.put('C', "F++#");
    NOTE_MAP.put('V', "G++#");
    NOTE_MAP.put('B', "A++#");
  }

  public static Pair<String, Integer> convert(String input) {
    StringBuilder output = new StringBuilder();
    StringBuilder bracketBuffer = null;
    int size = 0;

    Pattern pattern = Pattern.compile("(?<!\\[)[a-zA-Z](?![^\\[]*?])|\\[[^\\[\\]]+]");
    Matcher matcher = pattern.matcher(input);
    while (matcher.find()) { // or collect however you like
      size++;
    }

    for (int i = 0; i < input.length(); i++) {
      char ch = input.charAt(i);

      if (ch == '[') {
        bracketBuffer = new StringBuilder();
      } else if (ch == ']') {
        if (bracketBuffer != null && !bracketBuffer.isEmpty()) {
          StringBuilder mapped = new StringBuilder();
          for (char bch : bracketBuffer.toString().toCharArray()) {
            String note = NOTE_MAP.getOrDefault(bch, "?");
            mapped.append(note).append("—");
          }
          if (!mapped.isEmpty()) {
            mapped.setLength(mapped.length() - 1); // remove trailing dash
          }
          output.append(mapped).append(",");
          bracketBuffer = null;
        }
      } else if (bracketBuffer != null) {
        bracketBuffer.append(ch);
      } else {
        String note = NOTE_MAP.getOrDefault(ch, "?");
        output.append(note).append(",");
      }
    }

    return new Pair<>(output.toString().trim(), size);
  }

  public static void main(String[] args) {
    String rawInput =
        """
sdfsdfsdf[ms][md][mf]adfadfadf[na][nd][nf]pdfpdfpdf[bp][bd][bf]odfodfodf[vl][vh][vf][vs][do][ft]s[do][ft]s[do][ft]s[do][ft]a[od][rf]a[od][rf]a[od][rf]a[od][rf]p[ud][ef]p[ud][ef]p[ud][ef]p[ud][ef]o[wd][of]o[wd][of]o[wd][of]lhf[s8][wv][tl][8m][wv][tl][8m][wv][tl][8m][wv][tl][9n][wv][rz][9n][wv][rz][9n][wv][rz][9n][wv][rz][5b][9z][wh][5b][9z][wh][5b][9z][wh][5b][9z][wh][wv][rh][yk][wv][rh][yk][wv][rh][yk]oads[do][ft][do][ft][do][ft][so]sasd[rf][od][rf][od][rf][od][rf][rs]apas[ud][ef][ud][ef][ud][ef][ud][eo]popa[wd][of][wd][of][wd][of][wd][of]oiuo[4j][wh][4j][wh][4j][wh][4j][wg]hgfh[5g]h[5g]h[5g]h[5d]lkjk[l4][kq][4l][qk][4l][qk][4l][qj]zlkl[5z][wh][5z][wh][5z][wh][5z][wh][lxv]"""; // truncated example
    System.out.println(convert(rawInput));
  }
}
