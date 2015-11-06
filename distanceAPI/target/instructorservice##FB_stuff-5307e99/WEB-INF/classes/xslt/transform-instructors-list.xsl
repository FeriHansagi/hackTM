<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" encoding="UTF-8" indent="yes" />

	<xsl:param name="offset"></xsl:param>
	<xsl:param name="limit"></xsl:param>

	<xsl:template match="/resource">

		<tns:instructorResult xmlns:tns="http://api.haufe-akademie.de/instructor/v1/"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="SCHEMA/instructor-v1.0.xsd">

			<meta>
				<totalCount>
					<xsl:value-of select="@totalCount" />
				</totalCount>

				<xsl:if test="$offset != ''">
					<offset>
						<xsl:value-of select="$offset" />
					</offset>
				</xsl:if>

				<xsl:if test="$limit != ''">
					<limit>
						<xsl:value-of select="$limit" />
					</limit>
				</xsl:if>

			</meta>

			<instructors>
				<xsl:for-each select="link">
					<instructor id="{@id}">
						<links>
							<link rel="self" href="DOCROOT{@href}" />
						</links>
					</instructor>
				</xsl:for-each>
			</instructors>

		</tns:instructorResult>
	</xsl:template>

</xsl:stylesheet>


