/*

 Profiles.java
 *
 * Basead on the code by Peter Dietz:
 * https://gist.github.com/842301#file_dspace_add_about.diff (acessed 11-05-23)
 *
 * Modified to work with internationalization (i18n locales) and breadcrumbs
 * by Andre Nito Assada e Josi Perez Alvarez on 11-05-23
 *
 * Modified again by Miami University capstone team A-3 to allow users to 
 * create and add profiles for scholars in DSpace.
 */

package org.dspace.app.xmlui.aspect.artifactbrowser;

//imports for context path
import java.sql.*;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
//imports for servlet
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.*;
//imports for the form
import org.apache.cocoon.util.HashUtil;
import org.apache.commons.lang.StringUtils;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Radio;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.app.xmlui.wing.element.TextArea;
import org.dspace.content.Metadatum;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
//general imports
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Figure;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.authorize.AuthorizeException;

/**
 * Profiles pages
 *
 * @author Capstone Team A-3
 */
public class Profiles extends AbstractDSpaceTransformer {

	//private form variables
	private static final Message F_head =
        	message("Create Your Profile");
    	private static final Message F_para1 =
        	message("Test2");
    	private static final Message F_jobTitle =
        	message("Job Title");
	private static final Message F_name =
	        message("Name");
	private static final Message F_research =
	        message("Research");
	private static final Message F_address =
	        message("Office");
	private static final Message F_phone =
	        message("Phone");
	private static final Message F_email =
	        message("Email");
	private static final Message F_website =
	        message("Website");
	private static final Message F_picurl =
	        message("Picture URL");
	private static final Message F_degree =
	        message("Degree");
	private static final Message F_earnedFrom =
	        message("Degree Earned From");
	private static final Message F_datesAttended =
	        message("Dates Attended");
	private static final Message F_grantTitle =
	        message("Grant Title");
	private static final Message F_grantLength =
	        message("Grant Length");
	private static final Message F_grantNumber =
	        message("Grant Number");
	private static final Message F_organization =
	        message("Organization");
	private static final Message F_orgJobTitle =
	        message("Job Title");
	private static final Message F_dateWorked =
	        message("Dates Worked");
	private static final Message F_orcidURL =
	        message("Orcid URL");
	private static final Message F_academiaURL =
	        message("Academia URL");
	private static final Message F_googleplusURL =
	        message("Google Plus URL");
	private static final Message F_linkedinURL =
	        message("LinkedIn URL");
	private static final Message F_researchgateURL =
	        message("ResearchGate URL");
	private static final Message F_twitterURL =
	        message("Twitter URL");
	private static final Message T_submit =
         	message("xmlui.ArtifactBrowser.ItemRequestForm.submit");
	//end formvariables

	//breadcrumb information
	public static final Message T_dspace_home = message("xmlui.general.dspace_home");
	public static final Message T_title = message("Profiles");
	public static final Message T_trail = message("Profiles");
	//database information
	public static final String databaseConnection = "jdbc:postgresql://localhost:5432/profiles";
	public static final String databaseUsername = "postgres";
	public static final String databasePassword = "capstone";
	//image location information
	public static final String orcidLoc = "http://172.17.31.180:8080/xmlui/themes/Mirage/images/orcid-logo.png";
	public static final String academiaLoc = "http://172.17.31.180:8080/xmlui/themes/Mirage/images/academia-logo.png";
	public static final String googlePlusLoc = "http://172.17.31.180:8080/xmlui/themes/Mirage/images/google-plus-logo.png";
	public static final String linkedinLoc = "http://172.17.31.180:8080/xmlui/themes/Mirage/images/linkedin-logo.png";
	public static final String researchGateLoc = "http://172.17.31.180:8080/xmlui/themes/Mirage/images/researchgate-logo.png";
	public static final String twitterLoc = "http://172.17.31.180:8080/xmlui/themes/Mirage/images/twitter-logo.png";

	private static Logger log = Logger.getLogger(Profiles.class);

	/**
	 * Add a page title and trail links.
	 */	
	public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException {
		// Set the page title

		// breadcrumbs
		pageMeta.addMetadata("Title").addContent(T_title);
		// add trail
		pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
		pageMeta.addTrail().addContent(T_trail);
	}

