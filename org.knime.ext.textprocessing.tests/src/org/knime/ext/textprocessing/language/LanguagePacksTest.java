/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   12 Nov 2025 (Manuel Hotz, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.ext.textprocessing.language;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.knime.ext.textprocessing.language.arabic.TextprocessingArabicLanguagePack;
import org.knime.ext.textprocessing.language.chinese.TextprocessingChineseLanguagePack;
import org.knime.ext.textprocessing.language.french.TextprocessingFrenchLanguagePack;
import org.knime.ext.textprocessing.language.german.TextprocessingGermanLanguagePack;
import org.knime.ext.textprocessing.language.spanish.TextprocessingSpanishLanguagePack;

/**
 * Tests for resolution of language pack static assets.
 *
 * @author Manuel Hotz, KNIME GmbH, Konstanz, Germany
 */
final class LanguagePacksTest {

    // Tests resolving static asset paths with the Language Pack's own path resolver

    @SuppressWarnings("static-method")
    @Test
    void testResolveLanguagePackAssetsArabic() {
        final var asset = TextprocessingArabicLanguagePack.resolvePath("models/stanfordmodels/pos/arabic.tagger");
        assertNotNull(asset, "Expected resolved asset file");
        assertNotEquals(new File(""), asset, "Did not expect fallback (empty) path");
    }

    @SuppressWarnings("static-method")
    @Test
    void testResolveLanguagePackAssetsChinese() {
        // Chinese has no tagger, so we just resolve one of the included assets
        final var asset = TextprocessingChineseLanguagePack.resolvePath("models/stanfordmodels/tokenizer/data/ctb.gz");
        assertNotNull(asset, "Expected resolved asset file");
        assertNotEquals(new File(""), asset, "Did not expect fallback (empty) path");
    }

    @SuppressWarnings("static-method")
    @Test
    void testResolveLanguagePackAssetsFrench() {
        final var asset = TextprocessingFrenchLanguagePack.resolvePath("models/stanfordmodels/pos/french.tagger");
        assertNotNull(asset, "Expected resolved asset file");
        assertNotEquals(new File(""), asset, "Did not expect fallback (empty) path");
    }

    @SuppressWarnings("static-method")
    @Test
    void testResolveLanguagePackAssetsGerman() {
        final var asset = TextprocessingGermanLanguagePack.resolvePath("models/stanfordmodels/pos/german-fast.tagger");
        assertNotNull(asset, "Expected resolved asset file");
        assertNotEquals(new File(""), asset, "Did not expect fallback (empty) path");
    }

    @SuppressWarnings("static-method")
    @Test
    void testResolveLanguagePackAssetsSpanish() {
        final var asset = TextprocessingSpanishLanguagePack.resolvePath("models/stanfordmodels/posmodels/spanish.tagger");
        assertNotNull(asset, "Expected resolved asset file");
        assertNotEquals(new File(""), asset, "Did not expect fallback (empty) path");
    }

    // *Turkish has its own library setup and does not use an assets plugin
}
