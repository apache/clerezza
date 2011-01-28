<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:param name="baseUri"/>
	<xsl:param name="originBundleSymbolicName"/>
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="@href[parent::a]">
		<xsl:attribute name="href">
			<xsl:choose>
				<xsl:when test="starts-with(.,'bundle:///')">
					<xsl:value-of select="concat($baseUri,'bundle-doc/',$originBundleSymbolicName,substring-after(.,'bundle://'))"/>
				</xsl:when>
				<xsl:when test="starts-with(.,'bundle://')">
					<xsl:value-of select="concat($baseUri,'bundle-doc/',substring-after(.,'bundle://'))"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="."/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
</xsl:stylesheet>
