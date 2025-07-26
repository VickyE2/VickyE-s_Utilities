/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Version implements Comparable<Version> {
  private final int major;
  private final List<Integer> subs;

  private final String rawSuffix; // e.g. "SNAPSHOT" or "TYPO-HOT-FIX"
  private final Stage stage;

  private Version(int major, List<Integer> subs, String rawSuffix) {
    this.major = major;
    this.subs = List.copyOf(subs);
    this.rawSuffix = rawSuffix;
    Stage st = Stage.fromString(rawSuffix);
    this.stage = (st != null) ? st : Stage.UNKNOWN;
  }

  /**
   * Parse from a string like "1.2.3-BETA" or "2.0".
   */
  public static Version parse(String versionString) {
    String[] splitSuffix = versionString.split("-", 2);
    String numbersPart = splitSuffix[0];
    String suffix = splitSuffix.length > 1 ? splitSuffix[1] : null;

    String[] nums = numbersPart.split("\\.");
    int major = Integer.parseInt(nums[0]);
    List<Integer> subs = new ArrayList<>();
    for (int i = 1; i < nums.length; i++) {
      subs.add(Integer.parseInt(nums[i]));
    }
    return new Version(major, subs, suffix);
  }

  /**
   * Factory method matching your original of(...) signature.
   */
  public static Version of(int major, String suffix, int... subVersions) {
    List<Integer> subs = new ArrayList<>();
    for (int v : subVersions) subs.add(v);
    return new Version(major, subs, suffix);
  }

  /**
   * Reconstruct the canonical string form.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(major);
    for (int v : subs) {
      sb.append('.').append(v);
    }
    if (rawSuffix != null && !rawSuffix.isEmpty()) {
      sb.append('-').append(rawSuffix);
    }
    return sb.toString();
  }

  /**
   * Compare semantically: major, then each subversion, then suffix (null > any suffix).
   */
  @Override
  public int compareTo(Version o) {
    // 1) major
    if (major != o.major) return Integer.compare(major, o.major);
    // 2) subs lexicographically
    int len = Math.max(this.subs.size(), o.subs.size());
    for (int i = 0; i < len; i++) {
      int a = (i < this.subs.size() ? this.subs.get(i) : 0);
      int b = (i < o.subs.size() ? o.subs.get(i) : 0);
      if (a != b) return Integer.compare(a, b);
    }
    // 3) suffix: “no suffix” (final release) is considered greater than any suffix
    if (this.stage != o.stage) {
      return this.stage.rank - o.stage.rank;
    }
    // if both stages are the same but rawSuffix strings differ…
    if (!Objects.equals(rawSuffix, o.rawSuffix)) {
      // e.g. "TYPO-HOT-FIX" vs "HOTFIX" both map to HOTFIX stage
      return (rawSuffix == null ? "" : rawSuffix).compareTo(o.rawSuffix == null ? "" : o.rawSuffix);
    }
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Version v)) return false;
    return major == v.major
        && Objects.equals(subs, v.subs)
        && Objects.equals(rawSuffix, v.rawSuffix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, subs, rawSuffix);
  }

  // Optional getters
  public int getMajor() {
    return major;
  }

  public List<Integer> getSubVersions() {
    return List.copyOf(subs);
  }

  public String getRawSuffix() {
    return rawSuffix;
  }

  public Stage getSuffix() {
    return stage;
  }

  /**
   * An ordered list of well‐known suffixes.
   */
  public enum Stage {
    // earliest/development
    UNKNOWN(-1),
    SNAPSHOT(3),
    ALPHA(0),
    BETA(1),
    RC(2), // release candidate
    HOTFIX(4),
    // final release
    FINAL(5);

    public final int rank;

    Stage(int rank) {
      this.rank = rank;
    }

    /**
     * Try to parse a raw suffix into one of these.
     */
    public static Stage fromString(String s) {
      if (s == null) return FINAL;
      return switch (s.toUpperCase()) {
        case "SNAPSHOT" -> SNAPSHOT;
        case "ALPHA" -> ALPHA;
        case "BETA" -> BETA;
        case "RC" -> RC;
        case "HOTFIX" -> HOTFIX;
        default -> null; // unknown
      };
    }
  } // one of SNAPSHOT…FINAL or null if “unknown”
}
