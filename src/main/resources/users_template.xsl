<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xsl:strip-space elements="*"/>
    <!--<xsl:template match="/*[name()='Payload']/*[name()='Group']/*[name()='User']/*[name()='fullName']">-->

    <xsl:template match="*[name()='Project']/*[name()='Group']/*[name()='User']/*[name()='fullName']">

        <xsl:copy-of select="."/>
        <xsl:text>&#xa;</xsl:text><!-- put in the newline -->

    </xsl:template>
    <xsl:template match="text()"/>

</xsl:stylesheet>