	/**
	 * Add some basic contents
	 */
	
	public Text getText(List form, String textTitle, Message textLabel, int size) throws WingException {
		Text t = form.addItem().addText(textTitle);
		t.setLabel(textLabel);
		t.setValue(parameters.getParameter(textTitle, ""));
		t.setSize(0, size);
		return t;
	}

	public void addBody(Body body) throws SAXException, WingException {

		//parses the request to obtain user information
		Request request = ObjectModelHelper.getRequest(objectModel);
		String req = request.getPathInfo();
		String[] tok = req.split("/");
		String newM = tok[2];
		boolean containsUser = false;;

		//database variables
		String uniqueId = "", name = "", pictureURL = "";
		String jobTitle = "", researchArea = "Research: ", address = "Address: ";
		String phone = "Phone: ", email = "Email: ", website = "Personal Website: ";
		String school = "", degreeAndAttended = "";
		String grantTitle = "Grant Title: ", grantLength = "Grant Length: ", grantNumber = "Grant Number: ";
		String orcid = "", academia = "", googlePlus = "", linkedin = "", researchGate = "", twitter = "";
		String organization = "", orgJobTitle = "", dateRange = "";
		
		//database connection
		try {
			Connection conn = null;
			
			Statement stmt = null;
			conn = DriverManager.getConnection(databaseConnection, databaseUsername, databasePassword);
			stmt = conn.createStatement();
			String sql, sql1, sql2, sql3, sql4, sql5;
			
			sql5 = "SELECT * FROM faculty WHERE uniqueid = '" + newM + "'";
			ResultSet rs5 = stmt.executeQuery(sql5);		
			String uniqueIdCheck = "";
			while(rs5.next()) {
				uniqueIdCheck += rs5.getString(1);
			}
			rs5.close();
			if(!uniqueIdCheck.isEmpty())
				containsUser = true;
			
			if(containsUser) {
				sql = "SELECT * FROM faculty WHERE uniqueid = '" + newM + "'";
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					uniqueId += rs.getString("uniqueid");
					name += rs.getString("name");
					pictureURL = rs.getString("pictureurl");
					jobTitle += rs.getString("jobtitle");
					researchArea += rs.getString("research");
					address += rs.getString("address");
					phone += rs.getString("phone");
					email += rs.getString("email");
					website += rs.getString("website");
				}
				rs.close();
			
				sql1 = "SELECT * FROM bio WHERE uid = '" + newM + "'";
				ResultSet rs1 = stmt.executeQuery(sql1);
				while(rs1.next()) {
					uniqueId += rs1.getString("uid");
					school += rs1.getString("school");
					degreeAndAttended += rs1.getString("degree");
					degreeAndAttended += ", ";
					degreeAndAttended += rs1.getString("dateEarned");
				}
				rs1.close();
			
				sql2 = "SELECT * FROM funding WHERE uid = '" + newM + "'";
				ResultSet rs2 = stmt.executeQuery(sql2);
				while(rs2.next()) {
					uniqueId += rs2.getString("uid");
					grantTitle += rs2.getString("granttitle");
					grantLength += rs2.getString("grantlength");
					grantNumber += rs2.getString("grantnumber");
				}
				rs2.close();
			
				sql3 = "SELECT * FROM links WHERE uid = '" + newM + "'";
				ResultSet rs3 = stmt.executeQuery(sql3);
				while(rs3.next()) {
					uniqueId += rs3.getString("uid");
					orcid += rs3.getString("orcid");
					academia += rs3.getString("academia");
					googlePlus += rs3.getString("googleplus");
					linkedin += rs3.getString("linkedin");
					researchGate += rs3.getString("researchgate");
					twitter += rs3.getString("twitter");
				}
				rs3.close();
			
				sql4 = "SELECT * FROM employment WHERE uid = '" + newM + "'";
				ResultSet rs4 = stmt.executeQuery(sql4);
				while(rs4.next()) {
					uniqueId += rs4.getString("uid");
					organization += rs4.getString("organization");
					orgJobTitle += rs4.getString("jobtitle");
					dateRange += rs4.getString("daterange");
				}
				rs4.close();
			
				stmt.close();
				conn.close();
			}
			} catch (SQLException se) {
				
			}
		
