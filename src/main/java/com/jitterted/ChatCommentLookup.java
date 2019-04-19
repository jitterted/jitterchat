package com.jitterted;

import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatCommentLookup implements TreeModel {

  public static final String COMMENTS_ROOT = "Comments Root";
  private final Map<CommentLocation, String> lineComments = new HashMap<>();
  private List<TreeModelListener> treeModelListeners = new ArrayList<>();

  public boolean hasComment(int lineNumber, VirtualFile virtualFile) {
    return lineComments.containsKey(new CommentLocation(lineNumber, virtualFile));
  }

  public String commentForLine(int lineNumber, VirtualFile virtualFile) {
    return lineComments.get(new CommentLocation(lineNumber, virtualFile));
  }

  public void addComment(int lineNumber, VirtualFile virtualFile, String comment) {
    lineComments.put(new CommentLocation(lineNumber, virtualFile), comment);
    fireTreeStructureChanged();
  }

  public void removeComment(CommentLocation commentLocation) {
    lineComments.remove(commentLocation);
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
    if (parent.equals(COMMENTS_ROOT)) {
      return lineComments.keySet().toArray()[index];
    } else {
      return null;
    }
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent.equals(COMMENTS_ROOT)) {
      return lineComments.size();
    } else {
      return 0;
    }
  }

  @Override
  public boolean isLeaf(Object node) {
    return !node.equals(COMMENTS_ROOT);
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
