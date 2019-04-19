package com.jitterted;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.xdebugger.ui.DebuggerColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Provides display of the comment text inline with the code
 */
public class CommentLinePainter extends EditorLinePainter {
  public static int getCurrentLineIndex(@NotNull Editor editor) {
    CaretModel caretModel = editor.getCaretModel();
    if (!caretModel.isUpToDate()) {
      return -1;
    }
    LogicalPosition position = caretModel.getLogicalPosition();
    return position.line;
  }

  private static TextAttributes getNormalAttributes() {
    TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(DebuggerColors.INLINED_VALUES);
    if (attributes == null || attributes.getForegroundColor() == null) {
      return new TextAttributes(new JBColor(() -> EditorColorsManager
          .getInstance().isDarkEditor() ? new Color(0x3d8065) : Gray._135), null, null, null, Font.ITALIC);
    }
    return attributes;
  }

  private static TextAttributes getChangedAttributes() {
    TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(DebuggerColors.INLINED_VALUES_MODIFIED);
    if (attributes == null || attributes.getForegroundColor() == null) {
      return new TextAttributes(new JBColor(() -> EditorColorsManager.getInstance().isDarkEditor() ? new Color(0xa1830a) : new Color(0xca8021)), null, null, null, Font.ITALIC);
    }
    return attributes;
  }

  private static TextAttributes getTopFrameSelectedAttributes() {
    TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(DebuggerColors.INLINED_VALUES_EXECUTION_LINE);
    if (attributes == null || attributes.getForegroundColor() == null) {
      //noinspection UseJBColor
      return new TextAttributes(EditorColorsManager.getInstance().isDarkEditor() ? new Color(255, 235, 9) : new Color(0, 255, 86), null, null, null, Font.ITALIC);
    }
    return attributes;
  }

  @Nullable
  @Override
  public Collection<LineExtensionInfo> getLineExtensions(
      @NotNull Project project, @NotNull VirtualFile file, int lineNumber) {

    ChatCommentLookup lookup = ServiceManager.getService(ChatCommentLookup.class);

    if (!lookup.hasComment(lineNumber, file)) {
      return null;
    }


    Document document = FileDocumentManager.getInstance().getDocument(file);
    if (document != null) {
      Editor editor = getEditor(document, project);
      if (editor != null && isLineWithCaret(editor, lineNumber)) {

        String comment = lookup.commentForLine(lineNumber, file);

        LineExtensionInfo lineExtensionInfo = new LineExtensionInfo(
            " \u00BB " + comment,
            getNormalAttributes());
        return Collections.singletonList(lineExtensionInfo);
      }
    }

    return null;
  }

  @Nullable
  private Editor getEditor(@NotNull Document document, @NotNull Project project) {
    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (editor != null && Objects.equals(editor.getDocument(), document)) {
      return editor;
    }
    return null;
  }

  private boolean isLineWithCaret(@NotNull Editor editor, int editorLineIndex) {
    return getCurrentLineIndex(editor) == editorLineIndex;
  }

}
