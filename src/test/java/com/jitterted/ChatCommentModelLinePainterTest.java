package com.jitterted;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChatCommentModelLinePainterTest {

  @Test
  public void givenFileAndLineFindsExistingComment() throws Exception {
    ChatCommentModel chatCommentModel = new ChatCommentModel();
    VirtualFile file = new LightVirtualFile("Testing");

    chatCommentModel.addComment(15, file, "the comment");

    assertThat(chatCommentModel.hasComment(15, file))
        .isTrue();
  }

  @Test
  public void givenCommentExistsQueryReturnsComment() throws Exception {
    ChatCommentModel chatCommentModel = new ChatCommentModel();
    VirtualFile file = new LightVirtualFile("Testing");

    chatCommentModel.addComment(23, file, "prime comment");

    assertThat(chatCommentModel.commentForLine(23, file))
        .isEqualTo("prime comment");
  }



}