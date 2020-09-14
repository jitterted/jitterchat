package dev.chatcodes;

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
    chatCommentModel.addComment(10, file1, "comment 1", "Zaphod");

    Object root = chatCommentModel.getRoot();

    assertThat(chatCommentModel.getChildCount(root))
        .as("Root did not have expected File Node")
        .isEqualTo(1);

    Object fileNode = chatCommentModel.getChild(root, 0);

    assertThat(fileNode)
        .isInstanceOf(FileNode.class);

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
    chatCommentModel.addComment(31, file1, "a comment", "Zaphod");

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
    chatCommentModel.addComment(31, file1, "a comment", "Zaphod");

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
    chatCommentModel.addComment(31, file1, "a comment", "Zaphod");

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
         > file1
           > comment 1
         > file2
           > comment 1

     -- Remove comment 1 on file1:

       > ROOT
         > file2
           > comment 1
     */
    ChatCommentModel chatCommentModel = new ChatCommentModel();
    VirtualFile virtualFile1 = new LightVirtualFile("file1");
    chatCommentModel.addComment(31, virtualFile1, "comment 1 on file1", "Zaphod");
    VirtualFile virtualFile2 = new LightVirtualFile("file2");
    chatCommentModel.addComment(13, virtualFile2, "comment 1 on file2", "Zaphod");

    FileNode fileNode1 = (FileNode) chatCommentModel.getChild(chatCommentModel.getRoot(), 0);
    assertThat(fileNode1.virtualFile).isEqualTo(virtualFile1);
    CommentNode commentNode1 = (CommentNode) chatCommentModel.getChild(fileNode1, 0);


    chatCommentModel.removeComment(commentNode1.location);


    assertThat(chatCommentModel.getChildCount(chatCommentModel.getRoot()))
        .isEqualTo(1);

    FileNode fileNode = (FileNode) chatCommentModel.getChild(chatCommentModel.getRoot(), 0);
    assertThat(fileNode.virtualFile)
        .isEqualTo(virtualFile2);

    CommentNode commentNode = (CommentNode) chatCommentModel.getChild(fileNode, 0);
    assertThat(commentNode.comment)
        .isEqualTo("comment 1 on file2");
  }

  @Test
  public void fileNodeWithTwoCommentsRemovingOneCommentThenOtherCommentAndFileRemain() throws Exception {
        /*
       > ROOT
         > file1
           > comment 1 on file1
           > comment 2 on file1
         > file2
           > comment 1 on file2

     -- Remove "comment 1 on file1":

       > ROOT
         > file1
           > comment 2 on file1
         > file2
           > comment 1 on file2
     */

    ChatCommentModel chatCommentModel = new ChatCommentModel();
    VirtualFile virtualFile1 = new LightVirtualFile("file1");
    chatCommentModel.addComment(31, virtualFile1, "comment 1 on file1", "Zaphod");
    chatCommentModel.addComment(73, virtualFile1, "comment 2 on file1", "Zaphod");
    VirtualFile virtualFile2 = new LightVirtualFile("file2");
    chatCommentModel.addComment(13, virtualFile2, "comment 1 on file2", "Zaphod");

    FileNode fileNode1 = (FileNode) chatCommentModel.getChild(chatCommentModel.getRoot(), 0);
    assertThat(fileNode1.virtualFile).isEqualTo(virtualFile1);
    CommentNode commentNode1 = (CommentNode) chatCommentModel.getChild(fileNode1, 0);
    String commentFromCommentNode1 = commentNode1.comment;

    chatCommentModel.removeComment(commentNode1.location);

    Object root = chatCommentModel.getRoot();
    assertThat(chatCommentModel.getChildCount(root))
        .isEqualTo(2);

    fileNode1 = (FileNode) chatCommentModel.getChild(root, 0);
    assertThat(chatCommentModel.getChildCount(fileNode1))
        .isEqualTo(1);
    CommentNode commentNode = (CommentNode) chatCommentModel.getChild(fileNode1, 0);
    assertThat(commentNode.comment)
        .isNotEqualTo(commentFromCommentNode1);
  }

}
