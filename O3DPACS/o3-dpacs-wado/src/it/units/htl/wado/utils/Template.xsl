<?xml version="1.0"?>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
	<xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes" />
	<xsl:param name="versionParam" select="'1.0'" />
	<xsl:param name="wadoUrl" select="''" />

	<xsl:template name="FormatDate">
		<xsl:param name="str" />
		<xsl:if test="string-length($str) &gt; 0">
			<xsl:value-of select="substring($str,1,4)" />
			<xsl:text>/</xsl:text>
			<xsl:value-of select="substring($str,5,2)" />
			<xsl:text>/</xsl:text>
			<xsl:value-of select="substring($str,7,2)" />
		</xsl:if>
	</xsl:template>

	<xsl:template name="FormatTime">
		<xsl:param name="str" />
		<xsl:if test="string-length($str) &gt; 0">
			<xsl:value-of select="substring($str,1,2)" />
			<xsl:text>:</xsl:text>
			<xsl:value-of select="substring($str,3,2)" />
			<xsl:text>.</xsl:text>
			<xsl:value-of select="substring($str,5,2)" />
		</xsl:if>
	</xsl:template>
	
	
	<xsl:template name="FormatDateTime">
		<xsl:param name="dateTime" />
		<xsl:call-template name="FormatDate">
			<xsl:with-param name="str" select="substring($dateTime,1,8)" />
		</xsl:call-template>
		&#160;&#160;
		<xsl:call-template name="FormatTime">
			<xsl:with-param name="str" select="substring($dateTime,9,6)" />
		</xsl:call-template>
	
	</xsl:template>

	<xsl:template name="replace-string">
		<xsl:param name="text" />
		<xsl:param name="replace" />
		<xsl:param name="with" />
		<xsl:choose>
			<xsl:when test="contains($text,$replace)">
				<xsl:value-of select="substring-before($text,$replace)" />
				<xsl:value-of select="$with" />
				<xsl:call-template name="replace-string">
					<xsl:with-param name="text" select="substring-after($text,$replace)" />
					<xsl:with-param name="replace" select="$replace" />
					<xsl:with-param name="with" select="$with" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="add-row">
		<xsl:param name="param" />
		<xsl:param name="value" />
		<fo:table-row>
			<fo:table-cell width="120px" padding="3pt">
				<fo:block>
					<xsl:value-of select="$param" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell padding="3pt">
				<fo:block>
					<xsl:value-of select="$value" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>

	<xsl:template name="getImage">
		<xsl:param name="stUID" />
		<xsl:param name="seUID" />
		<xsl:param name="inUID" />
		<fo:external-graphic src="url({$wadoUrl}?requestType=WADO&amp;studyUID={$stUID}&amp;seriesUID={$seUID}&amp;objectUID={$inUID}&amp;contentType=image/jpeg&amp;rows=200&amp;columns=200)" />
	</xsl:template>

	<xsl:template name="table-head">
		<xsl:param name="title" />
		<fo:table-header>
			<fo:table-cell text-align="center" number-columns-spanned="2" padding="3pt">
				<fo:block text-align="center" width="100%" font-weight="bold">
					<xsl:value-of select="$title" />
				</fo:block>
			</fo:table-cell>
		</fo:table-header>
	</xsl:template>
	<xsl:template match="/">
		<fo:root xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

			<fo:layout-master-set>
				<fo:simple-page-master master-name="PaginaA4" page-height="29.7cm" page-width="21cm" margin-top="1.5cm" margin-bottom="1.5cm" margin-left="2.5cm" margin-right="2.5cm">
					<fo:region-body margin-top="1.8cm" />
					<fo:region-before extent="1.3cm" />
					<fo:region-after extent="1.3cm" />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<xsl:for-each select="*">
				<fo:page-sequence master-reference="PaginaA4">
					<fo:static-content flow-name="xsl-region-after">
						<fo:block text-align="end" font-size="6pt" font-family="times">
							<fo:page-number />
						</fo:block>
					</fo:static-content>

					<fo:flow flow-name="xsl-region-body">
						<fo:block space-after="5mm" text-align="left" font-size="small" border="1px solid blue">
							<fo:table table-layout="fixed" width="100%">
								<xsl:call-template name="table-head">
									<xsl:with-param name="title">
										Patient Information 
									</xsl:with-param>
								</xsl:call-template>
								<fo:table-body>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Patient Name
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:call-template name="replace-string">
												<xsl:with-param name="text">
													<xsl:value-of select="//tag[@tag='100010']" />
												</xsl:with-param>
													<xsl:with-param name="replace">^</xsl:with-param>
													<xsl:with-param name="with">&#160;</xsl:with-param>
											</xsl:call-template>
										</xsl:with-param>
									</xsl:call-template>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Birth Date
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:call-template name="FormatDate">
												<xsl:with-param name="str" select="//tag[@tag=100030]" />
											</xsl:call-template>
										</xsl:with-param>
									</xsl:call-template>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Patient ID
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:value-of select="//tag[@tag=100020]"></xsl:value-of>
										</xsl:with-param>
									</xsl:call-template>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Patient Sex
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:value-of select="//tag[@tag=100040]"></xsl:value-of>
										</xsl:with-param>
									</xsl:call-template>
								</fo:table-body>
							</fo:table>
						</fo:block>
						<!-- Block for information about the study -->
						<fo:block space-after="5mm" text-align="left" font-size="small" border="1px solid red">
							<fo:table table-layout="fixed" width="100%">
								<xsl:call-template name="table-head">
									<xsl:with-param name="title">
										Study Information
									</xsl:with-param>
								</xsl:call-template>
								<fo:table-body>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Study Instance UID
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:value-of select="//tag[@tag='20000d']" />
										</xsl:with-param>
									</xsl:call-template>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Study Date and Study Time
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:call-template name="FormatDate">
												<xsl:with-param name="str" select="//tag[@tag='80020']" />
											</xsl:call-template>
											&#160;&#160;
											<xsl:call-template name="FormatTime">
												<xsl:with-param name="str" select="//tag[@tag='80020']" />
											</xsl:call-template>
										</xsl:with-param>
									</xsl:call-template>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Accession Number
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:value-of select="//tag[@tag='80050']" />
										</xsl:with-param>
									</xsl:call-template>
								</fo:table-body>
							</fo:table>
						</fo:block>
						<!-- block for information abuot the series -->
						<fo:block space-after="5mm" font-size="small" border="1px solid orange">
							<fo:table table-layout="fixed" width="100%">
								<xsl:call-template name="table-head">
									<xsl:with-param name="title">
										Series Information
									</xsl:with-param>
								</xsl:call-template>
								<fo:table-body>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Series Instance UID
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:value-of select="//tag[@tag='20000e']" />
										</xsl:with-param>
									</xsl:call-template>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Modality
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:value-of select="//tag[@tag='80060']" />
										</xsl:with-param>
									</xsl:call-template>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											Series Number
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:value-of select="//tag[@tag='200011']" />
										</xsl:with-param>
									</xsl:call-template>
								</fo:table-body>
							</fo:table>
						</fo:block>
						<fo:block space-after="5mm" font-size="small" border="1px solid green">

							<fo:table table-layout="fixed" width="100%">
								<xsl:call-template name="table-head">
									<xsl:with-param name="title">
										Instance Information
									</xsl:with-param>
								</xsl:call-template>
								<fo:table-body>
									<xsl:call-template name="add-row">
										<xsl:with-param name="param">
											SOP Instance UID
										</xsl:with-param>
										<xsl:with-param name="value">
											<xsl:value-of select="//tag[@tag='80018']" />
										</xsl:with-param>
									</xsl:call-template>
									<xsl:if test="//tag[@tag='80060'] != 'SR'">
										<fo:table-row>
											<fo:table-cell>
												<fo:block>
													Image
											</fo:block>
											</fo:table-cell>
											<fo:table-cell>
												<fo:block>
													<xsl:call-template name="getImage">
														<xsl:with-param name="stUID">
															<xsl:value-of select="//tag[@tag='20000d']" />
														</xsl:with-param>
														<xsl:with-param name="seUID">
															<xsl:value-of select="//tag[@tag='20000e']" />
														</xsl:with-param>
														<xsl:with-param name="inUID">
															<xsl:value-of select="//tag[@tag='80018']" />
														</xsl:with-param>
													</xsl:call-template>
												</fo:block>
											</fo:table-cell>
										</fo:table-row>
									</xsl:if>
									<xsl:if test="//tag[@tag='80060'] = 'SR'">
										<xsl:call-template name="add-row">
											<xsl:with-param name="param">
												Content Date
											</xsl:with-param>
											<xsl:with-param name="value">
												<xsl:call-template name="FormatDate">
													<xsl:with-param name="str" select="//tag[@tag='80023']" />
												</xsl:call-template>
												at
												<xsl:call-template name="FormatTime">
													<xsl:with-param name="str" select="//tag[@tag='80033']" />
												</xsl:call-template>
											</xsl:with-param>
										</xsl:call-template>

										<xsl:call-template name="add-row">
											<xsl:with-param name="param">
												Verifing Observer Name
											</xsl:with-param>
											<xsl:with-param name="value">
												<xsl:call-template name="replace-string">
													<xsl:with-param name="text">
														<xsl:value-of select="//tag[@tag='40a073']/tag[@tag='40a075']" />
													</xsl:with-param>
													<xsl:with-param name="replace">^</xsl:with-param>
													<xsl:with-param name="with">&#160;</xsl:with-param>
												</xsl:call-template>
											</xsl:with-param>
										</xsl:call-template>
										
										<xsl:call-template name="add-row">
											<xsl:with-param name="param">
												Verification Date Time
											</xsl:with-param>
											<xsl:with-param name="value">
												<xsl:call-template name="FormatDateTime">
													<xsl:with-param name="dateTime">
														<xsl:value-of select="//tag[@tag='40a073']/tag[@tag='40a030']" />
													</xsl:with-param>													
												</xsl:call-template>
											</xsl:with-param>
										</xsl:call-template>
										
										
										<xsl:call-template name="add-row">
											<xsl:with-param name="param">
												Completion Flag
											</xsl:with-param>
											<xsl:with-param name="value">
												<xsl:value-of select="//tag[@tag='40a491']" />
											</xsl:with-param>
										</xsl:call-template>
										<xsl:call-template name="add-row">
											<xsl:with-param name="param">
												Verification Flag
											</xsl:with-param>
											<xsl:with-param name="value">
												<xsl:value-of select="//tag[@tag='40a493']" />
											</xsl:with-param>
										</xsl:call-template>


									
										<xsl:for-each select="//tag[@tag='40a730']/tag[@tag='40a043']">
												<xsl:call-template name="add-row">
													<xsl:with-param name="param">
														<xsl:value-of select="tag[@tag='80104']" />
													</xsl:with-param>
													<xsl:with-param name="value">
														<xsl:value-of select="tag[@tag='40a160']" />
													</xsl:with-param>
												</xsl:call-template>
										</xsl:for-each>
									
										
									</xsl:if>
								</fo:table-body>
							</fo:table>
						</fo:block>
					</fo:flow>
				</fo:page-sequence>
			</xsl:for-each>
		</fo:root>
	</xsl:template>
</xsl:stylesheet>