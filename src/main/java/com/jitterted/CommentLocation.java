package com.jitterted;

import com.intellij.openapi.vfs.VirtualFile;

public class CommentLocation {
  public final int lineNumber;
  public final VirtualFile virtualFile;

  CommentLocation(int lineNumber, VirtualFile virtualFile) {
    this.lineNumber = lineNumber;
    this.virtualFile = virtualFile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CommentLocation that = (CommentLocation) o;

    if (lineNumber != that.lineNumber) return false;
    return virtualFile.equals(that.virtualFile);
  }

  @Override
  public int hashCode() {
    int result = lineNumber;
    result = 31 * result + virtualFile.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return virtualFile.getNameWithoutExtension() + ": " + (lineNumber + 1); // display as 1-based line number
  }
}
