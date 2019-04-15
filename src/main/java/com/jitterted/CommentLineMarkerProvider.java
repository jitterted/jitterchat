package com.jitterted;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.diff.tools.util.text.LineOffsetsUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CommentLineMarkerProvider extends RelatedItemLineMarkerProvider {
  @Override
  protected void collectNavigationMarkers(@NotNull PsiElement element,
      @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
    VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    if (document == null) {
      return;
    }
    int lineNumber = LineOffsetsUtil.create(document).getLineNumber(element.getTextOffset());

    ChatCommentLookup lookup = ServiceManager.getService(ChatCommentLookup.class);
    if (lookup.hasComment(lineNumber, virtualFile)) {
      NavigationGutterIconBuilder<PsiElement> builder =
          NavigationGutterIconBuilder.create(AllIcons.Nodes.Aspect)
//                                     .setTargets()
                                     .setTooltipText(lookup.commentForLine(lineNumber, virtualFile));
      result.add(builder.createLineMarkerInfo(element));
    }
  }
}
