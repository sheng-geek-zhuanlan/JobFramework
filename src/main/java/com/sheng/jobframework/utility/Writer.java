package com.sheng.jobframework.utility;

import com.sheng.jobframework.annotation.ReportInfo;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


public class Writer {
    public Writer() {
    }

    public static void writeComponentXSL(String path) {
        try {
            //FileWriter fw = new FileWriter(path);
            OutputStreamWriter utput =
                new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
            String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"  xmlns:test=\"http://www.oracle.com/2005/ocs/qa/framework/junit/schema\" >\n" +
                "	<xsl:output method=\"html\"/>\n" +
                "	<xsl:template name=\"Root\" match=\"/test:report\">\n" +
                "		<html>\n" +
                "			<xsl:call-template name=\"html-head\"/>\n" +
                "			<!--xsl:call-template name=\"javascript\"/-->\n" +
                "			<body link=\"663300\" alink=\"#FF6600\" vlink=\"#996633\" bgcolor=\"#ffffff\">\n" +
                "				<xsl:call-template name=\"Header\"/>\n" +
                "				<div class=\"Heading\">\n" +
                "				<br/>\n" +
                "				<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n" +
                "							<tr>\n" +
                "								<td class=\"Heading\">" + ReportInfo.TEST_REPORT +
                " <xsl:value-of select=\"test:name\"/>\n" +
                "								</td>\n" +
                "								<td align=\"right\">\n";
            if (ReportInfo.getReportLicensed()) {
                s =
  s + "<font color=\"red\"><a href=\"http://www.cesoo.com\"><b>" +
      ReportInfo.COMP_COPYRIGHT_INFO + "</b></a></font>\n";
            } else {
                s =
  s + "<a href=\"../Summary.xml\">" + ReportInfo.COMP_PAGE_BACK + "</a>\n";
            }
            s =
  s + "								</td>\n" +
                    "							</tr>\n" +
                    "						</table>\n" +
                    "				</div>\n" +
                    "				<xsl:call-template name=\"SummaryTable\"/>\n" +
                    "				\n" +
                    "				<div class=\"Heading\"><br/><br/>" +
                    ReportInfo.COMP_DETAIL_INFO + "</div>\n" +
                    "				<xsl:for-each select=\"//test:report//test:testcase\">\n" +
                    "					<xsl:variable name=\"testID\" select=\"test:id\"/>\n" +
                    "					<div id=\"{$testID}\" style=\"margin-bottom:15px\">\n" +
                    "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n" +
                    "							<tr>\n" +
                    "								<td class=\"TableTitle\"><a> <xsl:attribute name=\"name\"><xsl:value-of select=\"test:name\"/></xsl:attribute><b>" +
                    ReportInfo.COMP_TEST_CASE +
                    ": <xsl:value-of select=\"test:name\"/></b></a>\n" +
                    "								</td>\n" +
                    "								<td align=\"right\">\n" +
                    "									<a href=\"#top\">\n" +
                    "										<div align=\"right\">" +
                    ReportInfo.COMP_PAGE_TOP + "</div>\n" +
                    "									</a>\n" +
                    "								</td>\n" +
                    "							</tr>\n" +
                    "						</table>\n" +
                    "						<xsl:call-template name=\"TestCaseTable\"/>\n" +
                    "					</div>\n" +
                    "				</xsl:for-each>\n" +
                    "<div class=\"Heading\"><br/><br/>Report Debug Information</div>\n" +
                    "					\n" +
                    "	\n" +

                    "	<tr>\n" +
                    "										<td ><b>Test Env:</b></td>\n" +
                    "										<td>\n" +
                    "											<table class=\"nameValueTable\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"300\">\n" +
                    "												<thead class=\"TestInfoHeader\">\n" +
                    "													<th>Name</th>\n" +
                    "													<th>Value</th>\n" +
                    "												</thead>\n" +
                    "												<xsl:for-each select=\"test:TestEnv/test:param\">\n" +
                    "													<xsl:call-template name=\"NameValueRow\">\n" +
                    "														<xsl:with-param name=\"name\" select=\"@name\"/>\n" +
                    "														<xsl:with-param name=\"value\" select=\".\"/>\n" +
                    "														<xsl:with-param name=\"width\" select=\"'200px'\"/>\n" +
                    "													</xsl:call-template>\n" +
                    "												</xsl:for-each>\n" +
                    "											</table>\n" +
                    "										</td>\n" +
                    "				</tr>\n" +
                    "				<tr>\n" +
                    "				</tr>\n" +
                    "			<tr>\n" +
                    "										<td ><b>Test Config:</b></td>\n" +
                    "										<td>\n" +
                    "											<table class=\"nameValueTable\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"300\">\n" +
                    "												<thead class=\"TestInfoHeader\">\n" +
                    "													<th>Name</th>\n" +
                    "													<th>Value</th>\n" +
                    "												</thead>\n" +
                    "												<xsl:for-each select=\"test:TestConfig/test:param\">\n" +
                    "													<xsl:call-template name=\"NameValueRow\">\n" +
                    "														<xsl:with-param name=\"name\" select=\"@name\"/>\n" +
                    "														<xsl:with-param name=\"value\" select=\".\"/>\n" +
                    "														<xsl:with-param name=\"width\" select=\"'200px'\"/>\n" +
                    "													</xsl:call-template>\n" +
                    "												</xsl:for-each>\n" +
                    "											</table>\n" +
                    "										</td>\n" +
                    "				</tr>		" +
                    "			</body>\n" +
                    "		</html>\n" +
                    "	</xsl:template>\n" +
                    "	<xsl:template name=\"html-head\">\n" +
                    "		<head>\n" +
                    "			<meta http-equiv=\"Content-Type\" content=\"text/html;CHARSET=iso-8859-1\"/>\n" +
                    "			<title>AC Automation Test Report - <xsl:value-of select=\"//test:report/test:time\"/>\n" +
                    "			</title>\n" +
                    "			<!--Javascript for expand and collapse of test info / error trace -->\n" +
                    "			<script language=\"JavaScript\">\n" +
                    "				// hides or shows given div and change contents of the switcher itself\n" +
                    "				function switchdiv(switcher, sw, closedname, openedname) {\n" +
                    "					//alert(\"Switcher: \" + switcher.tagName + \" Switched: \" + switched.tagName + \".\" + switched.id);\n" +
                    "					switched = document.getElementById(sw);\n" +
                    "					if (switched.style.display == \"none\") {\n" +
                    "						switched.style.display = \"block\";\n" +
                    "						switched.style.marginLeft = \"10px\";\n" +
                    "						switcher.innerText = openedname;\n" +
                    "					}  else {\n" +
                    "						switched.style.display = \"none\";\n" +
                    "						switcher.innerText = closedname;\n" +
                    "					}\n" +
                    "					window.event.cancelBubble = true;\n" +
                    "					return false;\n" +
                    "				}\n" +
                    "		    </script>\n" +
                    "		    <!-- CSS styles definitions -->\n" +
                    "			<style type=\"text/css\">\n" +
                    "			        BODY	{  BACKGROUND-REPEAT: no-repeat; FONT-FAMILY: Arial, Helvetica, sans-serif; BACKGROUND-COLOR: #ffffff } \n" +
                    "					.PageTitle { PADDING-BOTTOM: 8px;FONT-WEIGHT: bold; FONT-SIZE: medium; COLOR: #336699;  FONT-FAMILY: Arial, Times New Roman, Times, serif }\n" +
                    "					.PageTitleSmall { PADDING-RIGHT: 8px; FONT-WEIGHT: bold; FONT-SIZE: x-small; COLOR: #336699; FONT-FAMILY: Arial, Times New Roman, Times, serif }\n" +
                    "					.Heading { MARGIN-BOTTOM: 10px; MARGIN-TOP:15px; FONT-SIZE:medium; FONT-FAMILY: Arial, Helvetica, sans-serif; COLOR:#336699 }\n" +
                    "					.TableTitle { PADDING-TOP: 8px;FONT-SIZE: small; COLOR: #336699; FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif; BACKGROUND-COLOR: #ffffff }\n" +
                    "					.LargeTable { BORDER-RIGHT: #cccccc 1px solid; PADDING-RIGHT: 4px; BORDER-TOP: #cccccc 1px solid; PADDING-LEFT: 4px; BORDER-LEFT: #cccccc 1px solid; BORDER-BOTTOM: #cccccc 1px solid; BORDER-COLLAPSE: collapse;  BACKGROUND-COLOR: #999966 }\n" +
                    "					.LargeTableHeader {FONT-WEIGHT: bold; FONT-SIZE: x-small; VERTICAL-ALIGN: bottom;  FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif; BACKGROUND-COLOR: #93a9d5 }\n" +
                    "					.LargeTableText { FONT-SIZE: x-small;  MARGIN-BOTTOM: 0px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: top; COLOR: #000000; PADDING-TOP: 2px; FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif; BORDER-COLLAPSE: collapse; BACKGROUND-COLOR: #f7f7e7 }\n" +
                    "					.LargeTableSideHeader { WIDTH: 150px ; FONT-WEIGHT: bold; FONT-SIZE: x-small; VERTICAL-ALIGN: middle; FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif; BACKGROUND-COLOR: #93a9d5 }\n" +
                    "					.SmallTable { BORDER-RIGHT: #000000 1px; BORDER-TOP: #000000 1px; MARGIN: 0px; BORDER-LEFT: #000000 1px; BORDER-BOTTOM: #000000 1px; BORDER-COLLAPSE: collapse }\n" +
                    "					.SmallTableHeader { FONT-SIZE: xx-small; VERTICAL-ALIGN: bottom; TEXT-ALIGN: left }\n" +
                    "					.SmallTableText { FONT-SIZE: x-small; VERTICAL-ALIGN: top;  COLOR: #555555 }\n" +
                    "					.Timestamp {  }\n" +
                    "					.EventName { COLOR: #003366 }\n" +
                    "					.NameValueTable { PADDING-RIGHT: 0px; PADDING-LEFT: 0px; FONT-SIZE: x-small;  PADDING-BOTTOM: 0px; MARGIN: 0px; WIDTH: 100%;    BORDER-TOP-STYLE: none; PADDING-TOP: 0px; BORDER-RIGHT-STYLE: none; BORDER-LEFT-STYLE: none; BORDER-COLLAPSE: collapse; BORDER-BOTTOM-STYLE: none }\n" +
                    "					.TreeSwitcher { FONT-WEIGHT: bold; FONT-SIZE: x-small; VERTICAL-ALIGN: top;CURSOR: pointer }\n" +
                    "					.BigValue { BORDER-RIGHT: #cccccc 1px solid; PADDING-RIGHT: 2px;BORDER-TOP: #cccccc 1px solid; PADDING-LEFT: 2px;FONT-SIZE: x-small;   PADDING-BOTTOM: 2px; BORDER-LEFT: #cccccc 1px solid; WIDTH: 100%; PADDING-TOP: 2px; BORDER-BOTTOM: #cccccc 1px solid; BORDER-COLLAPSE: collapse}\n" +
                    "					.ValueName { PADDING-RIGHT: 4px; FONT-SIZE: x-small; WIDTH: 100px }\n" +
                    "					.ValueText { FONT-SIZE: x-small;  WIDTH: 100%; COLOR: #555555 }\n" +
                    "					.TestInfoHeader { FONT-WEIGHT: bold;FONT-SIZE: x-small; VERTICAL-ALIGN: middle; WIDTH: 100px; FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif;BACKGROUND-COLOR: #93a9d5 }\n" +
                    "					.InnerTestInfoHeader { FONT-WEIGHT: bold;FONT-SIZE: xx-small; VERTICAL-ALIGN: middle; WIDTH: 100px; FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif;BACKGROUND-COLOR: #93a9d5 }\n" +
                    "					.InnerValueText { FONT-SIZE: xx-small;  WIDTH: 100%; COLOR: #555555 }\n" +
                    "					.TestMethodTable {  PADDING-RIGHT: 4px; PADDING-LEFT: 4px; BORDER-COLLAPSE: collapse; BACKGROUND-COLOR: #9999ee }\n" +
                    "			</style>\n" +
                    "		</head>\n" +
                    "	</xsl:template>\n" +
                    "	<xsl:template name=\"Header\">\n" +
                    "	        <br/>\n";
            if (ReportInfo.getReportLicensed()) {
                s =
  s + "<table border='0' cellspacing='3' width='640' bgcolor='#FFF8F0' height='50' cellpadding='1'>\n" +
                        "                <tr>\n" +
                        "                <td align='center'>\n" +
                        "                <b><font color='red'>" +
                        ReportInfo.COMP_LICENSE_ACTRAIL + "</font></b>\n" +
                        "                <br/><font color='#008040'>" +
                        ReportInfo.COMP_LICENSE_BODY + "</font>\n" +
                        "                </td>\n" +
                        "                </tr>\n" +
                        "                </table>\n";
            }
            s =
  s + "		<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                    "			<tr class=\"PageTitle\">\n" +
                    "				<td colspan=\"2\">\n" +
                    "					<a name=\"top\">\n" +
                    "					   <xsl:value-of select=\"//test:component\"/>\n" +
                    "					   <xsl:text>" + ReportInfo.COMP_REPORT_HEADER +
                    "</xsl:text>\n" +
                    "					    \n" +
                    "					</a>\n" +
                    "				</td>\n" +
                    "			</tr>\n" +
                    "			<tr><td><br/></td></tr>\n" +
                    "			<tbody class=\"PageTitleSmall\">\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_VERSION +
                    "</td>\n" +
                    "					<td>\n" +
                    "					  <xsl:value-of select=\"//test:report/test:build\"/>\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "       <tr>\n" +
                    "         <td>" + ReportInfo.COMP_CLIENT_VERSION +
                    "</td>\n" +
                    "         <td>\n" +
                    "           <xsl:value-of select=\"//test:report/test:client_version\"/>\n" +
                    "         </td>\n" +
                    "       </tr>\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_STARTTIME +
                    "</td>\n" +
                    "					<td>\n" +
                    "					  <xsl:value-of select=\"//test:report/test:time\"/>\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_ACCOUNT +
                    "</td>\n" +
                    "					<td>\n" +
                    "                        <xsl:value-of select=\"//test:report/test:tester\"/>					\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_OS +
                    "</td>\n" +
                    "					<td>\n" +
                    "						<xsl:value-of select=\"//test:report/test:system/test:OS\"/>\n" +
                    "						<xsl:text>, </xsl:text>\n" +
                    "						<xsl:value-of select=\"//test:report/test:system/test:OS-version\"/>\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_LANG +
                    "</td>\n" +
                    "					<td>\n" +
                    "						            <xsl:value-of select=\"//test:report/test:language\"/>\n" +
                    "					</td>\n" +
                    "				</tr>							\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_ELAPSE +
                    "</td>\n" +
                    "					<td>\n" +
                    "						<xsl:variable name=\"exec_time\" select=\"sum(.//test:test/@elapsed)\"/>\n" +
                    "						\n" +
                    "                        			<xsl:choose>\n" +
                    "							<xsl:when test=\"$exec_time &gt; 3600\">\n" +
                    "								<xsl:value-of select=\"floor($exec_time div 3600)\"/> hr <xsl:value-of select=\"floor(($exec_time mod 3600) div 60)\"/> min <xsl:value-of select=\"$exec_time mod 60\"/> sec\n" +
                    "							</xsl:when>\n" +
                    "							<xsl:when test=\"$exec_time &gt; 60\">\n" +
                    "								<xsl:value-of select=\"floor($exec_time div 60)\"/> min <xsl:value-of select=\"$exec_time mod 60\"/> sec\n" +
                    "							</xsl:when>\n" +
                    "							<xsl:otherwise>\n" +
                    "								<xsl:value-of select=\"$exec_time\"/> sec\n" +
                    "							</xsl:otherwise>\n" +
                    "						</xsl:choose>				\n" +
                    "					</td>\n" +
                    "				</tr>	\n" +
                    "                                           <tr>\n" +
                    "                                                                           <td ><b>AC Host Info:</b></td>\n" +
                    "                                                                           <td>\n" +
                    "                                                                                   <table class=\"nameValueTable\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"1000\">\n" +
                    "                                                                                           <thead class=\"TestInfoHeader\">\n" +
                    "                                                                                                   <th>Name</th>\n" +
                    "                                                                                                   <th>Value</th>\n" +
                    "                                                                                           </thead>\n" +
                    "                                                                                           <xsl:for-each select=\"test:ReportDebug/test:param\">\n" +
                    "                                                                                                   <xsl:call-template name=\"NameValueRow\">\n" +
                    "                                                                                                           <xsl:with-param name=\"name\" select=\"@name\"/>\n" +
                    "                                                                                                           <xsl:with-param name=\"value\" select=\".\"/>\n" +
                    "                                                                                                           <xsl:with-param name=\"width\" select=\"'200px'\"/>\n" +
                    "                                                                                                   </xsl:call-template>\n" +
                    "                                                                                           </xsl:for-each>\n" +
                    "                                                                                   </table>\n" +
                    "                                                                           </td>\n" +
                    "                                                                   </tr>\n" +
                    "<tr>\n" +
                    "       </tr>     " + "<tr>\n" +
                    "       </tr>     " +
                    "             <xsl:if test=\"test:JMeterSummary\">\n" +
                    "                                           <tr>\n" +
                    "                                                                           <td ><b>JMeter Scenario:</b></td>\n" +
                    "                                                                           <td>\n" +
                    "                                                                                   <table class=\"nameValueTable\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"1000\">\n" +
                    "                                                                                           <thead class=\"TestInfoHeader\">\n" +
                    "                                                                                                   <th>Name</th>\n" +
                    "                                                                                                   <th>Value</th>\n" +
                    "                                                                                           </thead>\n" +
                    "                                                                                           <xsl:for-each select=\"test:JMeterSummary/test:param\">\n" +
                    "                                                                                                   <xsl:call-template name=\"NameValueRow\">\n" +
                    "                                                                                                           <xsl:with-param name=\"name\" select=\"@name\"/>\n" +
                    "                                                                                                           <xsl:with-param name=\"value\" select=\".\"/>\n" +
                    "                                                                                                           <xsl:with-param name=\"width\" select=\"'200px'\"/>\n" +
                    "                                                                                                   </xsl:call-template>\n" +
                    "                                                                                           </xsl:for-each>\n" +
                    "                                                                                   </table>\n" +
                    "                                                                           </td>\n" +
                    "                                                                   </tr>\n" +
                    "             </xsl:if>\n" +
                    "<tr>\n" +
                    "				</tr>			" +
                    "				<tr>\n" +
                    "				</tr>\n" +
                    "			</tbody>\n" +
                    "		</table>\n" +
                    "	</xsl:template>\n" +
                    "	\n" +
                    "	<xsl:template name=\"SummaryTable\">\n" +
                    "		<table id=\"TestSuiteTable\" class=\"LargeTable\" border=\"1\" cellpadding=\"0\" width=\"100%\">\n" +
                    "			<thead>\n" +
                    "				<tr class=\"LargeTableHeader\">\n" +
                    "					<th rowspan=\"2\">TestCases</th>\n" +
                    "					<th colspan=\"3\">Tests</th>					\n" +
                    "				</tr>\n" +
                    "				<tr class=\"LargeTableHeader\">					\n" +
                    "					<th>" + ReportInfo.COMP_STATUS_PASS +
                    "</th>					\n" +
                    "					<th>" + ReportInfo.COMP_STATUS_FAIL +
                    "</th>\n" +
                    "					<th>" + ReportInfo.COMP_STATUS_CNR +
                    "</th>					\n" +
                    "				</tr>\n" +
                    "			</thead>\n" +
                    "			<tbody class=\"LargeTableText\">\n" +
                    "				<xsl:variable name=\"totaltests\" select=\"count(//test:test)\"/>\n" +
                    "				<xsl:variable name=\"passedtest\" select=\"count(//test:test[@result='Passed'])\"/>					\n" +
                    "				<xsl:variable name=\"failedtest\" select=\"count(//test:test[@result='Failed'])\"/>	\n" +
                    "				<xsl:variable name=\"cnrtest\" select=\"count(//test:test[@result='CNR'])\"/>\n" +
                    "				<xsl:for-each select=\"//test:testcase\">					\n" +
                    "					<xsl:variable name=\"casetotaltests\" select=\"count(./test:test)\"/>\n" +
                    "					<xsl:variable name=\"casepassedtest\" select=\"count(./test:test[@result='Passed'])\"/>					\n" +
                    "					<xsl:variable name=\"casefailedtest\" select=\"count(./test:test[@result='Failed'])\"/>\n" +
                    "					<xsl:variable name=\"casecnrtest\" select=\"count(./test:test[@result='CNR'])\"/>						\n" +
                    "         <tr >\n" +
                    //"					<tr onmouseover=\"this.bgColor=0xeeeeaa\" onmouseout=\"this.bgColor=0xf7f7e7\">\n" +
                    "						<td>\n" +
                    "							<a>\n" +
                    "								<xsl:attribute name=\"href\"><xsl:value-of select=\"concat('#',test:name)\"/></xsl:attribute>								\n" +
                    "								<xsl:value-of select=\"test:name\"/>\n" +
                    "							</a>\n" +
                    "						</td>\n" +
                    "						<td align=\"center\">							\n" +
                    "						     <table border=\"0\"><tr><td align=\"left\">\n" +
                    "							<xsl:value-of select=\"$casepassedtest\"/>\n" +
                    "							</td><td></td><td align=\"right\">\n" +
                    "							<xsl:text>[</xsl:text>\n" +
                    "							<xsl:value-of select=\"floor($casepassedtest div $casetotaltests * 100+ 0.5)\"/>\n" +
                    "							<xsl:text>%</xsl:text>\n" +
                    "							<xsl:text>]</xsl:text>\n" +
                    "							</td></tr></table>\n" +
                    "						</td>						\n" +
                    "						<td align=\"center\">\n" +
                    "							<xsl:if test=\"$casefailedtest &gt; 0\">\n" +
                    "								<xsl:attribute name=\"bgcolor\">#ffcccc</xsl:attribute>\n" +
                    "							</xsl:if>\n" +
                    "							<table border=\"0\"><tr><td align=\"left\">\n" +
                    "							<xsl:value-of select=\"$casefailedtest\"/>\n" +
                    "							</td><td></td><td align=\"right\">\n" +
                    "							<xsl:text>[</xsl:text>\n" +
                    "							<xsl:value-of select=\"floor($casefailedtest div $casetotaltests * 100+ 0.5)\"/>\n" +
                    "							<xsl:text>%</xsl:text>\n" +
                    "							<xsl:text>]</xsl:text>\n" +
                    "							</td></tr></table>\n" +
                    "						</td>\n" +
                    "						<td align=\"center\">\n" +
                    "							<xsl:if test=\"$casecnrtest &gt; 0\">\n" +
                    "								<xsl:attribute name=\"bgcolor\">#ffd24d</xsl:attribute>\n" +
                    "							</xsl:if>\n" +
                    "							<table border=\"0\"><tr><td align=\"left\">\n" +
                    "							<xsl:value-of select=\"$casecnrtest\"/>\n" +
                    "							</td><td></td><td align=\"right\">\n" +
                    "							<xsl:text>[</xsl:text>\n" +
                    "							<xsl:value-of select=\"floor($casecnrtest div $casetotaltests * 100+ 0.5)\"/>\n" +
                    "							<xsl:text>%</xsl:text>\n" +
                    "							<xsl:text>]</xsl:text>\n" +
                    "							</td></tr></table>\n" +
                    "						</td>\n" +
                    "					</tr>\n" +
                    "				</xsl:for-each>\n" +
                    "				\n" +
                    "			</tbody>\n" +
                    "		</table>\n" +
                    "		\n" +
                    "	</xsl:template>\n" +
                    "	<xsl:template name=\"TestCaseTable\">\n" +
                    "		<xsl:variable name=\"testID\" select=\"test:id\"/>\n" +
                    "		<xsl:variable name=\"elapsed\" select=\"sum(./test:test/@elapsed)\"/>\n" +
                    "		<table class=\"LargeTable\" width=\"100%\" border=\"1\" cellpadding=\"0\">\n" +
                    "			<tbody class=\"LargeTableText\">\n" +
                    "				\n" +
                    "				<tr>\n" +
                    "					<td class=\"LargeTableSideHeader\">" +
                    ReportInfo.COMP_CASE_RESULT + "</td>\n" +
                    "					<td>\n" +
                    "						<xsl:choose>\n" +
                    "							<xsl:when test=\"test:test[@result='Failed']\">\n" +
                    "								<xsl:attribute name=\"bgcolor\">#ffcccc</xsl:attribute>\n" +
                    "								<xsl:text>Failed</xsl:text>\n" +
                    "							</xsl:when>\n" +
                    "							<xsl:otherwise>\n" +
                    "								<xsl:if test=\"test:test[@result='CNR']\">\n" +
                    "									<xsl:attribute name=\"bgcolor\">#ffd24d</xsl:attribute>\n" +
                    "									<xsl:text>CNR</xsl:text>\n" +
                    "								</xsl:if>\n" +
                    "								<xsl:if test=\"test:test[@result='Passed']\">\n" +
                    "									<xsl:attribute name=\"bgcolor\">#ccffcc</xsl:attribute>\n" +
                    "									<xsl:text>Passed</xsl:text>\n" +
                    "								</xsl:if>\n" +
                    "							</xsl:otherwise>\n" +
                    "						</xsl:choose>\n" +
                    "						<xsl:if test=\"test:test[@result='Failed']\">\n" +
                    "							<xsl:attribute name=\"bgcolor\">#ffcccc</xsl:attribute>\n" +
                    "						</xsl:if>\n" +
                    "						<xsl:value-of select=\"@result\"/>\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "				\n" +
                    "								\n" +
                    "				<xsl:apply-templates select=\"test:component\"/>\n" +
                    "				<xsl:apply-templates select=\"test:sub_component\"/>\n" +
                    "				<xsl:apply-templates select=\"test:area\"/>\n" +
                    "				<xsl:apply-templates select=\"test:description\"/>	\n" +
                    "				<tr>\n" +
                    "					<td class=\"LargeTableSideHeader\">" +
                    ReportInfo.COMP_TEST_ELAPSE + "</td>\n" +
                    "					<td>\n" +
                    "						<xsl:choose>\n" +
                    "							<xsl:when test=\"$elapsed &gt; 3600\">\n" +
                    "								<xsl:value-of select=\"floor($elapsed div 3600)\"/>" +
                    ReportInfo.COMP_TIME_HOUR +
                    " <xsl:value-of select=\"floor(($elapsed mod 3600) div 60)\"/> min <xsl:value-of select=\"$elapsed mod 60\"/> sec\n" +
                    "							</xsl:when>\n" +
                    "							<xsl:when test=\"$elapsed &gt; 60\">\n" +
                    "								<xsl:value-of select=\"floor($elapsed div 60)\"/> " +
                    ReportInfo.COMP_TIME_MIN +
                    " <xsl:value-of select=\"$elapsed mod 60\"/> sec\n" +
                    "							</xsl:when>\n" +
                    "							<xsl:otherwise>\n" +
                    "								<xsl:value-of select=\"$elapsed\"/> " +
                    ReportInfo.COMP_TIME_SEC + "\n" +
                    "							</xsl:otherwise>\n" +
                    "						</xsl:choose>						\n" +
                    "					</td>\n" +
                    "				</tr>			\n" +
                    "				<tr>\n" +
                    "					<td class=\"LargeTableSideHeader\">" +
                    ReportInfo.COMP_TEST + "</td>\n" +
                    "					<td>\n" +
                    "						<table class=\"LargeTable\" cellpadding=\"0\" border=\"1\" width=\"100%\">\n" +
                    "							<tbody class=\"LargeTableText\">\n" +
                    "								<xsl:apply-templates select=\"test:test\"/>\n" +
                    "							</tbody>\n" +
                    "						</table>\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "			</tbody>\n" +
                    "		</table>\n" +
                    "	</xsl:template>\n" +
                    "	\n" +
                    "	<xsl:template match=\"test:component\">\n" +
                    "		<tr>\n" +
                    "			<td class=\"LargeTableSideHeader\">" +
                    ReportInfo.COMP_NAME + "</td>\n" +
                    "			<td>\n" +
                    "				<xsl:value-of select=\"text()\"/>\n" +
                    "			</td>\n" +
                    "		</tr>\n" +
                    "	</xsl:template>\n" +
                    "	<xsl:template match=\"test:sub_component\">\n" +
                    "		<tr>\n" +
                    "			<td class=\"LargeTableSideHeader\">" +
                    ReportInfo.COMP_TEST_AREA + "</td>\n" +
                    "			<td>\n" +
                    "				<xsl:value-of select=\"text()\"/>\n" +
                    "			</td>\n" +
                    "		</tr>\n" +
                    "	</xsl:template>\n" +
                    "	<xsl:template match=\"test:area\">\n" +
                    "		<tr>\n" +
                    "			<td class=\"LargeTableSideHeader\">" +
                    ReportInfo.COMP_TEST_FOCUS + "</td>\n" +
                    "			<td>\n" +
                    "				<xsl:value-of select=\"text()\"/>\n" +
                    "			</td>\n" +
                    "		</tr>\n" +
                    "	</xsl:template>\n" +
                    "	<xsl:template match=\"test:description\">\n" +
                    "		<tr>\n" +
                    "			<td class=\"LargeTableSideHeader\">" +
                    ReportInfo.COMP_TEST_DESC + "</td>\n" +
                    "			<td>\n" +
                    "				<xsl:value-of select=\"text()\"/>\n" +
                    "			</td>\n" +
                    "		</tr>\n" +
                    "	</xsl:template>\n" +
                    "\n" +
                    "	<xsl:template match=\"test:test\">\n" +
                    "		<tr>\n" +
                    "			<td width=\"100%\">\n" +
                    "				<xsl:if test=\"@result='Passed'\">\n" +
                    "					<xsl:attribute name=\"bgcolor\">#ccffcc</xsl:attribute>\n" +
                    "				</xsl:if>\n" +
                    "				<xsl:if test=\"@result='Failed'\">\n" +
                    "					<xsl:attribute name=\"bgcolor\">#ffcccc</xsl:attribute>\n" +
                    "				</xsl:if>\n" +
                    "				<xsl:if test=\"@result='CNR'\">\n" +
                    "					<xsl:attribute name=\"bgcolor\">#ffd24d</xsl:attribute>\n" +
                    "				</xsl:if>\n" +
                    "				<xsl:variable name=\"first-line\">\n" +
                    "					<xsl:choose>					\n" +
                    "					<xsl:when test=\"@result='Failed'\">\n" +
                    "						<xsl:value-of select=\"test:name\"/>&#32;&#45;&#32;<xsl:text>Failed</xsl:text>\n" +
                    "					</xsl:when>\n" +
                    "					<xsl:otherwise>\n" +
                    "						<xsl:if test=\"@result='CNR'\">\n" +
                    "							<xsl:value-of select=\"test:name\"/>&#32;&#45;&#32;<xsl:text>CNR</xsl:text>\n" +
                    "						</xsl:if>\n" +
                    "						<xsl:if test=\"@result='Passed'\">\n" +
                    "							<xsl:value-of select=\"test:name\"/>&#32;&#45;&#32;<xsl:text>Passed</xsl:text>\n" +
                    "						</xsl:if>\n" +
                    "					</xsl:otherwise>\n" +
                    "				</xsl:choose>\n" +
                    "				</xsl:variable>\n" +
                    "				<xsl:variable name=\"switch-on\">+<xsl:value-of select=\"$first-line\"/>\n" +
                    "				</xsl:variable>\n" +
                    "				<xsl:variable name=\"switch-off\">-<xsl:value-of select=\"$first-line\"/>\n" +
                    "				</xsl:variable>\n" +
                    "				<div width=\"100%\">\n" +
                    "				   <xsl:variable name=\"unqId\" select=\"generate-id(.)\"/>\n" +
                    "					<div class=\"TreeSwitcher\" style=\"margin-left:6px\">\n" +
                    "					<xsl:attribute name=\"onclick\">\n" +
                    "					  switchdiv(this, '<xsl:value-of select=\"$unqId\"/>', \"<xsl:value-of select=\"$switch-on\"/>\",\"<xsl:value-of select=\"$switch-off\"/>\")\n" +
                    "                    </xsl:attribute>  \n" +
                    "				    <xsl:value-of select=\"$switch-on\"/>\n" +
                    "					</div>\n" +
                    "					<div style=\"display:none\" width=\"100%\" class=\"ValueText\">\n" +
                    "					    <xsl:attribute name=\"id\"><xsl:value-of select=\"$unqId\"/></xsl:attribute> \n" +
                    "						<table class=\"LargeTable\" cellpadding=\"0\" cellspacing=\"0\" border=\"1\" width=\"100%\">\n" +
                    "							<tbody class=\"LargeTableText\">\n" +
                    "								\n" +
                    "								<tr>\n" +
                    "									<td class=\"InnerTestInfoHeader\">" +
                    ReportInfo.TIME + "</td>\n" +
                    "									<td class=\"InnerValueText\">\n" +
                    "										<xsl:choose>\n" +
                    "											<xsl:when test=\"@elapsed &gt; 3600\">\n" +
                    "												<xsl:value-of select=\"floor(@elapsed div 3600)\"/>" +
                    ReportInfo.COMP_TIME_HOUR +
                    " <xsl:value-of select=\"floor((@elapsed mod 3600) div 60)\"/> min <xsl:value-of select=\"@elapsed mod 60\"/> sec\n" +
                    "											</xsl:when>\n" +
                    "											<xsl:when test=\"@elapsed &gt; 60\">\n" +
                    "												<xsl:value-of select=\"floor(@elapsed div 60)\"/> " +
                    ReportInfo.COMP_TIME_MIN +
                    " <xsl:value-of select=\"@elapsed mod 60\"/> sec\n" +
                    "											</xsl:when>\n" +
                    "											<xsl:otherwise>\n" +
                    "												<xsl:value-of select=\"@elapsed\"/> " +
                    ReportInfo.COMP_TIME_SEC + "\n" +
                    "											</xsl:otherwise>\n" +
                    "										</xsl:choose>										\n" +
                    "									</td>\n" +
                    "								</tr>\n" +
                    "								\n" +
                    "								<xsl:if test=\"test:desc\">\n" +
                    "								\n" +
                    "									<tr>\n" +
                    "										<td class=\"InnerTestInfoHeader\">\n" +
                    "											<b>" + ReportInfo.DESC +
                    "</b>\n" +
                    "										</td>\n" +
                    "										<td class=\"InnerValueText\" style=\"white-space:pre\">\n" +
                    "											<xsl:value-of select=\"test:desc\"/>\n" +
                    "										</td>\n" +
                    "									</tr>\n" +
                    "								\n" +
                    "								</xsl:if>\n" +
                    "								\n" +
                    "								<xsl:if test=\"test:output\">\n" +
                    "								\n" +
                    "									<tr>\n" +
                    "										<td class=\"InnerTestInfoHeader\">\n" +
                    "											<b>" + ReportInfo.OUPUT +
                    "</b>\n" +
                    "										</td>\n" +
                    "										<td class=\"InnerValueText\" style=\"white-space:pre\">\n" +
                    "											<xsl:variable name=\"output\" select=\"test:output\" />\n" +
                    "											<xsl:call-template name=\"LineBreaker\">\n" +
                    "												<xsl:with-param name=\"line\" select=\"substring-after($output, '&#xA;')\"/>\n" +
                    "												<xsl:with-param name=\"separator\" select=\"'&#xA;'\"/>\n" +
                    "											</xsl:call-template>\n" +
                    "										</td>\n" +
                    "									</tr>\n" +
                    "								\n" +
                    "								</xsl:if>\n" +
                    "								<xsl:if test=\"test:error\">\n" +
                    "								\n" +
                    "									<tr>\n" +
                    "										<td class=\"InnerTestInfoHeader\">\n" +
                    "											<b>" + ReportInfo.COMP_STATUS_FAIL +
                    "</b>\n" +
                    "										</td>\n" +
                    "										<td class=\"InnerValueText\" style=\"white-space:pre\">\n" +
                    "											<xsl:variable name=\"error\" select=\"test:error\" />\n" +
                    "											<xsl:call-template name=\"LineBreaker\">\n" +
                    "												<xsl:with-param name=\"line\" select=\"substring-after($error, '&#xA;')\"/>\n" +
                    "												<xsl:with-param name=\"separator\" select=\"'&#xA;'\"/>\n" +
                    "											</xsl:call-template>\n" +
                    "										</td>\n" +
                    "									</tr>\n" +
                    "								\n" +
                    "								</xsl:if>\n" +
                    "								<xsl:if test=\"test:system-out\">\n" +
                    "									<tr>\n" +
                    "										<td class=\"InnerTestInfoHeader\">\n" +
                    "											<b>" + ReportInfo.COMP_TEST_LOG +
                    "</b>\n" +
                    "										</td>\n" +
                    "										<td class=\"InnerValueText\">\n" +
                    "											<a>\n" +
                    "												<xsl:attribute name=\"href\"><xsl:value-of select=\"test:system-out/@href\"/></xsl:attribute>\n" +
                    "												<xsl:attribute name=\"target\">_new</xsl:attribute>\n" +
                    "												Runtime Log\n" +
                    "											</a></td>\n" +
                    "									</tr>\n" +
                    "								</xsl:if>\n" +
                    "								<xsl:if test=\"test:qtp-out\">\n" +
                    "									<tr>\n" +
                    "										<td class=\"InnerTestInfoHeader\">\n" +
                    "											<b>" + ReportInfo.QTP_REPORT +
                    "</b>\n" +
                    "										</td>\n" +
                    "										<td class=\"InnerValueText\">\n" +
                    "											<a>\n" +
                    "												<xsl:attribute name=\"href\"><xsl:value-of select=\"test:qtp-out/@href\"/></xsl:attribute>\n" +
                    "												<xsl:attribute name=\"target\">_new</xsl:attribute>\n" +
                    "												QTP Report\n" +
                    "											</a></td>\n" +
                    "									</tr>\n" +
                    "								</xsl:if>\n" +
                    "								<xsl:if test=\"test:screenshot\">\n" +
                    "									<tr>\n" +
                    "										<td class=\"InnerTestInfoHeader\">\n" +
                    "											<b>" + ReportInfo.COMP_TEST_SNAP +
                    "</b>\n" +
                    "										</td>\n" +
                    "										<td class=\"InnerValueText\">\n" +
                    "											<a>\n" +
                    "												<xsl:attribute name=\"href\"><xsl:value-of select=\"test:screenshot/@href\"/></xsl:attribute>\n" +
                    "												<xsl:attribute name=\"target\">_new</xsl:attribute>\n" +
                    "												Screenshot\n" +
                    "											</a></td>\n" +
                    "									</tr>\n" +
                    "								</xsl:if>\n" +
                    "							</tbody>\n" +
                    "						</table>\n" +
                    "					</div>\n" +
                    "				</div>\n" +
                    "			</td>\n" +
                    "		</tr>\n" +
                    "	</xsl:template>\n" +
                    "	\n" +
                    "<xsl:template name=\"NameValueRow\">\n" +
                    "		<xsl:param name=\"name\"/>\n" +
                    "		<xsl:param name=\"value\"/>\n" +
                    "		<xsl:param name=\"width\"/>\n" +
                    "		<xsl:if test=\"$value\">\n" +
                    "			<tr>\n" +
                    "				<td nowrap=\"true\" class=\"ValueName\">\n" +
                    "					<xsl:if test=\"$width\">\n" +
                    "						<xsl:attribute name=\"style\">width:<xsl:value-of select=\"$width\"/></xsl:attribute>\n" +
                    "					</xsl:if>\n" +
                    "					<xsl:value-of select=\"$name\"/>\n" +
                    "				</td>\n" +
                    "				<td nowrap=\"true\" class=\"ValueText\">\n" +
                    "					<xsl:value-of select=\"$value\"/>\n" +
                    "				</td>\n" +
                    "			</tr>\n" +
                    "		</xsl:if>\n" +
                    "	</xsl:template>" +
                    "	<xsl:template match=\"text()\"/>\n" +
                    "\n" +
                    "	<xsl:template name=\"LineBreaker\">\n" +
                    "\n" +
                    "		<xsl:param name=\"line\"/>\n" +
                    "		<xsl:param name=\"separator\"/>\n" +
                    "		<xsl:value-of select=\"substring-before($line, $separator)\"/>\n" +
                    "		<br/>\n" +
                    "				\n" +
                    "		<xsl:choose>	\n" +
                    "			<xsl:when test=\"string-length(substring-after($line, $separator)) != 0\">\n" +
                    "	\n" +
                    "				<xsl:call-template name=\"LineBreaker\">\n" +
                    "					<xsl:with-param name=\"line\" select=\"substring-after($line, $separator)\"/>\n" +
                    "					<xsl:with-param name=\"separator\" select=\"$separator\"/>\n" +
                    "				</xsl:call-template>	\n" +
                    "			</xsl:when>\n" +
                    "			<xsl:otherwise/>\n" +
                    "		</xsl:choose>\n" +
                    "					\n" +
                    "		\n" +
                    "	</xsl:template>\n" +
                    "\n" +
                    "</xsl:stylesheet>\n";
            utput.write(s, 0, s.length());
            utput.flush();
            //fw.write(s,0,s.length());
            //fw.flush();
        } catch (Exception e) {
            System.err.println("cannot geneate the xsl path.");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(11);
        }
    }

