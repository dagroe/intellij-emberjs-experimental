package com.emberjs.xml

import com.dmarcotte.handlebars.file.HbFileType
import com.intellij.lang.html.HTMLLanguage
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

/**
 * Descriptors are also requested by dumb-aware callers such as the spell checker and Grazie,
 * which run while indexing. Nothing on that path may touch indexes.
 */
class EmberXmlElementDescriptorDumbModeTest : BasePlatformTestCase() {

    @Test
    fun testAttributeDescriptorsDoNotHitIndexesInDumbMode() {
        myFixture.configureByText(HbFileType.INSTANCE, """<MyComponent class="a" @arg="b" />""")
        val htmlView = myFixture.file.viewProvider.getPsi(HTMLLanguage.INSTANCE)!!
        val tag = PsiTreeUtil.collectElementsOfType(htmlView, XmlTag::class.java).first()

        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            val descriptor = EmberXmlElementDescriptorProvider().getDescriptor(tag)
            // must not throw IndexNotReadyException
            descriptor?.getAttributesDescriptors(tag)
            descriptor?.getAttributeDescriptor("class", tag)
            tag.attributes.forEach { it.descriptor }
        }

        // and the real descriptors must be back once indexing is over
        assertNotNull(tag.attributes.first { it.name == "class" }.descriptor)
    }
}
