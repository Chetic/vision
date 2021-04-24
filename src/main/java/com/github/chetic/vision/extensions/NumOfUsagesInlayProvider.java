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
import com.jetbrains.cidr.lang.psi.OCDeclaration;
import com.jetbrains.cidr.lang.psi.OCDeclarator;
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
        return "NumOfUsages";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "5";
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
                if (psiElement instanceof OCDeclarationImpl)
                {
                    java.util.List<OCDeclarator> declarators = ((OCDeclarationImpl) psiElement).getDeclarators();
                    PsiElement psi = declarators.get(0);
                    Integer numOfUsages = ReferencesSearch.search(psi).findAll().size();
                    addInlineElement(psiElement, inlayHintsSink, numOfUsages, declarators.size() > 1 ? Color.orange : Color.green);
                }
                return true;
            }

            private void addInlineElement(@NotNull PsiElement psiElement, @NotNull InlayHintsSink inlayHintsSink, Integer numOfUsages, Color bgColor) {
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
                        graphics2D.setColor(bgColor);
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
