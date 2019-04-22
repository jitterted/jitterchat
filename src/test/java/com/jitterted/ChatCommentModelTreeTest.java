package com.jitterted;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatCommentModelTreeTest {

  @Test
  public void emptyModelResultsInTreeOnlyWithRoot() throws Exception {
    ChatCommentModel chatCommentModel = new ChatCommentModel();

    Object root = chatCommentModel.getRoot();
    assertThat(root)
        .isNotNull();

    assertThat(chatCommentModel.getChildCount(root))
        .isZero();

    assertThat(chatCommentModel.getChild(root, 0))
        .isNull();
  }

  @Test
  public void singleCommentResultsInOneParentFileNodeWithOneChildCommentNode() throws Exception {
    /* > ROOT
     *   > file1
     *     > comment 1 @ line 10
     */
    ChatCommentModel chatCommentModel = new ChatCommentModel();
    VirtualFile file1 = new LightVirtualFile("file1");
    chatCommentModel.addComment(10, file1, "comment 1");

    Object root = chatCommentModel.getRoot();

    assertThat(chatCommentModel.getChildCount(root))
        .as("Root did not have expected File Node")
        .isEqualTo(1);

    Object fileNode = chatCommentModel.getChild(root, 0);

    assertThat(fileNode)
        .isInstanceOf(VirtualFile.class);

    assertThat(chatCommentModel.getChildCount(fileNode))
        .as("File node did not have the single Comment Node")
        .isEqualTo(1);

    Object commentNode = chatCommentModel.getChild(fileNode, 0);

    assertThat(commentNode)
        .as("Expected the Comment Node to not be null")
        .isNotNull();

    assertThat(commentNode.toString())
        .isEqualTo("11: \"comment 1\"");
  }

  @Test
  public void addThenRemoveCommentResultsInEmptyTree() throws Exception {
    ChatCommentModel chatCommentModel = new ChatCommentModel();
    VirtualFile file1 = new LightVirtualFile("file1");
    chatCommentModel.addComment(31, file1, "a comment");

    chatCommentModel.removeComment(new CommentLocation(31, file1));

    Object root = chatCommentModel.getRoot();

    assertThat(chatCommentModel.getChildCount(root))
        .isZero();

    assertThat(chatCommentModel.getChild(root, 0))
        .isNull();

    assertThat(chatCommentModel.getChildCount(file1))
        .isZero();

    assertThat(chatCommentModel.getChild(file1, 0))
        .isNull();
  }

  @Test
  public void rootNodeIsNotLeaf() throws Exception {
    ChatCommentModel chatCommentModel = new ChatCommentModel();

    Object root = chatCommentModel.getRoot();

    assertThat(chatCommentModel.isLeaf(root))
        .isFalse();
  }

  @Test
  public void fileNodeIsNotLeaf() throws Exception {
    ChatCommentModel chatCommentModel = new ChatCommentModel();
    VirtualFile file1 = new LightVirtualFile("file1");
    chatCommentModel.addComment(31, file1, "a comment");

    Object root = chatCommentModel.getRoot();

    Object fileNode = chatCommentModel.getChild(root, 0);
    assertThat(fileNode)
        .isNotNull();

    assertThat(chatCommentModel.isLeaf(fileNode))
        .isFalse();
  }

  @Test
  public void commentNodeIsLeaf() throws Exception {
    ChatCommentModel chatCommentModel = new ChatCommentModel();
    VirtualFile file1 = new LightVirtualFile("file1");
    chatCommentModel.addComment(31, file1, "a comment");

    Object fileNode = chatCommentModel.getChild(chatCommentModel.getRoot(), 0);

    Object commentNode = chatCommentModel.getChild(fileNode, 0);

    assertThat(commentNode)
        .isNotNull();

    assertThat(chatCommentModel.isLeaf(commentNode))
        .isTrue();
  }

  @Test
  public void singleFileCommentIsRemovedBothChildAndParentNodesAreRemoved() throws Exception {
    /*
       > ROOT
         > file2
           > comment 1
         > file3
           > comment 1
     */
    /*
       > ROOT
         > file3
           > comment 1
     */
  }
}
