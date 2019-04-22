package com.jitterted;

import com.intellij.openapi.vfs.VirtualFile;

public class CommentNode {
  final String comment;
  final CommentLocation location;

  public CommentNode(int lineNumber, VirtualFile virtualFile, String comment) {
    this(new CommentLocation(lineNumber, virtualFile), comment);
  }

  public CommentNode(CommentLocation commentLocation, String comment) {
    location = commentLocation;
    this.comment = comment;
  }

  @Override
  public String toString() {
    // line numbers are stored 0-based, humans are 1-based
    return String.format("%d: \"%s\"", location.lineNumber + 1, comment);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CommentNode that = (CommentNode) o;

    if (!comment.equals(that.comment)) return false;
    return location.equals(that.location);

  }

  @Override
  public int hashCode() {
    int result = comment.hashCode();
    result = 31 * result + location.hashCode();
    return result;
  }
}
