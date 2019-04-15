package com.jitterted;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

public class ChatCommentLookup {

  private final Map<CommentLocation, String> lineComments = new HashMap<>();

  public boolean hasComment(int lineNumber, VirtualFile virtualFile) {
    return lineComments.containsKey(new CommentLocation(lineNumber, virtualFile));
  }

  public String commentForLine(int lineNumber, VirtualFile virtualFile) {
    return lineComments.get(new CommentLocation(lineNumber, virtualFile));
  }

  public void addComment(int lineNumber, VirtualFile virtualFile, String comment) {
    lineComments.put(new CommentLocation(lineNumber, virtualFile), comment);
  }

  public int commentCountFor(VirtualFile file) {
    return (int) lineComments.keySet()
                             .stream()
                             .map(l -> l.virtualFile)
                             .filter(f -> f.equals(file))
                             .count();
  }

  private static class CommentLocation {
    public final int lineNumber;
    public final VirtualFile virtualFile;

    private CommentLocation(int lineNumber, VirtualFile virtualFile) {
      this.lineNumber = lineNumber;
      this.virtualFile = virtualFile;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      CommentLocation that = (CommentLocation) o;

      if (lineNumber != that.lineNumber) return false;
//      return virtualFile.equals(that.virtualFile); // equals isn't defined, so using hashCode

      return virtualFile.hashCode() == (that.virtualFile.hashCode());

    }

    @Override
    public int hashCode() {
      int result = lineNumber;
      result = 31 * result + virtualFile.hashCode();
      return result;
    }
  }

}
