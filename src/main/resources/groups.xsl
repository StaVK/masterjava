<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <body>
                <h2>Groups</h2>
                <table border="1">
                    <tr bgcolor="#9acd32">
                        <th style="text-align:left">Name</th>
                        <!--<th style="text-align:left">email</th>-->
                    </tr>
                    <xsl:param name="project"/>

                    <!--<xsl:for-each select="*[name()='Payload'][name=$project]/*[name()='Project']/*[name()='Group']">-->
                    <xsl:for-each select="*[name()='Payload']/*[name()='Project']">
                        <xsl:if test="*[name()='name']=$project">
                            <xsl:for-each select="*[name()='Group']">
                                <tr>
                                    <td>
                                        <xsl:value-of select="*[name()='name']"/>
                                    </td>
                                    <!--<td><xsl:value-of select="name"/></td>-->
                                    <!--<td><xsl:value-of select="description"/></td>-->
                                    <!--<td><xsl:value-of select="@email"/></td>-->
                                </tr>
                            </xsl:for-each>
                        </xsl:if>
                    </xsl:for-each>

                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>