		Division division = body.addDivision("profile", "primary");
		division.setHead(name);

		// the divisions for the page
		Division page = division.addDivision("page");

		//if user is in database, build profile
		if (containsUser) {
			
			Division infoBar = division.addDivision("infoBar");
			
			Division picture = infoBar.addDivision("picture");
			picture.addParaFigure("", "", pictureURL, "", "");

			Division pinfo = page.addDivision("personalInformation");
			Division info1 = pinfo.addDivision("info1");
			info1.addPara(jobTitle);
			info1.addPara(researchArea);

			Division info2 = pinfo.addDivision("info2");
			info2.addPara(address);
			info2.addPara(phone);
			info2.addPara(email);
			info2.addPara(website);

			// pictures
			
			Division bios = page.addDivision("bios");
			
			Division academicContainer = bios.addDivision("academicContainer");
			Division employmentContainer = bios.addDivision("employmentContainer");
			Division grantsContainer = bios.addDivision("grantsContainer");
			
			// Set the headers of the bio containers
			Division academicHeader = academicContainer.addDivision("academicHeader");
			academicHeader.addPara("Education");
			Division employmentHeader = employmentContainer.addDivision("employmentHeader");
			employmentHeader.addPara("Employment");
			Division grantsHeader = grantsContainer.addDivision("grantsHeader");
			grantsHeader.addPara("Funding");
			
			Division educationContent = academicContainer.addDivision("educationContent");
			educationContent.addPara(school);
			educationContent.addPara(degreeAndAttended);
			
			Division employmentContent = employmentContainer.addDivision("employmentContent");
			employmentContent.addPara(organization);
			employmentContent.addPara(orgJobTitle);
			employmentContent.addPara(dateRange);
			
			Division fundingContent = grantsContainer.addDivision("fundingContent");
			fundingContent.addPara(grantTitle);
			fundingContent.addPara(grantLength);
			fundingContent.addPara(grantNumber);

			Division links = page.addDivision("links");
			// orcid/
			links.addParaFigure("", "", orcidLoc, orcid, "");
			// adacemia.edu
			links.addParaFigure("", "", academiaLoc, academia, "");
			// google+
			links.addParaFigure("", "", googlePlusLoc, googlePlus, "");
			// linkedin
			links.addParaFigure("", "", linkedinLoc, linkedin, "");
			// researchgate
			links.addParaFigure("", "", researchGateLoc, researchGate, "");
			// twitter
			links.addParaFigure("", "", twitterLoc, twitter, "");

		}