    public static void writeSuiteXSL(String path) {
        try {
            //FileWriter fw = new FileWriter(path);
            OutputStreamWriter utput =
                new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
            String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"  xmlns:test=\"http://www.oracle.com/2005/ocs/qa/framework/junit/schema\" >\n" +
                "	<xsl:output method=\"html\"/>\n" +
                "	<xsl:template name=\"Root\" match=\"/test:report\">\n" +
                "		<html>\n" +
                "			<xsl:call-template name=\"html-head\"/>\n" +
                "			<!--xsl:call-template name=\"javascript\"/-->\n" +
                "			<body link=\"663300\" alink=\"#FF6600\" vlink=\"#996633\" bgcolor=\"#ffffff\">\n" +
                "				<xsl:call-template name=\"Header\"/>\n" +
                "				<br/><br/>\n" +
                "				<div class=\"Heading\">" + ReportInfo.SUITE_TEST_SUMMARY +
                "</div>\n" +
                "				<xsl:call-template name=\"SummaryTable\"/>\n" +
                "				<div style=\"height:15\"/>\n" +
                "				<xsl:call-template name=\"TestSuiteTables\"/>				\n" +
                "<div class=\"Heading\"><br/><br/>Report Debug Information</div>\n" +
                "					\n" +
                "						<tr>\n" +
                "										<td ><b>AC Host Info:</b></td>\n" +
                "										<td>\n" +
                "											<table class=\"nameValueTable\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"1000\">\n" +
                "												<thead class=\"TestInfoHeader\">\n" +
                "													<th>Name</th>\n" +
                "													<th>Value</th>\n" +
                "												</thead>\n" +
                "												<xsl:for-each select=\"test:ReportDebug/test:param\">\n" +
                "													<xsl:call-template name=\"NameValueRow\">\n" +
                "														<xsl:with-param name=\"name\" select=\"@name\"/>\n" +
                "														<xsl:with-param name=\"value\" select=\".\"/>\n" +
                "														<xsl:with-param name=\"width\" select=\"'200px'\"/>\n" +
                "													</xsl:call-template>\n" +
                "												</xsl:for-each>\n" +
                "											</table>\n" +
                "										</td>\n" +
                "									</tr>\n" +
                "	\n" +
                "	<tr>\n" +
                "										<td ><b>Test Env:</b></td>\n" +
                "										<td>\n" +
                "											<table class=\"nameValueTable\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"300\">\n" +
                "												<thead class=\"TestInfoHeader\">\n" +
                "													<th>Name</th>\n" +
                "													<th>Value</th>\n" +
                "												</thead>\n" +
                "												<xsl:for-each select=\"test:TestEnv/test:param\">\n" +
                "													<xsl:call-template name=\"NameValueRow\">\n" +
                "														<xsl:with-param name=\"name\" select=\"@name\"/>\n" +
                "														<xsl:with-param name=\"value\" select=\".\"/>\n" +
                "														<xsl:with-param name=\"width\" select=\"'200px'\"/>\n" +
                "													</xsl:call-template>\n" +
                "												</xsl:for-each>\n" +
                "											</table>\n" +
                "										</td>\n" +
                "				</tr>\n" +
                "				<tr>\n" +
                "				</tr>\n" +
                "			<tr>\n" +
                "										<td ><b>Test Config:</b></td>\n" +
                "										<td>\n" +
                "											<table class=\"nameValueTable\" border=\"1\" cellspacing=\"0\" cellpadding=\"0\" width=\"300\">\n" +
                "												<thead class=\"TestInfoHeader\">\n" +
                "													<th>Name</th>\n" +
                "													<th>Value</th>\n" +
                "												</thead>\n" +
                "												<xsl:for-each select=\"test:TestConfig/test:param\">\n" +
                "													<xsl:call-template name=\"NameValueRow\">\n" +
                "														<xsl:with-param name=\"name\" select=\"@name\"/>\n" +
                "														<xsl:with-param name=\"value\" select=\".\"/>\n" +
                "														<xsl:with-param name=\"width\" select=\"'200px'\"/>\n" +
                "													</xsl:call-template>\n" +
                "												</xsl:for-each>\n" +
                "											</table>\n" +
                "										</td>\n" +
                "				</tr>		" +
                "			</body>\n" +
                "		</html>\n" +
                "	</xsl:template>\n" +
                "	<xsl:template name=\"html-head\">\n" +
                "		<head>\n" +
                "			<meta http-equiv=\"Content-Type\" content=\"text/html;CHARSET=iso-8859-1\"/>\n" +
                "			<title>Automation Center Test Report - <xsl:value-of select=\"//test:report/test:time\"/>\n" +
                "			</title>\n" +
                "			<!--Javascript for expand and collapse of test info / error trace -->\n" +
                "			<script language=\"JavaScript\">\n" +
                "				// hides or shows given div and change contents of the switcher itself\n" +
                "				function switchdiv(switcher, sw, closedname, openedname) {\n" +
                "					//alert(\"Switcher: \" + switcher.tagName + \" Switched: \" + switched.tagName + \".\" + switched.id);\n" +
                "					switched = document.getElementById(sw);\n" +
                "					if (switched.style.display == \"none\") {\n" +
                "						switched.style.display = \"block\";\n" +
                "						switched.style.marginLeft = \"10px\";\n" +
                "						switcher.innerText = openedname;\n" +
                "					}  else {\n" +
                "						switched.style.display = \"none\";\n" +
                "						switcher.innerText = closedname;\n" +
                "					}\n" +
                "					window.event.cancelBubble = true;\n" +
                "					return false;\n" +
                "				}\n" +
                "		    </script>\n" +
                "		    <!-- CSS styles definitions -->\n" +
                "			<style type=\"text/css\">\n" +
                "			        BODY	{  BACKGROUND-REPEAT: no-repeat; FONT-FAMILY: Arial, Helvetica, sans-serif; BACKGROUND-COLOR: #ffffff } \n" +
                "					.PageTitle { PADDING-BOTTOM: 8px;FONT-WEIGHT: bold; FONT-SIZE: medium; COLOR: #336699;  FONT-FAMILY: Arial, Times New Roman, Times, serif }\n" +
                "					.PageTitleSmall { PADDING-RIGHT: 8px; FONT-WEIGHT: bold; FONT-SIZE: x-small; COLOR: #336699; FONT-FAMILY: Arial, Times New Roman, Times, serif }\n" +
                "					.Heading { MARGIN-BOTTOM: 10px; MARGIN-TOP:15px; FONT-SIZE:medium; FONT-FAMILY: Arial, Helvetica, sans-serif; COLOR:#336699 }\n" +
                "					.TableTitle { PADDING-TOP: 8px;FONT-SIZE: small; COLOR: #336699; FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif; BACKGROUND-COLOR: #ffffff }\n" +
                "					.LargeTable { BORDER-RIGHT: #cccccc 1px solid; PADDING-RIGHT: 4px; BORDER-TOP: #cccccc 1px solid; PADDING-LEFT: 4px; BORDER-LEFT: #cccccc 1px solid; BORDER-BOTTOM: #cccccc 1px solid; BORDER-COLLAPSE: collapse;  BACKGROUND-COLOR: #999966 }\n" +
                "					.LargeTableHeader {FONT-WEIGHT: bold; FONT-SIZE: x-small; VERTICAL-ALIGN: bottom;  FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif; BACKGROUND-COLOR:  #93a9d5 }\n" +
                "					.LargeTableText { FONT-SIZE: x-small;  MARGIN-BOTTOM: 0px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: top; COLOR: #000000; PADDING-TOP: 2px; FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif; BORDER-COLLAPSE: collapse; BACKGROUND-COLOR: #f7f7e7 }\n" +
                "					.LargeTableSideHeader { WIDTH: 150px ; FONT-WEIGHT: bold; FONT-SIZE: x-small; VERTICAL-ALIGN: middle; FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif; BACKGROUND-COLOR: #93a9d5 }\n" +
                "					.SmallTable { BORDER-RIGHT: #000000 1px; BORDER-TOP: #000000 1px; MARGIN: 0px; BORDER-LEFT: #000000 1px; BORDER-BOTTOM: #000000 1px; BORDER-COLLAPSE: collapse }\n" +
                "					.SmallTableHeader { FONT-SIZE: xx-small; VERTICAL-ALIGN: bottom; TEXT-ALIGN: left }\n" +
                "					.SmallTableText { FONT-SIZE: x-small; VERTICAL-ALIGN: top;  COLOR: #555555 }\n" +
                "					.Timestamp {  }\n" +
                "					.EventName { COLOR: #003366 }\n" +
                "					.NameValueTable { PADDING-RIGHT: 0px; PADDING-LEFT: 0px; FONT-SIZE: x-small;  PADDING-BOTTOM: 0px; MARGIN: 0px; WIDTH: 100%;    BORDER-TOP-STYLE: none; PADDING-TOP: 0px; BORDER-RIGHT-STYLE: none; BORDER-LEFT-STYLE: none; BORDER-COLLAPSE: collapse; BORDER-BOTTOM-STYLE: none }\n" +
                "					.TreeSwitcher { FONT-WEIGHT: bold; FONT-SIZE: x-small; VERTICAL-ALIGN: top;CURSOR: pointer }\n" +
                "					.BigValue { BORDER-RIGHT: #cccccc 1px solid; PADDING-RIGHT: 2px;BORDER-TOP: #cccccc 1px solid; PADDING-LEFT: 2px;FONT-SIZE: x-small;   PADDING-BOTTOM: 2px; BORDER-LEFT: #cccccc 1px solid; WIDTH: 100%; PADDING-TOP: 2px; BORDER-BOTTOM: #cccccc 1px solid; BORDER-COLLAPSE: collapse}\n" +
                "					.ValueName { PADDING-RIGHT: 4px; FONT-SIZE: x-small; WIDTH: 100px }\n" +
                "					.ValueText { FONT-SIZE: x-small;  WIDTH: 100%; COLOR: #555555 }\n" +
                "					.TestInfoHeader { FONT-WEIGHT: bold;FONT-SIZE: x-small; VERTICAL-ALIGN: middle; WIDTH: 100px; FONT-FAMILY: Arial,Helvetica,Geneva,sans-serif;BACKGROUND-COLOR: #93a9d5 }\n" +
                "					.TestMethodTable {  PADDING-RIGHT: 4px; PADDING-LEFT: 4px; BORDER-COLLAPSE: collapse; BACKGROUND-COLOR: #9999ee }\n" +
                "ul.TabBarLevel1{\n" +
                "        list-style:none;\n" +
                "        margin:0;\n" +
                "        padding:0;\n" +
                "        height:29px;\n" +
                "        background-image:url(http://bej301037.cn.oracle.com/pic/tabbar_level1_bk.gif);\n" +
                "}\n" +
                "ul.TabBarLevel1 li{\n" +
                "        float:left;\n" +
                "        padding:0;\n" +
                "        height:29px;\n" +
                "        margin-right:1px;\n" +
                "        background:url(http://bej301037.cn.oracle.com/pic/tabbar_level1_slice_left_bk.gif) left top no-repeat;\n" +
                "}\n" +
                "ul.TabBarLevel1 li a{\n" +
                "        display:block;\n" +
                "        line-height:29px;\n" +
                "        padding:0 20px;\n" +
                "        color:#333;\n" +
                "        background:url(http://bej301037.cn.oracle.com/pic/tabbar_level1_slice_right_bk.gif) right top no-repeat;\n" +
                "        white-space: nowrap;\n" +
                "}\n" +
                "ul.TabBarLevel1 li.Selected{\n" +
                "        background:url(http://bej301037.cn.oracle.com/pic/tabbar_level1_slice_selected_left_bk.gif) left top no-repeat;\n" +
                "}\n" +
                "ul.TabBarLevel1 li.Selected a{\n" +
                "        background:url(http://bej301037.cn.oracle.com/pic/tabbar_level1_slice_selected_right_bk.gif) right top no-repeat;\n" +
                "}\n" +
                "\n" +
                "ul.TabBarLevel1 li a:link,ul.TabBarLevel1 li a:visited{\n" +
                "        color:#333;\n" +
                "}\n" +
                "ul.TabBarLevel1 li a:hover,ul.TabBarLevel1 li a:active{\n" +
                "        color:#F30;\n" +
                "        text-decoration:none;\n" +
                "}\n" +
                "ul.TabBarLevel1 li.Selected a:link,ul.TabBarLevel1 li.Selected a:visited{\n" +
                "        color:#000;\n" +
                "}\n" +
                "ul.TabBarLevel1 li.Selected a:hover,ul.TabBarLevel1 li.Selected a:active{\n" +
                "        color:#F30;\n" +
                "        text-decoration:none;\n" +
                "}" + "			</style>\n" +
                "		</head>\n" +
                "	</xsl:template>\n" +
                "	<xsl:template name=\"Header\">\n" +
                "		<br/>\n";
            if (ReportInfo.getReportLicensed()) {
                s =
  s + "<table border='0' cellspacing='3' width='640' bgcolor='#FFF8F0' height='50' cellpadding='1'>\n" +
                        "                <tr>\n" +
                        "                <td align='center'>\n" +
                        "                <b><font color='red'>" +
                        ReportInfo.COMP_LICENSE_ACTRAIL + "</font></b>\n" +
                        "                <br/><font color='#008040'>" +
                        ReportInfo.COMP_LICENSE_BODY + "</font>\n" +
                        "                </td>\n" +
                        "                </tr>\n" +
                        "                </table>\n";
            }

            s = s + "<div id=\"Whatever\">\n" +
                    "        <ul class=\"TabBarLevel1\" id=\"TabPage1\">\n" +
                    "  <xsl:for-each select=\"//test:report/test:report-tab/test:tabs\">\n" +
                    "   <xsl:choose>\n" +
                    "    <xsl:when test=\"@type='0'\">\n" +
                    "     <li class=\"Selected\" style=\"background-color:#FFFF99;\"><a><xsl:attribute name=\"href\"><xsl:value-of select=\"@name\"/>.html</xsl:attribute>\n" +
                    "       <xsl:value-of select=\"@name\"/>  </a></li>\n" +
                    "    </xsl:when>\n" +
                    "    <xsl:otherwise>\n" +
                    "         <li class=\"\" ><a><xsl:attribute name=\"href\"><xsl:value-of select=\"@name\"/>.html</xsl:attribute>\n" +
                    "         <xsl:value-of select=\"@name\"/>  </a></li>     \n" +
                    "    </xsl:otherwise>\n" +
                    "    </xsl:choose>\n" +
                    " </xsl:for-each>\n" +
                    " </ul>\n" +
                    " </div>" +
                    "		<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +

                    "			<tr class=\"PageTitle\">\n" +
                    "				<td colspan=\"2\">\n" +
                    "					<a name=\"top\">\n" +
                    "					   <xsl:text>" + ReportInfo.COMP_REPORT_TITLE +
                    "</xsl:text>\n" +
                    "					</a>\n" +
                    "				</td>\n" +
                    "			</tr>\n" +
                    "			<tr><td><br/></td></tr>\n" +
                    "			<tbody class=\"PageTitleSmall\">\n" +

                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_VERSION +
                    "</td>\n" +
                    "					<td>\n" +
                    "					  <xsl:value-of select=\"//test:report/test:build\"/>\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "       <tr>\n" +
                    "         <td>" + ReportInfo.COMP_CLIENT_VERSION +
                    "</td>\n" +
                    "         <td>\n" +
                    "           <xsl:value-of select=\"//test:report/test:client_version\"/>\n" +
                    "         </td>\n" +
                    "       </tr>\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.SUITE_TEST_DATE +
                    "</td>\n" +
                    "					<td>\n" +
                    "					  <xsl:value-of select=\"//test:report/test:time\"/>\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_ACCOUNT +
                    "</td>\n" +
                    "					<td>\n" +
                    "                        <xsl:value-of select=\"//test:report/test:tester\"/>					\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_OS +
                    "</td>\n" +
                    "					<td>\n" +
                    "						<xsl:value-of select=\"//test:report/test:system/test:OS\"/>\n" +
                    "						<xsl:text>, </xsl:text>\n" +
                    "						<xsl:value-of select=\"//test:report/test:system/test:OS-version\"/>\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_LANG +
                    "</td>\n" +
                    "					<td>\n" +
                    "                        <xsl:value-of select=\"//test:report/test:language\"/>					\n" +
                    "					</td>\n" +
                    "				</tr>\n" +
                    "				<tr>\n" +
                    "					<td>" + ReportInfo.COMP_TEST_ELAPSE +
                    "</td>\n" +
                    "					<td>\n" +
                    "						<xsl:variable name=\"exec_time\" select=\"sum(.//test:test/@elapsed)\"/>\n" +
                    "						<xsl:choose>\n" +
                    "							<xsl:when test=\"$exec_time &gt; 3600\">\n" +
                    "								<xsl:value-of select=\"floor($exec_time div 3600)\"/> hr <xsl:value-of select=\"floor(($exec_time mod 3600) div 60)\"/> min <xsl:value-of select=\"$exec_time mod 60\"/> sec\n" +
                    "							</xsl:when>\n" +
                    "							<xsl:when test=\"$exec_time &gt; 60\">\n" +
                    "								<xsl:value-of select=\"floor($exec_time div 60)\"/> min <xsl:value-of select=\"$exec_time mod 60\"/> sec\n" +
                    "							</xsl:when>\n" +
                    "							<xsl:otherwise>\n" +
                    "								<xsl:value-of select=\"$exec_time\"/> sec\n" +
                    "							</xsl:otherwise>\n" +
                    "						</xsl:choose>\n" +
                    "                        								\n" +
                    "					</td>\n" +
                    "				</tr>						\n" +
                    "<tr>\n" +
                    "				</tr>\n" +
                    "			</tbody>\n" +
                    "		</table>\n" +
                    "<br/><a><xsl:attribute name=\"href\"><xsl:value-of select=\"//test:link\"/></xsl:attribute><xsl:value-of select=\"//test:link/@name\"/></a>" +
                    "	</xsl:template>\n" +
                    "	<xsl:template name=\"SummaryTable\">\n" +
                    "		<table id=\"SummaryTable\" class=\"LargeTable\" border=\"1\" cellpadding=\"0\" width=\"100%\">\n" +
                    "			<thead>\n" +
                    "				<tr class=\"LargeTableHeader\">\n" +
                    "					<th rowspan=\"2\">" + ReportInfo.SUITE +
                    "</th>\n" +
                    "					<th colspan=\"3\">" + ReportInfo.COMP_TEST_CASE +
                    "</th>\n" +
                    "					<th colspan=\"3\">" + ReportInfo.COMP_TEST +
                    "</th>					\n" +
                    "				</tr>\n" +
                    "				<tr class=\"LargeTableHeader\">\n" +
                    "					<th>" + ReportInfo.COMP_STATUS_PASS +
                    "</th>					\n" +
                    "					<th>" + ReportInfo.COMP_STATUS_FAIL +
                    "</th>\n" +
                    "					<th>" + ReportInfo.COMP_STATUS_CNR_ABRV +
                    "</th>\n" +
                    "					<th>" + ReportInfo.COMP_STATUS_PASS +
                    "</th>					\n" +
                    "					<th>" + ReportInfo.COMP_STATUS_FAIL +
                    "</th>	\n" +
                    "					<th>" + ReportInfo.COMP_STATUS_CNR_ABRV +
                    "</th>				\n" +
                    "				</tr>\n" +
                    "			</thead>\n" +
                    "			<tbody class=\"LargeTableText\">\n" +
                    /* "				<xsl:for-each select=\"//test:suite\">\n" +
           // "					<tr align=\"center\" onmouseover=\"this.bgColor=0xeeeeaa\" onmouseout=\"this.bgColor=0xf7f7e7\">						\n" +
          "         <tr align=\"center\" >            \n" +
            "						<xsl:variable name=\"totalcases\" select=\"count(.//test:testcase)\"/>\n" +
            "						<xsl:variable name=\"passedcases\" select=\"count(.//test:testcase[@result='Passed'])\"/>\n" +
            "						<xsl:variable name=\"failedcases\" select=\"count(.//test:testcase[@result='Failed'])\"/>\n" +
            "						<xsl:variable name=\"cnrcases\" select=\"count(.//test:testcase[@result='CNR'])\"/>\n" +
            "						<xsl:variable name=\"totaltests\" select=\"count(.//test:test)\"/>\n" +
            "						<xsl:variable name=\"passedtests\" select=\"count(.//test:test[@result='Passed'])\"/>						\n" +
            "						<xsl:variable name=\"failedtests\" select=\"count(.//test:test[@result='Failed'])\"/>\n" +
            "						<xsl:variable name=\"cnrtests\" select=\"count(.//test:test[@result='CNR'])\"/>\n" +
              */
                    "       <xsl:for-each select=\"//test:report/test:testjob\">\n" +
                    // "         <tr align=\"center\" onmouseover=\"this.bgColor=0xeeeeaa\" onmouseout=\"this.bgColor=0xf7f7e7\">            \n" +
                    "         <tr align=\"center\" >            \n" +
                    "           <xsl:variable name=\"totalcases\" select=\"count(.//test:testjob[@result!=''])\"/>\n" +
                    "           <xsl:variable name=\"passedcases\" select=\"count(.//test:testjob[@result='Passed'])\"/>\n" +
                    "           <xsl:variable name=\"failedcases\" select=\"count(.//test:testjob[@result='Failed'])\"/>\n" +
                    "           <xsl:variable name=\"cnrcases\" select=\"count(.//test:testjob[@result='CNR'])\"/>\n" +
                    "           <xsl:variable name=\"totaltests\" select=\"count(.//test:test)\"/>\n" +
                    "           <xsl:variable name=\"passedtests\" select=\"count(.//test:test[@result='Passed'])\"/>           \n" +
                    "           <xsl:variable name=\"failedtests\" select=\"count(.//test:test[@result='Failed'])\"/>\n" +
                    "           <xsl:variable name=\"cnrtests\" select=\"count(.//test:test[@result='CNR'])\"/>\n" +
                    "						<td>\n" +

                    "							<xsl:value-of select=\"@name\"/>\n" +
                    "						</td>\n" +
                    "						<td align=\"center\">\n" +
                    "							<table border=\"0\"><tr><td align=\"left\">\n" +
                    "							<xsl:value-of select=\"$passedcases\"/>\n" +
                    "							</td><td></td><td align=\"right\">\n" +
                    "							<xsl:text>[</xsl:text>\n" +
                    "							<xsl:value-of select=\"floor($passedcases div $totalcases * 100 + 0.5)\"/>\n" +
                    "							<xsl:text>%</xsl:text>\n" +
                    "							<xsl:text>]</xsl:text>\n" +
                    "							</td></tr></table>\n" +
                    "						</td>						\n" +
                    "						<td align=\"center\">\n" +
                    "							<xsl:if test=\"$failedcases &gt; 0\">\n" +
                    "								<xsl:attribute name=\"bgcolor\">#ffcccc</xsl:attribute>\n" +
                    "							</xsl:if>\n" +
                    "							<table border=\"0\"><tr><td align=\"left\">\n" +
                    "							<xsl:value-of select=\"$failedcases\"/>\n" +
                    "							</td><td></td><td align=\"right\">\n" +
                    "							<xsl:text>[</xsl:text>\n" +
                    "							<xsl:value-of select=\"floor($failedcases div $totalcases * 100 + 0.5)\"/>\n" +
                    "							<xsl:text>%</xsl:text>\n" +
                    "							<xsl:text>]</xsl:text>\n" +
                    "							</td></tr></table>\n" +
                    "						</td>		\n" +
                    "						<td align=\"center\">\n" +
                    "							<xsl:if test=\"$cnrcases &gt; 0\">\n" +
                    "								<xsl:attribute name=\"bgcolor\">#ffd24d</xsl:attribute>\n" +
                    "							</xsl:if>\n" +
                    "							<table border=\"0\"><tr><td align=\"left\">\n" +
                    "							<xsl:value-of select=\"$cnrcases\"/>\n" +
                    "							</td><td></td><td align=\"right\">\n" +
                    "							<xsl:text>[</xsl:text>\n" +
                    "							<xsl:value-of select=\"floor($cnrcases div $totalcases * 100 + 0.5)\"/>\n" +
                    "							<xsl:text>%</xsl:text>\n" +
                    "							<xsl:text>]</xsl:text>\n" +
                    "							</td></tr></table>\n" +
                    "						</td>				\n" +
                    "						<td align=\"center\">							\n" +
                    "							<table border=\"0\"><tr><td align=\"left\">\n" +
                    "							<xsl:value-of select=\"$passedtests\"/>\n" +
                    "							</td><td></td><td align=\"right\">\n" +
                    "							<xsl:text>[</xsl:text>\n" +
                    "							<xsl:value-of select=\"floor($passedtests div $totaltests * 100 + 0.5)\"/>\n" +
                    "							<xsl:text>%</xsl:text>\n" +
                    "							<xsl:text>]</xsl:text>\n" +
                    "							</td></tr></table>\n" +
                    "						</td>							\n" +
                    "						<td align=\"center\">\n" +
                    "							<xsl:if test=\"$failedtests &gt; 0\">\n" +
                    "								<xsl:attribute name=\"bgcolor\">#ffcccc</xsl:attribute>\n" +
                    "							</xsl:if>\n" +
                    "							<table border=\"0\"><tr><td align=\"left\">\n" +
                    "							<xsl:value-of select=\"$failedtests\"/>\n" +
                    "							</td><td></td><td align=\"right\">\n" +
                    "							<xsl:text>[</xsl:text>\n" +
                    "							<xsl:value-of select=\"floor($failedtests div $totaltests * 100 + 0.5)\"/>\n" +
                    "							<xsl:text>%</xsl:text>\n" +
                    "							<xsl:text>]</xsl:text>\n" +
                    "							</td></tr></table>\n" +
                    "						</td>\n" +
                    "						<td align=\"center\">\n" +
                    "							<xsl:if test=\"$cnrtests &gt; 0\">\n" +
                    "								<xsl:attribute name=\"bgcolor\">#ffd24d</xsl:attribute>\n" +
                    "							</xsl:if>\n" +
                    "							<table border=\"0\"><tr><td align=\"left\">\n" +
                    "							<xsl:value-of select=\"$cnrtests\"/>\n" +
                    "							</td><td></td><td align=\"right\">\n" +
                    "							<xsl:text>[</xsl:text>\n" +
                    "							<xsl:value-of select=\"floor($cnrtests div $totaltests * 100 + 0.5)\"/>\n" +
                    "							<xsl:text>%</xsl:text>\n" +
                    "							<xsl:text>]</xsl:text>\n" +
                    "							</td></tr></table>\n" +
                    "						</td>\n" +
                    "					</tr>\n" +
                    "				</xsl:for-each>\n" +
                    "			</tbody>\n" +
                    "		</table>\n" +
                    "	</xsl:template>\n" +
                    "	<xsl:template name=\"TestSuiteTables\">\n" +
                    /*
            "		<xsl:for-each select=\"//test:suite\">\n" +
            "			<xsl:variable name=\"suitetotaltests\" select=\"count(.//test:test)\"/>\n" +
            "			<xsl:variable name=\"suitepassedtests\" select=\"count(.//test:test[@result='Passed'])\"/>\n" +
            "			<xsl:variable name=\"suitefailedtests\" select=\"count(.//test:test[@result='Failed'])\"/>\n" +
            "			<xsl:variable name=\"suitecnrtests\" select=\"count(.//test:test[@result='CNR'])\"/>			\n" +
            "			<xsl:variable name=\"suitetotalcases\" select=\"count(.//test:testcase)\"/>\n" +
            "			<xsl:variable name=\"suitepassedcases\" select=\"count(.//test:testcase[@result='Passed'])\"/>\n" +
            "			<xsl:variable name=\"suitefailedcases\" select=\"count(.//test:testcase[@result='Failed'])\"/>\n" +
            "			<xsl:variable name=\"suitecnrcases\" select=\"count(.//test:testcase[@result='CNR'])\"/>			\n" + */
                    "   <xsl:for-each select=\"//test:report/test:testjob\">\n" +

                    "     <xsl:variable name=\"suitetotaltests\" select=\"count(.//test:test)\"/>\n" +
                    "     <xsl:variable name=\"suitepassedtests\" select=\"count(.//test:test[@result='Passed'])\"/>\n" +
                    "     <xsl:variable name=\"suitefailedtests\" select=\"count(.//test:test[@result='Failed'])\"/>\n" +
                    "     <xsl:variable name=\"suitecnrtests\" select=\"count(.//test:test[@result='CNR'])\"/>      \n" +
                    "     <xsl:variable name=\"suitetotalcases\" select=\"count(.//test:testjob[@result!=''])\"/>\n" +
                    "     <xsl:variable name=\"suitepassedcases\" select=\"count(.//test:testjob[@result='Passed'])\"/>\n" +
                    "     <xsl:variable name=\"suitefailedcases\" select=\"count(.//test:testjob[@result='Failed'])\"/>\n" +
                    "     <xsl:variable name=\"suitecnrcases\" select=\"count(.//test:testjob[@result='CNR'])\"/>      \n" +
                    "			<div style=\"margin-bottom:10px\">\n" +


                    "				<div class=\"TableTitle\"><b>" + ReportInfo.SUITE +
                    "<xsl:value-of select=\"@name\"/></b><br/><br/>\n" +
                    "				</div>\n" +
                    "				\n" +
                    "				<table id=\"TestSuiteTable\" class=\"LargeTable\" border=\"1\" cellpadding=\"0\" width=\"100%\">\n" +
                    "					<thead>\n" +
                    "						<tr class=\"LargeTableHeader\">\n" +
                    "							<th rowspan=\"2\">" + ReportInfo.COMP_NAME +
                    "</th>\n" +
                    "							<th colspan=\"3\">" + ReportInfo.COMP_TEST_CASE +
                    "</th>\n" +
                    "							<th colspan=\"3\">" + ReportInfo.COMP_TEST +
                    "</th>					\n" +
                    "						</tr>\n" +
                    "						<tr class=\"LargeTableHeader\">\n" +
                    "							<th>" + ReportInfo.COMP_STATUS_PASS +
                    "</th>							\n" +
                    "							<th>" + ReportInfo.COMP_STATUS_FAIL +
                    "</th>\n" +
                    "							<th>" + ReportInfo.COMP_STATUS_CNR_ABRV +
                    "</th>\n" +
                    "							<th>" + ReportInfo.COMP_STATUS_PASS +
                    "</th>							\n" +
                    "							<th>" + ReportInfo.COMP_STATUS_FAIL +
                    "</th>\n" +
                    "							<th>" + ReportInfo.COMP_STATUS_CNR_ABRV +
                    "</th>					\n" +
                    "						</tr>\n" +
                    "					</thead>\n" +
                    "					<tbody class=\"LargeTableText\">\n" +
                    /*
            "						<xsl:for-each select=\"./test:component\">\n" +
            "							<xsl:variable name=\"compName\" select=\"@name\"/>\n" +
            "							<xsl:variable name=\"totaltests\" select=\"count(.//test:test)\"/>\n" +
            "							<xsl:variable name=\"failedtests\" select=\"count(.//test:test[@result='Failed'])\"/>							\n" +
            "							<xsl:variable name=\"passedtests\" select=\"count(.//test:test[@result='Passed'])\"/>\n" +
            "							<xsl:variable name=\"cnrtests\" select=\"count(.//test:test[@result='CNR'])\"/>\n" +
            "							<xsl:variable name=\"totalcases\" select=\"count(.//test:testcase)\"/>\n" +
            "							<xsl:variable name=\"failedcases\" select=\"count(.//test:testcase[@result='Failed'])\"/>							\n" +
            "							<xsl:variable name=\"passedcases\" select=\"count(.//test:testcase[@result='Passed'])\"/>\n" +
            "							<xsl:variable name=\"cnrcases\" select=\"count(.//test:testcase[@result='CNR'])\"/>\n" +*/
                    "           <xsl:for-each select=\"//test:report/test:testjob/test:testjob\">\n" +

                    "             <xsl:variable name=\"compName\" select=\"@name\"/>\n" +
                    "             <xsl:variable name=\"totaltests\" select=\"count(.//test:test)\"/>\n" +
                    "             <xsl:variable name=\"failedtests\" select=\"count(.//test:test[@result='Failed'])\"/>             \n" +
                    "             <xsl:variable name=\"passedtests\" select=\"count(.//test:test[@result='Passed'])\"/>\n" +
                    "             <xsl:variable name=\"cnrtests\" select=\"count(.//test:test[@result='CNR'])\"/>\n" +
                    "             <xsl:variable name=\"totalcases\" select=\"count(.//test:testjob[@result!=''])\"/>\n" +
                    "             <xsl:variable name=\"failedcases\" select=\"count(.//test:testjob[@result='Failed'])\"/>             \n" +
                    "             <xsl:variable name=\"passedcases\" select=\"count(.//test:testjob[@result='Passed'])\"/>\n" +
                    "             <xsl:variable name=\"cnrcases\" select=\"count(.//test:testjob[@result='CNR'])\"/>\n" +

                    //"							<tr onmouseover=\"this.bgColor=0xeeeeaa\" onmouseout=\"this.bgColor=0xf7f7e7\">\n" +
                    "             <tr >\n" +
                    "								<td>\n" +
                    "									<a>\n" +
                    "										<xsl:attribute name=\"href\"><xsl:value-of select=\"concat('./',$compName,'/',$compName,'.html')\"/></xsl:attribute>\n" +
                    "										<xsl:value-of select=\"@name\"/>\n" +
                    "									</a>\n" +
                    "								</td>\n" +
                    "								<td align=\"center\">									\n" +
                    "									<table border=\"0\"><tr><td align=\"left\">\n" +
                    "									<xsl:value-of select=\"$passedcases\"/>\n" +
                    "									</td><td></td><td align=\"right\">\n" +
                    "									<xsl:text>[</xsl:text>\n" +
                    "									<xsl:value-of select=\"floor($passedcases div $totalcases * 100 + 0.5)\"/>\n" +
                    "									<xsl:text>%</xsl:text>\n" +
                    "									<xsl:text>]</xsl:text>\n" +
                    "									</td></tr></table>\n" +
                    "								</td>								\n" +
                    "								<td align=\"center\">\n" +
                    "									<xsl:if test=\"$failedcases &gt; 0\">\n" +
                    "										<xsl:attribute name=\"bgcolor\">#ffcccc</xsl:attribute>\n" +
                    "									</xsl:if>\n" +
                    "									<table border=\"0\"><tr><td align=\"left\">\n" +
                    "									<xsl:value-of select=\"$failedcases\"/>\n" +
                    "									</td><td></td><td align=\"right\">\n" +
                    "									<xsl:text>[</xsl:text>\n" +
                    "									<xsl:value-of select=\"floor($failedcases div $totalcases * 100 + 0.5)\"/>\n" +
                    "									<xsl:text>%</xsl:text>\n" +
                    "									<xsl:text>]</xsl:text>\n" +
                    "									</td></tr></table>\n" +
                    "								</td>\n" +
                    "								<td align=\"center\">\n" +
                    "									<xsl:if test=\"$cnrcases &gt; 0\">\n" +
                    "										<xsl:attribute name=\"bgcolor\">#ffd24d</xsl:attribute>\n" +
                    "									</xsl:if>\n" +
                    "									<table border=\"0\"><tr><td align=\"left\">\n" +
                    "									<xsl:value-of select=\"$cnrcases\"/>\n" +
                    "									</td><td></td><td align=\"right\">\n" +
                    "									<xsl:text>[</xsl:text>\n" +
                    "									<xsl:value-of select=\"floor($cnrcases div $totalcases * 100 + 0.5)\"/>\n" +
                    "									<xsl:text>%</xsl:text>\n" +
                    "									<xsl:text>]</xsl:text>\n" +
                    "									</td></tr></table>\n" +
                    "								</td>\n" +
                    "								<td align=\"center\">									\n" +
                    "									<table border=\"0\"><tr><td align=\"left\">\n" +
                    "									<xsl:value-of select=\"$passedtests\"/>\n" +
                    "									</td><td></td><td align=\"right\">\n" +
                    "									<xsl:text>[</xsl:text>\n" +
                    "									<xsl:value-of select=\"floor($passedtests div $totaltests * 100 + 0.5)\"/>\n" +
                    "									<xsl:text>%</xsl:text>\n" +
                    "									<xsl:text>]</xsl:text>\n" +
                    "									</td></tr></table>\n" +
                    "								</td>								\n" +
                    "								<td align=\"center\">\n" +
                    "									<xsl:if test=\"$failedtests &gt; 0\">\n" +
                    "										<xsl:attribute name=\"bgcolor\">#ffcccc</xsl:attribute>\n" +
                    "									</xsl:if>\n" +
                    "									<table border=\"0\"><tr><td align=\"left\">\n" +
                    "									<xsl:value-of select=\"$failedtests\"/>\n" +
                    "									</td><td></td><td align=\"right\">\n" +
                    "									<xsl:text>[</xsl:text>\n" +
                    "									<xsl:value-of select=\"floor($failedtests div $totaltests * 100 + 0.5)\"/>\n" +
                    "									<xsl:text>%</xsl:text>\n" +
                    "									<xsl:text>]</xsl:text>\n" +
                    "									</td></tr></table>\n" +
                    "								</td>\n" +
                    "								<td align=\"center\">\n" +
                    "									<xsl:if test=\"$cnrtests &gt; 0\">\n" +
                    "										<xsl:attribute name=\"bgcolor\">#ffd24d</xsl:attribute>\n" +
                    "									</xsl:if>\n" +
                    "									<table border=\"0\"><tr><td align=\"left\">\n" +
                    "									<xsl:value-of select=\"$cnrtests\"/>\n" +
                    "									</td><td></td><td align=\"right\">\n" +
                    "									<xsl:text>[</xsl:text>\n" +
                    "									<xsl:value-of select=\"floor($cnrtests div $totaltests * 100 + 0.5)\"/>\n" +
                    "									<xsl:text>%</xsl:text>\n" +
                    "									<xsl:text>]</xsl:text>\n" +
                    "									</td></tr></table>\n" +
                    "								</td>\n" +
                    "							</tr>\n" +
                    "						</xsl:for-each>\n" +
                    "					</tbody>\n" +
                    "				</table>\n" +
                    "			</div>\n" +
                    "		</xsl:for-each>\n" +
                    "	</xsl:template>\n" +
                    "<xsl:template name=\"NameValueRow\">\n" +
                    "           <xsl:param name=\"name\"/>\n" +
                    "           <xsl:param name=\"value\"/>\n" +
                    "           <xsl:param name=\"width\"/>\n" +
                    "           <xsl:if test=\"$value\">\n" +
                    "                   <tr>\n" +
                    "                           <td nowrap=\"true\" class=\"ValueName\">\n" +
                    "                                   <xsl:if test=\"$width\">\n" +
                    "                                           <xsl:attribute name=\"style\">width:<xsl:value-of select=\"$width\"/></xsl:attribute>\n" +
                    "                                   </xsl:if>\n" +
                    "                                   <xsl:value-of select=\"$name\"/>\n" +
                    "                           </td>\n" +
                    "                           <td nowrap=\"true\" class=\"ValueText\">\n" +
                    "                                   <xsl:value-of select=\"$value\"/>\n" +
                    "                           </td>\n" +
                    "                   </tr>\n" +
                    "           </xsl:if>\n" +
                    "   </xsl:template>" + "	\n" +
                    "	\n" +
                    "</xsl:stylesheet>\n";
            utput.write(s, 0, s.length());
            utput.flush();
            //fw.write(s,0,s.length());
            //fw.flush();
        } catch (Exception e) {
            System.err.println("cannot geneate the xsl path.");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(11);
        }
    }

    public static void main(String[] args) {
        Writer writer = new Writer();
    }
}
