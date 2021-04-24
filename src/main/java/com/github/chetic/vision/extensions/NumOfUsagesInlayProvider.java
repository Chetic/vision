package com.github.chetic.vision.extensions;

import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.BasePresentation;
import com.intellij.execution.impl.InlayProvider;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.JBColor;
import com.jetbrains.cidr.lang.psi.OCCallExpression;
import com.jetbrains.cidr.lang.psi.OCDeclaration;
import com.jetbrains.cidr.lang.psi.OCDeclarator;
import com.jetbrains.cidr.lang.psi.OCReferenceExpression;
import com.jetbrains.cidr.lang.psi.impl.OCDeclarationImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class NumOfUsagesInlayProvider implements InlayHintsProvider<NoSettings> {
    @Override
    public boolean isVisibleInSettings() {
        return true;
    }

    @NotNull
    @Override
    public SettingsKey getKey() {
        return new SettingsKey("NumOfUsagesInlay");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "Vision";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "void foo(); void bar() { foo(); int i = 0; i++; i;i;i; }";
    }

    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        return language.is(Language.findLanguageByID("ObjectiveC"));
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull NoSettings noSettings) {
        return changeListener -> new JTextField("HELLO WHERE AM I?");
    }

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull NoSettings noSettings, @NotNull InlayHintsSink inlayHintsSink) {
        return new InlayHintsCollector() {
            @Override
            public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
                // C/C++
                if (psiElement instanceof OCCallExpression) {
                    Color blobColor;
                    String blobText = "";
                    var referenceExpression = PsiTreeUtil.findChildOfType(psiElement, OCReferenceExpression.class);
                    var declaration = referenceExpression.getFirstChild().getReference().resolve();
                    if (declaration == null) {
                        // TODO: When does this happen?
                        blobColor = Color.red;
                    } else {
                        blobColor = Color.cyan;
                        blobText = String.valueOf(ReferencesSearch.search(declaration).findAll().size());
                    }
                    addInlineElement(psiElement, inlayHintsSink, blobText, blobColor);
                }
                if (psiElement instanceof OCDeclarationImpl) {
                    var declarators = ((OCDeclarationImpl) psiElement).getDeclarators();
                    for (int i = 0; i < declarators.size(); i++) {
                        PsiElement psi = declarators.get(i);
                        Integer numOfUsages = ReferencesSearch.search(psi).findAll().size();
                        Color blobColor;
                        if (psi.getChildren().length > 1) {
                            blobColor = Color.orange;
                        } else {
                            blobColor = Color.green;
                        }
                        addInlineElement(psi, inlayHintsSink, numOfUsages.toString(), blobColor);
                    }
                }
                return true;
            }

            private void addInlineElement(@NotNull PsiElement psiElement, @NotNull InlayHintsSink inlayHintsSink, String text, Color bgColor) {
                inlayHintsSink.addInlineElement(psiElement.getTextOffset(), false, new BasePresentation() {
                    @Override
                    public int getWidth() {
                        return 14 + (4 * text.length());
                    }

                    @Override
                    public int getHeight() {
                        return 30;
                    }

                    @Override
                    public void paint(@NotNull Graphics2D graphics2D, @NotNull TextAttributes textAttributes) {
                        AffineTransform originalTransform = graphics2D.getTransform();
                        graphics2D.scale(0.7, 0.7);
                        graphics2D.setColor(bgColor);
                        graphics2D.fillArc(0, 0, getWidth(), getHeight(), 0, 360);
                        graphics2D.setColor(Color.black);
                        graphics2D.drawString(text, 4, 20);
                        graphics2D.setTransform(originalTransform);
                    }
                });
            }
        };
    }

    @NotNull
    @Override
    public NoSettings createSettings() {
        return new NoSettings();
    }
}