		//if user is not in database, build form
		else if (!containsUser) {

			// Build the item viewer division.
			Division formDiv = page.addInteractiveDivision("form", request.getRequestURI(), Division.METHOD_POST, "primary");
			formDiv.setHead(F_head);

			formDiv.addPara("Faculty Information");

			List form = formDiv.addList("form", List.TYPE_FORM);

			Text fname = getText(form, "name", F_name, 50);		
			Text fPictureURL = getText(form, "Picture URL", F_picurl, 100);			
			Text fJobTitle = getText(form, "job title", F_jobTitle, 100);
			Text fResearch = getText(form, "research", F_research, 100);
			Text fAddress = getText(form,"address", F_address, 50);
			Text fPhone = getText(form, "phone", F_phone, 12);
			Text fEmail = getText(form, "email", F_email, 50);
			Text fWebsite = getText(form, "website", F_website, 100);
			Text fDegree = getText(form, "degree", F_degree, 100);
			Text fEarnedFrom = getText(form, "earned from", F_earnedFrom, 100);
			Text fDatesAttended = getText(form, "dates attended", F_datesAttended, 50);
			Text fOrganization = getText(form, "organization", F_organization, 100);
			Text fOrgJobTitle = getText(form, "forg job title", F_orgJobTitle, 100);
			Text fdateWorked = getText(form, "date worked", F_orgJobTitle, 50);
			Text fGrantTitle = getText(form, "grant title", F_grantTitle, 100);
			Text fGrantLength = getText(form, "grant length", F_grantLength, 50);		
			Text fGrantNumber = getText(form, "grant number", F_grantNumber, 50);
			Text fOrcidURL = getText(form, "orcid", F_orcidURL,100);
			Text fAcademiaURL = getText(form, "academia", F_academiaURL, 100);
			Text fGooglePlusURL = getText(form, "google plus", F_googleplusURL, 100);
			Text fLinkedinURL = getText(form, "linkedin",F_linkedinURL, 100);
			Text fResearchGateURL = getText(form, "research gate", F_researchgateURL, 100);
			Text fTwitterURL = getText(form, "twitter", F_twitterURL, 100);			

			form.addItem().addHidden("isSent").setValue("true");
			form.addItem().addButton("submit").setValue("Submit");

			Division testPost = page.addDivision("testGet");
			String s = "Not Posted";
			try {
				s = request.getParameter("param1");
			} catch(Exception e) {
				s = "error";
			}
			
			//parse get variables
			//currently adds them to to the bottom of page
			
			String 	formname = request.getParameter("name"), 
			formPicURL = request.getParameter("Picture URL"),
			formJobTitle = request.getParameter("job title"),
			formResearch = request.getParameter("research"),
			formAddr = request.getParameter("address"),
			formPhone = request.getParameter("phone"),
			formEmail = request.getParameter("email"),
			formWebsite = request.getParameter("website"),
			formDeg = request.getParameter("degree"),
			formEarned = request.getParameter("earned from"),
			formAttend = request.getParameter("dates attended"),
			formOrg = request.getParameter("organization"),
			formOrgJobTitle = request.getParameter("forg job title"),
			formWorked = request.getParameter("date worked"),
			formGrantTitle = request.getParameter("grant title"),
			formGrantLen = request.getParameter("grant length"),
			formGrantNum = request.getParameter("grant number"),
			formOrcid = request.getParameter("orcid"),
			formAcadem = request.getParameter("academia"),
			formGP = request.getParameter("google plus"),
			formLink = request.getParameter("linkedin"),
			formResGate = request.getParameter("research gate"),
			formTwitter = request.getParameter("twitter");
	
			String isSent = request.getParameter("isSent");

			if (isSent != null && isSent.equals("true")) {
				try {
					Connection conn = null;
					
					Statement stmt = null;

					conn = DriverManager.getConnection(databaseConnection, databaseUsername, databasePassword);
					stmt = conn.createStatement();
					
					String insrtFac = "INSERT INTO faculty " + 
					"(uniqueid, name, pictureurl, jobtitle, research, address, phone, email, website) " + 
					" VALUES" + 
					" ('" + newM + "', '" + formname + "', '" + formPicURL + "', '" + 
					formJobTitle + "', '" + formResearch + "', '" + formAddr + "', '" + 
					formPhone + "', '" + formEmail + "', '" + formWebsite + "');";
						
					String insrtEmploy = "INSERT INTO employment"
						+ "(uid, organization, jobtitle, daterange)"
						+ " VALUES"
						+ " ('" + newM + "', '" + formOrg + "', '" + formOrgJobTitle + "', '" + formWorked + "');"; 
						
					String insrtBio = "INSERT INTO bio"
						+ "(uid, school, degree, dateearned)"
						+ " VALUES"
						+ " ('" + newM + "', '" + formEarned + "', '" + formDeg + "', '" + formAttend + "');";
						
					String insrtFund = "INSERT INTO funding" + 
						"(uid, granttitle, grantlength, grantnumber)" + 
						" VALUES" + " ('" + newM + "', '" + formGrantTitle + 
						"', '" + formGrantLen + "', '" + formGrantNum + "');";
						
					String insrtLink = "INSERT INTO links"
						+ "(uid, orcid, academia, googleplus, linkedin, researchgate, twitter)"
						+ " VALUES"
						+ " ('" + newM + "', '" + formOrcid + "', '" + formAcadem 
						+ "', '" + formGP + "', '" + formLink 
						+ "', '" + formResGate + "', '" + formTwitter + "');";
					stmt.executeUpdate(insrtFac);
					stmt.executeUpdate(insrtEmploy);
					stmt.executeUpdate(insrtBio);
					stmt.executeUpdate(insrtFund);
					stmt.executeUpdate(insrtLink);
				} catch (SQLException se) {
					
				}
			}  
		}
	}
}
