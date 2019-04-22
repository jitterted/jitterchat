package com.jitterted;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.diff.tools.util.text.LineOffsetsUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ConstantFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

//public class CommentLineMarkerProvider extends RelatedItemLineMarkerProvider {
public class CommentLineMarkerProvider implements LineMarkerProvider {
  @Nullable
  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
//    System.out.println("Element " + element + " text range " + element.getTextRange());

    VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    if (document == null) {
      return null;
    }
    int lineNumber = LineOffsetsUtil.create(document).getLineNumber(element.getTextOffset());
    ChatCommentModel lookup = ServiceManager.getService(ChatCommentModel.class);
    if (lookup.hasComment(lineNumber, virtualFile)) {
      String comment = lookup.commentForLine(lineNumber, virtualFile);
//      System.err.println("Found comment for element: " + element + " at line " + lineNumber + " at offset " + element.getTextOffset() + " with text range " + element.getTextRange());
      return new LineMarkerInfo<>(element,
                                  element.getTextRange(),
                                  AllIcons.Nodes.Aspect,
                                  Pass.UPDATE_ALL,
                                  new ConstantFunction<>(comment),
                                  null,
                                  GutterIconRenderer.Alignment.LEFT);
    }
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
/*
    VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    if (document == null) {
      return null;
    }
    int lineNumber = LineOffsetsUtil.create(document).getLineNumber(element.getTextOffset());

    ChatCommentLookup lookup = ServiceManager.getService(ChatCommentLookup.class);
    if (lookup.hasComment(lineNumber, virtualFile)) {
      String comment = lookup.commentForLine(lineNumber, virtualFile);
      System.err.println("Found comment for element: " + element + " at line " + lineNumber + " at offset " + element.getTextOffset() + " with text range " + element.getTextRange());
      return new LineMarkerInfo<>(element,
                                  element.getTextRange(),
                                  AllIcons.Nodes.Aspect,
                                  Pass.UPDATE_ALL,
                                  new ConstantFunction<>(comment),
                                  null,
                                  GutterIconRenderer.Alignment.LEFT);
    }

 */
//    System.out.println("---- collecting slow line markers ----");
//    System.out.println("     elements size = " + result.size());
//    System.out.println("     result size = " + result.size());

  }

//  @Override
//  protected void collectNavigationMarkers(@NotNull PsiElement element,
//      @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
//    VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
//    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
//    if (document == null) {
//      return;
//    }
//    int lineNumber = LineOffsetsUtil.create(document).getLineNumber(element.getTextOffset());
//
//    ChatCommentLookup lookup = ServiceManager.getService(ChatCommentLookup.class);
//    if (lookup.hasComment(lineNumber, virtualFile)) {
//      NavigationGutterIconBuilder<PsiElement> builder =
//          NavigationGutterIconBuilder.create(AllIcons.Nodes.Aspect)
//                                     .setTargets(element)
//                                     .setTooltipText(lookup.commentForLine(lineNumber, virtualFile));
//      result.add(builder.createLineMarkerInfo(element));
//    }
//  }
}
