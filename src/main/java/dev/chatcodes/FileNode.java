package dev.chatcodes;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.Objects;

public class FileNode {
  final VirtualFile virtualFile;

  public FileNode(VirtualFile virtualFile) {
    Objects.requireNonNull(virtualFile);
    this.virtualFile = virtualFile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FileNode fileNode = (FileNode) o;

    return virtualFile.equals(fileNode.virtualFile);

  }

  @Override
  public int hashCode() {
    return virtualFile.hashCode();
  }

  @Override
  public String toString() {
    return virtualFile.getNameWithoutExtension();
  }
}
