package life.qbic.model;

public enum PackageGroup {

  Sequencing, Mass_Spectrometry, Bioinformatics_Analysis, Project_Management, Other;

  public static boolean contains(String test) {
    for (PackageGroup c : PackageGroup.values()) {
      if (c.name().equals(test)) {
        return true;
      }
    }
    return false;
  }
}
