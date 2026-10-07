package com.edstem.interviewprep.file.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UploadedFileNameTest {

  @Test
  void anOrdinaryNameIsKept() {
    assertThat(UploadedFileName.sanitise("holiday.png")).isEqualTo("holiday.png");
  }

  @Test
  void aPosixTraversalPrefixIsStripped() {
    assertThat(UploadedFileName.sanitise("../../etc/passwd")).isEqualTo("passwd");
  }

  @Test
  void aWindowsTraversalPrefixIsStripped() {
    assertThat(UploadedFileName.sanitise("..\\..\\windows\\system32\\config")).isEqualTo("config");
  }

  @Test
  void anAbsolutePathIsReducedToItsLastSegment() {
    assertThat(UploadedFileName.sanitise("/var/data/report.pdf")).isEqualTo("report.pdf");
  }

  @Test
  void aNameThatIsOnlyTraversalFallsBackToAPlaceholder() {
    assertThat(UploadedFileName.sanitise("..")).isEqualTo("upload");
    assertThat(UploadedFileName.sanitise("../")).isEqualTo("upload");
  }

  @Test
  void aMissingOrBlankNameFallsBackToAPlaceholder() {
    assertThat(UploadedFileName.sanitise(null)).isEqualTo("upload");
    assertThat(UploadedFileName.sanitise("   ")).isEqualTo("upload");
  }

  @Test
  void controlCharactersThatCouldForgeAHeaderAreRemoved() {
    assertThat(UploadedFileName.sanitise("report\r\nX-Evil: 1.pdf"))
        .isEqualTo("reportX-Evil: 1.pdf");
  }
}
