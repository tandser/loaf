<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <articles>
            <xsl:for-each select="articles/article">
                <article>
                    <id_art><xsl:value-of select="@id_art"/></id_art>
                    <name><xsl:value-of select="@name"/></name>
                    <code><xsl:value-of select="@code"/></code>
                    <username><xsl:value-of select="@username"/></username>
                    <guid><xsl:value-of select="@guid"/></guid>
                </article>
            </xsl:for-each>
        </articles>
    </xsl:template>

</xsl:stylesheet>