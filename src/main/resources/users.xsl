<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <body>
                <h2>Users</h2>
                <table border="1">
                    <tr bgcolor="#9acd32">
                        <th style="text-align:left">fullName</th>
                        <th style="text-align:left">email</th>
                    </tr>
                    <xsl:for-each select="*[name()='Payload']/*[name()='Project']/*[name()='Group']/*[name()='User']">
                        <tr>
                            <td><xsl:value-of select="*[name()='fullName']"/></td>
                            <!--<td><xsl:value-of select="name"/></td>-->
                            <!--<td><xsl:value-of select="description"/></td>-->
                            <td><xsl:value-of select="@email"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>

