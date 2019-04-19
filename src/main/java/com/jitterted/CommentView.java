package com.jitterted;

import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

public class CommentView {
  private final Project project;
  private JPanel panel;
  private JTree tree;

  public CommentView(ToolWindow toolWindow, Project project) {
    this.project = project;
    createUIComponents();

    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(panel, "Comments", false);
    toolWindow.getContentManager().addContent(content);
  }


  private void createUIComponents() {
    ChatCommentLookup service = ServiceManager.getService(ChatCommentLookup.class);
    createTree(service);

    panel = ToolbarDecorator.createDecorator(tree)
                            .disableAddAction()
                            .setToolbarPosition(ActionToolbarPosition.LEFT)
                            .setRemoveAction(action -> {
                              CommentLocation selectedNode = (CommentLocation)
                                  tree.getSelectionModel().getSelectionPath().getLastPathComponent();
                              service.removeComment(selectedNode);
                            })
                            .createPanel();

    tree.getSelectionModel().addTreeSelectionListener(e -> {
      Object node = e.getPath().getLastPathComponent();
      if (!(node instanceof CommentLocation)) {
        return;
      }

      final CommentLocation commentLocation = (CommentLocation) node;

      ApplicationManager.getApplication().invokeLater(() -> {
        new OpenFileDescriptor(project,
                               commentLocation.virtualFile,
                               commentLocation.lineNumber,
                               -1, // first logical column
                               true)
            .navigate(true);
      });
    });

  }

  private void createTree(ChatCommentLookup service) {
    tree = new Tree(service);
    UIUtil.setLineStyleAngled(tree);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
  }

}
