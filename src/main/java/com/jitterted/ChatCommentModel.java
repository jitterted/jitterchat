package com.jitterted;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatCommentModel implements TreeModel {

  public static final String COMMENTS_ROOT = "Comments Root";

  private final Map<CommentLocation, String> lineComments = new HashMap<>();

  private final List<TreeModelListener> treeModelListeners = new ArrayList<>();

  private ArrayListValuedHashMap<FileNode, CommentNode> fileMap = new ArrayListValuedHashMap<>();
  private List<FileNode> fileList = new ArrayList<>();

  public boolean hasComment(int lineNumber, VirtualFile virtualFile) {
    return lineComments.containsKey(new CommentLocation(lineNumber, virtualFile));
  }

  public String commentForLine(int lineNumber, VirtualFile virtualFile) {
    return lineComments.get(new CommentLocation(lineNumber, virtualFile));
  }

  public void addComment(int lineNumber, VirtualFile virtualFile, String comment) {
    CommentLocation commentLocation = new CommentLocation(lineNumber, virtualFile);
    lineComments.put(commentLocation, comment);
    FileNode fileNode = new FileNode(virtualFile);
    fileMap.put(fileNode, new CommentNode(commentLocation, comment));
    if (!fileList.contains(fileNode)) {
      fileList.add(fileNode);
    }
    fireTreeStructureChanged();
  }

  public void removeComment(CommentLocation commentLocation) {
    String comment = lineComments.remove(commentLocation);
    FileNode fileNode = new FileNode(commentLocation.virtualFile);
    fileMap.removeMapping(fileNode,
                          new CommentNode(commentLocation, comment));
    if (fileMap.get(fileNode).isEmpty()) {
      fileList.remove(fileNode);
    }
    fireTreeStructureChanged();
  }

  public int commentCountFor(VirtualFile file) {
    return (int) lineComments.keySet()
                             .stream()
                             .map(l -> l.virtualFile)
                             .filter(f -> f.equals(file))
                             .count();
  }

  @Override
  public Object getRoot() {
    return COMMENTS_ROOT;
  }

  @Override
  public Object getChild(Object parent, int index) {
    List list = childListFor(parent);
    if (index >= list.size()) {
      return null;
    }
    return list.get(index);
  }

  @Override
  public int getChildCount(Object parent) {
    return childListFor(parent).size();
  }

  private List childListFor(Object parent) {
    List list;
    if (parent.equals(COMMENTS_ROOT)) {
      list = fileList;
    } else if (parent instanceof FileNode) {
      list = fileMap.get((FileNode) parent);
      list.sort(null);
    } else {
      list = Collections.emptyList();
    }
    return list;
  }

  @Override
  public boolean isLeaf(Object node) {
    return node instanceof CommentNode;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    return -1;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    treeModelListeners.add(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    treeModelListeners.remove(l);
  }

  protected void fireTreeStructureChanged() {
    TreeModelEvent e = new TreeModelEvent(this,
                                          new Object[]{COMMENTS_ROOT});
    for (TreeModelListener tml : treeModelListeners) {
      tml.treeStructureChanged(e);
    }
  }

}
