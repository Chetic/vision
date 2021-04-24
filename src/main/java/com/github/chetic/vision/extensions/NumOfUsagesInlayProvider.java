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
import com.intellij.ui.JBColor;
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
        return "NumOfUsages";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "5";
    }

    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        return language.is(Language.findLanguageByID("JAVA"));
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
                if (psiElement instanceof CompositePsiElement) {
                    String elementType = ((CompositePsiElement) psiElement).getElementType().toString();
                    Integer numOfUsages;
                    if (elementType.equals("LOCAL_VARIABLE")) {
                        numOfUsages = ReferencesSearch.search(psiElement).findAll().size();
                        addInlineElement(psiElement, inlayHintsSink, numOfUsages);
                    } else if (elementType.equals("METHOD_CALL_EXPRESSION")) {
                        PsiElement sourcePsi = ((PsiReference) psiElement.getFirstChild()).resolve();
                        if (sourcePsi != null) {
                            numOfUsages = ReferencesSearch.search(sourcePsi).findAll().size() - 1;
                            numOfUsages = numOfUsages == -1 ? 0 : numOfUsages;
                            addInlineElement(psiElement, inlayHintsSink, numOfUsages);
                        }
                    }
                }
                return true;
            }

            private void addInlineElement(@NotNull PsiElement psiElement, @NotNull InlayHintsSink inlayHintsSink, Integer numOfUsages) {
                inlayHintsSink.addInlineElement(psiElement.getTextOffset(), false, new BasePresentation() {
                    @Override
                    public int getWidth() {
                        return 14 + (4 * numOfUsages.toString().length());
                    }

                    @Override
                    public int getHeight() {
                        return 30;
                    }

                    @Override
                    public void paint(@NotNull Graphics2D graphics2D, @NotNull TextAttributes textAttributes) {
                        AffineTransform originalTransform = graphics2D.getTransform();
                        graphics2D.scale(0.7, 0.7);
                        graphics2D.setColor(Color.orange);
                        graphics2D.fillArc(0, 0, getWidth(), getHeight(), 0, 360);
                        graphics2D.setColor(Color.black);
                        graphics2D.drawString(numOfUsages.toString(), 4, 20);
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
